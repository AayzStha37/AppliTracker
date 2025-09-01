package com.github.aayzstha37.applitracker.model;

import java.util.Arrays;

// This enum defines the possible states of an application and their priority.
// This is crucial for the state management logic.
public enum ApplicationStatus {
    OFFER(5),
    INTERVIEW(4),
    ASSESSMENT(3),
    SCREENING(2),
    APPLIED(1),
    REJECTED(0), // Rejected is a terminal state, handled as a special case in SheetsService
    UNKNOWN(-1); // A terminal state

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