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
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class MessageService {
    private final MessageRepository messageRepository;

    private final MessageMapper messageMapper;

    private final MessageKafkaProducer messageKafkaProducer;

    private final MemberRepository memberRepository;

    private final RedisTemplate<String,MessageResponse> messageTemplate;

    private final RedisTemplate<String,String> logTemplate;

    private static final String messagePrefix = "message:";

    private static final String logPrefix = "log:";

    public MessageService(
            MessageRepository messageRepository, MessageMapper messageMapper,
            MessageKafkaProducer messageKafkaProducer, MemberRepository memberRepository,
            RedisTemplate<String,MessageResponse> messageTemplate, RedisTemplate<String,String> logTemplate) {
        this.messageRepository = messageRepository;
        this.messageMapper = messageMapper;
        this.messageKafkaProducer = messageKafkaProducer;
        this.memberRepository = memberRepository;
        this.messageTemplate = messageTemplate;
        this.logTemplate = logTemplate;
    }

    private String toMessage(String type, Integer memberId) {
        return String.format("type=%s | memberId=%d | time=%s",
                type, memberId, LocalDateTime.now());
    }



    @Transactional
    public MessageResponse saveMessage(MessageSaveRequest request) {
        Member member = memberRepository.findById(request.getMemberId()).orElseThrow(EntityNotFoundException::new);

        Message message = messageRepository.save(messageMapper.toMessageEntity(request,member));

        String log = toMessage("SAVE_TO_DB", request.getMemberId());

        messageKafkaProducer.send(log);
        saveToRedisLogAfterCommit(member.getId(),log);

        MessageResponse messageResponse =  messageMapper.toMessageResponse(message);

        saveToRedisMessageAfterCommit(member.getId(),messageResponse);

        return messageResponse;
    }

    public List<String> getAllLogs(Integer memberId) {
        String logKey = logPrefix + memberId;

        List<String> cachedList = logTemplate.opsForList().range(logKey, 0, -1);

        if (cachedList != null && !cachedList.isEmpty()) {
            return cachedList;
        }

        return new ArrayList<>();
    }

    @Transactional(readOnly = true)
    public MessageResponse getMessageById(Long id) {
        return messageMapper.toMessageResponse(messageRepository.findById(id).orElse(null));
    }

    public List<MessageResponse> getAllMessages(Integer memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(EntityNotFoundException::new);

        String redisKey = messagePrefix + memberId;

        String log = toMessage("SELECT_FROM_DB", member.getId());

        List<MessageResponse> cachedList = messageTemplate.opsForList().range(redisKey, 0, -1);

        saveToRedisLogAfterCommit(member.getId(),log);

        if (cachedList != null && !cachedList.isEmpty()) {
            return cachedList;
        }

        List<MessageResponse> messageResponses = messageMapper.toMessageResponseList(
            messageRepository.findAllByMember(member)
        );

        if (!messageResponses.isEmpty()) {
            messageTemplate.opsForList().rightPushAll(redisKey, messageResponses.toArray(new MessageResponse[0]));
        }

        return messageResponses;
    }

    private void saveToRedisLogAfterCommit(Integer memberId, String response) {
        String redisKey = logPrefix + memberId;

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        logTemplate.opsForList().rightPush(redisKey, response);
                    } catch (Exception e) {
                        log.warn("[REDIS ERROR] Failed to cache log: {}", e.getMessage());
                    }
                }
            });
        } else {
            logTemplate.opsForList().rightPush(redisKey, response);
        }
    }

    private void saveToRedisMessageAfterCommit(Integer memberId, MessageResponse response) {
        String redisKey = messagePrefix + memberId;

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        messageTemplate.opsForList().rightPush(redisKey, response);
                    } catch (Exception e) {
                        log.warn("[REDIS ERROR] Failed to cache message: {}", e.getMessage());
                    }
                }
            });
        } else {
            messageTemplate.opsForList().rightPush(redisKey, response);
        }
    }
}
