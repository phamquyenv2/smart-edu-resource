package com.paq.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.paq.pojo.Enrollment;
import com.paq.pojo.Payment;
import com.paq.pojo.response.ResPaymentDTO;
import com.paq.pojo.response.ResPaymentStatsDTO;
import com.paq.pojo.response.ResRevenueByMonthDTO;
import com.paq.repository.EnrollmentRepository;
import com.paq.repository.PaymentRepository;
import com.paq.repository.PaymentStatRepository;
import com.paq.service.PaymentService;
import com.paq.service.PermissionService;
import com.paq.utils.DTOMapper;
import com.paq.utils.constant.PaymentMethodEnum;
import com.paq.utils.constant.PaymentStatusEnum;
import com.paq.utils.error.IdInvalidException;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepo;

    @Autowired
    private PaymentStatRepository paymentStatRepo;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private EnrollmentRepository enrollmentRepo;

    @Override
    public List<ResPaymentDTO> getPayments(Map<String, String> params) {
        this.permissionService.requireAdmin();
        return this.paymentRepo.getPayments(params).stream()
                .map(DTOMapper::toResPaymentDTO)
                .collect(Collectors.toList());
    }

    @Override
    public long countPayments(Map<String, String> params) {
        this.permissionService.requireAdmin();
        return this.paymentRepo.countPayments(params);
    }

    @Override
    public ResPaymentDTO getPaymentById(int id) {
        this.permissionService.requirePaymentOwnerOrAdmin(id);

        Payment payment = this.paymentRepo.getPaymentById(id);
        if (payment == null) {
            throw new IdInvalidException("Payment không tồn tại");
        }

        return DTOMapper.toResPaymentDTO(payment);
    }

    @Override
    public ResPaymentDTO updatePaymentStatus(int id, PaymentStatusEnum status) {
        this.permissionService.requireAdmin();

        Payment payment = this.paymentRepo.getPaymentById(id);
        if (payment == null) {
            throw new IdInvalidException("Payment không tồn tại");
        }

        payment.setStatus(status);
        if (PaymentStatusEnum.SUCCESS.equals(status)) {
            if (payment.getPaidAt() == null) {
                payment.setPaidAt(new Date());
            }
        } else if (PaymentStatusEnum.PENDING.equals(status) || PaymentStatusEnum.CANCELLED.equals(status)) {
            payment.setPaidAt(null);
        }

        return DTOMapper.toResPaymentDTO(this.paymentRepo.updatePayment(payment));
    }

    @Override
    public ResPaymentStatsDTO getPaymentStats(Map<String, String> params) {
        this.permissionService.requireAdmin();
        this.validateDateRange(params);

        ResPaymentStatsDTO dto = new ResPaymentStatsDTO();
        dto.setTotalRevenue(this.paymentStatRepo.getTotalRevenue(params));
        dto.setTotalTransactions(this.paymentStatRepo.countPayments(params));
        dto.setSuccessfulTransactions(this.paymentStatRepo.countPaymentsByStatus(PaymentStatusEnum.SUCCESS, params));
        dto.setPendingTransactions(this.paymentStatRepo.countPaymentsByStatus(PaymentStatusEnum.PENDING, params));
        dto.setRefundedTransactions(this.paymentStatRepo.countPaymentsByStatus(PaymentStatusEnum.REFUNDED, params));
        dto.setCancelledTransactions(this.paymentStatRepo.countPaymentsByStatus(PaymentStatusEnum.CANCELLED, params));

        Map<String, Long> methodCounts = new HashMap<>();
        PaymentMethodEnum topMethod = null;
        long topMethodCount = -1L;
        for (Map.Entry<PaymentMethodEnum, Long> entry : this.paymentStatRepo.countPaymentsByMethod(params).entrySet()) {
            methodCounts.put(entry.getKey().name(), entry.getValue());
            if (entry.getValue() > topMethodCount) {
                topMethod = entry.getKey();
                topMethodCount = entry.getValue();
            }
        }
        dto.setMethodCounts(methodCounts);
        dto.setTopPaymentMethod(topMethod != null ? topMethod.name() : null);

        List<Object[]> monthlyData = this.paymentStatRepo.getRevenueByMonth(params);
        List<ResRevenueByMonthDTO> revenueByMonth = new ArrayList<>();
        for (Object[] row : monthlyData) {
            revenueByMonth.add(new ResRevenueByMonthDTO(
                    (Integer) row[0],
                    (Integer) row[1],
                    row[2] != null ? ((Number) row[2]).longValue() : 0L,
                    row[3] != null ? ((Number) row[3]).longValue() : 0L
            ));
        }
        dto.setRevenueByMonth(revenueByMonth);

        dto.setUserRoleCounts(this.paymentStatRepo.countPaymentsByUserRole(params));

        return dto;
    }

    @Override
    public byte[] exportPaymentStats(Map<String, String> params) {
        this.permissionService.requireAdmin();
        ResPaymentStatsDTO stats = this.getPaymentStats(params);

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            this.writeOverviewSheet(workbook, stats);
            this.writeRevenueSheet(workbook, stats.getRevenueByMonth());
            this.writeMapSheet(workbook, "Phương thức thanh toán", "Phương thức", "Giao dịch", stats.getMethodCounts());
            this.writeMapSheet(workbook, "Vai trò người dùng", "Vai trò", "Giao dịch", stats.getUserRoleCounts());
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Không thể xuất báo cáo thanh toán", ex);
        }
    }

    private void writeOverviewSheet(XSSFWorkbook workbook, ResPaymentStatsDTO stats) {
        Sheet sheet = workbook.createSheet("Overview");
        String[][] rows = {
            {"Metric", "Value"},
            {"Tổng doanh thu", String.valueOf(stats.getTotalRevenue())},
            {"Tổng giao dịch", String.valueOf(stats.getTotalTransactions())},
            {"Giao dịch thành công", String.valueOf(stats.getSuccessfulTransactions())},
            {"Giao dịch đang chờ", String.valueOf(stats.getPendingTransactions())},
            {"Giao dịch đã hoàn tiền", String.valueOf(stats.getRefundedTransactions())},
            {"Giao dịch đã hủy", String.valueOf(stats.getCancelledTransactions())},
            {"Top phương thức thanh toán", stats.getTopPaymentMethod() == null ? "" : stats.getTopPaymentMethod()}
        };

        for (int i = 0; i < rows.length; i++) {
            Row row = sheet.createRow(i);
            row.createCell(0).setCellValue(rows[i][0]);
            row.createCell(1).setCellValue(rows[i][1]);
        }
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    private void writeRevenueSheet(XSSFWorkbook workbook, List<ResRevenueByMonthDTO> revenueByMonth) {
        Sheet sheet = workbook.createSheet("Doanh thu theo tháng");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Năm");
        header.createCell(1).setCellValue("Tháng");
        header.createCell(2).setCellValue("Doanh thu");
        header.createCell(3).setCellValue("Giao dịch");

        int rowIndex = 1;
        for (ResRevenueByMonthDTO item : revenueByMonth) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(item.getYear());
            row.createCell(1).setCellValue(item.getMonth());
            row.createCell(2).setCellValue(item.getRevenue());
            row.createCell(3).setCellValue(item.getTransactions());
        }

        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void writeMapSheet(XSSFWorkbook workbook, String sheetName, String keyHeader, String valueHeader,
            Map<String, Long> values) {
        Sheet sheet = workbook.createSheet(sheetName);
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue(keyHeader);
        header.createCell(1).setCellValue(valueHeader);

        int rowIndex = 1;
        for (Map.Entry<String, Long> entry : values.entrySet()) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(entry.getKey());
            row.createCell(1).setCellValue(entry.getValue());
        }

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    @Override
    public List<ResPaymentDTO> getMyPayments(String username) {
        return this.paymentRepo.getPaymentsByUsername(username)
                .stream()
                .map(DTOMapper::toResPaymentDTO)
                .toList();
    }

    @Override
    public ResPaymentDTO createPayment(int enrollmentId,
            PaymentMethodEnum method,
            String username) {

        Enrollment enrollment
                = this.enrollmentRepo.getEnrollmentById(enrollmentId);

        if (enrollment == null) {
            throw new IdInvalidException("Enrollment không tồn tại");
        }

        Payment payment = new Payment();

        payment.setEnrollmentId(enrollment);

        payment.setAmount(
                enrollment.getCourseId().getPrice().longValue()
        );

        payment.setPaymentMethod(method);

        payment.setStatus(PaymentStatusEnum.PENDING);

        payment.setCreatedAt(new Date());

        payment.setTransactionCode(
                "PAY-" + System.currentTimeMillis()
        );

        return DTOMapper.toResPaymentDTO(
                this.paymentRepo.createPayment(payment)
        );
    }

    private void validateDateRange(Map<String, String> params) {
        if (params == null) {
            return;
        }

        Date fromDate = this.parseDate(params.get("fromDate"));
        Date toDate = this.parseDate(params.get("toDate"));
        if (fromDate != null && toDate != null && toDate.before(fromDate)) {
            throw new IllegalArgumentException("Đến ngày phải lớn hơn hoặc bằng từ ngày.");
        }
    }

    private Date parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(value);
        } catch (ParseException ex) {
            throw new IllegalArgumentException("Ngày phải có định dạng yyyy-MM-dd.");
        }
    }
}
