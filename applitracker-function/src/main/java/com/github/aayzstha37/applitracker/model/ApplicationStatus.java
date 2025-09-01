package com.github.aayzstha37.applitracker.model;

import java.util.Arrays;

// This enum defines the possible states of an application and their priority.
// This is crucial for the state management logic.
public enum ApplicationStatus {
    OFFER(5),
    INTERVIEW(4),
    ASSESSMENT(3),
    APPLIED(2),
    UPDATE(1),
    UNKNOWN(0),
    REJECTED(-1); // A terminal state

    public final int priority;

    ApplicationStatus(int priority) {
        this.priority = priority;
    }

    public static ApplicationStatus fromString(String text) {
        return Arrays.stream(ApplicationStatus.values())
                .filter(status -> status.name().equalsIgnoreCase(text))
                .findFirst()
                .orElse(UNKNOWN);
    }
}