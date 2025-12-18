package com.example.book2onandonuserservice.exception;

import com.example.book2onandonuserservice.auth.exception.AuthenticationFailedException;
import com.example.book2onandonuserservice.auth.exception.InvalidRefreshTokenException;
import com.example.book2onandonuserservice.auth.exception.PaycoServerException;
import com.example.book2onandonuserservice.point.exception.InvalidPointPolicyException;
import com.example.book2onandonuserservice.point.exception.SignupPointAlreadyGrantedException;
import com.example.book2onandonuserservice.user.domain.dto.request.UserUpdateRequestDto;
import com.example.book2onandonuserservice.user.exception.GradeNotFoundException;
import com.example.book2onandonuserservice.user.exception.SameAsOldPasswordException;
import com.example.book2onandonuserservice.user.exception.UserDormantException;
import com.example.book2onandonuserservice.user.exception.UserNotFoundException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestExceptionController {

    // 400: Validation 실패용
    @PostMapping("/invalid")
    public void invalidInput(@RequestBody @Valid UserUpdateRequestDto dto) {
        //
    }

    // 400: 일반적인 Bad Request (포인트 관련)
    @GetMapping("/bad-request-point")
    public void badRequestPoint() {
        throw new InvalidPointPolicyException("잘못된 포인트 정책");
    }

    // 400: User 관련 Bad Request (비밀번호 중복)
    @GetMapping("/bad-request-password")
    public void badRequestPassword() {
        throw new SameAsOldPasswordException();
    }

    // 401: 인증 실패
    @GetMapping("/auth-failed")
    public void authFailed() {
        throw new AuthenticationFailedException();
    }

    // 401: Refresh Token 유효성 검사 실패
    @GetMapping("/invalid-refresh-token")
    public void invalidRefreshToken() {
        throw new InvalidRefreshTokenException("유효하지 않은 RefreshToken입니다.");
    }

    // 403: 휴면 계정
    @GetMapping("/forbidden")
    public void forbidden() {
        throw new UserDormantException();
    }

    // 404: 리소스 없음 (회원)
    @GetMapping("/not-found-user")
    public void notFoundUser() {
        throw new UserNotFoundException(1L);
    }

    // 404: 리소스 없음 (등급)
    @GetMapping("/not-found-grade")
    public void notFoundGrade() {
        throw new GradeNotFoundException();
    }

    // 409: 충돌
    @GetMapping("/conflict")
    public void conflict() {
        throw new SignupPointAlreadyGrantedException("이미 가입 포인트를 받았습니다.");
    }

    // 500: 서버 오류
    @GetMapping("/server-error")
    public void serverError() {
        throw new RuntimeException("알 수 없는 오류");
    }

    // 502: 외부 API 오류 (Payco)
    @GetMapping("/bad-gateway")
    public void badGateway() {
        throw new PaycoServerException("Payco 서버 응답 없음");
    }
}