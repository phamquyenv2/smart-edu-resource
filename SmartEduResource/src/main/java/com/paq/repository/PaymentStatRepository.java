package com.paq.repository;

import com.paq.utils.constant.PaymentMethodEnum;
import com.paq.utils.constant.PaymentStatusEnum;
import java.util.List;
import java.util.Map;

public interface PaymentStatRepository {

    long countPayments(Map<String, String> params);

    long countPaymentsByStatus(PaymentStatusEnum status, Map<String, String> params);

    long getTotalRevenue(Map<String, String> params);

    Map<PaymentMethodEnum, Long> countPaymentsByMethod(Map<String, String> params);

    List<Object[]> getRevenueByMonth(Map<String, String> params);

    Map<String, Long> countPaymentsByUserRole(Map<String, String> params);
}
