package com.taskmanager.taskmanager.enums;

import lombok.Getter;

@Getter
public enum PriorityLevel {
    CRITICAL("CRITIQUE"),
    HIGH("HAUT"),
    MEDIUM("MOYEN"),
    LOW("BAS");

    private final String displayName;

    PriorityLevel(String displayName) {
        this.displayName = displayName;
    }
}
