package shop.mtcoding.bank.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import shop.mtcoding.bank.config.jwt.JwtAuthenticationFilter;
import shop.mtcoding.bank.domain.user.UserEnum;
import shop.mtcoding.bank.util.CustomResponseUtil;

@Configuration
public class SecurityConfig {
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    @Bean // IoC 컨테이너에 BCryptPasswordEncoder 객체 등록. ** @Configuration이 붙어있는 Bean만 등록됨.
    public BCryptPasswordEncoder passwordEncoder() {
        log.debug("디버그 : BCryptPasswordEncoder 빈 등록됨");
        return new BCryptPasswordEncoder();
    }

    // JWT 필터 등록 필요
    public class CustomSecurityFilterManager extends AbstractHttpConfigurer<CustomSecurityFilterManager, HttpSecurity> {
        @Override
        public void configure(HttpSecurity builder) throws Exception {
            AuthenticationManager authenticationManager = builder.getSharedObject(AuthenticationManager.class);
            builder.addFilter(new JwtAuthenticationFilter(authenticationManager));
            super.configure(builder);
        }
    }
    

    // JWT 서버 만들 예정: Session 사용 안 함.
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.debug("디버그: filterChain Bean 등록.");
        http.headers().frameOptions().disable(); // iframe 허용 안함.
        http.csrf().disable(); // csrf가 enable이면 post man 작동 안함. (유튜브 메타코딩 시큐리티 강의)
        http.cors().configurationSource(configurationSource());

        // jSessionId를 서버에서 관리하지 않게 설정
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        // react, app에서 요청할 예정
        http.formLogin().disable();
        // httpBasic은 브라우저가 팝업창을 이용해서 사용자 인증 진행.
        http.httpBasic().disable();

        // 필터 적용
        http.apply(new CustomSecurityFilterManager());

        // Exception 가로채기
        http.exceptionHandling().authenticationEntryPoint((request, response, authException) -> {
            CustomResponseUtil.unAuthentication(response, "로그인하세요.");
        });

        http.authorizeHttpRequests()
                .antMatchers("/api/s/**").authenticated()
                .antMatchers("/api/admin/**").hasRole("" + UserEnum.ADMIN)
                .anyRequest().permitAll();
        
        return http.build();
    }

    public CorsConfigurationSource configurationSource() {
        log.debug("디버그 : configurationSource cors 설정이 SecurityFilterChain에 등록됨");
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*"); // get, post, put, delete (javascript 요청 허용)
        configuration.addAllowedOriginPattern("*"); // 모든 IP 주소 허용 (프론트엔트 IP만 허용 react)
        configuration.setAllowCredentials(true); // 클라이언트 쿠키 요청 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
