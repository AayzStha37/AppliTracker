package com.github.aayzstha37.applitracker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PubSubMessage {

    private String data;
    private String messageId;

    public PubSubMessage() {
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    @Override
    public String toString() {
        // We don't log the 'data' field here because it's a long Base64 string
        return "PubSubMessage{" +
                "messageId='" + messageId + '\'' +
                '}';
    }
}
