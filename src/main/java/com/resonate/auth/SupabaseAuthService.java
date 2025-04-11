package com.resonate.auth;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.enterprise.context.ApplicationScoped;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class SupabaseAuthService {

    private static final Logger logger = LoggerFactory.getLogger(SupabaseAuthService.class);
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @ConfigProperty(name = "supabase.url")
    String supabaseUrl;

    @ConfigProperty(name = "supabase.apiKey")
    String supabaseApiKey;

    public static class AuthResult {
        public final UUID userId;
        public final String token;

        public AuthResult(UUID userId, String token) {
            this.userId = userId;
            this.token = token;
        }
    }

    /**
     * Signs up the user by calling Supabase's sign-up endpoint.
     * Expects Supabase to return a JSON payload containing a "user" object with an "id" field
     * and an "access_token" field.
     */
    public AuthResult signUp(String email, String password) {
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "email", email,
                    "password", password
            ));

            URI uri = new URI(supabaseUrl + "/auth/v1/signup");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("apikey", supabaseApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("Supabase sign-up response: {}", response.body());

            if (response.statusCode() != 200 && response.statusCode() != 201) {
                throw new RuntimeException("Sign-up failed: " + response.body());
            }

            Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
            Map<String, Object> userMap = (Map<String, Object>) result.get("user");
            String userIdStr = (String) userMap.get("id");
            UUID userId = UUID.fromString(userIdStr);
            String token = (String) result.get("access_token");

            logger.info("SignUp: Received user ID {} with token.", userId);
            return new AuthResult(userId, token);
        } catch (Exception e) {
            logger.error("Error during sign up", e);
            throw new RuntimeException("Sign up failed", e);
        }
    }

    /**
     * Signs in the user by calling Supabase's sign-in endpoint.
     * Expects a JSON payload with a "user" object containing "id" and an "access_token".
     */
    public AuthResult signIn(String email, String password) {
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "email", email,
                    "password", password
            ));

            URI uri = new URI(supabaseUrl + "/auth/v1/token?grant_type=password");
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header("apikey", supabaseApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("Supabase sign-in response: {}", response.body());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Sign-in failed: " + response.body());
            }

            Map<String, Object> result = objectMapper.readValue(response.body(), Map.class);
            Map<String, Object> userMap = (Map<String, Object>) result.get("user");
            String userIdStr = (String) userMap.get("id");
            UUID userId = UUID.fromString(userIdStr);
            String token = (String) result.get("access_token");

            logger.info("SignIn: Received user ID {} with token.", userId);
            return new AuthResult(userId, token);
        } catch (Exception e) {
            logger.error("Error during sign in", e);
            throw new RuntimeException("Sign in failed", e);
        }
    }
}
