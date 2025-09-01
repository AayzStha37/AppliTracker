package com.github.aayzstha37.applitracker.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.sheets.v4.Sheets;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.UserCredentials;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.io.StringReader;
import java.security.GeneralSecurityException;

@Configuration
public class GoogleApiConfig {

    private static final String APPLICATION_NAME = "AppliTracker Utility";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Value("${gcp.project.id}")
    private String gcpProjectId;

    /**
     * This bean is now the central point for fetching ALL secrets from Secret Manager.
     * It runs once at application startup.
     */
    @Bean
    public SecretsConfig secretsConfig() throws IOException {
        try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
            // Fetch Refresh Token
            SecretVersionName refreshTokenName = SecretVersionName.of(gcpProjectId, "job-tracker-access-token", "latest");
            AccessSecretVersionResponse refreshTokenResponse = client.accessSecretVersion(refreshTokenName);
            String refreshToken = refreshTokenResponse.getPayload().getData().toStringUtf8();

            // Fetch Sheet ID
            SecretVersionName sheetIdName = SecretVersionName.of(gcpProjectId, "job-tracker-sheet-id", "latest");
            AccessSecretVersionResponse sheetIdResponse = client.accessSecretVersion(sheetIdName);
            String sheetId = sheetIdResponse.getPayload().getData().toStringUtf8();

            // Fetch Client Secrets JSON
            SecretVersionName clientSecretsName = SecretVersionName.of(gcpProjectId, "job-tracker-client-secrets", "latest");
            AccessSecretVersionResponse clientSecretsResponse = client.accessSecretVersion(clientSecretsName);
            String clientSecretsJson = clientSecretsResponse.getPayload().getData().toStringUtf8();

            return new SecretsConfig(refreshToken, sheetId, clientSecretsJson);
        }
    }


    /**
     * Creates the central Google Credentials object using the user's refresh token.
     * This is the identity the app uses to act on the user's behalf.
     */
    @Bean("userGoogleCredentials")
    @Primary
    public GoogleCredentials userGoogleCredentials(SecretsConfig secretsConfig) throws IOException {
        String clientSecretsJsonContent = secretsConfig.getClientSecretsJson();
        String refreshToken = secretsConfig.getRefreshToken();

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new StringReader(clientSecretsJsonContent));
        GoogleClientSecrets.Details details = clientSecrets.getDetails();

        return UserCredentials.newBuilder()
                .setClientId(details.getClientId())
                .setClientSecret(details.getClientSecret())
                .setRefreshToken(refreshToken)
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
    @Qualifier("gmailApiClient")
    public Gmail gmailApiClient(GoogleCredentials credentials, HttpTransport httpTransport) {
        return new Gmail.Builder(httpTransport, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    /**
     * Creates an authenticated Google Sheets API client as a Spring Bean.
     */
    @Bean
    @Qualifier("sheetsApiClient")
    public Sheets sheetsApiClient(GoogleCredentials credentials, HttpTransport httpTransport) {
        return new Sheets.Builder(httpTransport, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

}