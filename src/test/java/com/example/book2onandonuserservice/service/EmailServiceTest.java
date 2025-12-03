package com.example.book2onandonuserservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.book2onandonuserservice.global.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;

class EmailServiceTest {

    @Mock
    JavaMailSender mailSender;
    @Mock
    MimeMessage mimeMessage;
    EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        emailService = new EmailService(mailSender);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    // 정상 메일 발송 테스트
    @Test
    void sendMail_success() {
        String to = "test@naver.com";
        String subject = "Test email";
        String content = "This is a test email";

        emailService.sendMail(to, subject, content);
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(mimeMessage);
    }

    //MessagingException 예외처리 테스트
    @Test
    void sendMail_messagingException_throwsRuntimeException() throws Exception {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // send() 호출 시 MailSendException 발생 (MessagingException은 불가)
        doThrow(new RuntimeException("이메일 발송 중 오류 발생"))
                .when(mailSender)
                .send(any(MimeMessage.class));

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                emailService.sendMail("test@test.com", "제목", "내용")
        );

        assertEquals("이메일 발송 중 오류 발생", ex.getMessage());
    }
}
