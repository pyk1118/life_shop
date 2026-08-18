package com.lifeshop.service.impl;

import com.lifeshop.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    /** 发件人邮箱，取自配置 spring.mail.username，避免硬编码 */
    @Value("${spring.mail.username}")
    private String fromEmail;

    private boolean isValidEmail(String email) {
        if (email == null || email.isBlank())
            return false;
        String trimmed = email.trim().toLowerCase();
        return trimmed.endsWith("@qq.com") || trimmed.endsWith("@163.com") || trimmed.endsWith("@126.com");
    }

    @Override
    @Async
    public void sendMerchantAuditResult(String toEmail, String shopName, String merchantAccount, boolean approved,
            String auditRemark) {
        if (!isValidEmail(toEmail)) {
            log.info("用户邮箱{}不是QQ/网易邮箱或不存在，跳过邮件发送", toEmail);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail.trim());
            message.setSubject("优品汇-商家入驻审核结果通知");
            String content;
            if (approved) {
                content = "尊敬的用户，您好！\n\n" +
                        "恭喜您！您申请入驻的店铺「" + shopName + "」已通过审核！\n" +
                        "您的商家登录账号为：" + merchantAccount + "\n" +
                        "您现在可以使用该账号登录优品汇商家后台，开始管理您的店铺和商品啦！\n" +
                        "祝您生意兴隆，期待您在优品汇平台上大展宏图！\n\n" +
                        "优品汇团队";
            } else {
                String remark = (auditRemark != null && !auditRemark.isBlank()) ? "\n审核备注：" + auditRemark : "";
                content = "尊敬的用户，您好！\n\n" +
                        "很遗憾地通知您，您申请入驻的店铺「" + shopName + "」暂未通过审核。" + remark + "\n" +
                        "请不要灰心，您可以根据审核备注修改相关信息后重新提交申请，我们期待您的再次尝试！\n" +
                        "如有任何疑问，欢迎随时通过平台留言反馈与我们联系。\n\n" +
                        "优品汇团队";
            }
            message.setText(content);
            mailSender.send(message);
            log.info("商家入驻审核邮件已发送至{}", toEmail);
        } catch (Exception e) {
            log.warn("发送商家入驻审核邮件失败: {}", e.getMessage());
        }
    }

    @Override
    @Async
    public void sendMessageReplyNotification(String toEmail, String messageTitle, String messageContent, String replyContent) {
        if (!isValidEmail(toEmail)) {
            log.info("用户邮箱{}不是QQ/网易邮箱或不存在，跳过邮件发送", toEmail);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail.trim());
            message.setSubject("优品汇-留言回复通知");
            message.setText("尊敬的用户，您好！\n\n" +
                    "您发布的留言「" + messageTitle + "」「" + messageContent + "」已收到开发者回复：\n" +
                    replyContent + "\n\n" +
                    "感谢您的反馈，我们将持续为您提供更好的服务！\n\n" +
                    "优品汇团队");
            mailSender.send(message);
            log.info("留言回复通知邮件已发送至{}", toEmail);
        } catch (Exception e) {
            log.warn("发送留言回复通知邮件失败: {}", e.getMessage());
        }
    }

    @Override
    @Async
    public void sendMerchantReplyNotification(String toEmail, String shopName, String messageTitle, String messageContent,
            String replyContent) {
        if (!isValidEmail(toEmail)) {
            log.info("用户邮箱{}不是QQ/网易邮箱或不存在，跳过邮件发送", toEmail);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail.trim());
            message.setSubject("优品汇-店铺回复通知");
            message.setText("尊敬的用户，您好！\n\n" +
                    "您发布的留言「" + messageTitle + "」「" + messageContent + "」已收到店铺「" + shopName + "」的回复：\n" +
                    replyContent + "\n\n" +
                    "感谢您的关注与反馈，祝您购物愉快！\n\n" +
                    "优品汇团队");
            mailSender.send(message);
            log.info("店铺回复通知邮件已发送至{}", toEmail);
        } catch (Exception e) {
            log.warn("发送店铺回复通知邮件失败: {}", e.getMessage());
        }
    }
}
