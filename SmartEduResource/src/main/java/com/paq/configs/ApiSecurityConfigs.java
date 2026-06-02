package com.paq.configs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.paq.filters.JwtFilter;
import com.paq.repository.UserRepository;


@Configuration
@Order(1)
public class ApiSecurityConfigs {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Environment env;

    @Bean
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {

        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .securityMatcher("/api/**")
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/secure/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/secure/lecturer/**").hasAnyRole("LECTURER", "ADMIN")
                        .requestMatchers("/api/secure/student/**").hasAnyRole("STUDENT", "ADMIN")
                        .requestMatchers("/api/secure/**").authenticated()
                        .anyRequest().permitAll()
                ).addFilterBefore(new JwtFilter(this.userRepository), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(getAllowedOrigins());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }

    private List<String> getAllowedOrigins() {
        List<String> origins = new ArrayList<>(List.of("http://localhost:3000", "http://localhost"));
        String configuredOrigins = env.getProperty("CORS_ALLOWED_ORIGINS");

        if (configuredOrigins != null && !configuredOrigins.isBlank()) {
            Arrays.stream(configuredOrigins.split(","))
                    .map(String::trim)
                    .filter(origin -> !origin.isBlank())
                    .forEach(origins::add);
        }

        return origins;
    }
}
