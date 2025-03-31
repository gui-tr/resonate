package com.resonate.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ResonateExceptionMapperTest {

    @Test
    public void testToResponse() {
        ResonateException ex = new ResonateException("Test error occurred", 1001);
        ResonateExceptionMapper mapper = new ResonateExceptionMapper();
        Response response = mapper.toResponse(ex);

        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());

        Object entity = response.getEntity();
        assertNotNull(entity);
        String entityStr = entity.toString();
        assertTrue(entityStr.contains("Test error occurred"), "Entity should contain the error message");
        assertTrue(entityStr.contains("1001"), "Entity should contain the error code");
        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
    }
}
