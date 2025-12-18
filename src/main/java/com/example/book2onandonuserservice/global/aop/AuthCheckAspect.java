package com.example.book2onandonuserservice.global.aop;

import com.example.book2onandonuserservice.global.annotation.AuthCheck;
import com.example.book2onandonuserservice.global.util.UserHeaderUtil;
import com.example.book2onandonuserservice.user.domain.entity.Role;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthCheckAspect {

    private final UserHeaderUtil userHeaderUtil;

    @Before("@annotation(authCheck)")
    public void checkRole(AuthCheck authCheck) {
        String userRoleStr = userHeaderUtil.getUserRole();

        // 1. 로그인 여부 확인 (Role 헤더가 없으면 권한 없음 처리)
        if (userRoleStr == null || userRoleStr.isBlank()) {
            throw new AccessDeniedException("권한 정보가 없습니다. (로그인 필요)");
        }

        // 2. SUPER_ADMIN은 모든 권한 프리패스
        if (Role.SUPER_ADMIN.getKey().equals(userRoleStr)) {
            return;
        }

        // 어노테이션에 명시된 권한 중 하나라도 일치하면 통과
        boolean hasPermission = Arrays.stream(authCheck.value())
                .anyMatch(allowedRole -> allowedRole.getKey().equals(userRoleStr));

        if (!hasPermission) {
            log.warn("권한 없는 접근 시도. User Role: {}, Required: {}", userRoleStr, Arrays.toString(authCheck.value()));
            throw new AccessDeniedException("해당 리소스에 접근할 권한이 없습니다.");
        }
    }
}