package com.example.back.controller;

import com.example.back.dao.UserDao;
import com.example.back.model.JwtAuthenticationResponse;
import com.example.back.model.Role;
import com.example.back.model.SigninRequest;
import com.example.back.model.SignupRequest;
import com.example.back.model.User;
import com.example.back.service.AuthenticationService;
import com.example.back.service.EmailService;
import com.example.back.service.JWTService;
import com.example.back.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@Slf4j
@RestController // RESTful 웹 서비스를 만들기 위한 컨트롤러로 JSON 데이터를 반환
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthController {

    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final EmailService emailService;
    private final UserDao userDao;
    private final JWTService jwtService;
    private final PasswordEncoder passwordEncoder;

    // 현재 로그인한 사용자 정보 확인
    @GetMapping("/me")
    public User getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("✅ 현재 로그인한 사용자: {}", userDetails.getUsername());
        // 데이터베이스에서 해당 사용자의 정보를 조회하여 반환
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
    public ResponseEntity<?> signin(@RequestBody SigninRequest signinRequest) {
        log.info("✅ 받은값 : " + signinRequest.getUser_id() + ", " + signinRequest.getUser_pw());
        try {
            User user = userDao.findByUsername(signinRequest.getUser_id());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("❌ 사용자 정보가 올바르지 않습니다.");
            }
            // ✅ 임시 비밀번호이면 로그인 차단
            if (user.isTempPw()) {
                log.warn("❌ 임시 비밀번호로 로그인 시도: {}", signinRequest.getUser_id());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "⚠️ 임시 비밀번호로는 로그인할 수 없습니다. 비밀번호를 변경해주세요.",
                                "is_temp_pw", true));
            }
            JwtAuthenticationResponse response = authenticationService.signin(signinRequest);
            log.info("✅ JWT발급 성공 : " + response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ 로그인 실패: ", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    // 로그아웃 API
    @CrossOrigin(origins = "*", allowedHeaders = "*") // CORS 설정 추가
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
    public ResponseEntity<?> signup(@RequestBody SignupRequest signupRequest) {
        log.info("✅ 회원가입 요청: {}", signupRequest);

        try {
            // ✅ 1️⃣ 같은 이메일로 등록된 계정이 있는지 확인
            User user = userDao.findByEmail(signupRequest.getUser_email());

            if (user != null) {
                log.info("🔍 기존 계정 정보 확인 - email: {}, role: {}", user.getUser_email(), user.getRole());
            
                if (user.getRole() == Role.USER) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(Map.of("message", "이미 가입된 이메일입니다."));
                } else if (user.getRole() == Role.SNS) {
                    log.info("🗑️ 기존 SNS 계정 삭제 시도 (user_no: {})", user.getUser_no());
                    int deletedRows = userDao.deleteUser(user.getUser_no());
            
                    if (deletedRows > 0) {
                        log.info("✅ 기존 SNS 계정 삭제 성공 (email: {})", signupRequest.getUser_email());
                    } else {
                        log.warn("❌ 기존 SNS 계정 삭제 실패 (email: {})", signupRequest.getUser_email());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(Map.of("message", "SNS 계정 삭제에 실패했습니다."));
                    }
                }
            }

            authenticationService.signup(signupRequest);
            log.info("✅ 회원가입 성공");
            return ResponseEntity.ok(Map.of("message", "회원가입이 성공적으로 완료되었습니다."));

        } catch (DataIntegrityViolationException e) {
            log.error("❌ 데이터 무결성 오류: 이메일 중복", e);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "이미 가입된 이메일입니다."));
        } catch (Exception e) {
            log.error("❌ 회원가입 실패: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "회원가입 실패", "details", e.getMessage()));
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
        System.out.println("📩 요청 데이터: " + request); // 로그로 확인

        String user_name = request.get("user_name");
        String user_email = request.get("user_email");

        log.info("📩 요청 데이터: {}", request);

        System.out.println("받은 이름: " + user_name);
        System.out.println("📩 받은 이메일: " + user_email); // 여기서 null이면 요청이 잘못된 것

        if (user_email == null || user_email.isEmpty() || user_name == null || user_name.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "이름과 이메일을 입력하세요."));
        }
        // 서비스에서 ID 찾기
        String userId = userService.findUserIdByNameAndEmail(user_name, user_email);

        if (userId == null) {
            log.warn("❌ [AuthController] 아이디 찾기 실패: 일치하는 아이디 없음");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "입력하신 정보와 일치하는 아이디를 찾을 수 없습니다."));
        }

        log.info("✅ [AuthController] 찾은 아이디: {}", userId);
        return ResponseEntity.ok(Map.of("user_id", userId));
    }

    // 비번 찾기 API
    @PostMapping("/find-pw")
    public ResponseEntity<?> findPw(@RequestBody Map<String, String> rquest) {
        String user_id = rquest.get("user_id");
        String user_email = rquest.get("user_email");

        // ✅ `processFindPassword()`에서 `temporaryPassword`를 반환하도록 수정
        String tempPassword = userService.processFindPassword(user_id, user_email);

        if (tempPassword != null) {
            return ResponseEntity.ok(Map.of("success", true, "message", "임시 비밀번호가 이메일로 전송되었습니다.",
                    "temporaryPassword", tempPassword));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "입력한 정보가 올바르지 않습니다."));
        }

    }

    // 비밀변호 변경 API
    @PostMapping("/change-pw")
    public ResponseEntity<?> changePw(@RequestBody Map<String, String> request) {
        String user_id = request.get("user_id"); // 프론트에서 전달받은 user_id 사용
        String tempPw = request.get("temporaryPassword");
        String newPw = request.get("newPassword");
        String confirmPw = request.get("confirmPassword");

        // 🔍 요청 받은 값 로그 출력
        log.info("🔍 비밀번호 변경 요청: user_id=" + user_id);
        log.info("🔍 tempPw: " + tempPw);
        log.info("🔍 newPw: " + newPw);
        log.info("🔍 confirmPw: " + confirmPw);

        if (tempPw == null || newPw == null || confirmPw == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("❌ 필수 입력값이 누락되었습니다.");
        }

        // 사용자 조회 (MyBatis 또는 JPA 사용 가능)
        User user = userDao.findByUsername(user_id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("사용자를 찾을 수 없습니다.");
        }

        log.info("🔍 DB 저장된 암호화된 비밀번호: " + user.getPassword());

        // 임시 비밀번호 검증 (저장된 비밀번호와 비교)
        if (!passwordEncoder.matches(tempPw, user.getPassword())) {
            return ResponseEntity.badRequest().body("임시 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 유효성 검사 (영문, 숫자, 특수문자 포함 8~16자)
        if (!newPw.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,16}$")) {
            return ResponseEntity.badRequest().body("비밀번호는 영문, 숫자, 특수문자를 포함하여 8~16자로 입력해야 합니다.");
        }

        // 새 비밀번호 확인 검증
        if (!newPw.equals(confirmPw)) {
            return ResponseEntity.badRequest().body("새 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 암호화 후 DB 업데이트
        String encryptedPassword = passwordEncoder.encode(newPw);
        userDao.updatePassword(user.getUser_no(), encryptedPassword, false); // ✅ 기존 updatePassword 메서드 재사용

        // 응답 반환
        Map<String, Object> response = new HashMap<>();
        response.put("message", "비밀번호가 성공적으로 변경되었습니다.");
        return ResponseEntity.ok(response);
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

    // ✅ 이메일 중복 확인 API (회원가입 시 호출)
    @PostMapping("/check-email-duplicate")
    public ResponseEntity<?> checkEmailDuplicate(@RequestBody Map<String, String> request) {
        String userEmail = request.get("user_email");

        if (userEmail == null || userEmail.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "이메일을 입력하세요."));
        }

        // 1️⃣ USER 계정으로 가입된 이메일인지 확인 (기존 countByEmail 활용)
        boolean hasUserRole = userDao.hasUserRoleByEmail(userEmail);
        if (hasUserRole) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "이미 가입된 이메일입니다.", "duplicate", true));
        }

        // 2️⃣ 이메일 인증이 완료되어 `expired = true` 인 경우 중복 처리 (기존 findVerificationCodeByEmail
        // 활용)
        boolean isExpired = userDao.isEmailExpired(userEmail);
        if (isExpired) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "이메일 인증이 완료된 계정입니다. 로그인하세요.", "expired", true));
        }

        return ResponseEntity.ok(Map.of("duplicate", false));
    }

    /**
     * 비밀번호 확인 API (마이페이지 접근 전)
     * 
     * @param request { "user_pw": "사용자가 입력한 비밀번호" }
     */
    // myPageCheck API
    @PostMapping("/check-password")
    public ResponseEntity<?> checkPassword(@RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String token) {
        log.info("📌 [checkPassword] 요청 들어옴: {}", request);

        // 입력된 비밀번호 가져오기
        String inputUser_pw = request.get("user_pw");

        if (inputUser_pw == null || inputUser_pw.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "비밀번호를 입력하세요."));
        }

        // jwt 토큰에서 user_id 가져오기
        String jwt = token.replace("Bearer ", ""); // Bearer 제거
        String user_id = jwtService.extractUserName(jwt);
        log.info("✅ JWT에서 추출한 user_id: {}", user_id);

        // DB에서 user_id로 사용자 정보 조회
        User user = userService.findByUsername(user_id);
        if (user == null) {
            log.warn("❌ 사용자 정보 없음: {}", user_id);
            return ResponseEntity.status(404).body(Map.of("error", "사용자를 찾을 수 없습니다."));
        }

        // 비밀번호 비교
        if (!passwordEncoder.matches(inputUser_pw, user.getUser_pw())) {
            log.warn("❌ 비밀번호 불일치: {}", user_id);
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "비밀번호가 틀렸습니다."));
        }

        // 비밀번호 일치 마이페이지 접근 허용
        log.info("✅ 비밀번호 확인 성공: {}", user_id);
        return ResponseEntity.ok(Map.of("success", true, "message", "비밀번호 확인 완료!"));
    }

    // 마이페이지 API
    @GetMapping("/user-info")
    public ResponseEntity<?> getUserInfo(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "❌ 인증되지 않은 요청입니다. 로그인 후 다시 시도하세요."));
        }

        String user_id = jwtService.extractUserName(token.substring(7)); // jwt에서 user_id 추출

        if (user_id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "❌ 사용자 정보를 찾을 수 없습니다."));
        }

        User user = userDao.findByUsername(user_id);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("❌ 사용자를 찾을 수 없습니다.");
        }

        // 사용자 정보 반환
        return ResponseEntity.ok(Map.of(
                "user_name", user.getUser_name(),
                "user_id", user.getUser_id(),
                "user_email", user.getUser_email(),
                "user_birth", user.getUser_birth()));
    }

    // 사용자 정보 업데이트 API
    @PutMapping("/update-user")
    public ResponseEntity<?> updateUserInfo(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> request) {

        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "❌ 인증되지 않은 요청입니다. 로그인 후 다시 시도하세요."));
        }

        String user_id = jwtService.extractUserName(token.substring(7));

        if (user_id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "❌ 사용자 정보를 찾을 수 없습니다."));
        }

        User user = userDao.findByUsername(user_id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "❌ 사용자를 찾을 수 없습니다."));
        }

        // 전달된 데이터 중 변경된 값만 업데이트
        String newUserName = request.getOrDefault("user_name", user.getUser_name());
        String newUserEmail = request.getOrDefault("user_email", user.getUser_email());
        String newUserBirth = request.getOrDefault("user_birth", user.getUser_birth());

        userDao.updateUserInfo(user.getUser_no(), newUserName, newUserEmail, newUserBirth);

        return ResponseEntity.ok(Map.of("message", "✅ 사용자 정보가 성공적으로 수정되었습니다."));
    }

    // 마이페이지 비밀번호 변경 API
    @PutMapping("/update-pw")
    public ResponseEntity<?> updatePw(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> request) {

        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "❌ 인증되지 않은 요청입니다. 로그인 후 다시 시도하세요."));
        }

        String user_id = jwtService.extractUserName(token.substring(7));

        if (user_id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "❌ 사용자 정보를 찾을 수 없습니다."));
        }

        User user = userDao.findByUsername(user_id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "❌ 사용자를 찾을 수 없습니다."));
        }

        // 현재 비밀번호
        String currentPw = request.get("current_pw");
        String newPw = request.get("new_pw");
        String confirmPw = request.get("confirm_pw");

        if (!passwordEncoder.matches(currentPw, user.getUser_pw())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "❌ 현재 비밀번호가 일치하지 않습니다."));
        }

        // 새 비밀번호 유효성 검사
        if (!newPw.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,16}$")) {
            return ResponseEntity.badRequest().body("비밀번호는 영문, 숫자, 특수문자를 포함하여 8~16자로 입력해야 합니다.");
        }

        // 새 비밀번호 확인 검증
        if (!newPw.equals(confirmPw)) {
            return ResponseEntity.badRequest().body("새 비밀번호가 일치하지 않습니다.");
        }

        // 새 비밀번호 암호화 후 DB에 저장
        String encryptedPassword = passwordEncoder.encode(newPw);
        userDao.updatePassword(user.getUser_no(), encryptedPassword, false); // ✅ 기존 updatePassword 메서드 재사용

        // 응답 반환
        return ResponseEntity.ok(Map.of("message", "✅ 비밀번호가 성공적으로 변경되었습니다."));
    }

    @DeleteMapping("/delete-info")
    public ResponseEntity<?> deleteInfo(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> request) {

        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "❌ 인증되지 않은 요청입니다. 로그인 후 다시 시도하세요."));
        }

        String user_id = jwtService.extractUserName(token.substring(7));

        if (user_id == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "❌ 사용자 정보를 찾을 수 없습니다."));
        }

        User user = userDao.findByUsername(user_id);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "❌ 사용자를 찾을 수 없습니다."));
        }

        // 현재 비밀번호 확인
        String currentPw = request.get("current_pw");
        if (!passwordEncoder.matches(currentPw, user.getUser_pw())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "❌ 현재 비밀번호가 일치하지 않습니다."));
        }

        userDao.deleteUser(user.getUser_no()); // 물리적 삭제 (완전히 제거)

        return ResponseEntity.ok(Map.of("message", "✅ 회원 탈퇴가 완료되었습니다."));
    }
}
