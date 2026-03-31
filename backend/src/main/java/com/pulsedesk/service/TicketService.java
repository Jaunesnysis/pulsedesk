package com.pulsedesk.service;

import com.pulsedesk.model.Comment;
import com.pulsedesk.model.Ticket;
import com.pulsedesk.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;

    public Ticket createTicket(String title, String category,
                               String priority, String summary,
                               Comment comment) {
        Ticket ticket = new Ticket();
        ticket.setTitle(title);
        ticket.setCategory(category);
        ticket.setPriority(priority);
        ticket.setSummary(summary);
        ticket.setComment(comment);
        return ticketRepository.save(ticket);
    }

    public List<Ticket> getAllTickets() {
        return ticketRepository.findAll();
    }

    public Optional<Ticket> getTicketById(Long id) {
        return ticketRepository.findById(id);
    }
}