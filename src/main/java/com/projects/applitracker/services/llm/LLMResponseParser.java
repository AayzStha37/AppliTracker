package com.projects.applitracker.services.llm;

public interface LLMResponseParser {

    public <T> T parseResponse(String jsonResponse, Class<T> targetClass);
}
