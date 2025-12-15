package com.example.book2onandonuserservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.book2onandonuserservice.address.exception.InvalidVerificationCodeException;
import com.example.book2onandonuserservice.auth.service.AuthVerificationService;
import com.example.book2onandonuserservice.global.event.EmailSendEvent;
import com.example.book2onandonuserservice.global.util.RedisKeyPrefix;
import com.example.book2onandonuserservice.global.util.RedisUtil;
import com.example.book2onandonuserservice.user.domain.entity.Status;
import com.example.book2onandonuserservice.user.domain.entity.Users;
import com.example.book2onandonuserservice.user.exception.UserEmailDuplicateException;
import com.example.book2onandonuserservice.user.exception.UserNotDormantException;
import com.example.book2onandonuserservice.user.exception.UserNotFoundException;
import com.example.book2onandonuserservice.user.repository.UsersRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

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

    // 회원가입용 인증번호 발송 테스트
    @Test
    @DisplayName("인증번호 발송 성공 - 중복되지 않은 이메일")
    void sendVerificationCode_success() {
        String email = "test@test.com";
        given(usersRepository.findByEmail(email)).willReturn(Optional.empty());

        verificationService.sendVerificationCode(email);

        verify(redisUtil).setData(anyString(), anyString(), anyLong());
        verify(eventPublisher).publishEvent(any(EmailSendEvent.class));
    }

    @Test
    @DisplayName("인증번호 발송 실패 - 이미 가입된 이메일")
    void sendVerificationCode_duplicateEmail() {
        String email = "duplicate@test.com";
        given(usersRepository.findByEmail(email)).willReturn(Optional.of(new Users()));

        assertThatThrownBy(() -> verificationService.sendVerificationCode(email))
                .isInstanceOf(UserEmailDuplicateException.class);

        verify(eventPublisher, never()).publishEvent(any());
    }

    // 인증번호 검증 테스트
    @Test
    @DisplayName("인증번호 검증 성공 - 코드가 일치함")
    void verifyEmail_success() {
        // Given
        String email = "test@test.com";
        String code = "123456";
        String key = RedisKeyPrefix.EMAIL_CODE.buildKey(email);

        given(redisUtil.getData(key)).willReturn(code);

        boolean result = verificationService.verifyEmail(email, code);

        assertThat(result).isTrue();
        verify(redisUtil).setData(eq(RedisKeyPrefix.EMAIL_VERIFIED.buildKey(email)), eq("true"), anyLong());
        verify(redisUtil).deleteData(key);
    }

    @Test
    @DisplayName("인증번호 검증 실패 - 코드가 불일치하거나 없음")
    void verifyEmail_fail() {
        // Given
        String email = "test@test.com";
        String code = "123456";
        String key = RedisKeyPrefix.EMAIL_CODE.buildKey(email);

        given(redisUtil.getData(key)).willReturn(null);

        boolean result = verificationService.verifyEmail(email, code);

        assertThat(result).isFalse();
        verify(redisUtil, never()).setData(eq(RedisKeyPrefix.EMAIL_VERIFIED.buildKey(email)), anyString(), anyLong());
    }

    // 휴면 해제용 인증번호 발송 테스트

    @Test
    @DisplayName("휴면 인증번호 발송 성공 - 휴면 계정인 경우")
    void sendDormantVerificationCode_success() {
        String email = "dormant@test.com";
        Users user = new Users();
        ReflectionTestUtils.setField(user, "status", Status.DORMANT);

        given(usersRepository.findByEmail(email)).willReturn(Optional.of(user));

        verificationService.sendDormantVerificationCode(email);

        verify(redisUtil).setData(anyString(), anyString(), anyLong());
        verify(eventPublisher).publishEvent(any(EmailSendEvent.class));
    }

    @Test
    @DisplayName("휴면 인증번호 발송 실패 - 회원이 아님")
    void sendDormantVerificationCode_userNotFound() {
        String email = "unknown@test.com";
        given(usersRepository.findByEmail(email)).willReturn(Optional.empty());

        assertThatThrownBy(() -> verificationService.sendDormantVerificationCode(email))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("휴면 인증번호 발송 실패 - 휴면 계정이 아님")
    void sendDormantVerificationCode_notDormant() {
        String email = "active@test.com";
        Users user = new Users();
        ReflectionTestUtils.setField(user, "status", Status.ACTIVE);

        given(usersRepository.findByEmail(email)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> verificationService.sendDormantVerificationCode(email))
                .isInstanceOf(UserNotDormantException.class);
    }

    // 휴면 해제 처리 테스트
    @Test
    @DisplayName("휴면 해제 처리 성공 - 인증번호 일치")
    void unlockDormantAccount_success() {
        String email = "dormant@test.com";
        String code = "123456";
        String key = RedisKeyPrefix.EMAIL_DORMANT_CODE.buildKey(email);

        Users user = new Users();
        ReflectionTestUtils.setField(user, "userId", 1L);
        ReflectionTestUtils.setField(user, "status", Status.DORMANT);

        given(redisUtil.getData(key)).willReturn(code);
        given(usersRepository.findByEmail(email)).willReturn(Optional.of(user));

        verificationService.unlockDormantAccount(email, code);

        assertThat(user.getStatus()).isEqualTo(Status.ACTIVE);
        verify(redisUtil).deleteData(key);
    }

    @Test
    @DisplayName("휴면 해제 처리 실패 - 인증번호 불일치")
    void unlockDormantAccount_invalidCode() {
        String email = "dormant@test.com";
        String code = "123456";
        String key = RedisKeyPrefix.EMAIL_DORMANT_CODE.buildKey(email);

        given(redisUtil.getData(key)).willReturn("999999"); // 다른 코드

        assertThatThrownBy(() -> verificationService.unlockDormantAccount(email, code))
                .isInstanceOf(InvalidVerificationCodeException.class);

        verify(usersRepository, never()).findByEmail(anyString());
    }
}