package com.example.book2onandonuserservice.global.aop;

import com.example.book2onandonuserservice.auth.domain.dto.request.LoginRequestDto;
import com.example.book2onandonuserservice.user.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ReEncryptionAspect {
    private final UsersRepository usersRepository;

    @AfterReturning(
            pointcut = "execution(* com.example.book2onandonuserservice.auth.service.AuthService.login(..))",
            returning = "result"
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW) // 로그인 트랜젝션과 분리하여 실패해도 로그인엔 지장 X
    public void migrateUserKeyOnLogin(JoinPoint joinPoint, Object result) {
        try {
            // 메서드의 첫 번째 인자가 LoginRequestDto인지 확인
            Object[] args = joinPoint.getArgs();
            for (Object arg : args) {
                if (arg instanceof LoginRequestDto requestDto) {
                    String userLoginId = requestDto.userId(); // LoginRequestDto의 userId (String)

                    // userLoginId로 유저 조회 후 save() 호출하여 재암호화 트리거
                    usersRepository.findByUserLoginId(userLoginId).ifPresent(user -> {
                        usersRepository.save(user);
                        log.info("User(Login ID: {}) Lazy Re-encryption completed on login.", userLoginId);
                    });
                    break;
                }
            }
        } catch (Exception e) {
            log.warn("Lazy Re-encryption failed during login process.", e);
            // 재암호화 실패가 로그인 흐름을 방해하지 않도록 예외를 무시
        }
    }
}
