package com.taskmanager.taskmanager.entity.enums;

import lombok.Getter;

@Getter
public enum IssueType {
    TASK("Task"),
    BUG("Bug"),
    STORY("Story"),
    FEATURE("Feature"),
    EPIC("Epic");

    private final String displayName;

    IssueType(String displayName) {
        this.displayName = displayName;
    }
}
