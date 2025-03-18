package com.example.back.service;

import com.example.back.dao.UserDao;
import com.example.back.model.JwtAuthenticationResponse;
import com.example.back.model.RefreshTokenRequest;
import com.example.back.model.SigninRequest;
import com.example.back.model.SignupRequest;
import com.example.back.model.User;
import lombok.extern.log4j.Log4j2;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Random;

@Log4j2
@Service // 이 클래스가 Spring의 서비스 레이어 역할을 함 (Spring Bean으로 등록됨)
public class AuthenticationService {

    private final UserDao userDao;
    private final JWTService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    // ✅ 명확한 생성자 추가 (SecurityConfig에서 주입 가능하도록)
    public AuthenticationService(UserDao userDao, JWTService jwtService, AuthenticationManager authenticationManager,
            PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean userExists(String user_email) {
        return userDao.userExists(user_email);
    }

    // 사용자를 DB에서 찾기
    public JwtAuthenticationResponse signin(SigninRequest signinRequest) {
        log.info("🔑 로그인 시도: ID = {}", signinRequest.getUser_id());
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                signinRequest.getUser_id(), signinRequest.getUser_pw()));
        User user = userDao.findByUsername(signinRequest.getUser_id());
        if (user == null) {
            log.warn("❌ 로그인 실패: 사용자 ID={}를 찾을 수 없음", signinRequest.getUser_id());
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        // 2. 비밀번호 비교 - 여기서 matches() 사용
        if (!passwordEncoder.matches(signinRequest.getUser_pw(), user.getUser_pw())) {
            log.warn("❌ 로그인 실패: 비밀번호 불일치 (ID: {})", signinRequest.getUser_id());
            throw new BadCredentialsException("비밀번호가 일치하지 않습니다.");
        }

        // 토큰 생성및 반환
        String jwt = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(new HashMap<>(), user);
        log.info("✅ 로그인 성공: ID = {}, JWT 발급 완료", signinRequest.getUser_id());
        return new JwtAuthenticationResponse(jwt, refreshToken, user.getUser_id(), user.getUser_email(),
                user.getUser_name(), user.getUser_birth(), user.getUser_no(), user.getRole());
    }

    public JwtAuthenticationResponse createJwtForOAuthUser(String user_email) {
        log.info("🔑 OAuth 로그인 시도: Email = {}", user_email);
        User user = userDao.findByEmail(user_email);
        if (user == null) {
            log.warn("❌ OAuth 로그인 실패: 이메일={}에 해당하는 사용자를 찾을 수 없음", user_email);
            throw new IllegalArgumentException("OAuth 로그인한 사용자를 찾을 수 없습니다.");
        }
        String jwt = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(new HashMap<>(), user);
        log.info("✅ OAuth 로그인 성공: Email = {}, JWT 발급 완료", user_email);
        return new JwtAuthenticationResponse(jwt, refreshToken, user.getUser_id(), user.getUser_email(),
                user.getUser_name(), user.getUser_birth(), user.getUser_no(), user.getRole());
    }

    public JwtAuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        log.info("🔄 토큰 갱신 시도");
        String userID = jwtService.extractUserName(refreshTokenRequest.getToken());
        User user = userDao.findByUsername(userID);
        if (user == null) {
            log.warn("❌ 토큰 갱신 실패: ID={}를 찾을 수 없음", userID);
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
        if (jwtService.isTokenValid(refreshTokenRequest.getToken(), user)) {
            String jwt = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(new HashMap<>(), user);
            log.info("✅ 토큰 갱신 완료: ID = {}", userID);
            return new JwtAuthenticationResponse(jwt, refreshToken, user.getUser_id(), user.getUser_email(),
                    user.getUser_name(), user.getUser_birth(), user.getUser_no(), user.getRole());
        }
        log.warn("❌ 토큰 갱신 실패: 유효하지 않은 Refresh Token");
        return null;
    }

    // 이메일
    public boolean verifyEmailCode(String user_email, String code) { // user_email 유지
        String storedCode = userDao.findVerificationCodeByEmail(user_email);
        return storedCode != null && storedCode.equals(code);
    }

    // 🔹 새로운 인증 코드 생성
    public String generateVerificationCode() {
        return String.valueOf(new Random().nextInt(900000) + 100000); // 6자리 랜덤 숫자 생성
    }

    public int signup(SignupRequest signupRequest) {
        // 회원가입 요청 시 로그 기록
        log.info("📝 회원가입 요청: Email = {}", signupRequest.getUser_email());
        // 비밀번호를 BCryptPasswordEncoder로 해싱하여 저장 (안전한 비밀번호 관리)
        signupRequest.setUser_pw(passwordEncoder.encode(signupRequest.getUser_pw()));

        int result = userDao.signup(signupRequest);
        if (result > 0) {
            log.info("✅ 회원가입 성공: Email = {}", signupRequest.getUser_email());
        } else {
            log.warn("❌ 회원가입 실패: Email = {}", signupRequest.getUser_email());
        }
        return result;
    }
}
