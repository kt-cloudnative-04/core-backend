package com.example.backend.module.member.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.module.member.dto.request.MemberLoginRequest;
import com.example.backend.module.member.dto.request.MemberSignupRequest;
import com.example.backend.module.member.dto.response.MemberLoginResponse;
import com.example.backend.module.member.exceptions.EmailAlreadyExistException;
import com.example.backend.module.member.exceptions.MemberNotFoundException;
import com.example.backend.module.member.exceptions.PasswordNotMatchException;
import com.example.backend.module.member.exceptions.PasswordSpecInvalidException;
import com.example.backend.module.member.service.MemberService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor

public class MemberController {
    private final MemberService memberService;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody MemberSignupRequest memberSignupRequest) {
        try {
            memberService.signup(memberSignupRequest);
            return ResponseEntity.ok("signed up");
        } catch (EmailAlreadyExistException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email already exists");
        } catch (PasswordSpecInvalidException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid spec of password");
        }
    }

    @PostMapping("/login")  
    public ResponseEntity<MemberLoginResponse> login(@RequestBody MemberLoginRequest memberLoginRequest, HttpServletRequest request) {
        try {
            MemberLoginResponse memberLoginResponse = memberService.login(memberLoginRequest, request);
            return ResponseEntity.ok(memberLoginResponse);
        } catch (MemberNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MemberLoginResponse(-1, ""));
        } catch (PasswordNotMatchException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MemberLoginResponse(-1, ""));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        memberService.logout(request);
        return ResponseEntity.ok("logged out");
    }

    @GetMapping("/session")
    public ResponseEntity<String> session(HttpServletRequest request) {
        if (request.getSession().getAttribute("memberID") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        } 
        HttpSession session = request.getSession();
        int memberID = (int)session.getAttribute("memberID");
        String memberEmail = (String)session.getAttribute("memberEmail");
        return ResponseEntity.ok("ID : " + memberID + " Email : " + memberEmail);
    }
}
