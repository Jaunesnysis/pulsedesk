package com.pulsedesk.service;

import com.pulsedesk.model.Comment;
import com.pulsedesk.model.Ticket;
import com.pulsedesk.repository.CommentRepository;
import com.pulsedesk.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private HuggingFaceService huggingFaceService;

    @Mock
    private TicketService ticketService;

    @InjectMocks
    private CommentService commentService;

    @Test
    void submitComment_whenAISaysTicket_shouldCreateTicket() {
        Comment saved = new Comment();
        saved.setId(1L);
        saved.setContent("App is broken!");
        saved.setSource("web-form");

        when(commentRepository.save(any(Comment.class))).thenReturn(saved);
        when(huggingFaceService.analyzeComment(anyString())).thenReturn("""
                {
                  "isTicket": true,
                  "title": "App broken",
                  "category": "bug",
                  "priority": "high",
                  "summary": "The app is broken."
                }
                """);

        Ticket ticket = new Ticket();
        ticket.setId(1L);
        when(ticketService.createTicket(anyString(), anyString(), anyString(), anyString(), any()))
                .thenReturn(ticket);

        Comment result = commentService.submitComment("App is broken!", "web-form");

        verify(ticketService, times(1)).createTicket(anyString(), anyString(), anyString(), anyString(), any());
        assertNotNull(result);
    }

    @Test
    void submitComment_whenAISaysNotTicket_shouldNotCreateTicket() {
        Comment saved = new Comment();
        saved.setId(2L);
        saved.setContent("Great app!");
        saved.setSource("web-form");

        when(commentRepository.save(any(Comment.class))).thenReturn(saved);
        when(huggingFaceService.analyzeComment(anyString())).thenReturn("""
                {
                  "isTicket": false
                }
                """);

        commentService.submitComment("Great app!", "web-form");

        verify(ticketService, never()).createTicket(anyString(), anyString(), anyString(), anyString(), any());
    }

    @Test
    void getAllComments_shouldReturnList() {
        when(commentRepository.findAll()).thenReturn(List.of(new Comment(), new Comment()));

        List<Comment> result = commentService.getAllComments();

        assertEquals(2, result.size());
    }
}