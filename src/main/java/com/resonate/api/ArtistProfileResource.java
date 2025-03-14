package com.resonate.api;

import com.resonate.domain.model.ArtistProfile;
import com.resonate.infrastructure.repository.ArtistProfileRepository;
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

@Path("/api/artist-profiles")
@Authenticated
@Tag(name = "Artist Profile", description = "Operations related to artist profiles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ArtistProfileResource {

    @Inject
    ArtistProfileRepository artistProfileRepository;

    @Inject
    SecurityIdentity securityIdentity;

    @POST
    @Transactional
    @Operation(summary = "Create or update artist profile", description = "Creates or updates the profile for the authenticated artist")
    @APIResponse(responseCode = "200", description = "Profile created or updated successfully")
    public Response createOrUpdateProfile(ArtistProfile profile) {
        UUID userId = UUID.fromString(securityIdentity.getPrincipal().getName());
        profile.setUserId(userId);
        artistProfileRepository.upsert(profile);
        return Response.ok(profile).build();
    }

    @GET
    @Operation(summary = "Get artist profile", description = "Retrieves the profile of the authenticated artist")
    @APIResponse(responseCode = "200", description = "Profile retrieved successfully")
    @APIResponse(responseCode = "404", description = "Artist profile not found")
    public Response getProfile() {
        UUID userId = UUID.fromString(securityIdentity.getPrincipal().getName());
        ArtistProfile profile = artistProfileRepository.findByUserId(userId);
        if (profile == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Artist profile not found").build();
        }
        return Response.ok(profile).build();
    }

    @DELETE
    @Transactional
    @Operation(summary = "Delete artist profile", description = "Deletes the profile of the authenticated artist")
    @APIResponse(responseCode = "204", description = "Profile deleted successfully")
    @APIResponse(responseCode = "404", description = "Artist profile not found")
    public Response deleteProfile() {
        UUID userId = UUID.fromString(securityIdentity.getPrincipal().getName());
        long deleted = artistProfileRepository.delete("userId", userId);
        if (deleted == 0L) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Artist profile not found").build();
        }
        return Response.noContent().build();
    }
}
