package com.example.book2onandonuserservice.point.support;

import com.example.book2onandonuserservice.user.domain.entity.Role;
import java.util.Set;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class AdminAuthorization {

    // 포인트 관리 접근 허용 관리자
    private static final Set<Role> POINT_ALLOWED_ROLES = Set.of(
            Role.SUPER_ADMIN,
            Role.MEMBER_ADMIN
    );

    public void requirePointAdmin(String role) {
        if (role == null) {
            throw new AccessDeniedException("권한 정보가 없습니다.");
        }

        Role parsed;
        try {
            parsed = Role.valueOf(role);
        } catch (IllegalArgumentException e) {
            throw new AccessDeniedException("올바르지 않은 권한 값입니다.");
        }

        if (!POINT_ALLOWED_ROLES.contains(parsed)) {
            throw new AccessDeniedException("포인트 관리 권한이 없습니다.");
        }
    }
}
