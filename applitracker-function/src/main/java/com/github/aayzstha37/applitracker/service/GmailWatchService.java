package com.github.aayzstha37.applitracker.service;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.WatchRequest;
import com.google.api.services.gmail.model.WatchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;

@Service
public class GmailWatchService {
    private final Gmail gmail;

    @Value("${gcp.project.id}")
    private String gcpProjectId;

    private static final String LABEL_NAME_TO_WATCH = "JobApplications";
    private static final String PUB_SUB_TOPIC_ID = "job-updates";
    private static final String USER_ID = "saayush97";

    public GmailWatchService(Gmail gmail) { this.gmail = gmail; }

    public void renewGmailWatch() throws IOException {
        System.out.println("Attempting to renew Gmail watch subscription...");
        String labelId = findLabelIdByName(LABEL_NAME_TO_WATCH);
        if (labelId == null) {
            throw new RuntimeException("CRITICAL FAILURE: Label 'JobApplications' not found.");
        }
        String fullTopicName = String.format("projects/%s/topics/%s", gcpProjectId, PUB_SUB_TOPIC_ID);
        WatchRequest watchRequest = new WatchRequest()
                .setLabelIds(Collections.singletonList(labelId))
                .setLabelFilterAction("include")
                .setTopicName(fullTopicName);
        WatchResponse watchResponse = gmail.users().watch(USER_ID, watchRequest).execute();
        System.out.println("SUCCESS! Gmail watch has been renewed. New Expiration: " + new Date(watchResponse.getExpiration()));
    }

    private String findLabelIdByName(String labelName) throws IOException {
        return gmail.users().labels().list(USER_ID).execute().getLabels().stream()
                .filter(label -> label.getName().equalsIgnoreCase(labelName))
                .map(Label::getId)
                .findFirst().orElse(null);
    }
}