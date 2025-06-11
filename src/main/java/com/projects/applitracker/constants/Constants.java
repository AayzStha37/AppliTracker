package com.projects.applitracker.constants;

public class Constants {
    public static final String NO_IMAGE = "No image file found";
    public static final String INVALID_PNG_FORMAT = "Inavlid image format. Needs to be of type - .png";
    public static final String GEMINI_TEXT_EXTRACT_PROMPT = "Extract the name of companies and their corresponding job roles from the image. Return the result strictly as JSON in the following format: { \"jobs\": [ { \"company\": \"Company Name\", \"role\": \"Role Title\" }, { \"company\": \"Company Name\", \"role\": \"Role Title\" } ] }. Do not return any explanation, markdown, or additional text—only valid JSON. Ensure that each object in the output array contains the exact keys: \"company\" and \"role\". Maintain the order as seen in the image. Do not include duplicates.";
}
