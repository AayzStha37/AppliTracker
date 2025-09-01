package com.github.aayzstha37.applitracker.service;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;
import org.springframework.stereotype.Service;
import com.google.api.services.gmail.model.Label;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class GmailService {
    private final Gmail gmail;
    private static final String LABEL_NAME = "JobApplications";

    public GmailService(Gmail gmailApiClient) {
        this.gmail = gmailApiClient;
    }

    public String getNewEmailContent(long historyId) throws IOException {
        String userId = "me";

        // Find the ID of our "JobApplications" label.
        String labelId = findLabelIdByName(LABEL_NAME);
        if (labelId == null) {
            System.err.println("Could not find the label ID for: " + LABEL_NAME);
            return "";
        }

        ListMessagesResponse listResponse = gmail.users().messages().list(userId)
                .setLabelIds(Collections.singletonList(labelId))
                .setMaxResults(1L) // We only care about the single most recent email.
                .execute();

        List<Message> messages = listResponse.getMessages();
        if (messages == null || messages.isEmpty()) {
            System.out.println("Query for most recent message with label '" + LABEL_NAME + "' returned no results.");
            return "";
        }

        // Get the full message content for the most recent message.
        String messageId = messages.get(0).getId();
        Message message = gmail.users().messages().get(userId, messageId).setFormat("full").execute();
        MessagePart payload = message.getPayload();

        // The rest of the parsing logic is the same.
        String subject = getHeader(payload.getHeaders(), "Subject");
        String body = getTextFromMessagePart(payload);

        return "Subject: " + subject + "\n\nBody:\n" + body;
    }

    private String findLabelIdByName(String labelName) throws IOException {
        return gmail.users().labels().list("me").execute().getLabels().stream()
                .filter(label -> label.getName().equalsIgnoreCase(labelName))
                .map(Label::getId)
                .findFirst().orElse(null);
    }

    private String getHeader(List<MessagePartHeader> headers, String name) {
        return headers.stream()
                .filter(h -> h.getName().equalsIgnoreCase(name))
                .map(MessagePartHeader::getValue)
                .findFirst().orElse("");
    }

    private String getTextFromMessagePart(MessagePart messagePart) {
        if ("text/plain".equals(messagePart.getMimeType())
                && messagePart.getBody() != null
                && messagePart.getBody().getData() != null) {
            byte[] data = Base64.getUrlDecoder().decode(messagePart.getBody().getData());
            return new String(data, StandardCharsets.UTF_8);
        }
        if (messagePart.getParts() != null) {
            for (MessagePart part : messagePart.getParts()) {
                String text = getTextFromMessagePart(part);
                if (!text.isEmpty()) return text;
            }
        }
        return "";
    }
}
