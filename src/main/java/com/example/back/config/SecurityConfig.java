package com.example.back.config;

import com.example.back.filter.JwtAuthenticationFilter;
import com.example.back.service.UserService;
import lombok.RequiredArgsConstructor;

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
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


import com.example.back.model.Role;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CorsFilter corsFilter; //반드시 RequiredArgsConstructor 추가할 것. - 컴파일에러 발생하지 않음.
    private final UserService userService; //UserDetailsService 객체를 제공받기 위해 선언
    //디폴트로 login요청을 시큐리티가 낚아채서 미리 약속된 필터를 통과하도록 강제 함.
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .addFilter(corsFilter) //프론트와 백엔드 원활한 통신을 위해 CORS활성화 및 필터 추가
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests((requests) -> requests
                .requestMatchers("/api/**").permitAll()
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/schedule/**").hasAnyAuthority(Role.ADMIN.name(), Role.USER.name())
                .requestMatchers("/notice/**").hasAnyAuthority(Role.USER.name())
                .requestMatchers("/admin/**").hasAnyAuthority(Role.ADMIN.name())
                .anyRequest().authenticated())
            //아래는 서버가 세션을 생성하거나 관리하지 못하게 설정함. - JWT 토큰 기반 인증 방식에서 사용함.        
            .sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            //커스텀하게 인증 로직을 포함한 인증 제공자를 등록함
                        //사용자 상세정보 (UserDetailsService)와 암호 인코더 활용하여 인증을 수행함.
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }//end of securityFilterChain
    
    //아래 코드가 없으면 user 12345로 로그인 안됨.
    //spring security 5이상에서는 비밀번호를 저장할 때 반드시 인코딩 방식이 명시되어야 함.
    //Bean이 붙은 자원은 스프링 컨테이너(spring-core.jar)가 관리 해줌- jar, war, ear
    @Bean
    public AuthenticationProvider authenticationProvider(){
         //ArrayList al = new ArrayList(), List al = new ArrayList() al= new Vector()
         //메소드 설계시에는 리턴 타입 자리에 상위 클래스를 작성하는 것이 좋다. - 유지보수나 재사용성에서 유리함.
         //AuthenticationProvider대신에 DaoAuthenticationProvider를 사용하여 DB와 연동(로그인)하고
         //로그인 후에 id, email, username, role 정보를 가져와서 UserDetails타입에 담아야 만
         //시큐리티 세션 영역에 담을 수 있다. 아무 클래스나 담을 수가 없다.
         //User클래스를 정의할 때 UserDetails implements를 했다.
         //로그인 화면은 리액트에서 제공하고 있고 사용자가 username과 password입력한다.
         //로그인 버튼을 누르면 SecurityConfig설정에서 loginProcessingUrl("/login") 이 부분을 말함
         //loginProcess 요청이 오면 자동으로 UserDetailsService타입으로 IoC되어 있는 loadUserByUsername함수가 실행됨.
         //이것이 시큐리티 컨벤션이다.
         //UserDetailsService가 선언하고 있는 loadUserByUsername메소드를 재정의 (Overriding) 해야 한다.        
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userService.userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
}//end of authenticationProvider

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}