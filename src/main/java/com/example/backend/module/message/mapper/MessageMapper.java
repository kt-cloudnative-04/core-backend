package com.example.backend.module.message.mapper;

import com.example.backend.module.member.entity.Member;
import com.example.backend.module.message.dto.request.MessageSaveRequest;
import com.example.backend.module.message.dto.response.MessageResponse;
import com.example.backend.module.message.entity.Message;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class MessageMapper {

    public Message toMessageEntity(MessageSaveRequest request, Member member
    ) {
        return new Message(
                request.getMessage(),
                member
        );
    }

    public MessageResponse toMessageResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getMessage(),
                message.getCreatedAt(),
                LocalDateTime.now()
        );
    }

    public List<MessageResponse> toMessageResponseList(List<Message> messages) {
        return messages.stream().map(this::toMessageResponse).toList();
    }
}
