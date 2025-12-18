package com.example.book2onandonuserservice.global;

import com.example.book2onandonuserservice.address.exception.AddressLimitExceededException;
import com.example.book2onandonuserservice.address.exception.AddressNameDuplicateException;
import com.example.book2onandonuserservice.address.exception.AddressNotFoundException;
import com.example.book2onandonuserservice.auth.exception.AuthenticationFailedException;
import com.example.book2onandonuserservice.auth.exception.InvalidRefreshTokenException;
import com.example.book2onandonuserservice.auth.exception.PaycoInfoMissingException;
import com.example.book2onandonuserservice.auth.exception.PaycoServerException;
import com.example.book2onandonuserservice.global.dto.ErrorResponse;
import com.example.book2onandonuserservice.point.exception.AdminAdjustPointNegativeBalanceException;
import com.example.book2onandonuserservice.point.exception.DuplicatePointPolicyException;
import com.example.book2onandonuserservice.point.exception.InactivePointPolicyException;
import com.example.book2onandonuserservice.point.exception.InsufficientPointException;
import com.example.book2onandonuserservice.point.exception.InvalidAdminAdjustPointException;
import com.example.book2onandonuserservice.point.exception.InvalidAuthenticationException;
import com.example.book2onandonuserservice.point.exception.InvalidPointPolicyException;
import com.example.book2onandonuserservice.point.exception.InvalidPointRateException;
import com.example.book2onandonuserservice.point.exception.InvalidRefundPointException;
import com.example.book2onandonuserservice.point.exception.OrderAlreadyRewardedException;
import com.example.book2onandonuserservice.point.exception.PointAlreadyUsedForOrderException;
import com.example.book2onandonuserservice.point.exception.PointPolicyNotFoundException;
import com.example.book2onandonuserservice.point.exception.PointRangeExceededException;
import com.example.book2onandonuserservice.point.exception.RefundPointRangeExceededException;
import com.example.book2onandonuserservice.point.exception.ReturnAlreadyProcessedException;
import com.example.book2onandonuserservice.point.exception.ReviewAlreadyRewardedException;
import com.example.book2onandonuserservice.point.exception.SignupPointAlreadyGrantedException;
import com.example.book2onandonuserservice.point.exception.UserIdMismatchException;
import com.example.book2onandonuserservice.user.exception.EmailNotVerifiedException;
import com.example.book2onandonuserservice.user.exception.GradeNameDuplicateException;
import com.example.book2onandonuserservice.user.exception.GradeNotFoundException;
import com.example.book2onandonuserservice.user.exception.PasswordMismatchException;
import com.example.book2onandonuserservice.user.exception.SameAsOldPasswordException;
import com.example.book2onandonuserservice.user.exception.SuperAdminDeletionException;
import com.example.book2onandonuserservice.user.exception.UserAlreadyWithdrawnException;
import com.example.book2onandonuserservice.user.exception.UserDormantException;
import com.example.book2onandonuserservice.user.exception.UserEmailDuplicateException;
import com.example.book2onandonuserservice.user.exception.UserLoginIdDuplicateException;
import com.example.book2onandonuserservice.user.exception.UserNicknameDuplicationException;
import com.example.book2onandonuserservice.user.exception.UserNotDormantException;
import com.example.book2onandonuserservice.user.exception.UserNotFoundException;
import com.example.book2onandonuserservice.user.exception.UserWithdrawnException;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * [400] @Valid 유효성 검사 실패
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "INVALID_INPUT", errorMessage);
    }

    /**
     * [400] Bad Request (잘못된 요청 공통 처리) - 새로 추가된 SameAsOldPasswordException 등도 여기서 일괄 처리됩니다.
     */
    @ExceptionHandler({
            // User Exceptions
            SameAsOldPasswordException.class,
            SuperAdminDeletionException.class,
            UserAlreadyWithdrawnException.class,
            UserLoginIdDuplicateException.class,
            UserEmailDuplicateException.class,
            UserNicknameDuplicationException.class,
            PasswordMismatchException.class,
            GradeNameDuplicateException.class,
            UserNotDormantException.class,
            EmailNotVerifiedException.class,

            // Address Exceptions
            AddressLimitExceededException.class,
            AddressNameDuplicateException.class,

            // Point Exceptions
            DuplicatePointPolicyException.class,
            InvalidPointPolicyException.class,
            InvalidPointRateException.class,
            InvalidRefundPointException.class,
            RefundPointRangeExceededException.class,
            AdminAdjustPointNegativeBalanceException.class,
            UserIdMismatchException.class,
            OrderAlreadyRewardedException.class,
            ReviewAlreadyRewardedException.class,
            ReturnAlreadyProcessedException.class,
            PointRangeExceededException.class,
            PointAlreadyUsedForOrderException.class,
            InsufficientPointException.class,
            InactivePointPolicyException.class,

            // Auth/Other Exceptions
            PaycoInfoMissingException.class,
            BadRequestException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequestExceptions(RuntimeException ex) {
        log.warn("Bad Request Exception: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage());
    }

    /**
     * [400] 헤더 누락
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "MISSING_HEADER",
                "필수 요청 헤더가 누락되었습니다: " + ex.getHeaderName());
    }

    /**
     * [401] 인증 실패
     */
    @ExceptionHandler({
            AuthenticationFailedException.class,
            InvalidAuthenticationException.class
    })
    public ResponseEntity<ErrorResponse> handleAuthenticationFailed(RuntimeException ex) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "AUTH_FAILED", ex.getMessage());
    }

    /**
     * [401] Refresh Token 유효성 검사 실패 (재로그인 필요)
     */
    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRefreshTokenException(InvalidRefreshTokenException ex) {
        log.warn("Invalid Refresh Token: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", ex.getMessage());
    }

    /**
     * [403] 권한 없음 / 휴면 / 접근 거부
     */
    @ExceptionHandler(UserDormantException.class)
    public ResponseEntity<ErrorResponse> handleUserDormantException(UserDormantException ex) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "ACCOUNT_DORMANT", ex.getMessage());
    }

    @ExceptionHandler(UserWithdrawnException.class)
    public ResponseEntity<ErrorResponse> handleAccountStatusException(RuntimeException ex) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "ACCESS_DENIED", ex.getMessage());
    }

    /**
     * [404] 리소스 찾을 수 없음 - GradeNotFoundException도 여기서 처리됩니다.
     */
    @ExceptionHandler({
            UserNotFoundException.class,
            AddressNotFoundException.class,
            PointPolicyNotFoundException.class,
            GradeNotFoundException.class // 중복 제거 및 통합
    })
    public ResponseEntity<ErrorResponse> handlerNotFoundException(RuntimeException ex) {
        log.warn("Not Found Exception: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage());
    }

    /**
     * [409] 충돌 (이미 존재함 등)
     */
    @ExceptionHandler({
            SignupPointAlreadyGrantedException.class,
            InvalidAdminAdjustPointException.class
    })
    public ResponseEntity<ErrorResponse> handleConflictExceptions(RuntimeException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage());
    }

    /**
     * [502] 외부 API 연동 오류
     */
    @ExceptionHandler(PaycoServerException.class)
    public ResponseEntity<ErrorResponse> handlePaycoServerException(PaycoServerException ex) {
        return buildErrorResponse(HttpStatus.BAD_GATEWAY, "PAYCO_BAD_GATEWAY", ex.getMessage());
    }

    /**
     * [500] 시스템 내부 오류
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex) {
        log.error("시스템 오류 발생: ", ex); // 스택트레이스 포함 로그
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다.");
    }

    // Helper Method (공통 응답 생성)
    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String code, String message) {
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                code,
                message
        );
        return new ResponseEntity<>(response, status);
    }
}