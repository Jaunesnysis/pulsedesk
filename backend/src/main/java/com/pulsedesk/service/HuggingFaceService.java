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
import java.util.List;
import java.util.Map;

@Service
public class HuggingFaceService {

    @Value("${huggingface.api.token}")
    private String apiToken;

    @Value("${huggingface.api.model}")
    private String model;

    @Value("${huggingface.api.timeout}")
    private int timeout;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public String analyzeComment(String commentContent) {
        try {
            Map<String, Object> requestBody = Map.of(
                "model", model,
                "max_tokens", 500,
                "stream", false,
                "messages", List.of(
                    Map.of("role", "system", "content",
                        "You are a support ticket triage assistant. Respond ONLY with valid JSON, no explanation."),
                    Map.of("role", "user", "content", buildPrompt(commentContent))
                )
            );

            String body = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://router.huggingface.co/v1/chat/completions"))
                .header("Authorization", "Bearer " + apiToken)
                .header("Content-Type", "application/json")
                .timeout(Duration.ofMillis(timeout))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

            HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString()
            );

            return extractContent(response.body());

        } catch (Exception e) {
            throw new RuntimeException("Failed to call Hugging Face API: " + e.getMessage(), e);
        }
    }

    private String buildPrompt(String commentContent) {
        return """
                Analyze the following user comment and respond ONLY with a valid JSON object.

                Rules:
                - If the comment is a compliment or general feedback with no issue, set "isTicket" to false.
                - If the comment describes a real problem, bug, or request, set "isTicket" to true and fill all fields.

                Categories: bug, feature, billing, account, other
                Priorities: low, medium, high

                Comment: "%s"

                Respond ONLY with this JSON:
                {
                  "isTicket": true or false,
                  "title": "short title",
                  "category": "bug | feature | billing | account | other",
                  "priority": "low | medium | high",
                  "summary": "one sentence summary"
                }
                """.formatted(commentContent);
    }

    private String extractContent(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            return responseBody;
        }
    }
}