package com.github.aayzstha37.applitracker.service;

import com.github.aayzstha37.applitracker.Constants;
import com.github.aayzstha37.applitracker.model.JobApplicationData;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.*;
import org.springframework.beans.factory.annotation.Qualifier;
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

    @Value("${gcp.project.id}")
    private String gcpProjectId;

    private static final String API_LOCATION = "global";
    private static final String GEMINI_MODEL = "gemini-2.0-flash-001";;

    public GeminiService(HttpTransport httpTransport, @Qualifier("userGoogleCredentials") GoogleCredentials credentials) {
        this.httpTransport = httpTransport;
        this.credentials = credentials;
    }

    public JobApplicationData parseEmailContent(String emailContent) throws IOException {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String finalPrompt = String.format(Constants.GEMINI_TEXT_EXTRACT_PROMPT, emailContent).replace("[TODAY'S_DATE]", today);

        String endpointUrl = String.format("https://aiplatform.googleapis.com/v1/projects/%s/locations/%s/publishers/google/models/%s:generateContent",
                gcpProjectId, API_LOCATION, GEMINI_MODEL);

        String requestBody = buildGeminiRequestBody(finalPrompt);;

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
            //TODO convert this to POJO or separate function
            JsonObject jsonResponse = JsonParser.parseString(rawResponse).getAsJsonObject();
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

    /* Builds the JSON payload for the Gemini API request in a structured and safe way.
     * @param prompt The full text prompt to send to the model.
     * @return A JSON string representing the request body.
     */
    private String buildGeminiRequestBody(String prompt) {
        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", prompt);

        JsonArray partsArray = new JsonArray();
        partsArray.add(textPart);

        JsonObject content = new JsonObject();
        content.addProperty("role", "user");
        content.add("parts", partsArray);

        JsonArray contentsArray = new JsonArray();
        contentsArray.add(content);

        JsonObject requestBodyJson = new JsonObject();
        requestBodyJson.add("contents", contentsArray);

        System.out.println("Gemini Request Body: " + requestBodyJson.toString());
        return gson.toJson(requestBodyJson);
    }
}