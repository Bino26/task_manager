package com.taskmanager.taskmanager.entity.enums;

import lombok.Getter;

@Getter
public enum PriorityLevel {
    URGENT("Urgent"),
    CRITICAL("Critical"),
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low"),
    LOWEST("Lowest");

    private final String displayName;

    PriorityLevel(String displayName) {
        this.displayName = displayName;
    }
}
