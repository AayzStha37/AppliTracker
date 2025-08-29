package com.github.aayzstha37.applitracker.service;

import com.github.aayzstha37.applitracker.Constants;
import com.github.aayzstha37.applitracker.model.JobApplicationData;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class GeminiService {
    private final HttpTransport httpTransport;
    private final GoogleCredentials credentials;
    private final Gson gson = new Gson();

    // Injecting values from application.properties
    @Value("${gcp.project.id}")
    private String gcpProjectId;

    @Value("${gcp.location}")
    private String gcpLocation;

    @Value("${gemini.llm.model}")
    private String geminiModel;

    public GeminiService(HttpTransport httpTransport, GoogleCredentials credentials) {
        this.httpTransport = httpTransport;
        this.credentials = credentials;
    }

    public JobApplicationData parseEmailContent(String emailContent) throws IOException {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String finalPrompt = String.format(Constants.GEMINI_TEXT_EXTRACT_PROMPT, emailContent).replace("[TODAY'S_DATE]", today);

        // The endpoint URL is now dynamically built using the injected properties
        String endpointUrl = String.format("https://%s-aiplatform.googleapis.com/v1/projects/%s/locations/%s/publishers/google/models/%s:generateContent",
                gcpLocation, gcpProjectId, gcpLocation, geminiModel);

        String requestBody = String.format("{\"contents\":[{\"parts\":[{\"text\":\"%s\"}]}]}", gson.toJson(finalPrompt));

        credentials.refreshIfExpired();
        String accessToken = credentials.getAccessToken().getTokenValue();

        HttpRequest request = httpTransport.createRequestFactory()
                .buildPostRequest(new GenericUrl(endpointUrl),
                        ByteArrayContent.fromString("application/json", requestBody));

        request.getHeaders().setAuthorization("Bearer " + accessToken);
        request.getHeaders().setContentType("application/json; charset=utf-8");

        String rawResponse = request.execute().parseAsString();
        System.out.println("Gemini Raw REST Response: " + rawResponse);

        try {
            JsonObject jsonResponse = JsonParser.parseString(rawResponse).getAsJsonObject();
            // This parsing logic is specific to the Gemini REST API response structure
            String geminiOutput = jsonResponse.getAsJsonArray("candidates")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts")
                    .get(0).getAsJsonObject()
                    .get("text").getAsString();

            String cleanedJson = geminiOutput.replace("```json", "").replace("```", "").trim();
            return gson.fromJson(cleanedJson, JobApplicationData.class);

        } catch (JsonSyntaxException | NullPointerException e) {
            System.err.println("Failed to parse JSON from Gemini REST response: " + rawResponse);
            e.printStackTrace();
            return null;
        }
    }
}