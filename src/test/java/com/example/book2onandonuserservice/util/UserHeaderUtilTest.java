package com.example.book2onandonuserservice.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.book2onandonuserservice.global.util.UserHeaderUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class UserHeaderUtilTest {

    private UserHeaderUtil userHeaderUtil;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        userHeaderUtil = new UserHeaderUtil();
        request = new MockHttpServletRequest();
        // MockRequest를 ContextHolder에 등록
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }


    @Test
    @DisplayName("User ID 가져오기 성공 - 헤더에 유효한 ID가 있을 때")
    void getUserId_Success() {
        request.addHeader("X-User-Id", "12345");
        Long userId = userHeaderUtil.getUserId();
        assertThat(userId).isEqualTo(12345L);
    }

    @Test
    @DisplayName("User ID 가져오기 실패 - 헤더 값이 숫자가 아님 (NumberFormatException)")
    void getUserId_Fail_NotNumber() {
        request.addHeader("X-User-Id", "invalid-id");
        Long userId = userHeaderUtil.getUserId();
        assertThat(userId).isNull();
    }

    @Test
    @DisplayName("User ID 가져오기 실패 - 헤더가 없거나 비어있음")
    void getUserId_Fail_EmptyHeader() {
        Long userId = userHeaderUtil.getUserId();
        assertThat(userId).isNull();
    }

    @Test
    @DisplayName("User ID 가져오기 실패 - 요청 컨텍스트가 없음 (Request is null)")
    void getUserId_Fail_NoRequest() {
        RequestContextHolder.resetRequestAttributes();
        Long userId = userHeaderUtil.getUserId();
        assertThat(userId).isNull();
    }

    @Test
    @DisplayName("User Role 가져오기 성공")
    void getUserRole_Success() {
        request.addHeader("X-User-Role", "ROLE_USER");
        String role = userHeaderUtil.getUserRole();
        assertThat(role).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("User Role 가져오기 실패 - 헤더 없음")
    void getUserRole_Fail_NoHeader() {
        String role = userHeaderUtil.getUserRole();
        assertThat(role).isNull();
    }

    @Test
    @DisplayName("User Role 가져오기 실패 - 요청 컨텍스트 없음")
    void getUserRole_Fail_NoRequest() {
        RequestContextHolder.resetRequestAttributes();
        String role = userHeaderUtil.getUserRole();
        assertThat(role).isNull();
    }
}