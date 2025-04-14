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
        artistProfileRepository.persist(artistProfile);
        return Response.ok(artistProfile).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get an artist profile by ID")
    @APIResponse(responseCode = "200", description = "Artist profile found")
    @APIResponse(responseCode = "404", description = "Artist profile not found")
    public Response getArtistProfile(@PathParam("id") UUID id) {
        ArtistProfile profile = artistProfileRepository.findByUserId(id);
        if (profile == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(profile).build();
    }

    @PUT
    @Path("/{id}")
    @Operation(summary = "Update an artist profile")
    @APIResponse(responseCode = "200", description = "Artist profile updated successfully")
    @APIResponse(responseCode = "404", description = "Artist profile not found")
    @Transactional
    public Response updateArtistProfile(@PathParam("id") UUID id, ArtistProfile artistProfile) {
        ArtistProfile existingProfile = artistProfileRepository.findByUserId(id);
        if (existingProfile == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        artistProfile.setUserId(id);
        artistProfileRepository.persist(artistProfile);
        return Response.ok(artistProfile).build();
    }
}
