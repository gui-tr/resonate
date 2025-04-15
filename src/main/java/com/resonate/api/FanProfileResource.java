package com.resonate.api;

import com.resonate.domain.model.FanProfile;
import com.resonate.infrastructure.repository.FanProfileRepository;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.UUID;

@Path("/api/fan-profiles")
@Tag(name = "Fan Profile", description = "Operations related to fan profiles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FanProfileResource {

    @Inject
    FanProfileRepository fanProfileRepository;

    @POST
    @Transactional
    @Operation(summary = "Create or update fan profile")
    @APIResponse(responseCode = "200", description = "Profile created or updated successfully")
    public Response createOrUpdateProfile(FanProfile profile) {
        try {
            fanProfileRepository.upsert(profile);
            return Response.ok(profile).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to create/update fan profile: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Operation(summary = "Get current fan profile")
    @APIResponse(responseCode = "200", description = "Profile retrieved successfully")
    @APIResponse(responseCode = "404", description = "Fan profile not found")
    public Response getCurrentProfile() {
        FanProfile profile = fanProfileRepository.findByUserId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        if (profile == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(profile).build();
    }

    @DELETE
    @Operation(summary = "Delete current fan profile")
    @APIResponse(responseCode = "200", description = "Profile deleted successfully")
    @APIResponse(responseCode = "404", description = "Fan profile not found")
    @Transactional
    public Response deleteCurrentProfile() {
        FanProfile profile = fanProfileRepository.findByUserId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        if (profile == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        fanProfileRepository.delete(profile);
        return Response.ok().build();
    }
}
