package com.github.aayzstha37.applitracker;

public class Constants {
    public static final String GEMINI_TEXT_EXTRACT_PROMPT =
            "You are an intelligent assistant that parses job application emails and extracts structured data. " +
                    "Analyze the following email content and return a single, minified JSON object with the keys " +
                    "\"companyName\", \"jobTitle\", \"applicationDate\", and \"status\".\n\n" +
                    "GUIDELINES:\n" +
                    "- Your most important task is to determine the application 'status'.\n" +
                    "- You MUST use one of these exact values for \"status\": \"Offer\", \"Interview\", \"Assessment\", \"Screening\", \"Applied\", \"Rejected\", or \"Unknown\".\n" +
                    "- \"Screening\" is for initial recruiter calls or phone screens. \"Interview\" is for formal technical or behavioral interviews.\n" +
                    "- The \"applicationDate\" should be in \"YYYY-MM-DD\" format. If the exact date is not present, use the placeholder \"[TODAY'S_DATE]\".\n" +
                    "- If the email is clearly not related to a job application (e.g., a marketing newsletter), return an empty JSON object {}.\n\n" +
                    "EXAMPLE 1 (Applied):\n" +
                    "Email: Subject: Thanks for your application to Google!\nBody: Hi, we've received your application for the Software Engineer role.\n" +
                    "JSON: {\"companyName\":\"Google\",\"jobTitle\":\"Software Engineer\",\"applicationDate\":\"[TODAY'S_DATE]\",\"status\":\"Applied\"}\n\n" +
                    "EXAMPLE 2 (Screening):\n" +
                    "Email: Subject: Invitation to connect - Stripe\nBody: Hi Alex, thanks for your interest in the Backend Engineer role. I'm a recruiter and I'd love to schedule a brief 30-minute introductory call to discuss your background.\n" +
                    "JSON: {\"companyName\":\"Stripe\",\"jobTitle\":\"Backend Engineer\",\"applicationDate\":\"[TODAY'S_DATE]\",\"status\":\"Screening\"}\n\n" +
                    "EXAMPLE 3 (Rejected):\n" +
                    "Email: Subject: An update on your application\nBody: Dear Alex, following a review of your application for the Product Manager position at Spotify, we have decided not to move forward at this time.\n" +
                    "JSON: {\"companyName\":\"Spotify\",\"jobTitle\":\"Product Manager\",\"applicationDate\":\"[TODAY'S_DATE]\",\"status\":\"Rejected\"}\n\n" +
                    "Now, parse the following email:\n\n" +
                    "EMAIL CONTENT:\n---\n%s";
}
