package com.example.back.config;

import com.example.back.controller.OAuth2LoginSuccessHandler;
import com.example.back.filter.JwtAuthenticationFilter;
import com.example.back.service.UserService;
import com.example.back.service.AuthenticationService;
import com.example.back.service.JWTService;
import com.example.back.dao.UserDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.web.filter.CorsFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration // spring 설정 클래스임을 나타냄
@EnableWebSecurity // Spring Security를 활성화하는 어노테이션
@Log4j2
@RequiredArgsConstructor // final로 선언된 필드를 자동으로 생성자를 통해 주입
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final CorsFilter corsFilter;
	private final UserService userService;
	private final UserDao userDao;
	private final JWTService jwtService;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationConfiguration authenticationConfiguration;

	// ✅ AuthenticationManager를 Bean으로 등록
	// Spring Security에서 인증을 관리하는 AuthenticationManager를 Bean으로 등록
	@Bean
	public AuthenticationManager authenticationManager() throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	/* AuthenticationService를 Bean으로 등록하여 의존성 주입을 가능하게 함 */
	@Bean
	public AuthenticationService authenticationService(AuthenticationManager authenticationManager,
			PasswordEncoder passwordEncoder) {
		return new AuthenticationService(userDao, jwtService, authenticationManager, passwordEncoder);
	}

	/* oauth2 로그인을 성공시 실행될 핸들러 bean으로 등록, google, naver, kakao jwt를 발급하는 역할 */
	@Bean
	public OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler(AuthenticationService authenticationService) {
		return new OAuth2LoginSuccessHandler(authenticationService);
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationService authenticationService)
			throws Exception {
		http
				.cors(Customizer.withDefaults()) // cors 설정
				.addFilter(corsFilter) // cors 필터 추가
				.csrf(csrf -> csrf.disable()) // csrf 보안 비활성화 (jwt 이므로 필요가 없음)
				.authorizeHttpRequests(requests -> requests
						.requestMatchers("/api/login").permitAll() // 로그인 경로 허용
						.requestMatchers("/oauth2/**").permitAll() // OAuth2 관련 경로 허용
						.requestMatchers("/api/search/**").permitAll() // 검색 API 경로 허용
						.requestMatchers("/api/places/**").permitAll() // Places API 경로 허용
						.requestMatchers("/api/check").authenticated() // 🔥 인증된 사용자만 접근 가능
						//.requestMatchers("/api/**").authenticated() // JWT 인증 필요
						.requestMatchers("/api/0user-info").authenticated() // 마이페이지 JWT 인증 필요
						.requestMatchers("/api/**").permitAll()
						.requestMatchers("/api/email-verification").permitAll() // ✅ 이메일 인증 API 허용
						.requestMatchers("/oauth2/authorization/**").permitAll() // ✅ OAuth2 로그인 엔드포인트 허용
						.requestMatchers("/oauth/naver/callback").permitAll() // 네이버 콜백 허용
						.requestMatchers("/oauth/google/callback").permitAll() // 구글 콜백 허용
						.requestMatchers("/error").permitAll() // 에러 경로 허용
						.anyRequest().authenticated()) // 그 외 모든 요청은 인증 필요
				.formLogin(form -> form.disable()) // ✅ Security에서 기본 로그인 페이지 제공 제거
				.httpBasic(httpBasic -> httpBasic.disable()) // ✅ HTTP Basic 인증 제거

				.oauth2Login(oauth2 -> oauth2
						.authorizationEndpoint(endpoint -> endpoint.baseUri("/oauth2/authorization"))
						.redirectionEndpoint(endpoint -> endpoint.baseUri("/login/oauth2/code/*"))
						.successHandler(oAuth2LoginSuccessHandler(authenticationService)) // ✅ 로그인 성공 후 처리
						.failureHandler((request, response, exception) -> { // ✅ 로그인 실패 시 `/login?error` 방지
							log.error("OAuth2 로그인 실패: {}", exception.getMessage());

							String referer = request.getHeader("Referer");
							if (referer != null && referer.contains("naver")) {
								response.sendRedirect("/oauth2/authorization/naver"); // 네이버 로그인 실패시 재시도
							} else {
								response.sendRedirect("/oauth2/authorization/google"); // 구글 로그인 실패 시 재시도
							}
						}))
				.sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authenticationProvider(authenticationProvider())
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	@Bean
	public AuthenticationProvider authenticationProvider() {
		DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
		authenticationProvider.setUserDetailsService(userService.userDetailsService());
		authenticationProvider.setPasswordEncoder(passwordEncoder);
		return authenticationProvider;
	}
}