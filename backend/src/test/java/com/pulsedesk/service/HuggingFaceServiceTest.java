package com.pulsedesk.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class HuggingFaceServiceTest {

    @InjectMocks
    private HuggingFaceService huggingFaceService;

    @Test
    void extractContent_withValidResponse_shouldReturnContent() {
        ReflectionTestUtils.setField(huggingFaceService, "apiToken", "test-token");
        ReflectionTestUtils.setField(huggingFaceService, "model", "test-model");
        ReflectionTestUtils.setField(huggingFaceService, "timeout", 30000);

        String fakeResponse = """
                {
                  "choices": [
                    {
                      "message": {
                        "content": "{\\"isTicket\\": true, \\"title\\": \\"Test\\"}"
                      }
                    }
                  ]
                }
                """;

        String result = invokeExtractContent(fakeResponse);
        assertTrue(result.contains("isTicket"));
    }

    @Test
    void extractContent_withInvalidResponse_shouldReturnRawBody() {
        String invalidResponse = "Not Found";
        String result = invokeExtractContent(invalidResponse);
        assertEquals("Not Found", result);
    }

    private String invokeExtractContent(String responseBody) {
        try {
            var method = HuggingFaceService.class
                    .getDeclaredMethod("extractContent", String.class);
            method.setAccessible(true);
            return (String) method.invoke(huggingFaceService, responseBody);
        } catch (Exception e) {
            fail("Failed to invoke extractContent: " + e.getMessage());
            return null;
        }
    }
}