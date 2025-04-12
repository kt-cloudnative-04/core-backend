package com.example.backend.module.message.repository;

import com.example.backend.module.member.entity.Member;
import com.example.backend.module.message.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message,Long> {
    List<Message> findAllByMember(Member member);
}
