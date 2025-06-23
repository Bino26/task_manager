package com.taskmanager.taskmanager.entity.enums;

import lombok.Getter;

@Getter
public enum IssueStatus {
    BACKLOG("Backlog"),
    IN_ANALYSIS("In Analysis"),
    IN_PROGRESS("In Progress"),
    CANCELLED("Cancelled"),
    BLOCKED("Blocked"),
    COMPLETED("Completed");

    private final String displayName;

    IssueStatus(String displayName) {
        this.displayName = displayName;
    }
}
