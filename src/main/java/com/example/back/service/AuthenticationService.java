package com.example.back.service;

import com.example.back.dao.UserDao;
import com.example.back.model.JwtAuthenticationResponse;
import com.example.back.model.RefreshTokenRequest;
import com.example.back.model.Role;
import com.example.back.model.SigninRequest;
import com.example.back.model.SignupRequest;
import com.example.back.model.User;
import lombok.extern.log4j.Log4j2;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Log4j2
@Service //이 클래스가 Spring의 서비스 레이어 역할을 함 (Spring Bean으로 등록됨)
@Repository
public class AuthenticationService {

    private final UserDao userDao;
    private final JWTService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    // ✅ 명확한 생성자 추가 (SecurityConfig에서 주입 가능하도록)
    public AuthenticationService(UserDao userDao, JWTService jwtService, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }


    // 프론트에서 받은 googleAccessToken을 직접 사용해서 유저 정보를 가져옴
    /* public JwtAuthenticationResponse authenticateWithGoogleToken(String googleAccessToken) {
        RestTemplate restTemplate = new RestTemplate();
        // Google 사용자 정보 요청
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(googleAccessToken);
        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
            "https://www.googleapis.com/oauth2/v3/userinfo", HttpMethod.GET, request, Map.class
        );
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Google 사용자 정보 요청 실패!");
        }
        Map<String, Object> userInfo = response.getBody();
        String userEmail = (String) userInfo.get("email");
        String userName = (String) userInfo.get("name");
    
        // DB 조회 후 JWT 생성
        User user = userDao.findByEmail(userEmail);
        if (user == null) {
            throw new IllegalArgumentException("OAuth 로그인한 사용자를 찾을 수 없습니다.");
        }
    
        // ✅ JWT 발급 (기존 시스템과 호환되도록)
        String jwt = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(new HashMap<>(), user);
    
        // ✅ Google 로그인에서는 googleAccessToken 포함하여 응답
        return new JwtAuthenticationResponse(jwt, refreshToken, 
            user.getUser_id(), user.getUser_email(), user.getUser_name(),
            user.getUser_birth(), user.getUser_no(), user.getRole());
    } */

/* 테스트 */

/* public JwtAuthenticationResponse exchangeGoogleCodeForToken(String code) {
    RestTemplate restTemplate = new RestTemplate();
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("client_id", "YOUR_GOOGLE_CLIENT_ID");
    params.add("client_secret", "YOUR_GOOGLE_CLIENT_SECRET");
    params.add("code", code);
    params.add("redirect_uri", "http://localhost:7007/login/oauth2/code/google");
    params.add("grant_type", "authorization_code");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

    ResponseEntity<Map> response = restTemplate.postForEntity("https://oauth2.googleapis.com/token", request, Map.class);
    if (!response.getStatusCode().is2xxSuccessful()) {
        throw new RuntimeException("Google OAuth Token 요청 실패!");
    }

    Map<String, Object> responseBody = response.getBody();
    String googleAccessToken = (String) responseBody.get("access_token"); // ✅ 구글에서 받은 액세스 토큰
    String refreshToken = (String) responseBody.get("refresh_token");

    // ✅ 구글 사용자 정보 가져오기
    HttpHeaders userInfoHeaders = new HttpHeaders();
    userInfoHeaders.setBearerAuth(googleAccessToken);
    HttpEntity<String> userInfoRequest = new HttpEntity<>(userInfoHeaders);

    ResponseEntity<Map> userInfoResponse = restTemplate.exchange(
        "https://www.googleapis.com/oauth2/v3/userinfo", HttpMethod.GET, userInfoRequest, Map.class
    );

    if (!userInfoResponse.getStatusCode().is2xxSuccessful()) {
        throw new RuntimeException("Google 사용자 정보 요청 실패!");
    }

    Map<String, Object> userInfo = userInfoResponse.getBody();
    String userEmail = (String) userInfo.get("email");
    String userName = (String) userInfo.get("name");

    // ✅ 데이터베이스에서 유저 정보 조회
    User user = userDao.findByEmail(userEmail);
    if (user == null) {
        throw new IllegalArgumentException("OAuth 로그인한 사용자를 찾을 수 없습니다.");
    }

    // ✅ JWT 토큰 생성
    String accessToken = jwtService.generateToken(user);
    String ourRefreshToken = jwtService.generateRefreshToken(new HashMap<>(), user);

    // ✅ 최종 응답 반환 (우리 JWT + 구글 액세스 토큰 포함)
    return new JwtAuthenticationResponse(googleAccessToken, accessToken, ourRefreshToken, 
        user.getUser_id(), user.getUser_email(), user.getUser_name(),
        user.getUser_birth(), user.getUser_no(), user.getRole());
}
 */
    /* 테스트 */

    public boolean userExists(String user_email) {
        return userDao.userExists(user_email);
    }


    public JwtAuthenticationResponse signin(SigninRequest signinRequest) {
        log.info("🔑 로그인 시도: ID = {}", signinRequest.getUser_id());
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                signinRequest.getUser_id(), signinRequest.getUser_pw()));
        User user = userDao.findByUsername(signinRequest.getUser_id());
        if (user == null) {
            log.warn("❌ 로그인 실패: 사용자 ID={}를 찾을 수 없음", signinRequest.getUser_id());
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }
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
