package com.projects.applitracker.controllers;

import com.projects.applitracker.constants.Constants;
import com.projects.applitracker.dto.GeminiImagePostResponse;
import com.projects.applitracker.services.encoder.ImageEncoder;
import com.projects.applitracker.services.llm.GeminiLLMService;
import com.projects.applitracker.services.llm.LLMResponseParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("app/v1")
public class MainController {
    private final ImageEncoder imageEncoder;
    private final LLMResponseParser llmResponseParser;
    private final GeminiLLMService geminiLLMService;

    @Autowired
    public MainController(ImageEncoder imageEncoder, LLMResponseParser llmResponseParser, GeminiLLMService geminiLLMService) {
        this.imageEncoder = imageEncoder;
        this.llmResponseParser = llmResponseParser;
        this.geminiLLMService = geminiLLMService;
    }

    @PostMapping("/updateExcelSheet")
    public ResponseEntity<String> linkedInImageProcessor(@RequestParam("imageSS") MultipartFile imageFile) {
        //Base64 Encoding
        if(imageFile.isEmpty())
            return ResponseEntity.badRequest().body(Constants.NO_IMAGE);

        if(!imageFile.getContentType().equals("image/png"))
            return ResponseEntity.badRequest().body(Constants.INVALID_PNG_FORMAT);

        String encodedImage = imageEncoder.encodeImage(imageFile);

        //Send BS64 Encoded Image to Gemini LLM
        String jsonResponse = geminiLLMService.sendRequest(encodedImage);

        //Process response JSON
        GeminiImagePostResponse response = llmResponseParser.parseResponse(jsonResponse, GeminiImagePostResponse.class);

        // Get company and role lists
        List<String> companies = response.getJobs().stream().map(GeminiImagePostResponse.Job::getCompany).toList();
        List<String> roles = response.getJobs().stream().map(GeminiImagePostResponse.Job::getRole).toList();

        if(companies.isEmpty() || roles.isEmpty()){
            return ResponseEntity.unprocessableEntity().body("Invalid image content - can't extract required data");
        }

        System.out.println("Companies:");
        companies.forEach(System.out::println);

        System.out.println("\nRoles:");
        roles.forEach(System.out::println);

        //Populate Excel Sheet

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
