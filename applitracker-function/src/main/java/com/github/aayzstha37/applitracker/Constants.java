package com.github.aayzstha37.applitracker;

public class Constants {
    public static final String GEMINI_TEXT_EXTRACT_PROMPT =
            "You are an intelligent assistant that parses job application emails and extracts structured data. " +
                    "Analyze the following email content and return a single, minified JSON object with the keys " +
                    "\"companyName\", \"jobTitle\", \"applicationDate\", and \"status\".\n\n" +
                    "GUIDELINES:\n" +
                    "- Possible values for \"status\" are: \"Applied\", \"Rejected\", \"Interview\", \"Assessment\", \"Offer\", \"Update\", or \"Unknown\".\n" +
                    "- The \"applicationDate\" should be in \"YYYY-MM-DD\" format. If the exact date is not present, use the placeholder \"[TODAY'S_DATE]\".\n" +
                    "- If the email is clearly not related to a job application (e.g., a marketing newsletter), return an empty JSON object {}.\n" +
                    "EMAIL CONTENT:\n---\n%s";
}
