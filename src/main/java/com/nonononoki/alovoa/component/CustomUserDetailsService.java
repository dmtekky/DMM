package com.nonononoki.alovoa.component;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.nonononoki.alovoa.config.SecurityConfig;
import com.nonononoki.alovoa.entity.User;
import com.nonononoki.alovoa.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;
    
    @Value("${supabase.url}")
    private String supabaseUrl;
    
    @Value("${supabase.key}")
    private String supabaseKey;

    @Override
    public UserDetails loadUserByUsername(String userIdOrEmail) throws UsernameNotFoundException {
        logger.debug("Loading user details for: {}", userIdOrEmail);
        
        // First check if this is a Supabase user ID (UUID format)
        if (isUuid(userIdOrEmail)) {
            logger.info("Detected Supabase UUID format: {}", userIdOrEmail);
            // For Supabase users, we create a UserDetails with empty password and basic authorities
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority(SecurityConfig.ROLE_USER));
            
            // Create a Spring Security User (not our application User entity)
            return new org.springframework.security.core.userdetails.User(
                userIdOrEmail, 
                "", // empty password since auth is handled by Supabase
                authorities
            );
        }
        
        // Fall back to database lookup by email
        try {
            logger.debug("Looking up user by email in database: {}", userIdOrEmail);
            User user = userRepository.findByEmail(userIdOrEmail);
            
            if (user != null) {
                // Return our application's User entity which implements UserDetails
                return user;
            }
            
            // Try by ID if email lookup failed
            try {
                Long userId = Long.parseLong(userIdOrEmail);
                user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    return user;
                }
            } catch (NumberFormatException e) {
                // Not a numeric ID, ignore
            }
        } catch (Exception e) {
            logger.error("Error finding user in database: {}", e.getMessage());
        }
        
        throw new UsernameNotFoundException("User not found: " + userIdOrEmail);
    }
    
    /**
     * Simple check to see if a string appears to be a UUID format which
     * Supabase uses for user IDs
     */
    private boolean isUuid(String str) {
        if (str == null || str.length() != 36) {
            return false;
        }
        
        // Check for UUID format: 8-4-4-4-12 hex digits
        String regex = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";
        return str.toLowerCase().matches(regex);
    }
}