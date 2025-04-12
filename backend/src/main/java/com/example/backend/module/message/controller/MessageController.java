package com.example.backend.module.message.controller;

import com.example.backend.module.message.dto.request.MessageSaveRequest;
import com.example.backend.module.message.dto.response.MessageResponse;
import com.example.backend.module.message.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/message")
public class MessageController {
    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping()
    public ResponseEntity<MessageResponse> saveMessage(@RequestBody MessageSaveRequest request) {
        return ResponseEntity.ok(messageService.saveMessage(request));
    }

    @GetMapping("/{messageId}")
    public ResponseEntity<MessageResponse> getMessage(@PathVariable("messageId") Long messageId) {
        return null;
    }

    @GetMapping("/all/{memberId}")
    public ResponseEntity<List<MessageResponse>> getAllMessages(@PathVariable("messageId") Integer memberId) {
        return null;
    }
}
