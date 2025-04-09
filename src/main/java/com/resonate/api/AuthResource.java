package com.resonate.api;

import io.quarkus.security.UnauthorizedException;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.resonate.domain.model.ArtistProfile;
import com.resonate.domain.model.FanProfile;
import com.resonate.infrastructure.repository.ArtistProfileRepository;
import com.resonate.infrastructure.repository.FanProfileRepository;
import com.resonate.auth.SupabaseAuthService;
import io.smallrye.jwt.build.Jwt;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    ArtistProfileRepository artistProfileRepository;

    @Inject
    FanProfileRepository fanProfileRepository;

    @Inject
    SupabaseAuthService supabaseAuthService;

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @POST
    @Path("/register")
    @Transactional
    public Response register(RegisterRequest request) {
        try {
            // Validate request
            if (request == null || request.email == null || request.password == null || request.userType == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Email, password and userType are required")
                        .build();
            }

            // Register with Supabase Auth
            UUID userId = supabaseAuthService.signUp(request.email, request.password);

            // Create profile based on user type
            if ("artist".equals(request.userType)) {
                ArtistProfile artistProfile = ArtistProfile.builder()
                        .userId(userId)
                        .biography(request.bio)
                        .socialLinks("{}")
                        .build();
                artistProfileRepository.persist(artistProfile);
            } else {
                FanProfile fanProfile = FanProfile.builder()
                        .userId(userId)
                        .subscriptionActive(false)
                        .build();
                fanProfileRepository.persist(fanProfile);
            }

            // Generate JWT token
            String token = generateToken(userId.toString(), request.userType);

            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId.toString());
            result.put("token", token);
            result.put("userType", request.userType);

            return Response.status(Response.Status.CREATED).entity(result).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Registration failed: " + e.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/login")
    public Response login(LoginRequest request) {
        try {
            // Validate request
            if (request == null || request.email == null || request.password == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Email and password are required")
                        .build();
            }

            // Authenticate with Supabase
            UUID userId = supabaseAuthService.signIn(request.email, request.password);
            if (userId == null) {
                throw new UnauthorizedException("Invalid credentials");
            }

            // Determine user type by checking if profiles exist
            String userType = "fan"; // default
            if (artistProfileRepository.findByUserId(userId) != null) {
                userType = "artist";
            }

            // Generate JWT token
            String token = generateToken(userId.toString(), userType);

            Map<String, Object> result = new HashMap<>();
            result.put("userId", userId.toString());
            result.put("token", token);
            result.put("userType", userType);

            return Response.ok(result).build();
        } catch (UnauthorizedException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Authentication failed: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred: " + e.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/logout")
    public Response logout() {
        try {
            // No server-side state to clear with JWT
            return Response.ok().entity(Map.of("message", "Logged out successfully")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Logout failed: " + e.getMessage())
                    .build();
        }
    }

    private String generateToken(String subject, String userType) {
        return Jwt.issuer(issuer)
                .subject(subject)
                .groups(new HashSet<>(Arrays.asList("user", userType)))
                .expiresIn(Duration.ofHours(24))
                .sign();
    }

    public static class RegisterRequest {
        public String email;
        public String password;
        public String userType; // "artist" or "fan"
        public String bio; // for artists
    }

    public static class LoginRequest {
        public String email;
        public String password;
    }
}