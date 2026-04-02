package com.pulsedesk.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pulsedesk.dto.CommentRequest;
import com.pulsedesk.model.Comment;
import com.pulsedesk.service.CommentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @Test
    void submitComment_shouldReturn201() throws Exception {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setContent("The app crashes!");
        comment.setSource("web-form");
        comment.setCreatedAt(LocalDateTime.now());
        comment.setConvertedToTicket(false);

        when(commentService.submitComment(anyString(), anyString())).thenReturn(comment);

        CommentRequest request = new CommentRequest("The app crashes!", "web-form");

        mockMvc.perform(post("/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("The app crashes!"))
                .andExpect(jsonPath("$.source").value("web-form"));
    }

    @Test
    void getAllComments_shouldReturn200() throws Exception {
        when(commentService.getAllComments()).thenReturn(List.of());

        mockMvc.perform(get("/comments"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void submitComment_withEmptyContent_shouldReturn400() throws Exception {
        CommentRequest request = new CommentRequest("", "web-form");

        mockMvc.perform(post("/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}