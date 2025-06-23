package com.taskmanager.taskmanager.entity.enums;

import lombok.Getter;

@Getter
public enum ProjectStatus {
    IN_PROGRESS("In Progress"),
    ON_HOLD("On Hold"),
    CANCELLED("Cancelled"),
    COMPLETED("Completed"),
    ARCHIVED("Archived");

    private final String displayName;

    ProjectStatus(String displayName) {
        this.displayName = displayName;
    }
}
