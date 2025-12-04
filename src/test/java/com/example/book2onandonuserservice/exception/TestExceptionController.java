package com.example.book2onandonuserservice.exception;

import com.example.book2onandonuserservice.point.exception.InvalidPointPolicyException;
import com.example.book2onandonuserservice.point.exception.SignupPointAlreadyGrantedException;
import com.example.book2onandonuserservice.user.exception.UserNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
class TestExceptionController {

    @Getter
    static class DummyRequest {
        @NotBlank(message = "이름은 필수입니다.")
        private String name;
    }

    @PostMapping("/test/invalid")
    public void invalid(@RequestBody @Valid DummyRequest request) {
        //
    }


    @GetMapping("/test/bad-request")
    public void badRequest() {
        throw new InvalidPointPolicyException("잘못된 정책");
    }

    @GetMapping("/test/not-found")
    public void notFound() {
        throw new UserNotFoundException(1L);
    }

    @GetMapping("/test/conflict")
    public void conflict() {
        throw new SignupPointAlreadyGrantedException(1L);
    }


    @GetMapping("/test/auth-failed")
    public void authFailed() {
        throw new com.example.book2onandonuserservice.auth.exception.AuthenticationFailedException();
    }

    @GetMapping("/test/server-error")
    public void serverError() {
        throw new RuntimeException("서버 에러");
    }
}
