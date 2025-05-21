package org.zensnorlax.config;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

/**
 * @author zensnorlax
 * @version 1.0
 * @description: TODO
 * @date 2025/3/23 13:53
 */

@Component
public class EmailClient {

    @Autowired
    private JavaMailSender mailSender;

    // 从配置文件中注入发件人地址
    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * 发送简单文本邮件，并设置发件人
     *
     * @param to      收件人邮箱地址
     * @param subject 邮件主题
     * @param content 邮件内容
     */
    public void sendSimpleEmail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);
    }

    /**
     * 发送 HTML 格式邮件，并设置发件人
     *
     * @param to          收件人邮箱地址
     * @param subject     邮件主题
     * @param htmlContent HTML 格式的邮件内容
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true); // 第二个参数为 true 表示内容为 HTML 格式
        mailSender.send(mimeMessage);
    }
}

