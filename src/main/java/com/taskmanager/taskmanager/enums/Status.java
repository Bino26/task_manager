package com.taskmanager.taskmanager.enums;

import lombok.Getter;

@Getter
public enum Status {
    TO_DO("A_FAIRE"),
    IN_PROGRESS("EN_COURS"),
    ON_HOLD("REVISION"),
    COMPLETED("TERMINE");

    private final String displayName;

    Status(String displayName) {
        this.displayName = displayName;
    }
}
