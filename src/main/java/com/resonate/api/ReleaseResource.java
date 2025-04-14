package com.resonate.api;

import com.resonate.api.dto.CreateReleaseRequest;
import com.resonate.api.dto.CreateTrackRequest;
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
    public List<Release> getAllReleases() {
        return releaseRepository.listAll();
    }

    @GET
    @Path("/{id}")
    public Release getRelease(@PathParam("id") Long id) {
        return releaseRepository.findById(id);
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

        releaseRepository.persist(release);
        
        return Response.status(Response.Status.CREATED)
                .entity(release)
                .build();
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
    @Operation(summary = "Update release")
    @APIResponse(responseCode = "200", description = "Release updated successfully")
    @APIResponse(responseCode = "404", description = "Release not found")
    public Response updateRelease(@PathParam("id") Long id, Release updatedRelease) {
        Release release = releaseRepository.findById(id);
        if (release == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Release not found").build();
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
    @Operation(summary = "Delete release")
    @APIResponse(responseCode = "204", description = "Release deleted successfully")
    @APIResponse(responseCode = "404", description = "Release not found")
    public Response deleteRelease(@PathParam("id") Long id) {
        Release release = releaseRepository.findById(id);
        if (release == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Release not found").build();
        }
        releaseRepository.delete(release);
        return Response.noContent().build();
    }

    @GET
    @Path("/public")
    @Operation(summary = "List all releases", description = "Returns a list of all published releases")
    @APIResponse(responseCode = "200", description = "List of releases retrieved successfully")
    public Response getAllReleases(
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

        return Response.ok(release).build();
    }

    @GET
    @Path("/artist/{artistId}")
    public Response getArtistReleases(@PathParam("artistId") UUID artistId) {
        List<Release> releases = releaseRepository.find("artistId", artistId).list();
        return Response.ok(releases).build();
    }
}