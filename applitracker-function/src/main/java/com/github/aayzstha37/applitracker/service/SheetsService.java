package com.github.aayzstha37.applitracker.service;

import com.github.aayzstha37.applitracker.config.SecretsConfig;
import com.github.aayzstha37.applitracker.model.ApplicationStatus;
import com.github.aayzstha37.applitracker.model.JobApplicationData;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.IntStream;

@Service
public class SheetsService {
    private final Sheets sheets;
    private final String spreadsheetId;
    private static final String SHEET_NAME = "Sheet1";

    public SheetsService(@Qualifier("sheetsApiClient") Sheets sheets, SecretsConfig secretsConfig) {
        this.sheets = sheets;
        this.spreadsheetId = secretsConfig.getSheetId();
    }

    /**
     * Updates or inserts an application record into the Google Sheet.
     * Includes deduplication to prevent processing the same email multiple times.
     *
     * @param data The parsed job application data from Gemini.
     * @param messageId The unique ID of the Gmail message being processed.
     */
    public void updateSheet(JobApplicationData data, String messageId) throws IOException {
        // Step 1: Deduplication. Check if this specific email has already been processed.
        String messageIdColumnRange = SHEET_NAME + "!G:G"; // Column G is our Message ID column.
        ValueRange messageIdResponse = sheets.spreadsheets().values().get(spreadsheetId, messageIdColumnRange).execute();
        List<List<Object>> messageIdValues = messageIdResponse.getValues();
        if (messageIdValues != null && messageIdValues.stream().anyMatch(row -> !row.isEmpty() && row.get(0).equals(messageId))) {
            System.out.println("Deduplication check: Skipping update because Message ID '" + messageId + "' has already been processed.");
            return; // Exit early if we've already seen this email.
        }

        // Step 2: Find existing application record using the company/title key.
        String uniqueKey = generateUniqueKey(data.getCompanyName(), data.getJobTitle());
        String keyColumnRange = SHEET_NAME + "!A:A"; // Column A is the Unique Key.
        ValueRange keyResponse = sheets.spreadsheets().values().get(spreadsheetId, keyColumnRange).execute();
        List<List<Object>> keyValues = keyResponse.getValues();
        OptionalInt rowIndexOpt = findRowIndex(keyValues, uniqueKey);

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // This list of data MUST match the column order in your Google Sheet.
        List<Object> rowData = Arrays.asList(
                uniqueKey,              // Column A: Unique Key
                data.getCompanyName(),    // Column B: Company Name
                data.getJobTitle(),       // Column C: Job Title
                data.getStatus(),         // Column D: Status
                data.getApplicationDate(),// Column E: Application Date
                timestamp,              // Column F: Last Updated
                messageId               // Column G: Message ID
        );

        if (rowIndexOpt.isPresent()) {
            // This application already exists, so we UPDATE its row.
            int rowIndex = rowIndexOpt.getAsInt();
            System.out.printf("Found existing application for key '%s' at row %d. Updating...\n", uniqueKey, rowIndex);

            // Fetch the current row data to check the status for our state machine logic.
            String currentRowRange = String.format("%s!A%d:G%d", SHEET_NAME, rowIndex, rowIndex);
            ValueRange currentRowResponse = sheets.spreadsheets().values().get(spreadsheetId, currentRowRange).execute();
            List<Object> currentRow = currentRowResponse.getValues().get(0);
            ApplicationStatus currentStatus = ApplicationStatus.fromString(currentRow.get(3).toString()); // Status is in Column D (index 3).
            ApplicationStatus newStatus = ApplicationStatus.fromString(data.getStatus());

            if (newStatus.priority > currentStatus.priority || newStatus == ApplicationStatus.REJECTED) {
                ValueRange body = new ValueRange().setValues(Collections.singletonList(rowData));
                sheets.spreadsheets().values().update(spreadsheetId, currentRowRange, body).setValueInputOption("USER_ENTERED").execute();
                System.out.println("Application status successfully updated to " + newStatus.name());
            } else {
                System.out.printf("Skipping status update for '%s' because new status '%s' is not an advancement over current status '%s'.\n", uniqueKey, newStatus.name(), currentStatus.name());
            }
        } else {
            // This is a new application, so we INSERT a new row.
            System.out.printf("No existing application for key '%s'. Appending new row...\n", uniqueKey);
            ValueRange body = new ValueRange().setValues(Collections.singletonList(rowData));
            sheets.spreadsheets().values().append(spreadsheetId, SHEET_NAME + "!A:G", body).setValueInputOption("USER_ENTERED").execute();
        }
    }

    private String generateUniqueKey(String companyName, String jobTitle) {
        if (companyName == null || jobTitle == null) return "";
        return (companyName.toLowerCase().replaceAll("[^a-z0-9]", "") +
                jobTitle.toLowerCase().replaceAll("[^a-z0-9]", "")).trim();
    }

    private OptionalInt findRowIndex(List<List<Object>> values, String key) {
        if (values == null || values.isEmpty()) return OptionalInt.empty();
        return IntStream.range(0, values.size())
                .filter(i -> !values.get(i).isEmpty() && values.get(i).get(0).toString().equals(key))
                .map(i -> i + 1) // Sheets rows are 1-based, not 0-based.
                .findFirst();
    }
}