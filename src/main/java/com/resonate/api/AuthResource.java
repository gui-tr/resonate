package com.resonate.api;

import com.resonate.auth.SupabaseAuthService;
import com.resonate.auth.SupabaseAuthService.AuthResult;
import com.resonate.domain.model.ArtistProfile;
import com.resonate.domain.model.FanProfile;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Path("/api/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    private static final Logger LOG = Logger.getLogger(AuthResource.class);

    @Inject
    SupabaseAuthService authService;

    @Inject
    EntityManager em;

    /**
     * Registers a new user.
     * Based on the 'userType' field, it creates either an artist profile or a fan profile.
     */
    @POST
    @Path("/register")
    @Transactional
    public Response register(RegisterRequest request) {
        LOG.info("Received registration request for email: " + request.email + ", userType: " + request.userType);
        
        try {
            // Call Supabase to register the user and get back the userId and token
            AuthResult authResult = authService.signUp(request.email, request.password);
            UUID userId = authResult.userId;
            String token = authResult.token;
            
            LOG.info("Successfully registered user with ID: " + userId);

            // Save the corresponding local profile based on the user type
            if ("artist".equalsIgnoreCase(request.userType)) {
                LOG.info("Creating artist profile for user: " + userId);
                ArtistProfile profile = ArtistProfile.builder()
                        .userId(userId)
                        .biography(request.bio)
                        .build();
                em.persist(profile);
            } else if ("fan".equalsIgnoreCase(request.userType)) {
                LOG.info("Creating fan profile for user: " + userId);
                FanProfile profile = FanProfile.builder()
                        .userId(userId)
                        .subscriptionActive(false)
                        .build();
                em.persist(profile);
            } else {
                LOG.error("Invalid user type: " + request.userType);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("message", "Invalid user type"))
                        .build();
            }

            // Build the response data
            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId);
            result.put("token", token);
            result.put("userType", request.userType);

            LOG.info("Registration successful for user: " + userId);
            return Response.status(Response.Status.CREATED).entity(result).build();
        } catch (Exception e) {
            LOG.error("Registration failed: " + e.getMessage(), e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }

    /**
     * Authenticates a user.
     */
    @POST
    @Path("/login")
    public Response login(LoginRequest request) {
        LOG.info("Received login request for email: " + request.email);
        
        try {
            AuthResult authResult = authService.signIn(request.email, request.password);
            
            Map<String, Object> result = new HashMap<>();
            result.put("userId", authResult.userId);
            result.put("token", authResult.token);
            
            LOG.info("Login successful for user: " + authResult.userId);
            return Response.ok(result).build();
        } catch (Exception e) {
            LOG.error("Login failed: " + e.getMessage(), e);
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }

    /**
     * Logs the user out.
     * (Note: In JWT-based systems, logout is typically a client-side operation.)
     */
    @POST
    @Path("/logout")
    public Response logout() {
        LOG.info("Received logout request");
        return Response.ok(Map.of("message", "Logged out successfully")).build();
    }

    // Request DTOs
    public static class RegisterRequest {
        public String email;
        public String password;
        public String userType; // e.g., "artist" or "fan"
        public String bio;      // Only applicable for artist registrations (optional for fan)
    }

    public static class LoginRequest {
        public String email;
        public String password;
    }
}
