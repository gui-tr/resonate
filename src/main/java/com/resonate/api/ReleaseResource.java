package com.resonate.api;

import com.resonate.domain.model.Release;
import com.resonate.infrastructure.repository.ReleaseRepository;
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

@Path("/api/releases")
@Authenticated
@Tag(name = "Release", description = "Operations related to music releases")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReleaseResource {

    @Inject
    ReleaseRepository releaseRepository;

    @Inject
    SecurityIdentity securityIdentity;

    @POST
    @Transactional
    @Operation(summary = "Create a release", description = "Creates a new music release for the authenticated artist")
    @APIResponse(responseCode = "201", description = "Release created successfully")
    public Response createRelease(Release release) {
        UUID artistId = UUID.fromString(securityIdentity.getPrincipal().getName());
        release.setArtistId(artistId);
        releaseRepository.persist(release);
        return Response.status(Response.Status.CREATED).entity(release).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get release", description = "Retrieves a release by its ID")
    @APIResponse(responseCode = "200", description = "Release retrieved successfully")
    @APIResponse(responseCode = "404", description = "Release not found")
    public Response getRelease(@PathParam("id") Long id) {
        Release release = releaseRepository.findById(id);
        if (release == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Release not found").build();
        }
        return Response.ok(release).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Update release", description = "Updates an existing release for the authenticated artist")
    @APIResponse(responseCode = "200", description = "Release updated successfully")
    @APIResponse(responseCode = "403", description = "Not authorized to update this release")
    @APIResponse(responseCode = "404", description = "Release not found")
    public Response updateRelease(@PathParam("id") Long id, Release updatedRelease) {
        Release release = releaseRepository.findById(id);
        if (release == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Release not found").build();
        }
        UUID userId = UUID.fromString(securityIdentity.getPrincipal().getName());
        if (!release.getArtistId().equals(userId)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Not authorized to update this release").build();
        }
        release.setTitle(updatedRelease.getTitle());
        release.setReleaseDate(updatedRelease.getReleaseDate());
        release.setUpc(updatedRelease.getUpc());
        releaseRepository.persist(release);
        return Response.ok(release).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Delete release", description = "Deletes a release for the authenticated artist")
    @APIResponse(responseCode = "204", description = "Release deleted successfully")
    @APIResponse(responseCode = "403", description = "Not authorized to delete this release")
    @APIResponse(responseCode = "404", description = "Release not found")
    public Response deleteRelease(@PathParam("id") Long id) {
        Release release = releaseRepository.findById(id);
        if (release == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Release not found").build();
        }
        UUID userId = UUID.fromString(securityIdentity.getPrincipal().getName());
        if (!release.getArtistId().equals(userId)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Not authorized to delete this release").build();
        }
        releaseRepository.delete(release);
        return Response.noContent().build();
    }
}
