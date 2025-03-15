package com.example.back.controller;

import com.example.back.dao.UserDao;
import com.example.back.model.JwtAuthenticationResponse;
import com.example.back.model.Role;
import com.example.back.model.SigninRequest;
import com.example.back.model.SignupRequest;
import com.example.back.model.User;
import com.example.back.service.AuthenticationService;
import com.example.back.service.EmailService;
import com.example.back.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@Slf4j
@RestController //RESTful 웹 서비스를 만들기 위한 컨트롤러로 JSON 데이터를 반환
@RequiredArgsConstructor 
@RequestMapping("/api")
public class AuthController {
    
    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final EmailService emailService;
    private final UserDao userDao;

    // 현재 로그인한 사용자 정보 확인
    @GetMapping("/me")
    public User getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("✅ 현재 로그인한 사용자: {}", userDetails.getUsername());
        //데이터베이스에서 해당 사용자의 정보를 조회하여 반환
        return userService.findByUsername(userDetails.getUsername());
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkAuthStatus(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) { 
        log.warn("❌ 로그인하지 않음");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Collections.singletonMap("isAuthenticated", false));
        }
        log.info("✅ 로그인 상태 확인: {}", userDetails.getUsername());
        Map<String, Object> response = new HashMap<>();
        response.put("isAuthenticated", true);
        response.put("userId", userDetails.getUsername()); // 로그인한 사용자 ID 반환
        return ResponseEntity.ok(response);
    }

    // 로그인 API
    @PostMapping("/login") // 프론트랑 맞추기
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

    //로그아웃 API
    @CrossOrigin(origins = "*", allowedHeaders = "*")  // CORS 설정 추가
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
    log.info("✅ 로그아웃 요청 받음");
    // ✅ Spring Security의 SecurityContext 초기화
    SecurityContextHolder.clearContext();
    // ✅ 세션 무효화 (추가)
    request.getSession().invalidate();
    // ✅ JWT 삭제를 위한 응답 반환 (프론트에서 localStorage에서 삭제 필요)
    return ResponseEntity.ok(Collections.singletonMap("message", "로그아웃 성공"));
    }

    // 회원가입 API 추가
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest signupRequest){
        log.info("✅ 회원가입 요청: " + signupRequest);

         // 이메일 중복 확인 (백엔드에서도 중복 체크 추가)
        //List<String> roles = userDao.findRolesByEmail(signupRequest.getUser_email());
        boolean isVerified = userDao.userExists(signupRequest.getUser_email());
        log.info("🔍 [회원가입] 이메일 인증 여부 확인 결과: {}", isVerified);  // 로그 추가

        //if (exists) {
        if (!isVerified) {
        //if (roles.contains("USER")) {
        log.warn("❌ 회원가입 실패 - 이메일 인증되지 않음: {}", signupRequest.getUser_email());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "이메일 인증이 완료되지 않았습니다. 인증을 먼저 진행해주세요."));
        }

        // 🔹 이메일 중복 확인 (같은 이메일이라도 Role = SNS만 있으면 허용)
        boolean exists = userDao.userExists(signupRequest.getUser_email());
        if (exists) {
            log.warn("❌ 회원가입 실패 - 이미 존재하는 기본 로그인 계정: {}", signupRequest.getUser_email());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "이미 가입된 이메일입니다. 다른 이메일을 사용하거나 소셜 로그인으로 시도해주세요."));
        }

        try {
            authenticationService.signup(signupRequest);
            log.info("✅ 회원가입 성공");
            return ResponseEntity.ok(Map.of("message", "회원가입이 성공적으로 완료되었습니다.")); // 프론트에서 json형식으로 받길 원해서 수정
        } catch (Exception e) {
            log.error("❌ 회원가입 실패: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "회원가입 실패", "details", e.getMessage())); // 프론트에서 json형식으로 받길 원해서 수정
        }
    }

    // 아이디 중복검사 API
    @PostMapping("/check-username")  
    public ResponseEntity<?> checkUsername(@RequestBody Map<String, String> request) {
        String user_id = request.get("user_id"); // 프론트에서 user_id 가져오기
        boolean isAvailable = userService.isUsernameAvailable(user_id); // 중복 여부 확인
        return ResponseEntity.ok(Collections.singletonMap("isAvailable", isAvailable));
    }

    // 아이디 찾기 API
    @PostMapping("/find-id")
    public ResponseEntity<?> findId(@RequestBody Map<String, String> request) {
    System.out.println("📩 요청 데이터: " + request);  // 로그로 확인
    String user_email = request.get("user_email");
    System.out.println("📩 받은 이메일: " + user_email);  // 여기서 null이면 요청이 잘못된 것

    if (user_email == null || user_email.isEmpty()) {
        return ResponseEntity.badRequest().body(Collections.singletonMap("error", "이메일을 입력하세요."));
    }
    // 서비스에서 ID 찾기
    String userId = userService.findUserIdByEmail(user_email);
    
    return ResponseEntity.ok(Collections.singletonMap("user_id", userId));
}

     // ✅ 이메일 인증 요청 API (사용자가 이메일 입력 후 "인증 코드 받기" 버튼 클릭)
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody Map<String, String> request) {
        String user_email = request.get("user_email");
         String provider = request.get("provider"); // "gmail" 또는 "naver"
        if (user_email == null || provider == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "이메일과 제공업체를 입력해주세요."));
        }
        log.info("📩 이메일 인증 코드 요청 - 이메일: {}, 제공업체: {}", user_email, provider);
         // 1️⃣ 랜덤 인증 코드 생성
        String code = authenticationService.generateVerificationCode();
         // 2️⃣ 인증 코드 저장
        userDao.saveVerificationCode(user_email, code);
         // 3️⃣ 이메일 전송 (Gmail 또는 Naver 선택)
        try {
            emailService.sendEmail(provider, user_email, "이메일 인증 코드", "인증 코드: " + code);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
        return ResponseEntity.ok(Map.of("message", "인증 코드가 " + provider + "로 전송되었습니다."));
    }

    // ✅ 이메일 인증 확인 API (사용자가 이메일 + 인증코드 입력 후 확인 버튼 클릭)
    @PostMapping("/check-verification")
    public ResponseEntity<?> checkVerification(@RequestBody Map<String, String> request) {
        String user_email = request.get("user_email");  
        String code = request.get("code");  

        log.info("🔍 [이메일 인증 요청] 이메일: {}, 입력된 코드: {}", user_email, code);

        if (user_email == null || user_email.isBlank() || code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "이메일과 인증 코드를 입력해주세요."));
        }

        try {
            // 2️⃣ DB에서 저장된 인증 코드 조회
            String storedCode = userDao.findVerificationCodeByEmail(user_email);
            if (storedCode == null || storedCode.isBlank()) {
                log.warn("❌ [인증 실패] 이메일: {} - 저장된 인증 코드 없음", user_email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("message", "인증 코드가 존재하지 않습니다."));
            }
    
            // 3️⃣ 사용자가 입력한 코드와 저장된 코드 비교 (대소문자 무시)
            if (!storedCode.trim().equalsIgnoreCase(code.trim())) {
                log.warn("❌ [인증 실패] 이메일: {}, 저장된 코드: {}, 입력된 코드: {}", user_email, storedCode, code);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", "인증 코드가 올바르지 않습니다."));
            }
    
            // 4️⃣ 인증 성공 후 `expired` 값을 `1`로 업데이트 ✅
            userDao.updateVerificationStatus(user_email);
            log.info("✅ [인증 성공] 이메일: {}", user_email);
    
            return ResponseEntity.ok(Map.of("message", "이메일 인증이 완료되었습니다."));
        } catch (Exception e) {
            log.error("❌ [서버 오류] 이메일 인증 중 오류 발생 - 이메일: {}, 오류 메시지: {}", user_email, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "서버 오류가 발생했습니다.", "error", e.getMessage()));
        }
    }

     // ✅ 이메일 중복 확인 API
    @PostMapping("/check-email-duplicate")
    public ResponseEntity<?> checkEmailDuplicate(@RequestBody Map<String, String> request) {
        log.info("🔍 이메일 중복 확인 요청: {}", request);  // 📌 요청 데이터 확인 로그 추가

        String user_email = request.get("user_email");

        if (user_email == null || user_email.isBlank()) {
            log.warn("❌ 이메일 중복 확인 실패 - 입력된 이메일 없음");
            return ResponseEntity.badRequest().body(Map.of("message", "이메일을 입력해주세요."));
        }
         boolean exists = userDao.userExists(user_email); // ✅ 이미 존재하는 이메일인지 확인
        log.info("✅ 이메일 중복 확인 결과 - 이메일: {}, 중복 여부: {}", user_email, exists);
         return ResponseEntity.ok(Map.of("duplicate", exists)); // { "duplicate": true } or { "duplicate": false }
    }

    // 리프레시 토큰
    /* @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshAccessToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "리프레시 토큰이 없습니다."));
        }

        // 1️⃣ 리프레시 토큰 만료 여부 확인
        if (jwtService.isRefreshTokenExpired(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "리프레시 토큰이 만료되었습니다. 다시 로그인해주세요."));
        }

        // 2️⃣ 사용자 정보 추출
        String userId = jwtService.extractUserName(refreshToken);
        UserDetails userDetails = userService.userDetailsService().loadUserByUsername(userId);

        // 3️⃣ 새로운 액세스 토큰 발급
        String newAccessToken = jwtService.generateToken(userDetails);

        log.info("🔄 새로운 액세스 토큰 발급 완료 - User: {}", userId);
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    } */
    
}
