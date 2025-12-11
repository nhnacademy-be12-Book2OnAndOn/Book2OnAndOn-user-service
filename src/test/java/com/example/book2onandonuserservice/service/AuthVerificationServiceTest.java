package com.example.book2onandonuserservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.book2onandonuserservice.auth.service.AuthVerificationService;
import com.example.book2onandonuserservice.global.event.EmailSendEvent;
import com.example.book2onandonuserservice.global.util.RedisKeyPrefix;
import com.example.book2onandonuserservice.global.util.RedisUtil;
import com.example.book2onandonuserservice.user.repository.UsersRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class AuthVerificationServiceTest {

    @InjectMocks
    private AuthVerificationService verificationService;

    @Mock
    private UsersRepository usersRepository;
    @Mock
    private RedisUtil redisUtil;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    @DisplayName("인증번호 발송 성공")
    void sendVerificationCode_success() {
        String email = "test@test.com";
        when(usersRepository.findByEmail(email)).thenReturn(java.util.Optional.empty());

        verificationService.sendVerificationCode(email);

        verify(redisUtil).setData(anyString(), anyString(), anyLong());
        verify(eventPublisher).publishEvent(any(EmailSendEvent.class));
    }

    @Test
    @DisplayName("인증번호 검증 성공")
    void verifyEmail_success() {
        String email = "test@test.com";
        String code = "123456";
        String key = RedisKeyPrefix.EMAIL_CODE.buildKey(email);

        when(redisUtil.getData(key)).thenReturn(code);

        boolean result = verificationService.verifyEmail(email, code);

        assertThat(result).isTrue();
        verify(redisUtil).setData(eq(RedisKeyPrefix.EMAIL_VERIFIED.buildKey(email)), eq("true"), anyLong());
    }
}