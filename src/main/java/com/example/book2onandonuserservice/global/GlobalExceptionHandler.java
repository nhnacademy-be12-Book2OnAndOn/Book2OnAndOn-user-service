package com.example.book2onandonuserservice.global;

import com.example.book2onandonuserservice.address.exception.AddressLimitExceededException;
import com.example.book2onandonuserservice.address.exception.AddressNameDuplicateException;
import com.example.book2onandonuserservice.address.exception.AddressNotFoundException;
import com.example.book2onandonuserservice.auth.exception.AuthenticationFailedException;
import com.example.book2onandonuserservice.auth.exception.PaycoInfoMissingException;
import com.example.book2onandonuserservice.auth.exception.PaycoServerException;
import com.example.book2onandonuserservice.global.dto.ErrorResponse;
import com.example.book2onandonuserservice.point.exception.DuplicatePointPolicyException;
import com.example.book2onandonuserservice.point.exception.InactivePointPolicyException;
import com.example.book2onandonuserservice.point.exception.InsufficientPointException;
import com.example.book2onandonuserservice.point.exception.InvalidAuthenticationException;
import com.example.book2onandonuserservice.point.exception.InvalidPointPolicyException;
import com.example.book2onandonuserservice.point.exception.OrderAlreadyRewardedException;
import com.example.book2onandonuserservice.point.exception.PointAlreadyUsedForOrderException;
import com.example.book2onandonuserservice.point.exception.PointPolicyNotFoundException;
import com.example.book2onandonuserservice.point.exception.PointRangeExceededException;
import com.example.book2onandonuserservice.point.exception.ReturnAlreadyProcessedException;
import com.example.book2onandonuserservice.point.exception.ReviewAlreadyRewardedException;
import com.example.book2onandonuserservice.point.exception.SignupPointAlreadyGrantedException;
import com.example.book2onandonuserservice.point.exception.UserIdMismatchException;
import com.example.book2onandonuserservice.user.exception.EmailNotVerifiedException;
import com.example.book2onandonuserservice.user.exception.GradeNameDuplicateException;
import com.example.book2onandonuserservice.user.exception.GradeNotFoundException;
import com.example.book2onandonuserservice.user.exception.PasswordMismatchException;
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
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    //400 Bad Request
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "INVALID_INPUT",
                errorMessage);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    //400 Bad Request
    @ExceptionHandler({
            AddressLimitExceededException.class,
            AddressNameDuplicateException.class,
            UserLoginIdDuplicateException.class,
            UserEmailDuplicateException.class,
            UserNicknameDuplicationException.class,
            PasswordMismatchException.class,
            GradeNameDuplicateException.class,
            DuplicatePointPolicyException.class,
            InvalidPointPolicyException.class,
            UserIdMismatchException.class,
            OrderAlreadyRewardedException.class,
            ReviewAlreadyRewardedException.class,
            ReturnAlreadyProcessedException.class,
            PointRangeExceededException.class,
            PointAlreadyUsedForOrderException.class,
            InsufficientPointException.class,
            InactivePointPolicyException.class,
            UserNotDormantException.class,
            PaycoInfoMissingException.class,
            EmailNotVerifiedException.class,
            BadRequestException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequestExceptions(RuntimeException ex) {
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "BAD_REQUEST",
                ex.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    //401 Unauthorized
    @ExceptionHandler({
            AuthenticationFailedException.class,
            InvalidAuthenticationException.class
    })
    public ResponseEntity<ErrorResponse> handleAuthenticationFailed(RuntimeException ex) {
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "AUTH_FAILED",
                ex.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    //403 Forbidden
    @ExceptionHandler(UserDormantException.class)
    public ResponseEntity<ErrorResponse> handleUserDormantException(UserDormantException ex) {
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                "ACCOUNT_DORMANT",
                ex.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UserWithdrawnException.class)
    public ResponseEntity<ErrorResponse> handleAccountStatusException(RuntimeException ex) {
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                "ACCESS_DENIED",
                ex.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    //404 Not Found
    @ExceptionHandler({
            UserNotFoundException.class,
            AddressNotFoundException.class,
            PointPolicyNotFoundException.class,
            GradeNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handlerNotFoundException(RuntimeException ex) {
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "NOT_FOUND",
                ex.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // 409 Conflict
    @ExceptionHandler(SignupPointAlreadyGrantedException.class)
    public ResponseEntity<ErrorResponse> handleSignupPointAlreadyGranted(RuntimeException ex) {
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "CONFLICT",
                ex.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }


    //500 Internal Server Error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex) {
        log.error("시스템 오류 발생: " + ex); //서버 로그에 로그를 남김
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "서버 오류가 발생했습니다."
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    //502 Bad Gateway
    @ExceptionHandler(PaycoServerException.class)
    public ResponseEntity<ErrorResponse> handlePaycoServerException(PaycoServerException ex) {
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_GATEWAY.value(),
                "BAD_REQUEST",
                ex.getMessage()
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_GATEWAY);
    }

}
