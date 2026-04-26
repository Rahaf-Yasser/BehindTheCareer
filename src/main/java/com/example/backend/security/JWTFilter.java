package com.example.backend.security;

import com.example.backend.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JWTFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    // @RequiredArgsConstructor already creates the constructor, so we don't need to
    // add one
    // The log will be added in a @PostConstruct method instead

    @jakarta.annotation.PostConstruct
    public void init() {
        log.info("🚀 JWTFilter BEAN CREATED and registered in Spring context!");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        log.info("🔍 JWTFilter EXECUTING for URI: {}", request.getRequestURI());
        log.info("Request Method: {}", request.getMethod());

        final String authHeader = request.getHeader("Authorization");
        log.info("Authorization header present: {}", authHeader != null);

        if (authHeader != null) {
            log.info("Auth header starts with Bearer: {}", authHeader.startsWith("Bearer "));
            log.info("Auth header length: {}", authHeader.length());
            log.info("Auth header first 20 chars: {}",
                    authHeader.length() > 20 ? authHeader.substring(0, 20) + "..." : authHeader);
        }

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("No Bearer token found in Authorization header");
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        log.info("JWT Token extracted: {}...", jwt.substring(0, Math.min(20, jwt.length())));

        try {
            final String userEmail = jwtUtil.extractUsername(jwt);
            log.info("Username from token: {}", userEmail);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                log.info("UserDetails loaded: {}", userDetails.getUsername());

                if (jwtUtil.isTokenValid(jwt, userDetails)) {
                    log.info("✅ Token is valid for user: {}", userEmail);
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.info("✅ Authentication set in SecurityContext");
                } else {
                    log.warn("❌ Token is NOT valid for user: {}", userEmail);
                }
            }
        } catch (Exception e) {
            log.error("JWT validation error: {}", e.getMessage());
            log.error("Exception type: {}", e.getClass().getName());
        }

        filterChain.doFilter(request, response);
    }
}