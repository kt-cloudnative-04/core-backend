package com.example.backend.module.member.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.module.member.dto.request.MemberLoginRequest;
import com.example.backend.module.member.dto.request.MemberSignupRequest;
import com.example.backend.module.member.dto.response.MemberLoginResponse;
import com.example.backend.module.member.entity.Member;
import com.example.backend.module.member.exceptions.EmailAlreadyExistException;
import com.example.backend.module.member.exceptions.MemberNotFoundException;
import com.example.backend.module.member.exceptions.PasswordNotMatchException;
import com.example.backend.module.member.exceptions.PasswordSpecInvalidException;
import com.example.backend.module.member.repository.MemberRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;

    public void signup(MemberSignupRequest memberRegisterRequest) {
        Optional<Member> member = memberRepository.findByEmail(memberRegisterRequest.getEmail());
        if (member.isPresent()) {
            throw new EmailAlreadyExistException();
        }
        if (memberRegisterRequest.getPassword().length() < 8) {
            throw new PasswordSpecInvalidException();
        }
        Member newMember = Member.builder()
            .email(memberRegisterRequest.getEmail())
            .password(memberRegisterRequest.getPassword())
            .build();
        memberRepository.save(newMember);
    }

    public MemberLoginResponse login(MemberLoginRequest memberLoginRequest, HttpServletRequest request) {
        Optional<Member> member = memberRepository.findByEmail(memberLoginRequest.getEmail());
        if (member.isEmpty()) {
            throw new MemberNotFoundException();
        }
        else if (!memberLoginRequest.getPassword().equals(member.get().getPassword())) {
            throw new PasswordNotMatchException();
        }
        HttpSession session = request.getSession();
        session.setAttribute("memberID", member.get().getId());
        session.setAttribute("memberEmail", member.get().getEmail());
        return new MemberLoginResponse(member.get().getId(), member.get().getEmail());
    }

    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.invalidate();
        return;
    }
}