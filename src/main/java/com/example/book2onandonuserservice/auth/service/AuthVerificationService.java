package com.example.book2onandonuserservice.auth.service;

import com.example.book2onandonuserservice.address.exception.InvalidVerificationCodeException;
import com.example.book2onandonuserservice.global.event.EmailSendEvent;
import com.example.book2onandonuserservice.global.util.EncryptionUtils;
import com.example.book2onandonuserservice.global.util.RedisKeyPrefix;
import com.example.book2onandonuserservice.global.util.RedisUtil;
import com.example.book2onandonuserservice.user.domain.entity.Status;
import com.example.book2onandonuserservice.user.domain.entity.Users;
import com.example.book2onandonuserservice.user.exception.UserEmailDuplicateException;
import com.example.book2onandonuserservice.user.exception.UserNotDormantException;
import com.example.book2onandonuserservice.user.exception.UserNotFoundException;
import com.example.book2onandonuserservice.user.repository.UsersRepository;
import java.security.SecureRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthVerificationService {

    private final UsersRepository usersRepository;
    private final RedisUtil redisUtil;
    private final ApplicationEventPublisher eventPublisher;
    private static final SecureRandom secureRandom = new SecureRandom();
    private final EncryptionUtils encryptionUtils;

    // 회원가입용 이메일 인증번호 발송
    public void sendVerificationCode(String email) {
        String cleanEmail = email.trim();
        String emailHash = encryptionUtils.hash(cleanEmail);

        if (usersRepository.findByEmailHash(emailHash).isPresent()) {
            throw new UserEmailDuplicateException();
        }

        String code = generateRandomCode();

        String key = RedisKeyPrefix.EMAIL_CODE.buildKey(cleanEmail);
        redisUtil.setData(key, code, 5 * 60 * 1000L);

        String subject = "[Book2OnAndOn] 회원가입 인증번호";
        String text = "인증번호는 <b>" + code + "</b> 입니다.";
        eventPublisher.publishEvent(new EmailSendEvent(cleanEmail, subject, text));

        log.info("회원가입 인증번호 발송 요청 완료: email={}", cleanEmail);
    }

    // 이메일 인증번호 검증
    public boolean verifyEmail(String email, String code) {
        String cleanEmail = email.trim();
        String codeKey = RedisKeyPrefix.EMAIL_CODE.buildKey(cleanEmail);
        String savedCode = redisUtil.getData(codeKey);

        if (savedCode == null || !savedCode.equals(code)) {
            return false;
        }

        String verifiedKey = RedisKeyPrefix.EMAIL_VERIFIED.buildKey(cleanEmail);
        redisUtil.setData(verifiedKey, "true", 30 * 60 * 1000L);

        redisUtil.deleteData(codeKey);

        log.info("이메일 인증 성공: email={}", cleanEmail);
        return true;
    }

    // 휴면 해제용 인증번호 발송
    public void sendDormantVerificationCode(String email) {
        String cleanEmail = email.trim();
        String emailHash = encryptionUtils.hash(cleanEmail);

        Users user = usersRepository.findByEmailHash(emailHash)
                .orElseThrow(() -> new UserNotFoundException(0L));

        if (user.getStatus() != Status.DORMANT) {
            throw new UserNotDormantException();
        }

        String code = generateRandomCode();

        String key = RedisKeyPrefix.EMAIL_DORMANT_CODE.buildKey(cleanEmail);
        redisUtil.setData(key, code, 5 * 60 * 1000L);

        String subject = "[Book2OnAndOn] 휴면 해제 인증번호";
        String text = "휴면 상태를 해제하려면 인증번호 <b>" + code + "</b>를 입력해주세요.";
        eventPublisher.publishEvent(new EmailSendEvent(cleanEmail, subject, text));

        log.info("휴면 해제 인증번호 발송 요청 완료: email={}", cleanEmail);
    }


    // 휴면계정 해제처리
    @Transactional
    public void unlockDormantAccount(String email, String code) {
        String key = RedisKeyPrefix.EMAIL_DORMANT_CODE.buildKey(email);
        String savedCode = redisUtil.getData(key);

        if (savedCode == null || !savedCode.equals(code)) {
            throw new InvalidVerificationCodeException();
        }

        redisUtil.deleteData(key);

        String emailHash = encryptionUtils.hash(email);

        Users user = usersRepository.findByEmailHash(emailHash)
                .orElseThrow(() -> new UserNotFoundException(0L));

        if (user.getStatus() == Status.DORMANT) {
            user.changeStatus(Status.ACTIVE);
            user.updateLastLogin();
            log.info("휴면 계정 해제 완료: userId={}", user.getUserId());
        }
    }

    // 헬퍼 메서드 6자리 랜덤 숫자 생성
    private String generateRandomCode() {
        int randomNum = secureRandom.nextInt(900000) + 100000;
        return String.valueOf(randomNum);
    }
}