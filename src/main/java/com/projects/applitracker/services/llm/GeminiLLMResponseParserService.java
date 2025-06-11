package com.projects.applitracker.services.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class GeminiLLMResponseParserService implements LLMResponseParser {

    ObjectMapper mapper = new ObjectMapper();

    @Override
    public <T> T parseResponse(String jsonResponse, Class<T> targetClass) {

        // Step 1: Parse outer JSON to find the text field
        JsonNode root = null;
        try {
            root = mapper.readTree(jsonResponse);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error reading response from Gemini: "+e);
        }

        String rawText = root.at("/candidates/0/content/parts/0/text").asText();

        // Step 2: Remove ```json and ``` to get the clean JSON string
        String cleanJson = rawText.replaceAll("(?s)```json\\s*", "").replaceAll("```\\s*", "");

        // Deserialize to single POJO with inner class
        try {
            return mapper.readValue(cleanJson, targetClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error mapping response from Gemini: "+e);
        }
    }
}
