package com.github.aayzstha37.applitracker.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// This annotation tells the JSON parser to ignore any extra fields it might receive
// from Pub/Sub in the future, which makes your application more resilient to changes.
@JsonIgnoreProperties(ignoreUnknown = true)
public class PubSubPushPayload {

    private PubSubMessage message;

    public PubSubPushPayload() {
    }

    public PubSubMessage getMessage() {
        return message;
    }

    public void setMessage(PubSubMessage message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "PubSubPushPayload{" +
                "message=" + message +
                '}';
    }
}