package org.talentlink.talentlink.config;

import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.talentlink.talentlink.security.CustomUserDetailsService;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public AuthenticationFailureHandler loginFailureHandler() {
        return (request, response, exception) -> {
            HttpSession session = request.getSession(true);
            String errorMessage;

            Throwable rootCause = exception;
            while (rootCause.getCause() != null
                    && !(rootCause instanceof DisabledException)
                    && !(rootCause instanceof LockedException)) {
                rootCause = rootCause.getCause();
            }

            if (rootCause instanceof DisabledException || rootCause instanceof LockedException) {
                errorMessage = rootCause.getMessage() != null && !rootCause.getMessage().isBlank()
                        ? rootCause.getMessage()
                        : "Tài khoản của bạn đã bị khoá, không thể đăng nhập.";
            } else {
                errorMessage = "Email hoặc mật khẩu không chính xác.";
            }

            session.setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, exception);
            session.setAttribute("LOGIN_ERROR_MESSAGE", errorMessage);
            response.sendRedirect(request.getContextPath() + "/login?error");
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CustomUserDetailsService customUserDetailsService,
            AuthenticationSuccessHandler roleBasedAuthenticationSuccessHandler
    ) throws Exception {
        http
                .userDetailsService(customUserDetailsService)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/css/**", "/js/**", "/images/**", "/login", "/register/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/partner/**").hasRole("PARTNER")
                        .requestMatchers("/me/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(roleBasedAuthenticationSuccessHandler)
                        .failureHandler(loginFailureHandler())
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .maximumSessions(1)
                )
                .rememberMe(remember -> remember
                        .key("talentlink-remember-me-secret-key")
                        .rememberMeParameter("remember-me")
                        .tokenValiditySeconds(7 * 24 * 60 * 60)
                        .userDetailsService(customUserDetailsService)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID", "remember-me")
                        .permitAll()
                );

        // CSRF mặc định được bật cho mọi request/form
        return http.build();
    }
}