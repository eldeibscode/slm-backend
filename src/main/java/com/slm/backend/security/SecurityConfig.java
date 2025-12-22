package com.slm.backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${cors.allowed-origins:http://localhost:4200,http://localhost:3000,http://localhost:5173}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/auth/register", "/auth/login").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        // Authenticated report endpoints (must be before public GET /reports/**)
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/reports/my/**").authenticated()
                        // Public report endpoints (GET only)
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/reports", "/reports/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/reports/*/view").permitAll()
                        // Public category and tag endpoints (GET only)
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/categories", "/categories/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/tags", "/tags/**").permitAll()
                        // Public testimonial endpoints (GET only)
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/testimonials", "/testimonials/**").permitAll()
                        // Public uploads endpoint (serve images)
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/uploads/**").permitAll()
                        // Authenticated endpoints
                        .requestMatchers("/auth/me", "/auth/profile").authenticated()
                        .requestMatchers("/users/**").authenticated()
                        // Report write operations require authentication (handled by @PreAuthorize)
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/reports").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/reports/**").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/reports/**").authenticated()
                        // Category/Tag write operations require authentication
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/categories", "/tags").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/categories/**", "/tags/**").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/categories/**", "/tags/**").authenticated()
                        // Testimonial write operations require authentication (handled by @PreAuthorize)
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/testimonials").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/testimonials/**").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/testimonials/**").authenticated()
                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // Allow H2 console to be displayed in frames (for development only)
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Parse allowed origins from environment variable or use defaults
        List<String> origins = Arrays.asList(allowedOrigins.split(","));

        // Use allowedOriginPatterns for more flexibility (supports wildcards and same-origin)
        // This allows patterns like "https://*.biedle.com" or dynamic origins
        configuration.setAllowedOriginPatterns(origins);

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setMaxAge(3600L); // Cache preflight requests for 1 hour

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
