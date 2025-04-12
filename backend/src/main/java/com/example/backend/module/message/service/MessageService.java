package com.example.backend.module.message.service;

import com.example.backend.global.kafka.MessageKafkaProducer;
import com.example.backend.module.member.entity.Member;
import com.example.backend.module.member.repository.MemberRepository;
import com.example.backend.module.message.dto.request.MessageSaveRequest;
import com.example.backend.module.message.dto.response.MessageResponse;
import com.example.backend.module.message.entity.Message;
import com.example.backend.module.message.mapper.MessageMapper;
import com.example.backend.module.message.repository.MessageRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {
    private final MessageRepository messageRepository;

    private final MessageMapper messageMapper;

    private final MessageKafkaProducer messageKafkaProducer;

    private final MemberRepository memberRepository;

    public MessageService(
            MessageRepository messageRepository, MessageMapper messageMapper,
            MessageKafkaProducer messageKafkaProducer, MemberRepository memberRepository) {
        this.messageRepository = messageRepository;
        this.messageMapper = messageMapper;
        this.messageKafkaProducer = messageKafkaProducer;
        this.memberRepository = memberRepository;
    }

    private String toKafkaMessage(Message message, Integer memberId) {
        return String.format("type=SAVED_TO_DB | id=%d | memberId=%d | time=%s",
                message.getId(), memberId, LocalDateTime.now());
    }

    @Transactional
    public MessageResponse saveMessage(MessageSaveRequest request) {
        Member member = memberRepository.findById(request.getMemberId()).orElseThrow(EntityNotFoundException::new);

        Message message = messageRepository.save(messageMapper.toMessageEntity(request,member));

        messageKafkaProducer.send(toKafkaMessage(message, request.getMemberId()));

        return messageMapper.toMessageResponse(message);
    }

    @Transactional(readOnly = true)
    public MessageResponse getMessageById(Long id) {
        return messageMapper.toMessageResponse(messageRepository.findById(id).orElse(null));
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> getAllMessages(Integer memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(EntityNotFoundException::new);

        return messageMapper.toMessageResponseList(
            messageRepository.findAllByMember(member)
        );
    }
}
