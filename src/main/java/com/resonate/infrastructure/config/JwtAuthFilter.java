package com.resonate.infrastructure.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.security.Principal;

@Provider
@ApplicationScoped
public class JwtAuthFilter implements ContainerRequestFilter {

    private static final Logger LOG = Logger.getLogger(JwtAuthFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getPath();
        LOG.debug("Processing request for path: " + path);
        
        // Skip authentication for login and register endpoints
        if (path.equals("/api/auth/login") || path.equals("/api/auth/register")) {
            LOG.debug("Skipping authentication for public endpoint: " + path);
            return;
        }
        
        String authHeader = requestContext.getHeaderString("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            LOG.debug("Found Bearer token, setting security context");
            
            // Create a simple principal with the token
            SecurityContext securityContext = new SecurityContext() {
                @Override
                public Principal getUserPrincipal() {
                    return () -> "user";
                }

                @Override
                public boolean isUserInRole(String role) {
                    return true;
                }

                @Override
                public boolean isSecure() {
                    return requestContext.getSecurityContext().isSecure();
                }

                @Override
                public String getAuthenticationScheme() {
                    return "Bearer";
                }
            };
            requestContext.setSecurityContext(securityContext);
        } else {
            LOG.debug("No Authorization header found for path: " + path);
        }
    }
} 