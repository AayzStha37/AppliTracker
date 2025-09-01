package com.github.aayzstha37.setup;

import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Label;
import com.google.api.services.gmail.model.ListLabelsResponse;
import com.google.api.services.gmail.model.WatchRequest;
import com.google.api.services.gmail.model.WatchResponse;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.UserCredentials;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class StartGmailWatch {

    private static final String APPLICATION_NAME = "Gmail Watch Setup";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String USER_ID = "me";

    // --- CONFIGURE THESE VALUES ---
    private static final String LABEL_NAME_TO_WATCH = "JobApplications";
    private static final String GCP_PROJECT_ID = "appli-tracker-470223";
    private static final String PUB_SUB_TOPIC_ID = "job-updates";
    // ----------------------------

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        // Load client secrets from the classpath (resources folder)
        InputStream in = StartGmailWatch.class.getResourceAsStream("/client_secrets.json");
        if (in == null) {
            throw new RuntimeException("Cannot find client_secrets.json in classpath. Make sure it's in `setup-utils/src/main/resources`");
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // 1. Get the User's Refresh Token
        System.out.println("Please paste your user refresh token (from Postman) and press Enter:");
        Scanner scanner = new Scanner(System.in);
        String refreshToken = scanner.nextLine().trim();
        if (refreshToken.isEmpty()) {
            System.err.println("Refresh token cannot be empty.");
            return;
        }

        // 2. Build credentials
        UserCredentials credentials = UserCredentials.newBuilder()
                .setClientId(clientSecrets.getDetails().getClientId())
                .setClientSecret(clientSecrets.getDetails().getClientSecret())
                .setRefreshToken(refreshToken)
                .build();

        // 3. Build the Gmail API client
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Gmail gmail = new Gmail.Builder(httpTransport, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();

        // 4. Find the ID of the label we want to watch
        String labelId = findLabelIdByName(gmail, LABEL_NAME_TO_WATCH);
        if (labelId == null) {
            System.err.printf("Error: Label '%s' not found in your Gmail account. Please create it in Gmail first.\n", LABEL_NAME_TO_WATCH);
            return;
        }
        System.out.printf("Found Label '%s' with ID: %s\n", LABEL_NAME_TO_WATCH, labelId);

        // 5. Create the Watch request
        String fullTopicName = String.format("projects/%s/topics/%s", GCP_PROJECT_ID, PUB_SUB_TOPIC_ID);
        WatchRequest watchRequest = new WatchRequest()
                .setLabelIds(Collections.singletonList(labelId))
                .setLabelFilterAction("include")
                .setTopicName(fullTopicName);

        System.out.println("Attempting to set up watch on topic: " + fullTopicName);

        // 6. Execute the Watch request
        WatchResponse watchResponse = gmail.users().watch(USER_ID, watchRequest).execute();

        System.out.println("\n============================================================");
        System.out.println("SUCCESS! Gmail watch has been configured.");
        System.out.println("History ID: " + watchResponse.getHistoryId());
        System.out.println("Expiration: " + new Date(watchResponse.getExpiration()));
        System.out.println("Your automated renewal function will handle this from now on.");
        System.out.println("============================================================");
    }

    private static String findLabelIdByName(Gmail gmail, String labelName) throws IOException {
        return gmail.users().labels().list(USER_ID).execute().getLabels().stream()
                .filter(label -> label.getName().equalsIgnoreCase(labelName))
                .map(Label::getId)
                .findFirst()
                .orElse(null);
    }
}
