package com.projects.applitracker.services.encoder;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

@Service
public class Base64ImageEncoderService implements ImageEncoder {
    @Override
    public String encodeImage(MultipartFile imageFile) {
        try {
            return Base64.getEncoder().encodeToString(imageFile.getBytes());
        }catch (IOException e){
            throw new RuntimeException("Failed to encode image", e);
        }
    }
}
