package com.resonate.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class ResonateExceptionMapper implements ExceptionMapper<ResonateException> {

    private static final Logger logger = LoggerFactory.getLogger(ResonateExceptionMapper.class);

    @Override
    public Response toResponse(ResonateException exception) {
        logger.error("Exception caught: {}", exception.getMessage(), exception);
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorResponse(exception.getMessage()))
                .build();
    }

    public static class ErrorResponse {
        public String message;
        public ErrorResponse(String message) {
            this.message = message;
        }
    }
}
