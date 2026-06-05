package com.paq.controllers.admin;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.paq.pojo.response.ResPaymentDTO;
import com.paq.pojo.response.ResPaymentStatsDTO;
import com.paq.pojo.response.ResPageDTO;
import com.paq.pojo.response.ResResponse;
import com.paq.service.PaymentService;
import com.paq.utils.DTOMapper;
import com.paq.utils.constant.PaymentStatusEnum;

@RestController
@RequestMapping("/api/secure/admin")
public class ApiAdminPaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private Environment env;

    @GetMapping("/payments")
    public ResponseEntity<ResResponse<ResPageDTO<ResPaymentDTO>>> getPayments(
            @RequestParam Map<String, String> params) {
        int page = params.containsKey("page") ? Integer.parseInt(params.get("page")) : 1;
        int pageSize = this.env.getProperty("payments.page_size", Integer.class, 10);
        long totalItems = this.paymentService.countPayments(new HashMap<>(params));

        ResResponse<ResPageDTO<ResPaymentDTO>> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Lấy danh sách giao dịch thành công");
        res.setData(DTOMapper.toResPageDTO(this.paymentService.getPayments(params), totalItems, page, pageSize));

        return ResponseEntity.ok(res);
    }

    @GetMapping("/payments/stats")
    public ResponseEntity<ResResponse<ResPaymentStatsDTO>> getPaymentStats(
            @RequestParam Map<String, String> params) {
        ResResponse<ResPaymentStatsDTO> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Lấy thống kê thanh toán thành công");
        res.setData(this.paymentService.getPaymentStats(params));

        return ResponseEntity.ok(res);
    }

    @GetMapping("/payments/stats/export")
    public ResponseEntity<byte[]> exportPaymentStats(@RequestParam Map<String, String> params) {
        byte[] file = this.paymentService.exportPaymentStats(params);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.attachment().filename("payment-stats.xlsx").build());

        return new ResponseEntity<>(file, headers, HttpStatus.OK);
    }

    @GetMapping("/payments/{id}")
    public ResponseEntity<ResResponse<ResPaymentDTO>> getPaymentById(@PathVariable int id) {
        ResResponse<ResPaymentDTO> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Lấy chi tiết giao dịch thành công");
        res.setData(this.paymentService.getPaymentById(id));

        return ResponseEntity.ok(res);
    }

    @PutMapping("/payments/{id}/status")
    public ResponseEntity<ResResponse<ResPaymentDTO>> updatePaymentStatus(
            @PathVariable int id,
            @RequestParam PaymentStatusEnum status) {
        ResResponse<ResPaymentDTO> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Cập nhật trạng thái giao dịch thành công");
        res.setData(this.paymentService.updatePaymentStatus(id, status));

        return ResponseEntity.ok(res);
    }
}
