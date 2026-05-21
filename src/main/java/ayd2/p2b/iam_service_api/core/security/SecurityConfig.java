package ayd2.p2b.iam_service_api.core.security;

import ayd2.p2b.iam_service_api.feature.auth.infrastructure.security.filter.JwtAuthenticationFilter;
import ayd2.p2b.iam_service_api.feature.auth.infrastructure.security.handler.RestAuthenticationEntryPoint;
import ayd2.p2b.iam_service_api.feature.auth.infrastructure.security.token.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            RestAuthenticationEntryPoint authenticationEntryPoint) {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/refresh", "/users/register").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/logout").authenticated()
                        .requestMatchers(HttpMethod.POST, "/users/system-admins", "/users/congress-admins")
                        .hasRole("SYSTEM_ADMIN")
                        .requestMatchers(HttpMethod.POST, "/users/guest-speakers").hasRole("CONGRESS_ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/users/*/activate", "/users/*/deactivate")
                        .hasRole("SYSTEM_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/users").hasAnyRole("SYSTEM_ADMIN", "CONGRESS_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/users/*/can-be-committee").hasRole("CONGRESS_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/users/me", "/users/*").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/users/*").authenticated()
                        .anyRequest().denyAll())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
