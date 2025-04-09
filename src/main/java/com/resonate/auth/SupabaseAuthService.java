package com.resonate.auth;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;

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
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(supabaseUrl + "/auth/v1/signup"))
                .header("apikey", supabaseApiKey)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200 && response.statusCode() != 201) {
            Map<String, Object> errorResponse = objectMapper.readValue(response.body(), Map.class);
            throw new Exception(errorResponse.getOrDefault("message", "Registration failed").toString());
        }

        Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);
        Map<String, Object> user = (Map<String, Object>) responseBody.get("user");
        if (user == null || user.get("id") == null) {
            throw new Exception("Failed to extract user ID from response");
        }
        UUID userId = UUID.fromString(user.get("id").toString());
        // Extract the access token from the response (assuming the field is "access_token")
        String token = (String) responseBody.get("access_token");
        return new AuthResult(userId, token);
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
        Map<String, String> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(supabaseUrl + "/auth/v1/token?grant_type=password"))
                .header("apikey", supabaseApiKey)
                .header("Content-Type", MediaType.APPLICATION_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new UnauthorizedException("Invalid credentials");
        }

        Map<String, Object> responseBody = objectMapper.readValue(response.body(), Map.class);
        Map<String, Object> user = (Map<String, Object>) responseBody.get("user");
        if (user == null || user.get("id") == null) {
            throw new Exception("Failed to extract user ID from response");
        }
        UUID userId = UUID.fromString(user.get("id").toString());
        String token = (String) responseBody.get("access_token");
        return new AuthResult(userId, token);
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
