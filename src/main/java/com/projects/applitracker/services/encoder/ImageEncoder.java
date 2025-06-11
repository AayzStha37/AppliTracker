package com.projects.applitracker.services.encoder;

import org.springframework.web.multipart.MultipartFile;

public interface ImageEncoder {
    public String encodeImage(MultipartFile imageFile);
}
