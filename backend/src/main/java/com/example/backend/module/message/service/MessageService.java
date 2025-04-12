package com.example.backend.module.message.service;

import com.example.backend.global.kafka.MessageKafkaProducer;
import com.example.backend.module.message.dto.request.MessageSaveRequest;
import com.example.backend.module.message.dto.response.MessageResponse;
import com.example.backend.module.message.entity.Message;
import com.example.backend.module.message.mapper.MessageMapper;
import com.example.backend.module.message.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {
    private final MessageRepository messageRepository;

    private final MessageMapper messageMapper;

    private final MessageKafkaProducer messageKafkaProducer;

    public MessageService(
            MessageRepository messageRepository, MessageMapper messageMapper,
            MessageKafkaProducer messageKafkaProducer) {
        this.messageRepository = messageRepository;
        this.messageMapper = messageMapper;
        this.messageKafkaProducer = messageKafkaProducer;
    }

    private String toKafkaMessage(Message message) {
        return String.format("type=SAVED_TO_DB | id=%d | member=MEMBER | time=%d",
                message.getId(), LocalDateTime.now());
    }

    @Transactional
    public MessageResponse saveMessage(MessageSaveRequest request) {
        Message message = messageRepository.save(messageMapper.toMessageEntity(request));

        messageKafkaProducer.send(toKafkaMessage(message));

        return messageMapper.toMessageResponse(message);
    }

    @Transactional(readOnly = true)
    public MessageResponse getMessageById(Long id) {
        return messageMapper.toMessageResponse(messageRepository.findById(id).orElse(null));
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getAllMessages(Integer memberId) {
        return null;
    }
}
