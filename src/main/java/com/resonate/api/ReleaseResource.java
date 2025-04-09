package com.resonate.api;

import com.resonate.domain.model.Release;
import com.resonate.domain.model.Track;
import com.resonate.infrastructure.repository.ReleaseRepository;
import com.resonate.infrastructure.repository.TrackRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.annotation.security.PermitAll;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/api/releases")
@Tag(name = "Release", description = "Operations related to music releases")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReleaseResource {

    @Inject
    ReleaseRepository releaseRepository;

    @Inject
    TrackRepository trackRepository;

    @Inject
    SecurityIdentity securityIdentity;

    @POST
    @Authenticated
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
    @Authenticated
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
    @Authenticated
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
    @Authenticated
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

    @GET
    @Path("/public")
    @PermitAll  // Allow public access without authentication
    @Operation(summary = "List all releases", description = "Returns a list of all published releases")
    @APIResponse(responseCode = "200", description = "List of releases retrieved successfully")
    public Response getAllReleases(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {

        // Implement pagination
        List<Release> releaseList = releaseRepository.findAll(Sort.by("releaseDate").descending())
                .page(Page.of(page, size))
                .list();

        return Response.ok(releaseList).build();
    }

    @GET
    @Path("/public/{id}")
    @PermitAll  // Allow public access without authentication
    @Operation(summary = "Get release details", description = "Returns detailed information about a specific release including its tracks")
    @APIResponse(responseCode = "200", description = "Release retrieved successfully")
    @APIResponse(responseCode = "404", description = "Release not found")
    public Response getPublicReleaseDetails(@PathParam("id") Long id) {
        Release release = releaseRepository.findById(id);
        if (release == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Release not found").build();
        }

        // Load tracks eagerly
        List<Track> tracks = trackRepository.find("release.id", id).list();

        // Create a response DTO with release and tracks
        Map<String, Object> result = new HashMap<>();
        result.put("release", release);
        result.put("tracks", tracks);

        return Response.ok(result).build();
    }

    @GET
    @Authenticated
    @Operation(summary = "Get artist releases", description = "Returns all releases for the authenticated artist")
    @APIResponse(responseCode = "200", description = "List of artist releases retrieved successfully")
    public Response getArtistReleases() {
        UUID artistId = UUID.fromString(securityIdentity.getPrincipal().getName());
        List<Release> releases = releaseRepository.find("artistId", artistId).list();
        return Response.ok(releases).build();
    }
}