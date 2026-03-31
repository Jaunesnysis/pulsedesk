package com.pulsedesk.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsedesk.model.Comment;
import com.pulsedesk.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final HuggingFaceService huggingFaceService;
    private final TicketService ticketService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Comment submitComment(String content, String source) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setSource(source);
        Comment saved = commentRepository.save(comment);

        try {
            String aiResponse = huggingFaceService.analyzeComment(content);
            String json = extractJson(aiResponse);
            JsonNode result = objectMapper.readTree(json);

            if (result.path("isTicket").asBoolean()) {
                ticketService.createTicket(
                    result.path("title").asText(),
                    result.path("category").asText(),
                    result.path("priority").asText(),
                    result.path("summary").asText(),
                    saved
                );
                saved.setConvertedToTicket(true);
                commentRepository.save(saved);
            }
        } catch (Exception e) {
            System.err.println("AI analysis failed for comment " + saved.getId() + ": " + e.getMessage());
        }

        return saved;
    }

    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }

    private String extractJson(String text) {
        int start = text.indexOf("{");
        int end = text.lastIndexOf("}");
        if (start != -1 && end != -1) {
            return text.substring(start, end + 1);
        }
        return text;
    }
}