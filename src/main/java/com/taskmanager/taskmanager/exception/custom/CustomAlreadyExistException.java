package com.taskmanager.taskmanager.exception.custom;

public class CustomAlreadyExistException extends RuntimeException {
    public CustomAlreadyExistException(String s) {
        super(s);
    }
}
