package com.github.aayzstha37.applitracker.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.sheets.v4.Sheets;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.UserCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;
import java.io.StringReader;
import java.security.GeneralSecurityException;

@Configuration
public class GoogleApiConfig {

    private static final String APPLICATION_NAME = "AppliTracker Utility";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Value("${user.refresh.token}")
    private String userRefreshToken;

    @Value("${google.oauth.client-secrets-json}")
    private String clientSecretsJsonContent;

    /**
     * Creates the central Google Credentials object using the user's refresh token.
     * This is the identity the app uses to act on the user's behalf.
     */
    @Bean
    public GoogleCredentials googleCredentials() throws IOException {
        if (clientSecretsJsonContent == null || clientSecretsJsonContent.isEmpty()) {
            throw new RuntimeException("Client secrets JSON content not loaded from Secret Manager.");
        }

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new StringReader(clientSecretsJsonContent));
        GoogleClientSecrets.Details details = clientSecrets.getDetails();

        return UserCredentials.newBuilder()
                .setClientId(details.getClientId())
                .setClientSecret(details.getClientSecret())
                .setRefreshToken(userRefreshToken)
                .build();
    }

    /**
     * Creates an HTTPTransport Bean.
     */
    @Bean
    public HttpTransport httpTransport() throws GeneralSecurityException, IOException {
        return GoogleNetHttpTransport.newTrustedTransport();
    }

    /**
     * Creates an authenticated Gmail API client as a Spring Bean.
     */
    @Bean
    public Gmail gmailService(GoogleCredentials credentials, HttpTransport httpTransport) {
        return new Gmail.Builder(httpTransport, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Creates an authenticated Google Sheets API client as a Spring Bean.
     */
    @Bean
    public Sheets sheetsService(GoogleCredentials credentials, HttpTransport httpTransport) {
        return new Sheets.Builder(httpTransport, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

}