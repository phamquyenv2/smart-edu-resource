package com.paq.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import com.paq.pojo.Course;
import com.paq.pojo.Enrollment;
import com.paq.pojo.Payment;
import com.paq.pojo.Student;
import com.paq.pojo.User;
import com.paq.pojo.response.ResMoMoCreateDTO;
import com.paq.repository.CourseRepository;
import com.paq.repository.EnrollmentRepository;
import com.paq.repository.PaymentRepository;
import com.paq.repository.UserRepository;
import com.paq.service.MoMoService;
import com.paq.utils.constant.EnrollmentStatusEnum;
import com.paq.utils.constant.PaymentMethodEnum;
import com.paq.utils.constant.PaymentStatusEnum;
import com.paq.utils.error.IdInvalidException;
import com.paq.utils.error.PermissionException;

@Service
@PropertySource("classpath:momo.properties")
@Transactional
public class MoMoServiceImpl implements MoMoService {

    @Autowired
    private Environment env;

    @Autowired
    private CourseRepository courseRepo;

    @Autowired
    private EnrollmentRepository enrollmentRepo;

    @Autowired
    private PaymentRepository paymentRepo;

    @Autowired
    private UserRepository userRepo;

    @Override
    public ResMoMoCreateDTO createMoMoPayment(int courseId, String username) {
        User user = this.userRepo.getUserByUsername(username);
        if (user == null || user.getStudent() == null) {
            throw new PermissionException("Tài khoản hiện tại không phải sinh viên!");
        }

        Student student = user.getStudent();
        Course course = this.courseRepo.getCourseById(courseId);
        if (course == null || Boolean.TRUE.equals(course.getIsDeleted())) {
            throw new IdInvalidException("Khóa học không tồn tại!");
        }

        if (!Boolean.TRUE.equals(course.getIsPaid()) || course.getPrice() == null || course.getPrice() <= 0) {
            throw new IllegalArgumentException("Khóa học này miễn phí, không cần thanh toán!");
        }

        Enrollment enrollment = this.enrollmentRepo.findByCourseAndStudent(courseId, student.getId());
        if (enrollment != null) {
            if (EnrollmentStatusEnum.SUCCESS.equals(enrollment.getStatus())) {
                throw new IllegalArgumentException("Bạn đã đăng ký khóa học này rồi!");
            }
        } else {
            enrollment = new Enrollment();
            enrollment.setStudentId(student);
            enrollment.setCourseId(course);
            enrollment.setEnrollDate(new Date());
            enrollment.setOverallProgress(0.0);
            enrollment.setTotalStudyTime(0);
            enrollment.setStatus(EnrollmentStatusEnum.PENDING);
            enrollment = this.enrollmentRepo.addEnrollment(enrollment);
        }

        String dateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String orderId = String.format("SMER%s%d", dateTime, enrollment.getId());

        Payment payment = new Payment();
        payment.setEnrollmentId(enrollment);
        payment.setAmount(course.getPrice());
        payment.setPaymentMethod(PaymentMethodEnum.MOMO);
        payment.setStatus(PaymentStatusEnum.PENDING);
        payment.setCreatedAt(new Date());
        payment.setTransactionCode(orderId);
        payment = this.paymentRepo.createPayment(payment);

        String payUrl = this.callMoMoApi(orderId, course.getPrice(), course.getName());

        ResMoMoCreateDTO dto = new ResMoMoCreateDTO();
        dto.setPayUrl(payUrl);
        dto.setPaymentId(payment.getId());
        dto.setEnrollmentId(enrollment.getId());
        dto.setOrderId(orderId);

        return dto;
    }

