package com.taskmanager.taskmanager.enums;

import lombok.Getter;

@Getter
public enum UserStatus {
    ACTIF("Actif"),
    DELETED("Deleted"),
    BLOCKED("Blocked"),
    COMPLETED("Completed");

    private final String displayName;

    UserStatus(String displayName) {
        this.displayName = displayName;
    }
}
