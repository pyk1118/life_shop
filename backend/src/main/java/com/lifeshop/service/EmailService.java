package com.lifeshop.service;

public interface EmailService {
    void sendMerchantAuditResult(String toEmail, String shopName, String merchantAccount, boolean approved, String auditRemark);
    void sendMessageReplyNotification(String toEmail, String messageTitle, String messageContent, String replyContent);
    void sendMerchantReplyNotification(String toEmail, String shopName, String messageTitle, String messageContent, String replyContent);
}
