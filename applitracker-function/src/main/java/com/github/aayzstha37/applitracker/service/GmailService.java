package com.github.aayzstha37.applitracker.service;

import com.github.aayzstha37.applitracker.model.GmailResult;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class GmailService {

    private final Gmail gmail;
    private static final String LABEL_NAME = "JobApplications";

    public GmailService(@Qualifier("gmailApiClient") Gmail gmail) {
        this.gmail = gmail;
    }

    /**
     * Fetches the content of the most recent email with the "JobApplications" label.
     * This method is robust against notification bursts by directly querying for the current state
     * rather than relying on a potentially noisy history ID.
     *
     * @param historyId The history ID from the Pub/Sub trigger (used to wake up the function, but not in the logic).
     * @return A GmailResult object containing the unique message ID and the email's content.
     */
    public GmailResult getNewEmailContent(long historyId) throws IOException {
        String userId = "me";

        String labelId = findLabelIdByName(LABEL_NAME);
        if (labelId == null) {
            System.err.println("Could not find the label ID for: " + LABEL_NAME);
            return new GmailResult(null, "");
        }

        // Directly ask Gmail for the single most recent message with our label.
        // This is the most reliable way to get the email we care about.
        ListMessagesResponse listResponse = gmail.users().messages().list(userId)
                .setLabelIds(Collections.singletonList(labelId))
                .setMaxResults(1L)
                .execute();

        List<Message> messages = listResponse.getMessages();
        if (messages == null || messages.isEmpty()) {
            System.out.println("Query for most recent message with label '" + LABEL_NAME + "' returned no results.");
            return new GmailResult(null, "");
        }

        // We have the ID, now get the full content.
        String messageId = messages.get(0).getId();
        Message message = gmail.users().messages().get(userId, messageId).setFormat("full").execute();
        MessagePart payload = message.getPayload();

        String subject = getHeader(payload.getHeaders(), "Subject");
        String body = getTextFromMessagePart(payload);
        String emailContent = "Subject: " + subject + "\n\nBody:\n" + body;

        // Return the message ID and its content together.
        return new GmailResult(messageId, emailContent);
    }

    // Helper method to find a label's internal ID by its human-readable name.
    private String findLabelIdByName(String labelName) throws IOException {
        return gmail.users().labels().list("me").execute().getLabels().stream()
                .filter(label -> label.getName().equalsIgnoreCase(labelName))
                .map(Label::getId)
                .findFirst().orElse(null);
    }

    // Helper method to extract a specific header (like "Subject") from an email.
    private String getHeader(List<MessagePartHeader> headers, String name) {
        return headers.stream()
                .filter(h -> h.getName().equalsIgnoreCase(name))
                .map(MessagePartHeader::getValue)
                .findFirst().orElse("");
    }

    // Helper method to recursively parse an email's MIME parts to find the plain text body.
    private String getTextFromMessagePart(MessagePart messagePart) {
        if ("text/plain".equals(messagePart.getMimeType()) && messagePart.getBody() != null && messagePart.getBody().getData() != null) {
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