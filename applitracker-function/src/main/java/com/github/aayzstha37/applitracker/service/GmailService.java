package com.github.aayzstha37.applitracker.service;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class GmailService {

    private final Gmail gmail;

    public GmailService(Gmail gmail) {
        this.gmail = gmail;
    }

    public String getNewEmailContent(long historyId) throws IOException {
        String userId = "me";
        ListHistoryResponse historyResponse = gmail.users().history().list(userId).setStartHistoryId(BigInteger.valueOf(historyId)).execute();
        List<History> historyList = historyResponse.getHistory();

        if (historyList == null || historyList.isEmpty()) return "";

        Optional<String> messageIdOpt = historyList.stream()
                .filter(history -> history.getMessagesAdded() != null)
                .flatMap(history -> history.getMessagesAdded().stream())
                .map(HistoryMessageAdded::getMessage)
                .map(Message::getId)
                .findFirst();

        if (messageIdOpt.isEmpty()) return "";

        String messageId = messageIdOpt.get();
        Message message = gmail.users().messages().get(userId, messageId).setFormat("full").execute();
        MessagePart payload = message.getPayload();
        String subject = getHeader(payload.getHeaders(), "Subject");
        String body = getTextFromMessagePart(payload);
        return "Subject: " + subject + "\n\nBody:\n" + body;
    }

    private String getHeader(List<MessagePartHeader> headers, String name) {
        return headers.stream()
                .filter(h -> h.getName().equalsIgnoreCase(name))
                .map(MessagePartHeader::getValue)
                .findFirst().orElse("");
    }

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