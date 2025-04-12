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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class MessageService {
    private final MessageRepository messageRepository;

    private final MessageMapper messageMapper;

    private final MessageKafkaProducer messageKafkaProducer;

    private final MemberRepository memberRepository;

    private final RedisTemplate<String,MessageResponse> redisTemplate;

    private static final String messagePrefix = "message:";

    public MessageService(
            MessageRepository messageRepository, MessageMapper messageMapper,
            MessageKafkaProducer messageKafkaProducer, MemberRepository memberRepository,
            RedisTemplate<String,MessageResponse> redisTemplate) {
        this.messageRepository = messageRepository;
        this.messageMapper = messageMapper;
        this.messageKafkaProducer = messageKafkaProducer;
        this.memberRepository = memberRepository;
        this.redisTemplate = redisTemplate;
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

        MessageResponse messageResponse =  messageMapper.toMessageResponse(message);

        saveToRedisAfterCommit(member.getId(),messageResponse);

        return messageResponse;
    }

    @Transactional(readOnly = true)
    public MessageResponse getMessageById(Long id) {
        return messageMapper.toMessageResponse(messageRepository.findById(id).orElse(null));
    }

    public List<MessageResponse> getAllMessages(Integer memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(EntityNotFoundException::new);

        String redisKey = messagePrefix + memberId;

        List<MessageResponse> cachedList = redisTemplate.opsForList().range(redisKey, 0, -1);

        if (cachedList != null && !cachedList.isEmpty()) {
            return cachedList;
        }

        List<MessageResponse> messageResponses = messageMapper.toMessageResponseList(
            messageRepository.findAllByMember(member)
        );

        if (!messageResponses.isEmpty()) {
            redisTemplate.opsForList().rightPushAll(redisKey, messageResponses.toArray(new MessageResponse[0]));
        }

        return messageResponses;
    }

    private void saveToRedisAfterCommit(Integer memberId, MessageResponse response) {
        String redisKey = messagePrefix + memberId;

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        redisTemplate.opsForList().rightPush(redisKey, response);
                    } catch (Exception e) {
                        log.warn("[REDIS ERROR] Failed to cache message: {}", e.getMessage());
                    }
                }
            });
        } else {
            redisTemplate.opsForList().rightPush(redisKey, response);
        }
    }
}
