package com.resonate.api;

import com.resonate.domain.model.FanProfile;
import com.resonate.infrastructure.repository.FanProfileRepository;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
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
@Authenticated
@Tag(name = "Fan Profile", description = "Operations related to fan profiles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FanProfileResource {

    @Inject
    FanProfileRepository fanProfileRepository;

    @Inject
    SecurityIdentity securityIdentity;

    @POST
    @Transactional
    @Operation(summary = "Create or update fan profile", description = "Creates or updates the profile for the authenticated fan")
    @APIResponse(responseCode = "200", description = "Profile created or updated successfully")
    public Response createOrUpdateProfile(FanProfile profile) {
        UUID userId = UUID.fromString(securityIdentity.getPrincipal().getName());
        profile.setUserId(userId);
        fanProfileRepository.upsert(profile);
        return Response.ok(profile).build();
    }

    @GET
    @Operation(summary = "Get fan profile", description = "Retrieves the profile of the authenticated fan")
    @APIResponse(responseCode = "200", description = "Profile retrieved successfully")
    @APIResponse(responseCode = "404", description = "Fan profile not found")
    public Response getProfile() {
        UUID userId = UUID.fromString(securityIdentity.getPrincipal().getName());
        FanProfile profile = fanProfileRepository.findByUserId(userId);
        if (profile == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Fan profile not found").build();
        }
        return Response.ok(profile).build();
    }

    @DELETE
    @Transactional
    @Operation(summary = "Delete fan profile", description = "Deletes the profile of the authenticated fan")
    @APIResponse(responseCode = "204", description = "Profile deleted successfully")
    @APIResponse(responseCode = "404", description = "Fan profile not found")
    public Response deleteProfile() {
        UUID userId = UUID.fromString(securityIdentity.getPrincipal().getName());
        long deleted = fanProfileRepository.delete("userId", userId);
        if (deleted == 0L) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Fan profile not found").build();
        }
        return Response.noContent().build();
    }
}
