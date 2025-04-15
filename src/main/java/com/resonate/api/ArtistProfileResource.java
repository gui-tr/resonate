package com.resonate.api;

import com.resonate.domain.model.ArtistProfile;
import com.resonate.infrastructure.repository.ArtistProfileRepository;
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
@Tag(name = "Artist Profile", description = "Operations related to artist profiles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ArtistProfileResource {

    @Inject
    ArtistProfileRepository artistProfileRepository;

    @POST
    @Operation(summary = "Create a new artist profile")
    @APIResponse(responseCode = "200", description = "Artist profile created successfully")
    @Transactional
    public Response createArtistProfile(ArtistProfile artistProfile) {
        try {
            artistProfileRepository.persist(artistProfile);
            return Response.ok(artistProfile).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to create artist profile: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Operation(summary = "Get current artist profile")
    @APIResponse(responseCode = "200", description = "Artist profile found")
    @APIResponse(responseCode = "404", description = "Artist profile not found")
    public Response getCurrentProfile() {
        ArtistProfile profile = artistProfileRepository.findByUserId(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        if (profile == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(profile).build();
    }

    @DELETE
    @Operation(summary = "Delete current artist profile")
    @APIResponse(responseCode = "200", description = "Artist profile deleted successfully")
    @APIResponse(responseCode = "404", description = "Artist profile not found")
    @Transactional
    public Response deleteCurrentProfile() {
        ArtistProfile profile = artistProfileRepository.findByUserId(UUID.fromString("00000000-0000-0000-0000-000000000000"));
        if (profile == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        artistProfileRepository.delete(profile);
        return Response.ok().build();
    }
}
