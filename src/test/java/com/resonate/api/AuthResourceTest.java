package com.resonate.api;

import com.resonate.auth.SupabaseAuthService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
public class AuthResourceTest {

    private final String basePath = "/api/auth";

    @InjectMock
    SupabaseAuthService supabaseAuthService;

    @Test
    public void testRegisterArtist() throws Exception {
        UUID mockUserId = UUID.randomUUID();
        when(supabaseAuthService.signUp(anyString(), anyString())).thenReturn(mockUserId);

        AuthResource.RegisterRequest request = new AuthResource.RegisterRequest();
        request.email = "test-artist@example.com";
        request.password = "password123";
        request.userType = "artist";
        request.bio = "Test artist biography";

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(basePath + "/register")
                .then()
                .statusCode(201)
                .body("userId", notNullValue())
                .body("token", notNullValue())
                .body("userType", equalTo("artist"));
    }

    @Test
    public void testRegisterFan() throws Exception {
        UUID mockUserId = UUID.randomUUID();
        when(supabaseAuthService.signUp(anyString(), anyString())).thenReturn(mockUserId);

        AuthResource.RegisterRequest request = new AuthResource.RegisterRequest();
        request.email = "test-fan@example.com";
        request.password = "password123";
        request.userType = "fan";

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(basePath + "/register")
                .then()
                .statusCode(201)
                .body("userId", notNullValue())
                .body("token", notNullValue())
                .body("userType", equalTo("fan"));
    }

    @Test
    public void testRegisterWithInvalidData() throws Exception {
        AuthResource.RegisterRequest request = new AuthResource.RegisterRequest();
        // Missing required fields

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(basePath + "/register")
                .then()
                .statusCode(400);
    }

    @Test
    public void testLogin() throws Exception {
        UUID mockUserId = UUID.randomUUID();
        when(supabaseAuthService.signIn(anyString(), anyString())).thenReturn(mockUserId);

        AuthResource.LoginRequest request = new AuthResource.LoginRequest();
        request.email = "existing@example.com";
        request.password = "password123";

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(basePath + "/login")
                .then()
                .statusCode(200)
                .body("userId", notNullValue())
                .body("token", notNullValue());
    }

    @Test
    public void testLoginWithInvalidCredentials() throws Exception {
        when(supabaseAuthService.signIn(anyString(), anyString()))
                .thenThrow(new io.quarkus.security.UnauthorizedException("Invalid credentials"));

        AuthResource.LoginRequest request = new AuthResource.LoginRequest();
        request.email = "wrong@example.com";
        request.password = "wrongpassword";

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post(basePath + "/login")
                .then()
                .statusCode(401)
                .body("$", containsString("Invalid credentials"));
    }

    @Test
    public void testLogout() {
        given()
                .when()
                .post(basePath + "/logout")
                .then()
                .statusCode(200)
                .body("message", equalTo("Logged out successfully"));
    }
}
