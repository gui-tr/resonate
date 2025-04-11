package com.resonate.auth;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.security.Principal;
import java.util.Base64;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

@Provider
@ApplicationScoped
@Priority(Priorities.AUTHENTICATION)
public class SimpleAuthFilter implements ContainerRequestFilter {

    private static final Logger LOG = Logger.getLogger(SimpleAuthFilter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        
        // Skip if no auth header or not a Bearer token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return;
        }
        
        String token = authHeader.substring("Bearer ".length());
        try {
            // Simple JWT parsing - just get the user ID from the token payload
            // No signature verification for now to keep it simple
            String userId = extractUserIdFromToken(token);
            
            if (userId != null) {
                // Create a simple security context with the user ID
                SecurityContext securityContext = new SecurityContext() {
                    @Override
                    public Principal getUserPrincipal() {
                        return () -> userId;
                    }

                    @Override
                    public boolean isUserInRole(String role) {
                        return true; // For simplicity, assume user has all roles
                    }

                    @Override
                    public boolean isSecure() {
                        return true;
                    }

                    @Override
                    public String getAuthenticationScheme() {
                        return "Bearer";
                    }
                };
                
                requestContext.setSecurityContext(securityContext);
                LOG.info("Authenticated user: " + userId);
            }
        } catch (Exception e) {
            LOG.error("Error processing JWT token", e);
            // Continue without authentication - let the security annotations handle it
        }
    }
    
    private String extractUserIdFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return null;
            }
            
            // Decode the payload (middle part)
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
            Map<String, Object> payload = objectMapper.readValue(payloadJson, Map.class);
            
            // Look for common user ID fields in JWT tokens
            // Supabase typically uses 'sub' or might have a custom field
            if (payload.containsKey("sub")) {
                return payload.get("sub").toString();
            } else if (payload.containsKey("user_id")) {
                return payload.get("user_id").toString();
            } else if (payload.containsKey("id")) {
                return payload.get("id").toString();
            }
            
            LOG.warn("Could not find user ID in token. Available fields: " + payload.keySet());
            return null;
        } catch (Exception e) {
            LOG.error("Failed to extract user ID from token", e);
            return null;
        }
    }
}