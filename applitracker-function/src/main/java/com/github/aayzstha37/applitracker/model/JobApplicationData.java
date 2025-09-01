package com.github.aayzstha37.applitracker.model;

public class JobApplicationData {
    private String jobTitle;
    private String companyName;
    private String applicationDate;
    private String status;

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(String applicationDate) {
        this.applicationDate = applicationDate;
    }

    @Override
    public String toString() {
        return "JobApplicationData{" +
                "companyName='" + companyName + '\'' +
                ", jobTitle='" + jobTitle + '\'' +
                ", applicationDate='" + applicationDate + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
