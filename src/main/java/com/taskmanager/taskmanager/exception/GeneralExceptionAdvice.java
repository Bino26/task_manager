package com.taskmanager.taskmanager.exception;

import com.taskmanager.taskmanager.exception.custom.*;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GeneralExceptionAdvice extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex, HttpServletRequest req) {
        return new ResponseEntity<>(new ErrorResponse(
                ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, System.currentTimeMillis(), req.getRequestURI()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(CustomNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(CustomNotFoundException ex, HttpServletRequest req) {
        return new ResponseEntity<>(new ErrorResponse(
                ex.getMessage(), HttpStatus.NOT_FOUND, System.currentTimeMillis(), req.getRequestURI()),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(StatusChangeException.class)
    public ResponseEntity<ErrorResponse> handleStatusChangeException(StatusChangeException ex, HttpServletRequest req) {
        return new ResponseEntity<>(new ErrorResponse(
                ex.getMessage(), HttpStatus.BAD_REQUEST, System.currentTimeMillis(), req.getRequestURI()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CustomAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyExistException(CustomAlreadyExistException ex, HttpServletRequest req) {
        return new ResponseEntity<>(new ErrorResponse(
                ex.getMessage(), HttpStatus.CONFLICT, System.currentTimeMillis(), req.getRequestURI()),
                HttpStatus.CONFLICT);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentialsException(BadCredentialsException ex, HttpServletRequest req) {
        return new ResponseEntity<>(new ErrorResponse(
                ex.getMessage(), HttpStatus.UNAUTHORIZED, System.currentTimeMillis(), req.getRequestURI()),
                HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest req) {
        return new ResponseEntity<>(new ErrorResponse(
                ex.getMessage(), HttpStatus.FORBIDDEN, System.currentTimeMillis(), req.getRequestURI()),
                HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<Object> handleInsufficientAuthenticationException(InsufficientAuthenticationException ex, HttpServletRequest req) {
        return new ResponseEntity<>(new ErrorResponse(
                ex.getMessage(), HttpStatus.UNAUTHORIZED, System.currentTimeMillis(), req.getRequestURI()),
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<Object> handleJwtException(SignatureException ex, HttpServletRequest req) {
        return new ResponseEntity<>(new ErrorResponse(
                ex.getMessage(), HttpStatus.UNAUTHORIZED, System.currentTimeMillis(), req.getRequestURI()),
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<Object> handleTokenExpiredException(TokenExpiredException ex, HttpServletRequest req) {
        return new ResponseEntity<>(new ErrorResponse(
                ex.getMessage(), HttpStatus.UNAUTHORIZED, System.currentTimeMillis(), req.getRequestURI()),
                HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(CustomAccessDeniedException.class)
    public ResponseEntity<Object> handleCustomAccessDeniedException(CustomAccessDeniedException ex, HttpServletRequest req) {
        return new ResponseEntity<>(new ErrorResponse(
                ex.getMessage(), HttpStatus.FORBIDDEN, System.currentTimeMillis(), req.getRequestURI()),
                HttpStatus.FORBIDDEN
        );
    }
}
