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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Path("/api/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

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
        try {
            // Call Supabase to register the user and get back the userId and token
            AuthResult authResult = authService.signUp(request.email, request.password);
            UUID userId = authResult.userId;
            String token = authResult.token;

            // Save the corresponding local profile based on the user type
            if ("artist".equalsIgnoreCase(request.userType)) {
                ArtistProfile profile = ArtistProfile.builder()
                        .userId(userId)
                        .biography(request.bio)
                        .build();
                em.persist(profile);
            } else if ("fan".equalsIgnoreCase(request.userType)) {
                FanProfile profile = FanProfile.builder()
                        .userId(userId)
                        .subscriptionActive(false)
                        .build();
                em.persist(profile);
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("message", "Invalid user type"))
                        .build();
            }

            // Build the response data
            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId);
            result.put("token", token);
            result.put("userType", request.userType);

            return Response.status(Response.Status.CREATED).entity(result).build();
        } catch (Exception e) {
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
        try {
            AuthResult authResult = authService.signIn(request.email, request.password);
            Map<String, Object> result = new HashMap<>();
            result.put("userId", authResult.userId);
            result.put("token", authResult.token);
            return Response.ok(result).build();
        } catch (Exception e) {
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
