package com.github.aayzstha37.applitracker.config;

public class SecretsConfig {

    private final String refreshToken;
    private final String sheetId;
    private final String clientSecretsJson;

    public SecretsConfig(String refreshToken, String sheetId, String clientSecretsJson) {
        this.refreshToken = refreshToken;
        this.sheetId = sheetId;
        this.clientSecretsJson = clientSecretsJson;
    }

    // Getters for each secret
    public String getRefreshToken() {
        return refreshToken;
    }

    public String getSheetId() {
        return sheetId;
    }

    public String getClientSecretsJson() {
        return clientSecretsJson;
    }
}