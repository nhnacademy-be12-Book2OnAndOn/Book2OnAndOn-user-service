package com.example.book2onandonuserservice.global.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class UserHeaderUtil {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_ROLE = "X-User-Role";

    public Long getUserId() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return null;
        }

        String userIdStr = request.getHeader(HEADER_USER_ID);
        if (userIdStr == null || userIdStr.isEmpty()) {
            return null;
        }

        try {
            return Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public String getUserRole() {
        HttpServletRequest request = getRequest();
        return (request != null) ? request.getHeader(HEADER_USER_ROLE) : null;
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return (attributes != null) ? attributes.getRequest() : null;
    }
}