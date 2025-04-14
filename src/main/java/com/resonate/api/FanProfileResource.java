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
        fanProfileRepository.upsert(profile);
        return Response.ok(profile).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get fan profile")
    @APIResponse(responseCode = "200", description = "Profile retrieved successfully")
    @APIResponse(responseCode = "404", description = "Fan profile not found")
    public Response getProfile(@PathParam("id") UUID userId) {
        FanProfile profile = fanProfileRepository.findByUserId(userId);
        if (profile == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Fan profile not found").build();
        }
        return Response.ok(profile).build();
    }
}
