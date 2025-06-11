package com.projects.applitracker.services.llm;

import com.projects.applitracker.constants.Constants;
import com.projects.applitracker.dto.GeminiImagePostRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Service
public class GeminiLLMService{

    private final GeminiLLMProperties geminiLLMProperties;

    @Autowired
    public GeminiLLMService(GeminiLLMProperties geminiLLMProperties) {
        this.geminiLLMProperties = geminiLLMProperties;
    }

    private String buildApiUrl(){
        return UriComponentsBuilder
                .fromHttpUrl(geminiLLMProperties.getBaseUrl())
                .path(geminiLLMProperties.getCurrentModel() + ":generateContent")
                .queryParam("key", geminiLLMProperties.getApiKey())
                .toUriString();
    }

    private GeminiImagePostRequest generateLLMRequest(String bs64EncodedImageText) {
        // Build DTO structure
        GeminiImagePostRequest.InlineData inlineData = new GeminiImagePostRequest.InlineData("image/png",bs64EncodedImageText);

        GeminiImagePostRequest.Part imagePart = new GeminiImagePostRequest.Part(inlineData);
        GeminiImagePostRequest.Part promptPart = new GeminiImagePostRequest.Part(Constants.GEMINI_TEXT_EXTRACT_PROMPT);

        GeminiImagePostRequest.Content content = new GeminiImagePostRequest.Content(List.of(imagePart, promptPart));

        return new GeminiImagePostRequest(List.of(content));
    }

    public String sendRequest(String bs64EncodedImageText) {

        //prepare header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        //prepare entity
        HttpEntity<GeminiImagePostRequest> entity = new HttpEntity<>(generateLLMRequest(bs64EncodedImageText), headers);

        //send entity
        try {
            ResponseEntity<String> responseEntity = new RestTemplate().postForEntity(buildApiUrl(), entity, String.class);
            return responseEntity.getBody();
        }catch (HttpStatusCodeException e) {
            throw new RuntimeException("API Exception: "+e.getResponseBodyAsString());
        }
    }

}
