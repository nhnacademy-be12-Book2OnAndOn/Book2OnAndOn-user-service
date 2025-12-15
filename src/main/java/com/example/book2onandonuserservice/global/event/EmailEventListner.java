package com.example.book2onandonuserservice.global.event;

import com.example.book2onandonuserservice.global.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailEventListner {
    private final EmailService emailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleEmailSendEvent(EmailSendEvent event) {
        log.info("이메일 발송 시작: {}", event.getTo());
        try {
            emailService.sendMail(event.getTo(), event.getSubject(), event.getText());
            log.info("이메일 발송 완료: {}", event.getTo());
        } catch (Exception e) {
            log.error("이메일 발송 실패: {}", e.getMessage());
        }
    }
}
