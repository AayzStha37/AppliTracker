package com.github.aayzstha37.applitracker.service;

import com.github.aayzstha37.applitracker.config.SecretsConfig;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.github.aayzstha37.applitracker.model.ApplicationStatus;
import com.github.aayzstha37.applitracker.model.JobApplicationData;
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
    private static final String SHEET_NAME = "Sheet1"; // Assumes the default sheet name

    public SheetsService(@Qualifier("sheetsApiClient") Sheets sheetsApiClient, SecretsConfig secretsConfig) {
        this.sheets = sheetsApiClient;
        this.spreadsheetId = secretsConfig.getSheetId(); // Get the sheet ID from the config
    }

    public void updateSheet(JobApplicationData data) throws IOException {
        String uniqueKey = generateUniqueKey(data.getCompanyName(), data.getJobTitle());
        String keyColumnRange = SHEET_NAME + "!A:A"; // Unique key is in Column A
        ValueRange response = sheets.spreadsheets().values().get(spreadsheetId, keyColumnRange).execute();
        List<List<Object>> values = response.getValues();
        OptionalInt rowIndexOpt = findRowIndex(values, uniqueKey);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        if (rowIndexOpt.isPresent()) {
            // UPDATE LOGIC
            int rowIndex = rowIndexOpt.getAsInt();
            System.out.printf("Found existing entry for key '%s' at row %d. Updating...\n", uniqueKey, rowIndex);

            // Fetch the current row data to check the status
            String currentRowRange = String.format("%s!A%d:F%d", SHEET_NAME, rowIndex, rowIndex);
            ValueRange currentRowResponse = sheets.spreadsheets().values().get(spreadsheetId, currentRowRange).execute();
            List<Object> currentRow = currentRowResponse.getValues().get(0);

            // STATE MANAGEMENT: Only update if the new status is an upgrade
            ApplicationStatus currentStatus = ApplicationStatus.fromString(currentRow.get(4).toString()); // Status is in 5th column (index 4)
            ApplicationStatus newStatus = ApplicationStatus.fromString(data.getApplicationStatus());

            if (newStatus.priority > currentStatus.priority || newStatus == ApplicationStatus.REJECTED) {
                List<Object> updatedRowData = Arrays.asList(
                        uniqueKey,
                        String.valueOf(data.getCompanyName()),
                        String.valueOf(data.getJobTitle()),
                        String.valueOf(data.getApplicationDate()),
                        newStatus.name()
                );
                ValueRange body = new ValueRange().setValues(Collections.singletonList(updatedRowData));
                sheets.spreadsheets().values().update(spreadsheetId, currentRowRange, body).setValueInputOption("USER_ENTERED").execute();
            } else {
                System.out.printf("Skipping status update for '%s' because new status '%s' does not have higher priority than current status '%s'.\n", uniqueKey, newStatus, currentStatus);
            }
        } else {
            System.out.printf("No existing entry for key '%s'. Appending new row...\n", uniqueKey);
            List<Object> newRowData = Arrays.asList(
                    uniqueKey,
                    String.valueOf(data.getCompanyName()),
                    String.valueOf(data.getJobTitle()),
                    String.valueOf(data.getApplicationDate()),
                    String.valueOf(data.getApplicationStatus())
            );
            ValueRange body = new ValueRange().setValues(Collections.singletonList(newRowData));
            sheets.spreadsheets().values().append(spreadsheetId, SHEET_NAME, body).setValueInputOption("USER_ENTERED").execute();
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
                .map(i -> i + 1) // Sheets rows are 1-based
                .findFirst();
    }
}