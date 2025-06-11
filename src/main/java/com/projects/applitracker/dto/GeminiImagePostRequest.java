package com.projects.applitracker.dto;

import java.util.List;

public class GeminiImagePostRequest {
    private List<Content> contents;

    public GeminiImagePostRequest() {} // No-arg constructor

    public GeminiImagePostRequest(List<Content> contents) {
        this.contents = contents;
    }

    public List<Content> getContents() {
        return contents;
    }

    public void setContents(List<Content> contents) {
        this.contents = contents;
    }

    // Inner class: Content
    public static class Content {
        private List<Part> parts;

        public Content() {}

        public Content(List<Part> parts) {
            this.parts = parts;
        }

        public List<Part> getParts() {
            return parts;
        }

        public void setParts(List<Part> parts) {
            this.parts = parts;
        }
    }

    // Inner class: Part
    public static class Part {
        private InlineData inline_data;
        private String text;

        public Part() {}

        public Part(InlineData inline_data) {
            this.inline_data = inline_data;
        }

        public Part(String text) {
            this.text = text;
        }

        public InlineData getInline_data() {
            return inline_data;
        }

        public void setInline_data(InlineData inline_data) {
            this.inline_data = inline_data;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    // Inner class: InlineData
    public static class InlineData {
        private String mime_type;
        private String data;

        public InlineData() {}

        public InlineData(String mime_type, String data) {
            this.mime_type = mime_type;
            this.data = data;
        }

        public String getMime_type() {
            return mime_type;
        }

        public void setMime_type(String mime_type) {
            this.mime_type = mime_type;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }
}
