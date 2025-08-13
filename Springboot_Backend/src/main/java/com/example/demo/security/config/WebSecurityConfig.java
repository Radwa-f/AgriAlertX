package com.example.demo.security.config;

import com.example.demo.service.AppUserService;
import com.example.demo.security.jwt.JwtRequestFilter;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class WebSecurityConfig {

    private final AppUserService appUserService;
    // Commenting out BCryptPasswordEncoder for now
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtRequestFilter jwtRequestFilter;

    /**
     * Define security filter chain configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Enable CORS
                // CSRF is disabled as this application uses stateless JWT authentication, which is immune to CSRF attacks.
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(
                                "/api/v1/login/**",
                                "/api/v1/registration/**",
                                "/api/crops/**",
                                "/api/news/**",
                                "/api/images/**",
                                "/api/weather/**",
                                "/api/crops/weather-analysis/**",
                                "/swagger-ui/**", "/v3/api-docs/**",
                                "/forgotPassword/**",
                                "/changePassword/**"
                        ).permitAll()
                        .requestMatchers("/api/v1/user/**").authenticated()  // Secure the /api/v1/user/** route
                        .anyRequest().authenticated()  // Secure other routes
                )
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class); // Add JWT filter

        return http.build();
    }

    /**
     * Define authentication manager as a bean.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Define the DAO authentication provider.
     */
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();

        // Commenting out BCryptPasswordEncoder for now, using NoOpPasswordEncoder
         provider.setPasswordEncoder(bCryptPasswordEncoder);
//        provider.setPasswordEncoder(org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance());

        provider.setUserDetailsService(appUserService);
        return provider;
    }

    /**
     * CORS configuration source.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList("http://localhost:3000")); // Allow frontend origin
        configuration.setAllowCredentials(true); // Allow credentials (cookies, headers)
        configuration.addAllowedHeader("*"); // Allow all headers
        configuration.addAllowedMethod("*"); // Allow all methods (POST, GET, OPTIONS, etc.)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
