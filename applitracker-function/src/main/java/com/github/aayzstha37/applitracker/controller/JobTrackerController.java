package com.github.aayzstha37.applitracker.function;

import com.github.aayzstha37.applitracker.model.MessageData;
import com.github.aayzstha37.applitracker.model.PubSubMessage;
import com.github.aayzstha37.applitracker.service.GmailWatchService;
import com.github.aayzstha37.applitracker.service.JobApplicationService;
import com.google.gson.Gson;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Base64;
import java.util.function.Consumer;

@Configuration
public class JobTrackerFunction {
    private final JobApplicationService jobApplicationService;
    private final GmailWatchService gmailWatchService;
    private final Gson gson = new Gson();

    public JobTrackerFunction(JobApplicationService jobApplicationService, GmailWatchService gmailWatchService) {
        this.jobApplicationService = jobApplicationService;
        this.gmailWatchService = gmailWatchService;
    }

    /**
     * This function processes incoming job application emails.
     * Triggered by: messages on 'job-updates' topic.
     */
    @Bean
    public Consumer<PubSubMessage> pubSubFunction() {
        return message -> {
            String decodedData = new String(Base64.getDecoder().decode(message.getTopicData()));
            System.out.println("Received email notification: " + decodedData);
            MessageData messageData = gson.fromJson(decodedData, MessageData.class);
            jobApplicationService.processJobApplicationEmail(messageData.getHistoryId());
        };
    }

    /**
     * This function renews the Gmail watch subscription every 6 days.
     * Triggered by: Cloud Scheduler via 'renew-watch' topic.
     */
    @Bean
    public Consumer<PubSubMessage> renewWatchFunction() {
        return message -> {
            try {
                gmailWatchService.renewGmailWatch();
            } catch (Exception e) {
                System.err.println("FATAL: Failed to renew Gmail watch subscription.");
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        };
    }
}