package com.taskmanager.taskmanager.exception.custom;

public class TokenExpiredException extends RuntimeException {
    public TokenExpiredException(String s) {
        super(s);
    }
}
