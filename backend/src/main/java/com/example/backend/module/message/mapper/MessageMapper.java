package com.example.backend.module.message.mapper;

import com.example.backend.module.message.dto.request.MessageSaveRequest;
import com.example.backend.module.message.dto.response.MessageResponse;
import com.example.backend.module.message.entity.Message;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MessageMapper {

    public Message toMessageEntity(MessageSaveRequest request) {
        return new Message(
                request.getMessage()
        );
    }

    public MessageResponse toMessageResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getMessage(),
                message.getCreatedAt()
        );
    }

    public List<MessageResponse> toMessageResponseList(List<Message> messages) {
        return messages.stream().map(this::toMessageResponse).toList();
    }
}
