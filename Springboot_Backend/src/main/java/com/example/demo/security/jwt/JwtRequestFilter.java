package com.example.demo.security.jwt;


import com.example.demo.service.AppUserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtil; // JwtUtil is the utility class to generate/validate JWT tokens

    private final AppUserService appUserService; // Service to load user details based on token

    public JwtRequestFilter(JwtUtils jwtUtil, AppUserService appUserService) {
        this.jwtUtil = jwtUtil;
        this.appUserService = appUserService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Get the Authorization header
        String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // If the header is in the form of "Bearer token"
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7); // Extract token
            try {
                username = jwtUtil.extractUsername(jwt); // Extract username
            } catch (Exception e) {
                logger.error("Invalid token: " + e.getMessage());
            }
        }

        // If the token is valid and not already authenticated, authenticate the user
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null && jwtUtil.validateToken(jwt, appUserService.loadUserByUsername(username))) {
                // If the token is valid, set the authentication in the security context
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(username, null, appUserService.loadUserByUsername(username).getAuthorities())
                );
            }


        // Proceed with the request
        filterChain.doFilter(request, response);
    }
}

