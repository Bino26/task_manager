package com.taskmanager.taskmanager.entity.enums;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

@Getter
public enum Role implements GrantedAuthority {
    PROJECT_MANAGER("Project Manager"),
    TEAM_LEADER("Team Leader"),
    TEAM_MEMBER("Team Member");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getAuthority() {
        return name();
    }
}
