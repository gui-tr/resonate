package com.resonate.api;

import com.resonate.auth.SupabaseAuthService;
import com.resonate.auth.SupabaseAuthService.AuthResult;
import com.resonate.util.TestUtil;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import io.quarkus.test.security.TestSecurity;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@QuarkusTest
public class AuthResourceTest {

    private final String basePath = "/api/auth";

    @InjectMock
    SupabaseAuthService supabaseAuthService;

    @Test
    public void testRegisterArtist() throws Exception {
        // Use fixed test UUID and token for artist registration.
        when(supabaseAuthService.signUp(anyString(), anyString()))
                .thenReturn(new AuthResult(TestUtil.ARTIST_UUID, "mock-artist-token"));

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
                // Compare the returned userId with our fixed constant as a string.
                .body("userId", equalTo(TestUtil.ARTIST_ID_STRING))
                .body("token", equalTo("mock-artist-token"))
                .body("userType", equalTo("artist"));
    }

    @Test
    public void testRegisterFan() throws Exception {
        // Use fixed test UUID and token for fan registration.
        when(supabaseAuthService.signUp(anyString(), anyString()))
                .thenReturn(new AuthResult(TestUtil.FAN_UUID, "mock-fan-token"));

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
                .body("userId", equalTo(TestUtil.FAN_ID_STRING))
                .body("token", equalTo("mock-fan-token"))
                .body("userType", equalTo("fan"));
    }

    @Test
    public void testRegisterWithInvalidData() throws Exception {
        // Missing required fields: expect a 400 Bad Request.
        AuthResource.RegisterRequest request = new AuthResource.RegisterRequest();
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
        // Use a fixed UUID and token for the login endpoint.
        when(supabaseAuthService.signIn(anyString(), anyString()))
                .thenReturn(new AuthResult(TestUtil.ARTIST_UUID, "mock-login-token"));

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
                .body("userId", equalTo(TestUtil.ARTIST_ID_STRING))
                .body("token", equalTo("mock-login-token"));
    }

    @Test
    public void testLoginWithInvalidCredentials() throws Exception {
        when(supabaseAuthService.signIn(anyString(), anyString()))
                .thenThrow(new io.quarkus.security.UnauthorizedException("Authentication failed"));

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
                .body("message", equalTo("Authentication failed"));
    }

    @Test
    @TestSecurity(user = "test-user", roles = {"user"})
    public void testLogout() {
        given()
                .contentType(ContentType.JSON)
                .when()
                .post(basePath + "/logout")
                .then()
                .statusCode(200)
                .body("message", equalTo("Logged out successfully"));
    }
}
