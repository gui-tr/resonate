package com.resonate.api;

import com.resonate.auth.SupabaseAuthService;
import com.resonate.auth.SupabaseAuthService.AuthResult;
import com.resonate.domain.model.ArtistProfile;
import com.resonate.domain.model.FanProfile;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Path("/api/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    private static final Logger logger = LoggerFactory.getLogger(AuthResource.class);

    @Inject
    SupabaseAuthService authService;

    @Inject
    EntityManager em;

    @POST
    @Path("/register")
    @Transactional
    public Response register(RegisterRequest request) {
        logger.info("Received registration request for email: {}, userType: {}", request.email, request.userType);
        try {
            if (request.email == null || request.email.isBlank() ||
                    request.password == null || request.password.isBlank() ||
                    request.userType == null || request.userType.isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("message", "Email, password, and user type are required"))
                        .build();
            }

            AuthResult authResult = authService.signUp(request.email, request.password);

            if (authResult == null) {
                logger.info("Email confirmation required for: {}", request.email);
                Map<String, Object> result = new HashMap<>();
                result.put("message", "Registration initiated. Please check your email for confirmation.");
                result.put("email_confirmation_required", true);
                result.put("userType", request.userType);
                return Response.status(Response.Status.ACCEPTED).entity(result).build();
            }

            UUID userId = authResult.userId;
            String token = authResult.token;
            logger.info("Successfully registered user with ID: {}", userId);

            if ("artist".equalsIgnoreCase(request.userType)) {
                logger.info("Creating artist profile for user: {}", userId);
                ArtistProfile profile = ArtistProfile.builder()
                        .userId(userId)
                        .biography(request.bio)
                        .build();
                em.persist(profile);
            } else if ("fan".equalsIgnoreCase(request.userType)) {
                logger.info("Creating fan profile for user: {}", userId);
                FanProfile profile = FanProfile.builder()
                        .userId(userId)
                        .subscriptionActive(false)
                        .build();
                em.persist(profile);
            } else {
                logger.error("Invalid user type: {}", request.userType);
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("message", "Invalid user type"))
                        .build();
            }

            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId);
            result.put("token", token);
            result.put("userType", request.userType);
            logger.info("Registration successful for user: {}", userId);
            return Response.status(Response.Status.CREATED).entity(result).build();
        } catch (Exception e) {
            logger.error("Registration failed: {}", e.getMessage(), e);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }

    @POST
    @Path("/login")
    public Response login(LoginRequest request) {
        logger.info("Received login request for email: {}", request.email);
        try {
            if (request.email == null || request.email.isBlank() ||
                    request.password == null || request.password.isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("message", "Email and password are required"))
                        .build();
            }
            AuthResult authResult = authService.signIn(request.email, request.password);
            Map<String, Object> result = new HashMap<>();
            result.put("userId", authResult.userId);
            result.put("token", authResult.token);
            String userType = determineUserType(authResult.userId);
            if (userType != null) {
                result.put("userType", userType);
            }
            logger.info("Login successful for user: {}", authResult.userId);
            return Response.ok(result).build();
        } catch (Exception e) {
            logger.error("Login failed: {}", e.getMessage(), e);
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("message", "Authentication failed"))
                    .build();
        }
    }

    @POST
    @Path("/logout")
    public Response logout() {
        logger.info("Received logout request");
        return Response.ok(Map.of("message", "Logged out successfully")).build();
    }

    @DELETE
    @Path("/user/{userId}")
    @Transactional
    public Response deleteUser(@PathParam("userId") UUID userId, @Context HttpHeaders headers) {
        logger.info("Received delete request for user: {}", userId);
        try {
            String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(Map.of("message", "Authentication required"))
                        .build();
            }
            String token = authHeader.substring("Bearer ".length());
            String userType = determineUserType(userId);
            if (userType == null) {
                logger.warn("No local profile found for user: {}", userId);
            } else {
                if ("artist".equals(userType)) {
                    em.createQuery("DELETE FROM ArtistProfile a WHERE a.userId = :userId")
                            .setParameter("userId", userId)
                            .executeUpdate();
                    logger.info("Deleted artist profile for user: {}", userId);
                } else if ("fan".equals(userType)) {
                    em.createQuery("DELETE FROM FanProfile f WHERE f.userId = :userId")
                            .setParameter("userId", userId)
                            .executeUpdate();
                    logger.info("Deleted fan profile for user: {}", userId);
                }
            }
            logger.info("User deleted successfully: {}", userId);
            return Response.ok(Map.of("message", "User deleted successfully")).build();
        } catch (Exception e) {
            logger.error("Failed to delete user: {}", e.getMessage(), e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("message", "Failed to delete user: " + e.getMessage()))
                    .build();
        }
    }

    private String determineUserType(UUID userId) {
        try {
            TypedQuery<Long> artistQuery = em.createQuery(
                    "SELECT COUNT(a) FROM ArtistProfile a WHERE a.userId = :userId", Long.class);
            artistQuery.setParameter("userId", userId);
            if (artistQuery.getSingleResult() > 0) {
                return "artist";
            }
            TypedQuery<Long> fanQuery = em.createQuery(
                    "SELECT COUNT(f) FROM FanProfile f WHERE f.userId = :userId", Long.class);
            fanQuery.setParameter("userId", userId);
            if (fanQuery.getSingleResult() > 0) {
                return "fan";
            }
            return null;
        } catch (NoResultException e) {
            return null;
        }
    }

    // Request DTOs
    public static class RegisterRequest {
        public String email;
        public String password;
        public String userType; // "artist" or "fan"
        public String bio;      // Optional for artist
    }

    public static class LoginRequest {
        public String email;
        public String password;
    }
}
