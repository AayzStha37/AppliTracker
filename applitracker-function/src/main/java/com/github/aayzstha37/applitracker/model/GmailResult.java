package com.github.aayzstha37.applitracker.model;

/**
 * A simple Data Transfer Object (DTO) to hold the results from a Gmail API call.
 * This allows us to pass both the unique ID of an email and its content together
 * in a clean, structured way.
 */
public class GmailResult {

    private final String messageId;
    private final String emailContent;

    public GmailResult(String messageId, String emailContent) {
        this.messageId = messageId;
        this.emailContent = emailContent;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getEmailContent() {
        return emailContent;
    }
}