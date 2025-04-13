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
        return ResponseEntity.ok(messageService.getMessageById(messageId));
    }

    @GetMapping("/all/{memberId}")
    public ResponseEntity<List<MessageResponse>> getAllMessages(@PathVariable("memberId") Integer memberId) {
        return ResponseEntity.ok(messageService.getAllMessages(memberId));
    }

    @GetMapping("/logs/{memberId}")
    public ResponseEntity<List<String>> getAllLogs(@PathVariable("memberId") Integer memberId) {
        return ResponseEntity.ok(messageService.getAllLogs(memberId));
    }
}
