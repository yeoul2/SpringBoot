package com.example.back.controller;

import com.example.back.dao.UserDao;
import com.example.back.model.JwtAuthenticationResponse;
import com.example.back.model.Role;
import com.example.back.model.SignupRequest;
import com.example.back.model.User;
import com.example.back.service.AuthenticationService;
import com.example.back.service.UserService;

import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Log4j2
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthenticationService authenticationService;

    public OAuth2LoginSuccessHandler(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        
        String provider = determineProvider(oauth2User);
        log.info("🔥 OAuth2 로그인 성공! - Provider: {}", provider);

        if ("naver".equals(provider)) {
            handleNaverLogin(response, oauth2User);
        } else if ("google".equals(provider)) {
            handleGoogleLogin(response, oauth2User);
        } else {
            log.error("❌ 지원되지 않는 OAuth2 Provider: {}", provider);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported OAuth2 Provider");
        }
    }

    private String determineProvider(OAuth2User oauth2User) {
        if (oauth2User.getAttributes().containsKey("response")) {
            return "naver";
        }
        else if (oauth2User.getAttributes().containsKey("sub")) {
            return "google";
        }
        return "unknown";
    }

    private void handleNaverLogin(HttpServletResponse response, OAuth2User oauth2User) throws IOException {
        log.info("🔥 네이버 로그인 처리 시작");
        
        Map<String, Object> responseMap = Optional.ofNullable((Map<String, Object>) oauth2User.getAttribute("response"))
                .orElseThrow(() -> new IllegalArgumentException("❌ 네이버 사용자 정보가 없습니다."));
        
        String user_email = (String) responseMap.getOrDefault("email", null);
        String user_name = (String) responseMap.getOrDefault("name", "Naver_User");

        processOAuthUser(response, user_email, user_name, "naver");
    }

    private void handleGoogleLogin(HttpServletResponse response, OAuth2User oauth2User) throws IOException {
        log.info("🔥 구글 로그인 처리 시작");
        
        String user_email = oauth2User.getAttribute("email");
        String user_name = oauth2User.getAttribute("name");

        processOAuthUser(response, user_email, user_name, "google");
    }

    private void processOAuthUser(HttpServletResponse response, String user_email, String user_name, String provider) throws IOException {
        if (user_email == null || user_email.isEmpty()) {
            log.error("❌ OAuth2 로그인 오류: 이메일 정보가 없음. Provider: {}", provider);
            throw new IllegalArgumentException("OAuth 로그인한 사용자의 이메일 정보를 찾을 수 없습니다.");
        }
        if (user_name == null || user_name.isEmpty()) {
            user_name = "OAuth2_User";
        }

        try {

            // 테스트중 

             // 기존 사용자 확인
            //User existingUser = userService.findByEmail(user_email);  // 이메일로 기존 사용자 조회
            //if (existingUser != null) {
            // 기존 사용자가 있으면 role을 SNS로 업데이트
            //userDao.updateRoleByEmail(user_email, "SNS");  // 직접 UserDao를 통해 role 업데이트

            // 테스트 중

            JwtAuthenticationResponse jwtResponse = authenticationService.createJwtForOAuthUser(user_email);
            log.info("✅ {} JWT 발급 완료 - Email: {}", provider, user_email);
            redirectUser(response, jwtResponse, provider);
        } catch (IllegalArgumentException e) {
            log.warn("❌ {} OAuth 사용자 정보 없음, 자동 회원가입 진행: {}", provider, user_email);

            SignupRequest signupRequest = new SignupRequest();
            signupRequest.setUser_email(user_email);
            signupRequest.setUser_name(user_name);
            signupRequest.setUser_id(user_email);
            signupRequest.setUser_pw("OAUTH2_USER");
            signupRequest.setUser_birth(null);
            signupRequest.setRole(Role.SNS);

            authenticationService.signup(signupRequest);

            // 회원가입 후 다시 사용자 정보 가져오기
            JwtAuthenticationResponse jwtResponse = authenticationService.createJwtForOAuthUser(user_email);
            redirectUser(response, jwtResponse, provider);
            log.info("✅ {} 자동 회원가입 후 JWT 발급 완료: {}", provider, user_email);
        }
    }

    private void redirectUser(HttpServletResponse response, JwtAuthenticationResponse jwtResponse, String provider) throws IOException {
        log.info("🚀 OAuth2 리디렉트 시작 - provider: {}", provider);
        log.info("✅ 발급된 AccessToken: {}", jwtResponse.getAccessToken());
        log.info("✅ 발급된 RefreshToken: {}", jwtResponse.getRefreshToken());
        String redirectUrl = "http://localhost:6006/" + provider + "/callback"
                + "?accessToken=" + jwtResponse.getAccessToken()
                + "&refreshToken=" + jwtResponse.getRefreshToken();

        log.info("✅ {} 로그인 리디렉트: {}", provider, redirectUrl);
        response.sendRedirect(redirectUrl);
    }
}
