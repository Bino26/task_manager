package com.taskmanager.taskmanager.exception.custom;

public class CustomAccessDeniedException extends RuntimeException {
    public CustomAccessDeniedException(String s) {
        super(s);
    }
}
