package com.example.back.controller;

import com.example.back.model.JwtAuthenticationResponse;
import com.example.back.model.Role;
import com.example.back.model.SignupRequest;
import com.example.back.service.AuthenticationService;
import lombok.extern.log4j.Log4j2;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@Log4j2
@Component //Spring이 자동으로 이 클래스를 Bean으로 관리하도록 설정
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthenticationService authenticationService;

    public OAuth2LoginSuccessHandler(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /* ouath2 로그인 성공시 실행되는 메서드 */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                    Authentication authentication) throws IOException, ServletException {

    /* 로그인한 사용자 정보 가져오기 */
    OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal(); //로그인한 사용자 정보를 OAuth2User 객체로 가져옴
    String provider = request.getRequestURI().contains("naver") ? "naver" : "google"; // 제공자 구분

    /* OAuth2 사용자 정보 매핑(Google, Naver) */
    String user_email;
    String user_name;
    // 네이버의 경우 response 객체 내부에 사용자 정보가 있음 → getAttribute("response") 사용
    if(provider.equals("naver")) {
        Map<String, Object> responseMap = (Map<String, Object>) oauth2User.getAttribute("response");
            user_email = responseMap != null ? (String) responseMap.get("email") : null;
            // 네이버의 경우 이름정보가 없을수 있으므로 기본값 Naver_User로 지정
            user_name = responseMap != null ? (String) responseMap.get("name") : "Naver_User";
    } else {
        // Google 로그인 처리
        // 구글은 email, name 값을 가져올 수 있음
        user_email = oauth2User.getAttribute("email");
        user_name = oauth2User.getAttribute("name");
    }

    log.info("🆕 OAuth 로그인 성공 - provider: {}, Email: {}, Name: {}", provider, user_email, user_name);

    // ✅ 이메일이 없을 경우 예외 처리 또는 대체값 사용
    if (user_email == null || user_email.isEmpty()) {
        log.warn("⚠️ OAuth2 응답에서 이메일 정보가 없음. 'sub' 필드를 대체 사용.");
        user_email = oauth2User.getAttribute("sub"); // ✅ Google의 고유 ID 사용
    }

    if (user_email == null || user_email.isEmpty()) {
        log.error("❌ OAuth2 로그인 오류: 이메일 정보가 제공되지 않음.");
        throw new IllegalArgumentException("OAuth 로그인한 사용자의 이메일 정보를 찾을 수 없습니다.");
    }

    /* 사용자의 이름이 없을 경우 */
    if (user_name == null || user_name.isEmpty()) {
        log.warn("⚠️ OAuth2에서 사용자 이름(name) 정보를 제공하지 않음. 기본값 설정.");
        user_name = "OAuth2_User"; // ✅ 기본값 설정
    }

    /* JWT 발급(이미 가입된 사용자) */
    // 이미 가입된 사용자의 경우 createJwtForOAuthUser(user_email)를 호출하여 JWT를 발급
    try {
        JwtAuthenticationResponse jwtResponse = authenticationService.createJwtForOAuthUser(user_email);
        log.info("✅ JWT 발급 완료 - Email: {}", user_email);

        // ✅ 사용자 정보를 SecurityContext에 저장 (자동 로그인 가능하게 설정)
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                oauth2User, null, oauth2User.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);
        log.info("✅ SecurityContext에 OAuth2 로그인 정보 저장 완료!");

        // JWT 토큰을 JSON 응답으로 반환
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        out.print("{\"accessToken\": \"" + jwtResponse.getAccessToken() + "\", \"refreshToken\": \"" + jwtResponse.getRefreshToken() + "\"}");
        out.flush();
        // 자동회원가입 (소셜 로그인 한에서)
    } catch (IllegalArgumentException e) {
        log.warn("❌ OAuth 사용자 정보 없음, 자동 회원가입 진행: {}", user_email);

        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUser_email(user_email);
        signupRequest.setUser_name(user_name);
        signupRequest.setUser_id(user_email);
        signupRequest.setUser_pw("OAUTH2_USER");
        signupRequest.setUser_birth(null);
        signupRequest.setRole(Role.USER);

        authenticationService.signup(signupRequest);
        JwtAuthenticationResponse jwtResponse = authenticationService.createJwtForOAuthUser(user_email);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        out.print("{\"accessToken\": \"" + jwtResponse.getAccessToken() + "\", \"refreshToken\": \"" + jwtResponse.getRefreshToken() + "\"}");
        out.flush();

        log.info("✅ 자동 회원가입 후 JWT 발급 완료: {}", user_email);
        }
    }

}
