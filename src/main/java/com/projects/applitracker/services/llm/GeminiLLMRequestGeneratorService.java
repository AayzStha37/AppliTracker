package com.projects.applitracker.services.llm;

import com.projects.applitracker.constants.Constants;
import com.projects.applitracker.dto.GeminiImagePostRequest;
import com.projects.applitracker.dto.GeminiImagePostRequest.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class GeminiLLMRequestGeneratorService implements LLMRequestGenerator {

    private final String baseURL;
    private final String geminiModel;
    private final String geminiAPIKey;


    @Autowired
    public GeminiLLMRequestGeneratorService(@Value("${gemini.base.url}") String baseURL, @Value("${gemini.current.model}") String geminiModel, @Value("${gemini.api.key}") String geminiAPIKey) {
        this.baseURL = baseURL;
        this.geminiModel = geminiModel;
        this.geminiAPIKey = geminiAPIKey;
    }

    @Override
    public String sendTextExtractionRequest(String bs64EncodedImageText) {
        GeminiImagePostRequest imagePostRequest = prepareRequest(bs64EncodedImageText);

        return sendExtractionRequest(imagePostRequest);
    }

    private GeminiImagePostRequest prepareRequest(String bs64EncodedImageText) {
        // Build DTO structure
        InlineData inlineData = new InlineData("image/png",bs64EncodedImageText);

        Part imagePart = new Part(inlineData);
        Part promptPart = new Part(Constants.GEMINI_TEXT_EXTRACT_PROMPT);

        Content content = new Content(List.of(imagePart, promptPart));

        return new GeminiImagePostRequest(List.of(content));
    }

    private String sendExtractionRequest(GeminiImagePostRequest imagePostRequest) {
        //prepare header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        //prepare url
        String url = String.format("%s%s:generateContent?key=%s", baseURL, geminiModel, geminiAPIKey);

        //prepare entity
        HttpEntity<GeminiImagePostRequest> entity = new HttpEntity<>(imagePostRequest, headers);

        //send entity
        try {
            ResponseEntity<String> responseEntity = new RestTemplate().postForEntity(url, entity, String.class);
            return responseEntity.getBody();
        }catch (HttpStatusCodeException e) {
            throw new RuntimeException("API Exception: "+e.getResponseBodyAsString());
        }
    }
}