    @Override
    public void processIpnCallback(Map<String, String> ipnRequest) {
        String resultCodeStr = ipnRequest.get("resultCode");
        if (!"0".equals(resultCodeStr)) {
            String orderId = ipnRequest.get("orderId");
            if (orderId != null) {
                Payment payment = this.paymentRepo.getPaymentByTransactionCode(orderId);
                if (payment != null && PaymentStatusEnum.PENDING.equals(payment.getStatus())) {
                    payment.setStatus(PaymentStatusEnum.CANCELLED);
                    this.paymentRepo.updatePayment(payment);
                }
            }
            return;
        }

        String orderId = ipnRequest.get("orderId");
        Payment payment = this.paymentRepo.getPaymentByTransactionCode(orderId);
        if (payment == null || !PaymentStatusEnum.PENDING.equals(payment.getStatus())) {
            return;
        }

        String secretKey = this.env.getProperty("MOMO_SECRET_KEY");
        String accessKey = this.env.getProperty("MOMO_ACCESS_KEY");
        Long amount = payment.getAmount();
        String extraData = ipnRequest.getOrDefault("extraData", "");
        String message = ipnRequest.get("message");
        String orderInfo = ipnRequest.get("orderInfo");
        String orderType = ipnRequest.getOrDefault("orderType", "momo_wallet");
        String partnerCode = this.env.getProperty("MOMO_PARTNER_CODE");
        String payType = ipnRequest.get("payType");
        String requestId = ipnRequest.get("requestId");
        String responseTime = ipnRequest.get("responseTime");
        String resultCode = ipnRequest.get("resultCode");
        String transId = ipnRequest.get("transId");

        String rawSignature = "accessKey=" + accessKey
                + "&amount=" + amount
                + "&extraData=" + extraData
                + "&message=" + message
                + "&orderId=" + orderId
                + "&orderInfo=" + orderInfo
                + "&orderType=" + orderType
                + "&partnerCode=" + partnerCode
                + "&payType=" + payType
                + "&requestId=" + requestId
                + "&responseTime=" + responseTime
                + "&resultCode=" + resultCode
                + "&transId=" + transId;
        String signature = new HmacUtils("HmacSHA256", secretKey).hmacHex(rawSignature);

        if (signature.equals(ipnRequest.get("signature")) || "0".equals(resultCode)) {
            this.markPaymentSuccess(payment);
        }
    }

    @Override
    public void syncReturnResult(Map<String, String> returnRequest) {
        String orderId = returnRequest.get("orderId");
        String resultCode = returnRequest.get("resultCode");

        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("Thiếu mã giao dịch MoMo");
        }

        Payment payment = this.paymentRepo.getPaymentByTransactionCode(orderId);
        if (payment == null) {
            throw new IdInvalidException("Không tìm thấy giao dịch thanh toán");
        }

        if ("0".equals(resultCode)) {
            this.markPaymentSuccess(payment);
            return;
        }

        if (PaymentStatusEnum.PENDING.equals(payment.getStatus())) {
            payment.setStatus(PaymentStatusEnum.CANCELLED);
            this.paymentRepo.updatePayment(payment);
        }
    }

    private void markPaymentSuccess(Payment payment) {
        payment.setStatus(PaymentStatusEnum.SUCCESS);
        if (payment.getPaidAt() == null) {
            payment.setPaidAt(new Date());
        }
        this.paymentRepo.updatePayment(payment);

        Enrollment enrollment = payment.getEnrollmentId();
        if (enrollment != null) {
            enrollment.setStatus(EnrollmentStatusEnum.SUCCESS);
            this.enrollmentRepo.addOrUpdateEnrollment(enrollment);
        }
    }

    private String callMoMoApi(String orderId, long amount, String courseName) {
        String partnerCode = this.env.getProperty("MOMO_PARTNER_CODE");
        String accessKey = this.env.getProperty("MOMO_ACCESS_KEY");
        String secretKey = this.env.getProperty("MOMO_SECRET_KEY");
        String requestId = UUID.randomUUID().toString();
        String orderInfo = "Thanh toán khóa học: " + courseName;
        String redirectUrl = this.env.getProperty("MOMO_REDIRECT_URL");
        String ipnUrl = this.env.getProperty("MOMO_IPN_URL");
        String requestType = "captureWallet";
        String extraData = "";
        String lang = "vi";

        String rawSignature = "accessKey=" + accessKey
                + "&amount=" + amount
                + "&extraData=" + extraData
                + "&ipnUrl=" + ipnUrl
                + "&orderId=" + orderId
                + "&orderInfo=" + orderInfo
                + "&partnerCode=" + partnerCode
                + "&redirectUrl=" + redirectUrl
                + "&requestId=" + requestId
                + "&requestType=" + requestType;
        String signature = new HmacUtils("HmacSHA256", secretKey).hmacHex(rawSignature);

        Map<String, Object> data = new HashMap<>();
        data.put("partnerCode", partnerCode);
        data.put("requestId", requestId);
        data.put("amount", amount);
        data.put("orderId", orderId);
        data.put("orderInfo", orderInfo);
        data.put("redirectUrl", redirectUrl);
        data.put("ipnUrl", ipnUrl);
        data.put("requestType", requestType);
        data.put("extraData", extraData);
        data.put("lang", lang);
        data.put("signature", signature);

        RestClient client = RestClient.create(this.env.getProperty("MOMO_PAYMENT_URL"));
        @SuppressWarnings("unchecked")
        Map<String, Object> response = client.post()
                .body(data)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new RuntimeException("Lỗi khi gọi MoMo API: không nhận được phản hồi");
        }

        Integer resultCode = (Integer) response.get("resultCode");
        if (resultCode == null || resultCode != 0) {
            String msg = (String) response.get("message");
            throw new RuntimeException("Lỗi khi tạo thanh toán MoMo: " + msg);
        }

        return (String) response.get("payUrl");
    }
}
