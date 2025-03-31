package com.resonate.api;

import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Path("/api/audio-files")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AudioFileResource {

    @Inject
    SecurityIdentity securityIdentity;


    @GET
    @Path("/upload")
    @Operation(summary = "Generate a signed URL for file upload", description = "Returns a signed URL that allows the client to directly upload a file to Backblaze B2")
    @APIResponse(responseCode = "200", description = "Signed URL generated successfully")
    public Response getSignedUploadUrl(@QueryParam("fileName") String fileName) {

        // temp before connecting to real backblaze api
        String bucketUrl = "https://f001.backblazeb2.com/file/mybucket/";

        // Simulate a signed URL valid for 10 minutes.
        Instant expiry = Instant.now().plus(Duration.ofMinutes(10));
        String signedUrl = bucketUrl + fileName + "?Authorization=mockedSignedToken&Expires=" + expiry.toEpochMilli();

        Map<String, Object> result = new HashMap<>();
        result.put("signedUrl", signedUrl);
        result.put("expiresAt", expiry.toString());
        return Response.ok(result).build();
    }

    // Additional endpoints for storing metadata after upload will be added here

}
