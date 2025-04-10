package com.resonate.auth;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.security.UnauthorizedException;

@ApplicationScoped
public class SupabaseAuthService {

    private static final Logger LOG = Logger.getLogger(SupabaseAuthService.class);

    @ConfigProperty(name = "supabase.url")
    String supabaseUrl;

    @ConfigProperty(name = "supabase.apiKey")
    String supabaseApiKey;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public SupabaseAuthService() {
        this.httpClient = HttpClient.newBuilder().build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Registers a new user via Supabase Auth.
     *
     * @param email    the user's email.
     * @param password the user's password.
     * @return an AuthResult containing the Supabase user ID and access token.
     * @throws Exception if registration fails.
     */
    public AuthResult signUp(String email, String password) throws Exception {
        LOG.info("Attempting to sign up user with email: " + email);
        
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        String requestBody = objectMapper.writeValueAsString(body);
        LOG.debug("Signup request body: " + requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(supabaseUrl + "/auth/v1/signup"))
                .header("apikey", supabaseApiKey)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        LOG.debug("Sending signup request to: " + supabaseUrl + "/auth/v1/signup");
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        LOG.info("Signup response status: " + response.statusCode());
        LOG.debug("Signup response body: " + response.body());
        
        if (response.statusCode() != 200 && response.statusCode() != 201) {
            try {
                Map<String, Object> errorResponse = objectMapper.readValue(response.body(), Map.class);
                String errorMessage = errorResponse.getOrDefault("message", "Registration failed").toString();
                LOG.error("Signup failed: " + errorMessage);
                throw new Exception(errorMessage);
            } catch (Exception e) {
                LOG.error("Failed to parse error response: " + e.getMessage());
                throw new Exception("Registration failed: " + response.body());
            }
        }

        try {
            Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);
            Map<String, Object> user = (Map<String, Object>) responseBody.get("user");
            if (user == null || user.get("id") == null) {
                LOG.error("Failed to extract user ID from response: " + response.body());
                throw new Exception("Failed to extract user ID from response");
            }
            UUID userId = UUID.fromString(user.get("id").toString());
            String token = (String) responseBody.get("access_token");
            if (token == null) {
                LOG.error("Failed to extract access token from response: " + response.body());
                throw new Exception("Failed to extract access token from response");
            }
            LOG.info("Successfully signed up user with ID: " + userId);
            return new AuthResult(userId, token);
        } catch (Exception e) {
            LOG.error("Error processing signup response: " + e.getMessage(), e);
            throw new Exception("Error processing signup response: " + e.getMessage());
        }
    }

    /**
     * Authenticates a user via Supabase Auth.
     *
     * @param email    the user's email.
     * @param password the user's password.
     * @return an AuthResult containing the user ID and access token.
     * @throws Exception if authentication fails.
     */
    public AuthResult signIn(String email, String password) throws Exception {
        LOG.info("Attempting to sign in user with email: " + email);
        
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        String requestBody = objectMapper.writeValueAsString(body);
        LOG.debug("Signin request body: " + requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(supabaseUrl + "/auth/v1/token?grant_type=password"))
                .header("apikey", supabaseApiKey)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        LOG.debug("Sending signin request to: " + supabaseUrl + "/auth/v1/token?grant_type=password");
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        LOG.info("Signin response status: " + response.statusCode());
        LOG.debug("Signin response body: " + response.body());
        
        if (response.statusCode() != 200) {
            LOG.error("Signin failed with status: " + response.statusCode() + ", body: " + response.body());
            throw new UnauthorizedException("Invalid credentials");
        }

        try {
            Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);
            Map<String, Object> user = (Map<String, Object>) responseBody.get("user");
            if (user == null || user.get("id") == null) {
                LOG.error("Failed to extract user ID from response: " + response.body());
                throw new Exception("Failed to extract user ID from response");
            }
            UUID userId = UUID.fromString(user.get("id").toString());
            String token = (String) responseBody.get("access_token");
            if (token == null) {
                LOG.error("Failed to extract access token from response: " + response.body());
                throw new Exception("Failed to extract access token from response");
            }
            LOG.info("Successfully signed in user with ID: " + userId);
            return new AuthResult(userId, token);
        } catch (Exception e) {
            LOG.error("Error processing signin response: " + e.getMessage(), e);
            throw new Exception("Error processing signin response: " + e.getMessage());
        }
    }

    public static class AuthResult {
        public final UUID userId;
        public final String token;

        public AuthResult(UUID userId, String token) {
            this.userId = userId;
            this.token = token;
        }
    }
}
