package com.paq.controllers.client;

import java.security.Principal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.paq.pojo.response.ResMoMoCreateDTO;
import com.paq.pojo.response.ResResponse;
import com.paq.service.MoMoService;

@RestController
@RequestMapping("/api")
public class ApiMoMoPaymentController {

    @Autowired
    private MoMoService momoService;

    @PostMapping("/secure/student/payments/momo/create")
    public ResponseEntity<ResResponse<ResMoMoCreateDTO>> createMoMoPayment(
            @RequestParam int courseId,
            Principal principal) {

        ResResponse<ResMoMoCreateDTO> res = new ResResponse<>();
        res.setStatusCode(HttpStatus.OK.value());
        res.setMessage("Tạo thanh toán MoMo thành công");
        res.setData(this.momoService.createMoMoPayment(courseId, principal.getName()));

        return ResponseEntity.ok(res);
    }

    @PostMapping("/payments/momo/ipn")
    public ResponseEntity<Void> handleMoMoIpn(@RequestBody Map<String, String> ipnRequest) {
        this.momoService.processIpnCallback(ipnRequest);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/payments/momo/sync")
    public ResponseEntity<Void> syncMoMoReturn(@RequestBody Map<String, String> returnRequest) {
        this.momoService.syncReturnResult(returnRequest);
        return ResponseEntity.noContent().build();
    }
}
