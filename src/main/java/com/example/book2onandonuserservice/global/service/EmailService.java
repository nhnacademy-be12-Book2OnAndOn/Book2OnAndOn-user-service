package com.example.book2onandonuserservice.global.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender javaMailSender;

    @Async
    public void sendMail(String to, String subject, String text) {
        MimeMessage message = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true);

            javaMailSender.send(message);
            log.info("이메일 발송 성공: {}", to);
        } catch (MessagingException e) {
            log.error("이메일 발송 실패: {}", e.getCause());
            throw new RuntimeException("이메일 발송 중 오류 발생");
        }
    }
}
