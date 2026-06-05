package com.paq.service;

import com.paq.pojo.response.ResPaymentDTO;
import com.paq.pojo.response.ResPaymentStatsDTO;
import com.paq.utils.constant.PaymentMethodEnum;
import com.paq.utils.constant.PaymentStatusEnum;
import java.util.List;
import java.util.Map;

public interface PaymentService {

    List<ResPaymentDTO> getPayments(Map<String, String> params);

    long countPayments(Map<String, String> params);

    ResPaymentDTO getPaymentById(int id);

    ResPaymentDTO updatePaymentStatus(int id, PaymentStatusEnum status);

    ResPaymentStatsDTO getPaymentStats(Map<String, String> params);

    byte[] exportPaymentStats(Map<String, String> params);

    List<ResPaymentDTO> getMyPayments(String username);

    ResPaymentDTO createPayment(int enrollmentId,
            PaymentMethodEnum method,
            String username);
}
