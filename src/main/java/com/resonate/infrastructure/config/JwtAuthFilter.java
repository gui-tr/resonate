//package com.resonate.infrastructure.config;
//
//import io.quarkus.security.identity.SecurityIdentity;
//import jakarta.annotation.Priority;
//import jakarta.inject.Inject;
//import jakarta.ws.rs.Priorities;
//import jakarta.ws.rs.container.ContainerRequestContext;
//import jakarta.ws.rs.container.ContainerRequestFilter;
//import jakarta.ws.rs.ext.Provider;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import java.io.IOException;
//
//@Provider
//@Priority(Priorities.AUTHENTICATION)
//public class JwtAuthFilter implements ContainerRequestFilter {
//
//    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);
//
//    @Inject
//    SecurityIdentity securityIdentity;
//
//    @Override
//    public void filter(ContainerRequestContext requestContext) throws IOException {
//        String authHeader = requestContext.getHeaderString("Authorization");
//        if (authHeader != null && authHeader.startsWith("Bearer ")) {
//            String token = authHeader.substring("Bearer ".length());
//            // Simplified token validation: In production, use a JWT library to validate the token signature and expiration.
//            if (isValidToken(token)) {
//                logger.info("Token validated successfully.");
//                // Here you would set up the SecurityContext accordingly.
//            } else {
//                logger.warn("Invalid JWT token.");
//                requestContext.abortWith(
//                        jakarta.ws.rs.core.Response.status(jakarta.ws.rs.core.Response.Status.UNAUTHORIZED)
//                                .entity("Invalid token").build());
//            }
//        }
//    }
//
//    private boolean isValidToken(String token) {
//        // For example purposes, consider a token valid if it ends with "-token".
//        return token.endsWith("-token");
//    }
//}
