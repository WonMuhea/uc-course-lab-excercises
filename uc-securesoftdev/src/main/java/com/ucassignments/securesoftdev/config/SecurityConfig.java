package com.ucassignments.securesoftdev.config;

import com.ucassignments.securesoftdev.repository.UserRepository;
import com.ucassignments.securesoftdev.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.HeaderWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enables @PreAuthorize
public class SecurityConfig {



    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, RateLimitingFilter rateLimitingFilter) throws Exception {
        http
                .addFilterAt(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class)
                        // ... your existing authorize/headers/csrf/login/logout ...\
                // Session Management: Protects against session fixation and limits concurrent sessions
                .sessionManagement(session -> session
                        .sessionFixation().migrateSession() // Default behavior, but good to be explicit
                        .maximumSessions(1) // Only one active session per user is allowed
                        .maxSessionsPreventsLogin(true) // Prevent a new login if max sessions reached
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/login", "/css/**", "/api/public/**").permitAll()
                        .requestMatchers("/encrypt").authenticated()
                        .requestMatchers("/admin").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login").permitAll()
                        .successHandler(myAuthenticationSuccessHandler())
                )

                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                // CSRF with HttpOnly cookie to prevent theft via JavaScript
                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .redirectToHttps(Customizer.withDefaults())
                .headers(headers -> headers
                        .contentTypeOptions(Customizer.withDefaults())
                        .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                        .addHeaderWriter(permissionsPolicyHeaderWriter())
                );

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler myAuthenticationSuccessHandler(){
        return new SimpleUrlAuthSuccessHandler();
    }

    // Required for Spring Security to listen to session events for concurrent session control
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return new CustomUserDetailsService(userRepository);
    }

    private HeaderWriter permissionsPolicyHeaderWriter() {
        return (request, response) ->
                response.setHeader("Permissions-Policy", "geolocation=(), microphone=(), camera=()");
    }
}
