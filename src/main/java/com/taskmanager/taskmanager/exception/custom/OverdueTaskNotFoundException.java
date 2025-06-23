package com.taskmanager.taskmanager.exception.custom;

public class OverdueTaskNotFoundException extends RuntimeException {
    public OverdueTaskNotFoundException(String message) {
        super(message);
    }
}
