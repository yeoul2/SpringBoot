package com.example.back.controller;

import com.example.back.model.JwtAuthenticationResponse;
import com.example.back.model.SigninRequest;
import com.example.back.model.SignupRequest;
import com.example.back.model.User;
import com.example.back.service.AuthenticationService;
import com.example.back.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController //RESTful 웹 서비스를 만들기 위한 컨트롤러로 JSON 데이터를 반환
@RequiredArgsConstructor 
//@RequestMapping("/api/v1/auth")
@RequestMapping("/api")
public class AuthController {
    
    private final UserService userService;
    private final AuthenticationService authenticationService;

    // 현재 로그인한 사용자 정보 확인
    @GetMapping("/me")
    public User getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("✅ 현재 로그인한 사용자: {}", userDetails.getUsername());
        //데이터베이스에서 해당 사용자의 정보를 조회하여 반환
        return userService.findByUsername(userDetails.getUsername());
    }

    // 로그인 API
    //@PostMapping("/signin")
    @PostMapping("/api")
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
