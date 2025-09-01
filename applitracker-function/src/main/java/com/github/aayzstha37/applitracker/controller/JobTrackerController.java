package com.github.aayzstha37.applitracker.controller;

import com.github.aayzstha37.applitracker.model.MessageData;
import com.github.aayzstha37.applitracker.model.PubSubPushPayload;
import com.github.aayzstha37.applitracker.service.GmailWatchService;
import com.github.aayzstha37.applitracker.service.JobApplicationService;
import com.google.gson.Gson;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;

@RestController
public class JobTrackerController {

    private final JobApplicationService jobApplicationService;
    private final GmailWatchService gmailWatchService;
    private final Gson gson = new Gson();

    public JobTrackerController(JobApplicationService jobApplicationService, GmailWatchService gmailWatchService) {
        this.jobApplicationService = jobApplicationService;
        this.gmailWatchService = gmailWatchService;
    }

    // This endpoint will be the target for our Pub/Sub Push Subscription.
    @PostMapping("/") // Listen for POST requests on the root URL
    public ResponseEntity<String> receivePubSubPush(@RequestBody PubSubPushPayload payload) {
        System.out.println("Received Pub/Sub Push Request.");

        if (payload == null || payload.getMessage() == null || payload.getMessage().getData() == null) {
            System.out.println("Received an empty or malformed push request. Acknowledging.");
            return new ResponseEntity<>("Request processed.", HttpStatus.OK);
        }

        try {
            // The data from Pub/Sub push is also Base64 encoded.
            String decodedData = new String(Base64.getDecoder().decode(payload.getMessage().getData()));
            System.out.println("Decoded Pub/Sub message data: " + decodedData);

            MessageData messageData = gson.fromJson(decodedData, MessageData.class);

            // Call our existing service logic, which remains unchanged.
            jobApplicationService.processJobApplicationEmail(messageData.getHistoryId());

            // By returning a 2xx status code, we "ACK" the message, telling Pub/Sub it was processed successfully.
            return new ResponseEntity<>("Request processed successfully.", HttpStatus.OK);

        } catch (Exception e) {
            System.err.println("FATAL: Error processing Pub/Sub push message. The message will be redelivered.");
            e.printStackTrace();
            // By returning a non-2xx status code, we "NACK" the message. Pub/Sub will try to send it again.
            return new ResponseEntity<>("Error processing request.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/renew-watch")
    public ResponseEntity<String> renewGmailWatch() {
        System.out.println("Received scheduled request to renew Gmail watch.");
        try {
            gmailWatchService.renewGmailWatch();
            return new ResponseEntity<>("Gmail watch renewed successfully.", HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("FATAL: Failed to renew Gmail watch subscription.");
            e.printStackTrace();
            return new ResponseEntity<>("Error renewing watch.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}