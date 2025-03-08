package com.example.back.filter;

import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.example.back.service.JWTService;
import com.example.back.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

//JWT 기반 인증을 처리하기위한 Spring Security 커스텀 필터 입니다.
//OncePerRequestFilter를 상속 받아 요청 당 한 번씩 필터가 실행되도록 보장합니다.
//스프링에서는 setter객체 주입법과 생성자 객체 주입법이 있다.
@Log4j2
@Component //이 클래스를 스프링 빈으로 등록. 다른 컴포넌트 설정에서 주입받아 사용이 가능함.
@RequiredArgsConstructor //final로 선언된 필드에 대한 생성자를 자동으로 생성하여 의존성 주입을 해줌.
public class JwtAuthenticationFilter extends OncePerRequestFilter{
    private final JWTService jwtService;
    private final UserService userService;
    //이 필터의 핵심 메소드이다. - 핵심로직을 수행하는 메소드이다.
    //요청이 들어올 때 마다 실행이 됨.
    @Override
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
    // 인증이 필요 없는 경로는 바로 지나가게 처리
    String path = request.getRequestURI();
    if (path.startsWith("/api/v1/auth/signup") || path.startsWith("/api/v1/auth/signin")) {
        filterChain.doFilter(request, response);  // 인증 없이 그냥 요청을 처리
        return;
    }

    // 기존 JWT 인증 로직
    final String authHeader = request.getHeader("Authorization");
    final String jwt;
    final String userId;
    try {
        // authHeader가 빈값이면 Bearer로 시작하지 않으면 JWT검증 없이 다음 필터로 넘어간다.
        if (StringUtils.isEmpty(authHeader) || !org.apache.commons.lang3.StringUtils.startsWith(authHeader, "Bearer")) {
            filterChain.doFilter(request, response);
            return;
        }
        // 헤더에 있는 Bearer 뒤에 실제 JWT토큰 문자열을 추출하기
        jwt = authHeader.substring(7); // 문자열을 자를 때 사용함.
        // 추출한 토큰에서 사용자 이름을 추출함.
        userId = jwtService.extractUserName(jwt);
        // userId가 존재하고 현재 SecurityContext에 인증 정보가 없는 경우에만 추가 인증을 수행함
        if (StringUtils.isNotEmpty(userId) && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userService.userDetailsService().loadUserByUsername(userId);
            // jwtService.isTokenValid(jwt, userDetails)를 통해 토큰이 유효한지 확인합니다.
            if (jwtService.isTokenValid(jwt, userDetails)) {
                // 새 SecurityContext 생성하고 사용자 정보와 권한 정보를 담은 UsernamePasswordAuthenticationToken을 생성함.
                SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
                UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                // 요청에 대한 세부 정보를 토큰에 추가한 뒤 이 토큰을 SecurityContext에 설정함.
                token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                securityContext.setAuthentication(token);
                // 최종적으로 SecurityContextHolder에 저장함.
                SecurityContextHolder.setContext(securityContext);
            }
        } // 사용자 인증 처리
    } catch (ExpiredJwtException e) { // 예외처리: 토큰 만료
        log.info("토큰이 만료되었습니다");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write("토큰이 만료되었습니다");
        return;
    }
    // 다음 필터 체인을 계속 실행시킴
    filterChain.doFilter(request, response);
    }

}//end of JwtAuthenticationFilter
/*
 * 1. Authorization 헤더 처리
 * 
 * 2. JWT토큰 검증
 * 
 * 3. JWT 추출 및 사용자 식별
 * 
 * 4. 사용자 인증처리
 * 
 * 5. 예외처리 : 토큰 만료
 * 
 * 
 * 6. 필터 체인 계속 실행
 * 
 * 
 * 
 */