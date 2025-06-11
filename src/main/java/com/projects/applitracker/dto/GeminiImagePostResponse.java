package com.projects.applitracker.dto;

import java.util.List;

public class GeminiImagePostResponse {

    public List<Job> jobs;

    public GeminiImagePostResponse() {}

    public GeminiImagePostResponse(List<Job> jobs) {
        this.jobs = jobs;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public void setJobs(List<Job> jobs) {
        this.jobs = jobs;
    }

    public static class Job{
        public String company;
        public String role;

        public Job() {}

        public Job(String company, String role) {
            this.company = company;
            this.role = role;
        }

        public String getCompany() {
            return company;
        }

        public void setCompany(String company) {
            this.company = company;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

}
