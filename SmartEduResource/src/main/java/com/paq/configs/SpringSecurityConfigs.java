package com.paq.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Configuration
@EnableWebSecurity
@EnableTransactionManagement
@PropertySource(value = "classpath:cloudinary.properties", ignoreResourceNotFound = true)
@ComponentScan(
        basePackages = {
            "com.paq.controllers",
            "com.paq.repository",
            "com.paq.service",
            "com.paq.utils"
        }
)
@Order(2)
public class SpringSecurityConfigs {

    @Autowired
    private Environment env;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public HandlerMappingIntrospector mvcHandlerMappingIntrospector() {
        return new HandlerMappingIntrospector();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/admin/**", "/", "/login").csrf(c -> c.disable()).authorizeHttpRequests((requests) -> requests
                .requestMatchers(
                        "/",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/webjars/**"
                ).permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().permitAll()
        ).formLogin(form -> form.permitAll() // Đường dẫn tới trang đăng nhập
                .loginProcessingUrl("/login") // Đường dẫn xử lý POST
                .defaultSuccessUrl("/", true) // Chuyển hướng khi thành công
                .failureUrl("/admin/login?error=true") // Chuyển hướng khi thất bại
                .permitAll()
        ).logout((logout) -> logout.logoutSuccessUrl("/admin/login").permitAll());

        return http.build();
    }

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", this.getRequiredProperty("CLOUDINARY_CLOUD_NAME", "cloudinary.cloud_name"),
                "api_key", this.getRequiredProperty("CLOUDINARY_API_KEY", "cloudinary.api_key"),
                "api_secret", this.getRequiredProperty("CLOUDINARY_API_SECRET", "cloudinary.api_secret"),
                "secure", true));
    }

    private String getRequiredProperty(String environmentKey, String localPropertyKey) {
        String value = this.env.getProperty(environmentKey);
        if (value == null || value.isBlank()) {
            value = this.env.getProperty(localPropertyKey);
        }
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required config: " + environmentKey);
        }

        return value;
    }

    // @Bean
    // public CorsConfigurationSource corsConfigurationSource() {
    //     CorsConfiguration config = new CorsConfiguration();
    //     config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost"));
    //     config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
    //     config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    //     config.setExposedHeaders(List.of("Authorization"));
    //     config.setAllowCredentials(true);
    //     UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    //     source.registerCorsConfiguration("/**", config);
    //     return source;
    // }
}
