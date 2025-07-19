package com.nonononoki.alovoa.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.JWTVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);
    private final UserDetailsService userDetailsService;
    
    @Value("${supabase.url}")
    private String supabaseUrl;
    
    @Value("${supabase.key}")
    private String supabaseKey;

    public JwtFilter(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Check if already authenticated
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Check for JWT token in cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                // Check both JWT cookie names - our app's and Supabase's
                if ("JWT".equals(cookie.getName()) || "sb-access-token".equals(cookie.getName())) {
                    String jwtToken = cookie.getValue();
                    try {
                        // First try to decode the token without verification
                        DecodedJWT jwt = JWT.decode(jwtToken);
                        String userId = jwt.getSubject();
                        
                        logger.debug("Found JWT token for user ID: {}", userId);
                        
                        // Attempt to load user with this ID
                        try {
                            UserDetails userDetails = userDetailsService.loadUserByUsername(userId);
                            if (userDetails != null) {
                                UsernamePasswordAuthenticationToken authentication = 
                                    new UsernamePasswordAuthenticationToken(
                                        userDetails, 
                                        null, 
                                        userDetails.getAuthorities());
                                        
                                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                                logger.info("Successfully authenticated user: {}", userId);
                            }
                        } catch (Exception e) {
                            logger.error("Error loading user details for ID {}: {}", userId, e.getMessage());
                        }
                    } catch (JWTDecodeException e) {
                        logger.error("Invalid JWT token: {}", e.getMessage());
                        // If token is invalid, clear it
                        cookie.setMaxAge(0);
                        response.addCookie(cookie);
                    }
                }
            }
        }
        
        // Check for Authorization header (Bearer token)
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwtToken = authHeader.substring(7);
            try {
                DecodedJWT jwt = JWT.decode(jwtToken);
                String userId = jwt.getSubject();
                
                logger.debug("Found Bearer token for user ID: {}", userId);
                
                // Attempt to load user with this ID
                try {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(userId);
                    if (userDetails != null) {
                        UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(
                                userDetails, 
                                null, 
                                userDetails.getAuthorities());
                                
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        logger.info("Successfully authenticated user via Bearer token: {}", userId);
                    }
                } catch (Exception e) {
                    logger.error("Error loading user details for ID {}: {}", userId, e.getMessage());
                }
            } catch (JWTDecodeException e) {
                logger.error("Invalid Bearer token: {}", e.getMessage());
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
