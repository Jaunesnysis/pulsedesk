package com.pulsedesk.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class HuggingFaceService {

    @Value("${huggingface.api.token}")
    private String apiToken;

    @Value("${huggingface.api.model}")
    private String model;

    @Value("${huggingface.api.url}")
    private String apiUrl;

    @Value("${huggingface.api.timeout}")
    private int timeout;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String analyzeComment(String commentContent) {
        String prompt = buildPrompt(commentContent);
        try {
            String requestBody = objectMapper.writeValueAsString(
                new PromptRequest(prompt)
            );

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl + model))
                .header("Authorization", "Bearer " + apiToken)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofMillis(timeout))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString()
            );

            return extractGeneratedText(response.body());

        } catch (Exception e) {
            throw new RuntimeException("Failed to call Hugging Face API: " + e.getMessage(), e);
        }
    }

    private String buildPrompt(String commentContent) {
        return """
                [INST]
                You are a support ticket triage assistant. Analyze the following user comment and respond ONLY with a valid JSON object, no explanation.

                Rules:
                - If the comment is a compliment, greeting, or general feedback with no issue, set "isTicket" to false and leave other fields empty.
                - If the comment describes a real problem, bug, or request, set "isTicket" to true and fill in all fields.

                Categories: bug, feature, billing, account, other
                Priorities: low, medium, high

                Comment: "%s"

                Respond ONLY with this JSON format:
                {
                  "isTicket": true or false,
                  "title": "short title",
                  "category": "bug | feature | billing | account | other",
                  "priority": "low | medium | high",
                  "summary": "one sentence summary"
                }
                [/INST]
                """.formatted(commentContent);
    }

    private String extractGeneratedText(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            if (root.isArray() && root.size() > 0) {
                return root.get(0).path("generated_text").asText();
            }
            return responseBody;
        } catch (Exception e) {
            return responseBody;
        }
    }

    record PromptRequest(String inputs) {}
}