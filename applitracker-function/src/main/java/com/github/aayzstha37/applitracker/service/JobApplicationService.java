package com.github.aayzstha37.applitracker.service;

import com.github.aayzstha37.applitracker.model.GmailResult;
import com.github.aayzstha37.applitracker.model.JobApplicationData;
import org.springframework.stereotype.Service;

@Service
public class JobApplicationService {
    private final GmailService gmailService;
    private final GeminiService geminiService;
    private final SheetsService sheetsService;

    public JobApplicationService(GmailService gmailService, GeminiService geminiService, SheetsService sheetsService) {
        this.gmailService = gmailService;
        this.geminiService = geminiService;
        this.sheetsService = sheetsService;
    }

    /**
     * The main business logic flow for processing a new job email notification.
     *
     * @param historyId The history ID from the Pub/Sub trigger.
     */
    public void processJobApplicationEmail(long historyId) {
        try {
            // 1. Fetch the latest email and its unique ID.
            GmailResult gmailResult = gmailService.getNewEmailContent(historyId);
            if (gmailResult.getMessageId() == null || gmailResult.getEmailContent().isEmpty()) {
                System.out.println("No new message content found. Processing finished.");
                return;
            }

            // 2. Use Gemini to parse the email content into structured data.
            JobApplicationData parsedData = geminiService.parseEmailContent(gmailResult.getEmailContent());
            if (parsedData == null || parsedData.getCompanyName() == null) {
                System.out.println("Gemini did not return valid data. Skipping sheet update.");
                return;
            }

            // 3. Pass both the parsed data and the unique message ID to the Sheets service for updating.
            sheetsService.updateSheet(parsedData, gmailResult.getMessageId());

        } catch (Exception e) {
            System.err.println("FATAL: An unhandled exception occurred during email processing for historyId " + historyId);
            e.printStackTrace();
            // Re-throw to signal a failure to the controller, which will cause Pub/Sub to redeliver.
            throw new RuntimeException(e);
        }
    }
}