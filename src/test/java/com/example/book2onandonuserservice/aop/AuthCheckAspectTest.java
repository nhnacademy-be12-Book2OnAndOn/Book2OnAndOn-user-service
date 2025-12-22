package com.example.book2onandonuserservice.aop;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.example.book2onandonuserservice.global.annotation.AuthCheck;
import com.example.book2onandonuserservice.global.aop.AuthCheckAspect;
import com.example.book2onandonuserservice.global.util.UserHeaderUtil;
import com.example.book2onandonuserservice.user.domain.entity.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class AuthCheckAspectTest {

    @InjectMocks
    private AuthCheckAspect authCheckAspect;

    @Mock
    private UserHeaderUtil userHeaderUtil;

    @Test
    @DisplayName("권한 체크 성공 - 유저가 필요한 권한을 가지고 있음")
    void checkRole_Success() {
        // Given
        AuthCheck authCheck = mock(AuthCheck.class);
        given(authCheck.value()).willReturn(new Role[]{Role.USER});

        given(userHeaderUtil.getUserRole()).willReturn(Role.USER.getKey());

        assertThatCode(() -> authCheckAspect.checkRole(authCheck))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("권한 체크 성공 - 슈퍼 관리자(SUPER_ADMIN)는 무조건 통과")
    void checkRole_SuperAdmin_Pass() {
        AuthCheck authCheck = mock(AuthCheck.class);
        given(userHeaderUtil.getUserRole()).willReturn(Role.SUPER_ADMIN.getKey());

        assertThatCode(() -> authCheckAspect.checkRole(authCheck))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("권한 체크 실패 - 헤더에 권한 정보가 없음 (로그인 안함)")
    void checkRole_Fail_NoHeader() {
        AuthCheck authCheck = mock(AuthCheck.class);
        given(userHeaderUtil.getUserRole()).willReturn(null);

        assertThatThrownBy(() -> authCheckAspect.checkRole(authCheck))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("권한 정보가 없습니다. (로그인 필요)");
    }

    @Test
    @DisplayName("권한 체크 실패 - 필요한 권한이 없음")
    void checkRole_Fail_AccessDenied() {
        AuthCheck authCheck = mock(AuthCheck.class);
        given(authCheck.value()).willReturn(new Role[]{Role.MEMBER_ADMIN});

        given(userHeaderUtil.getUserRole()).willReturn(Role.USER.getKey());

        assertThatThrownBy(() -> authCheckAspect.checkRole(authCheck))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("해당 리소스에 접근할 권한이 없습니다.");
    }
}