package com.github.aayzstha37.applitracker;

public class Constants {
    public static final String GEMINI_TEXT_EXTRACT_PROMPT =
            "You are a highly intelligent assistant specializing in parsing job application emails to extract structured data. " +
                    "Analyze the following email content and return a single, minified JSON object with the keys " +
                    "\"companyName\", \"jobTitle\", \"applicationDate\", and \"status\".\n\n" +

                    "--- IMPORTANT GUIDELINES ---\n" +
                    "1.  **Primary Goal:** Your only goal is to track the status of applications that have **already been submitted**. " +
                    "If an email is a job alert, a newsletter, a promotional message, or a reminder to apply to a saved job, it is IRRELEVANT. " +
                    "For all irrelevant emails, you MUST return an empty JSON object: {}.\n\n" +

                    "2.  **Company Name (`companyName`):** This is the most critical field. You MUST identify the **actual hiring company**, NOT the job board. " +
                    "PRIORITIZE the company name mentioned in the email subject or body. IGNORE job boards like Indeed, LinkedIn, ZipRecruiter, etc., as the company name.\n\n" +

                    "3.  **Application Status (`status`):** You MUST use one of these exact string values: \"Offer\", \"Interview\", \"Assessment\", \"Screening\", \"Applied\", \"Rejected\", or \"Unknown\".\n" +
                    "   - \"Screening\" is for initial recruiter calls or phone screens.\n" +
                    "   - \"Interview\" is for formal technical or behavioral interviews.\n\n" +

                    "4.  **Application Date (`applicationDate`):** Format as \"YYYY-MM-DD\". If no date is found, use the placeholder \"[TODAY'S_DATE]\".\n\n" +

                    "--- EXAMPLES ---\n\n" +
                    "**EXAMPLE 1 (Standard Application):**\n" +
                    "Email: Subject: Thanks for your application to Google!\nBody: Hi, we've received your application for the Software Engineer role.\n" +
                    "JSON: {\"companyName\":\"Google\",\"jobTitle\":\"Software Engineer\",\"applicationDate\":\"[TODAY'S_DATE]\",\"status\":\"Applied\"}\n\n" +

                    "**EXAMPLE 2 (Screening Invite):**\n" +
                    "Email: Subject: Invitation to connect - Stripe\nBody: Hi Alex, thanks for your interest in the Backend Engineer role. I'm a recruiter and would love to schedule an introductory call.\n" +
                    "JSON: {\"companyName\":\"Stripe\",\"jobTitle\":\"Backend Engineer\",\"applicationDate\":\"[TODAY'S_DATE]\",\"status\":\"Screening\"}\n\n" +

                    "**EXAMPLE 3 (Rejection):**\n" +
                    "Email: Subject: An update on your application\nBody: Dear Alex, we have decided not to move forward with your application for the Product Manager position at Spotify.\n" +
                    "JSON: {\"companyName\":\"Spotify\",\"jobTitle\":\"Product Manager\",\"applicationDate\":\"[TODAY'S_DATE]\",\"status\":\"Rejected\"}\n\n" +

                    "**EXAMPLE 4 (Job Board - The Tricky Case):**\n" +
                    "Email: From: \"Indeed <noreply@indeed.com>\"\nSubject: Your application for Senior Developer at Microsoft was sent.\n" +
                    "JSON: {\"companyName\":\"Microsoft\",\"jobTitle\":\"Senior Developer\",\"applicationDate\":\"[TODAY'S_DATE]\",\"status\":\"Applied\"}\n\n" +

                    "**EXAMPLE 5 (Job Alert - TO BE IGNORED):**\n" +
                    "Email: From: \"LinkedIn Job Alerts <jobalerts-noreply@linkedin.com>\"\nSubject: 25+ new Software Engineer jobs in Toronto, ON\n" +
                    "JSON: {}\n\n" +

                    "**EXAMPLE 6 (Saved Job Reminder - TO BE IGNORED):**\n" +
                    "Email: Subject: Don't miss out on your saved job at Netflix\nBody: You saved a job for a UI/UX Designer at Netflix. Apply now before it closes!\n" +
                    "JSON: {}\n\n" +

                    "--- TASK ---\n" +
                    "Now, parse the following email:\n\n" +
                    "EMAIL CONTENT:\n%s";
}
