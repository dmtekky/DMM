package com.nonononoki.alovoa.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.nonononoki.alovoa.component.ExceptionHandler;
import com.nonononoki.alovoa.component.SupabaseClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.nonononoki.alovoa.Tools;
import com.nonononoki.alovoa.entity.User;
import com.nonononoki.alovoa.model.AlovoaException;
import com.nonononoki.alovoa.repo.UserRepository;

@Service
public class AuthService {

	@Autowired
	private UserRepository userRepo;
	
	@Autowired
	private SupabaseClient supabaseClient;

	private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

	/**
	 * Get the current user from the authentication context
	 * This method supports both traditional database users and Supabase users
	 * 
	 * @param throwExceptionWhenNull Whether to throw an exception when no user is found
	 * @return The current authenticated user
	 * @throws AlovoaException If user is not found and throwExceptionWhenNull is true
	 */
	public synchronized User getCurrentUser(boolean throwExceptionWhenNull) throws AlovoaException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth == null) {
			logger.debug("No authentication found in context");
			if (throwExceptionWhenNull) {
				throw new AlovoaException(ExceptionHandler.USER_NOT_FOUND);
			}
			return null;
		}
		
		// Get user identifier (could be email or Supabase UUID)
		String userIdentifier;
		if (auth.getPrincipal() instanceof User) {
			// If principal is a User entity, get email directly
			logger.debug("Principal is User entity");
			userIdentifier = ((User) auth.getPrincipal()).getEmail();
		} else {
			// Otherwise, it's a String which could be email or Supabase UUID
			userIdentifier = (String) auth.getPrincipal();
			logger.debug("Principal is String: {}", userIdentifier);
		}

		// Check if it's a Supabase UUID format
		boolean isUuid = isUuid(userIdentifier);
		User user = null;
		
		if (isUuid) {
			// For Supabase users, try to find existing user by Supabase ID first
			// This would require adding a supabaseId field to the User entity
			// For now, we'll just look up by email after getting user info from Supabase
			logger.debug("Looking up Supabase user with ID: {}", userIdentifier);
			try {
				JsonNode supabaseUser = supabaseClient.getUserById(userIdentifier);
				if (supabaseUser != null && supabaseUser.has("email")) {
					String email = supabaseUser.get("email").asText();
					logger.debug("Found Supabase user email: {}", email);
					user = userRepo.findByEmail(Tools.cleanEmail(email));
				}
			} catch (Exception e) {
				logger.error("Error fetching Supabase user: {}", e.getMessage());
			}
		} else {
			// For traditional users, look up by email directly
			logger.debug("Looking up user by email: {}", userIdentifier);
			user = userRepo.findByEmail(Tools.cleanEmail(userIdentifier));
		}
		
		if (user == null && throwExceptionWhenNull) {
			logger.warn("User not found for identifier: {}", userIdentifier);
			throw new AlovoaException(ExceptionHandler.USER_NOT_FOUND);
		}
		
		return user;
	}
	
	/**
	 * Convenience method that calls getCurrentUser(false)
	 */
	public User getCurrentUser() throws AlovoaException {
		return getCurrentUser(false);
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
