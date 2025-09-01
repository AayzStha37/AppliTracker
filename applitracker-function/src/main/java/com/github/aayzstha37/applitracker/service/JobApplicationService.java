package com.github.aayzstha37.applitracker.service;

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

    public void processJobApplicationEmail(long historyId) {
        try {
            String emailContent = gmailService.getNewEmailContent(historyId);
            if (emailContent == null || emailContent.isEmpty()) {
                System.out.println("No new message content found for historyId: " + historyId);
                return;
            }
            JobApplicationData parsedData = geminiService.parseEmailContent(emailContent);
            if (parsedData == null || parsedData.getCompanyName() == null) {
                System.out.println("Gemini did not return valid data. Skipping sheet update.");
                return;
            }
            System.out.println("Gemini Parsed Data: " + parsedData);
            sheetsService.updateSheet(parsedData);
        } catch (Exception e) {
            System.err.println("FATAL: Failed to process email for historyId " + historyId);
            e.printStackTrace();
            throw new RuntimeException(e); // Re-throw to signal failure to the Cloud Function
        }
    }
}