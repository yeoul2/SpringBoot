package com.example.back.controller;

import com.example.back.model.JwtAuthenticationResponse;
import com.example.back.model.SigninRequest;
import com.example.back.service.AuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody SigninRequest signinRequest){
        log.info("✅ 받은값 : " + signinRequest.getUser_id() + ", " + signinRequest.getUser_pw());
        try {
            JwtAuthenticationResponse response = authenticationService.signin(signinRequest);
            log.info("✅ JWT발급 성공 : " + response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ 로그인 실패: ", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}
