package com.example.book2onandonuserservice.global;

import com.example.book2onandonuserservice.address.exception.AddressLimitExceededException;
import com.example.book2onandonuserservice.address.exception.AddressNameDuplicateException;
import com.example.book2onandonuserservice.address.exception.AddressNotFoundException;
import com.example.book2onandonuserservice.auth.exception.AuthenticationFailedException;
import com.example.book2onandonuserservice.global.dto.ErrorResponseDto;
import com.example.book2onandonuserservice.user.exception.PasswordMismatchException;
import com.example.book2onandonuserservice.user.exception.UserDormantException;
import com.example.book2onandonuserservice.user.exception.UserEmailDuplicateException;
import com.example.book2onandonuserservice.user.exception.UserLoginIdDuplicateException;
import com.example.book2onandonuserservice.user.exception.UserNotFoundException;
import com.example.book2onandonuserservice.user.exception.UserWithdrawnException;
import lombok.extern.slf4j.Slf4j;
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
    public ResponseEntity<ErrorResponseDto> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        ErrorResponseDto response = new ErrorResponseDto("INVALID_INPUT", errorMessage);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    //400 Bad Request
    @ExceptionHandler({
            AddressLimitExceededException.class,
            AddressNameDuplicateException.class,
            UserLoginIdDuplicateException.class,
            UserEmailDuplicateException.class,
            PasswordMismatchException.class
    })
    public ResponseEntity<ErrorResponseDto> handleBadRequestExceptions(RuntimeException ex) {
        ErrorResponseDto response = new ErrorResponseDto("BAD_REQUEST", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    //401 Unauthorized
    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ErrorResponseDto> handleAuthenticationFailed(AuthenticationFailedException ex) {
        ErrorResponseDto response = new ErrorResponseDto("AUTH_FAILED", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    //403 Forbidden
    @ExceptionHandler({UserDormantException.class, UserWithdrawnException.class})
    public ResponseEntity<ErrorResponseDto> handleAccountStatusException(UserDormantException ex) {
        ErrorResponseDto response = new ErrorResponseDto("ACCESS_DENIED", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    //404 Not Found
    @ExceptionHandler({UserNotFoundException.class, AddressNotFoundException.class})
    public ResponseEntity<ErrorResponseDto> handlerNotFoundException(RuntimeException ex) {
        ErrorResponseDto response = new ErrorResponseDto("NOT_FOUND", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    //500 Internal Server Error
    public ResponseEntity<ErrorResponseDto> handleAllExceptions(Exception ex) {
        log.error("시스템 오류 발생: " + ex); //서버 로그에 로그를 남김
        ErrorResponseDto response = new ErrorResponseDto("INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다.");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
