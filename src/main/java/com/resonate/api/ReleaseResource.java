package com.resonate.api;

import com.resonate.api.dto.CreateReleaseRequest;
import com.resonate.api.dto.CreateTrackRequest;
import com.resonate.api.dto.UpdateReleaseRequest;
import com.resonate.domain.model.Release;
import com.resonate.domain.model.Track;
import com.resonate.domain.model.ArtistProfile;
import com.resonate.infrastructure.repository.ReleaseRepository;
import com.resonate.infrastructure.repository.ArtistProfileRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Path("/api/releases")
@Tag(name = "Release", description = "Operations related to music releases")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
public class ReleaseResource {

    @Inject
    ReleaseRepository releaseRepository;
    
    @Inject
    ArtistProfileRepository artistProfileRepository;

    @GET
    public Response getAllReleases() {
        List<Release> releases = releaseRepository.listAll();
        return Response.ok(releases).build();
    }

    @GET
    @Path("/{id}")
    public Response getRelease(@PathParam("id") Long id) {
        Release release = releaseRepository.findById(id);
        if (release == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(release).build();
    }

    @POST
    @Transactional
    public Response createRelease(CreateReleaseRequest request) {
        log.info("Creating release with artistId: {}", request.getArtistId());
        
        if (request.getArtistId() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("artistId is required")
                    .build();
        }

        ArtistProfile artist = artistProfileRepository.findByUserId(request.getArtistId());
        if (artist == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Artist profile not found")
                    .build();
        }

        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("title is required")
                    .build();
        }

        if (request.getReleaseDate() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("releaseDate is required")
                    .build();
        }

        Release release = new Release();
        release.setArtistId(request.getArtistId());
        release.setTitle(request.getTitle());
        release.setReleaseDate(request.getReleaseDate());
        release.setUpc(request.getUpc());

        try {
            releaseRepository.persist(release);
            return Response.status(Response.Status.CREATED).entity(release).build();
        } catch (Exception e) {
            log.error("Failed to create release", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to create release: " + e.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/{releaseId}/tracks")
    @Transactional
    public Response addTrack(@PathParam("releaseId") Long releaseId, CreateTrackRequest request) {
        Release release = releaseRepository.findById(releaseId);
        if (release == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Release not found")
                    .build();
        }

        Track track = new Track();
        track.setTitle(request.getTitle());
        track.setDuration(request.getDuration());
        track.setIsrc(request.getIsrc());
        track.setFilePath(request.getFilePath());
        track.setFileSize(request.getFileSize());
        track.setRelease(release);

        release.getTracks().add(track);
        releaseRepository.persist(release);

        return Response.status(Response.Status.CREATED)
                .entity(track)
                .build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response updateRelease(@PathParam("id") Long id, UpdateReleaseRequest request) {
        Release release = releaseRepository.findById(id);
        if (release == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Check if the current user is the artist who owns this release
        UUID currentUserId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        if (!release.getArtistId().equals(currentUserId)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        if (request.getTitle() != null) {
            release.setTitle(request.getTitle());
        }
        if (request.getReleaseDate() != null) {
            release.setReleaseDate(request.getReleaseDate());
        }
        if (request.getUpc() != null) {
            release.setUpc(request.getUpc());
        }

        try {
            releaseRepository.persist(release);
            return Response.ok(release).build();
        } catch (Exception e) {
            log.error("Failed to update release", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to update release: " + e.getMessage())
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteRelease(@PathParam("id") Long id) {
        Release release = releaseRepository.findById(id);
        if (release == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Check if the current user is the artist who owns this release
        UUID currentUserId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        if (!release.getArtistId().equals(currentUserId)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        try {
            releaseRepository.delete(release);
            return Response.noContent().build();
        } catch (Exception e) {
            log.error("Failed to delete release", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to delete release: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/public")
    @Operation(summary = "List all releases", description = "Returns a list of all published releases")
    @APIResponse(responseCode = "200", description = "List of releases retrieved successfully")
    public Response getAllPublicReleases(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {

        List<Release> releaseList = releaseRepository.findAll(Sort.by("releaseDate").descending())
                .page(Page.of(page, size))
                .list();

        return Response.ok(releaseList).build();
    }

    @GET
    @Path("/public/{id}")
    @Operation(summary = "Get release details", description = "Returns detailed information about a specific release including its tracks")
    @APIResponse(responseCode = "200", description = "Release retrieved successfully")
    @APIResponse(responseCode = "404", description = "Release not found")
    public Response getPublicReleaseDetails(@PathParam("id") Long id) {
        Release release = releaseRepository.findById(id);
        if (release == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Release not found").build();
        }

        List<Track> tracks = releaseRepository.findTracksByReleaseId(id);
        release.setTracks(tracks);

        return Response.ok(release).build();
    }

    @GET
    @Path("/artist/{artistId}")
    public Response getArtistReleases(@PathParam("artistId") UUID artistId) {
        List<Release> releases = releaseRepository.find("artistId", artistId).list();
        return Response.ok(releases).build();
    }
}