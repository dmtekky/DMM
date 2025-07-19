package com.nonononoki.alovoa.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple client for interacting with Supabase REST API
 */
@Component
public class SupabaseClient {
    private static final Logger logger = LoggerFactory.getLogger(SupabaseClient.class);
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${supabase.url}")
    private String supabaseUrl;
    
    @Value("${supabase.key}")
    private String supabaseKey;
    
    public SupabaseClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Get user info from a JWT token
     * 
     * @param token JWT token from Supabase
     * @return JsonNode containing user data or null if not found/error
     */
    public JsonNode getUserByToken(String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.set("apikey", supabaseKey);
            
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                supabaseUrl + "/auth/v1/user", 
                HttpMethod.GET, 
                entity, 
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return objectMapper.readTree(response.getBody());
            } else {
                logger.error("Failed to get user by token: {}", response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            logger.error("Error getting user by token: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Get user by their ID
     * 
     * @param userId User's UUID
     * @return JsonNode containing user data or null if not found/error
     */
    public JsonNode getUserById(String userId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", supabaseKey);
            headers.set("Authorization", "Bearer " + supabaseKey);
            
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                supabaseUrl + "/auth/v1/admin/users/" + userId, 
                HttpMethod.GET, 
                entity, 
                String.class
            );
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return objectMapper.readTree(response.getBody());
            } else {
                logger.error("Failed to get user by ID {}: {}", userId, response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            logger.error("Error getting user by ID {}: {}", userId, e.getMessage());
            return null;
        }
    }
    
    /**
     * Verify if a token is valid
     * 
     * @param token JWT token to verify
     * @return true if token is valid, false otherwise
     */
    public boolean verifyToken(String token) {
        try {
            JsonNode user = getUserByToken(token);
            return user != null && user.has("id");
        } catch (Exception e) {
            logger.error("Error verifying token: {}", e.getMessage());
            return false;
        }
    }

}
