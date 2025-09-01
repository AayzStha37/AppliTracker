package com.github.aayzstha37.applitracker.model;

// Represents the inner JSON payload from the Gmail notification
public class MessageData {
    private String emailAddress;
    private long historyId;

    // Getters and Setters
    public String getEmailAddress() { return emailAddress; }
    public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }
    public long getHistoryId() { return historyId; }
    public void setHistoryId(long historyId) { this.historyId = historyId; }
}