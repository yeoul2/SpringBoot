package com.example.back.controller;

import com.example.back.model.JwtAuthenticationResponse;
import com.example.back.model.SigninRequest;
import com.example.back.model.SignupRequest;
import com.example.back.service.AuthenticationService;
import lombok.extern.slf4j.Slf4j;
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

    // 로그인 API
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

    // 회원가입 API 추가
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest signupRequest){
        log.info("✅ 회원가입 요청: " + signupRequest);
        try {
            authenticationService.signup(signupRequest);
            log.info("✅ 회원가입 성공");
            return ResponseEntity.ok("회원가입이 성공적으로 완료되었습니다.");
        } catch (Exception e) {
            log.error("❌ 회원가입 실패: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
