package com.paq.pojo.response;

import java.util.List;
import java.util.Map;

public class ResPaymentStatsDTO {

    private Long totalRevenue;
    private Long totalTransactions;
    private Long successfulTransactions;
    private Long pendingTransactions;
    private Long refundedTransactions;
    private Long cancelledTransactions;
    private String topPaymentMethod;
    private Map<String, Long> methodCounts;
    private List<ResRevenueByMonthDTO> revenueByMonth;
    private Map<String, Long> userRoleCounts;

    public Long getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(Long totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Long getTotalTransactions() {
        return totalTransactions;
    }

    public void setTotalTransactions(Long totalTransactions) {
        this.totalTransactions = totalTransactions;
    }

    public Long getSuccessfulTransactions() {
        return successfulTransactions;
    }

    public void setSuccessfulTransactions(Long successfulTransactions) {
        this.successfulTransactions = successfulTransactions;
    }

    public Long getPendingTransactions() {
        return pendingTransactions;
    }

    public void setPendingTransactions(Long pendingTransactions) {
        this.pendingTransactions = pendingTransactions;
    }

    public Long getRefundedTransactions() {
        return refundedTransactions;
    }

    public void setRefundedTransactions(Long refundedTransactions) {
        this.refundedTransactions = refundedTransactions;
    }

    public Long getCancelledTransactions() {
        return cancelledTransactions;
    }

    public void setCancelledTransactions(Long cancelledTransactions) {
        this.cancelledTransactions = cancelledTransactions;
    }

    public String getTopPaymentMethod() {
        return topPaymentMethod;
    }

    public void setTopPaymentMethod(String topPaymentMethod) {
        this.topPaymentMethod = topPaymentMethod;
    }

    public Map<String, Long> getMethodCounts() {
        return methodCounts;
    }

    public void setMethodCounts(Map<String, Long> methodCounts) {
        this.methodCounts = methodCounts;
    }

    public List<ResRevenueByMonthDTO> getRevenueByMonth() {
        return revenueByMonth;
    }

    public void setRevenueByMonth(List<ResRevenueByMonthDTO> revenueByMonth) {
        this.revenueByMonth = revenueByMonth;
    }

    public Map<String, Long> getUserRoleCounts() {
        return userRoleCounts;
    }

    public void setUserRoleCounts(Map<String, Long> userRoleCounts) {
        this.userRoleCounts = userRoleCounts;
    }
}
