package com.resonate.api;

import com.resonate.api.dto.CreateTrackRequest;
import com.resonate.domain.model.Release;
import com.resonate.domain.model.Track;
import com.resonate.domain.media.AudioFile;
import com.resonate.infrastructure.repository.ReleaseRepository;
import com.resonate.infrastructure.repository.TrackRepository;
import com.resonate.infrastructure.repository.AudioFileRepository;
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

@Path("/api/tracks")
@Tag(name = "Track", description = "Operations related to music tracks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Slf4j
public class TrackResource {

    @Inject
    TrackRepository trackRepository;

    @Inject
    ReleaseRepository releaseRepository;

    @Inject
    AudioFileRepository audioFileRepository;

    @GET
    public Response getAllTracks() {
        List<Track> tracks = trackRepository.listAll();
        return Response.ok(tracks).build();
    }

    @GET
    @Path("/{id}")
    public Response getTrack(@PathParam("id") Long id) {
        Track track = trackRepository.findById(id);
        if (track == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(track).build();
    }

    @POST
    @Transactional
    public Response createTrack(@QueryParam("releaseId") Long releaseId, Track track) {
        if (releaseId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("releaseId is required")
                    .build();
        }

        Release release = releaseRepository.findById(releaseId);
        if (release == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Release not found")
                    .build();
        }

        // Check if the current user is the artist who owns this release
        UUID currentUserId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        if (!release.getArtistId().equals(currentUserId)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        track.setRelease(release);

        try {
            trackRepository.persist(track);
            return Response.status(Response.Status.CREATED).entity(track).build();
        } catch (Exception e) {
            log.error("Failed to create track", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to create track: " + e.getMessage())
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response updateTrack(@PathParam("id") Long id, Track track) {
        Track existingTrack = trackRepository.findById(id);
        if (existingTrack == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Check if the current user is the artist who owns this track's release
        UUID currentUserId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        if (!existingTrack.getRelease().getArtistId().equals(currentUserId)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        existingTrack.setTitle(track.getTitle());
        existingTrack.setDuration(track.getDuration());
        existingTrack.setIsrc(track.getIsrc());
        existingTrack.setFilePath(track.getFilePath());
        existingTrack.setFileSize(track.getFileSize());

        try {
            trackRepository.persist(existingTrack);
            return Response.ok(existingTrack).build();
        } catch (Exception e) {
            log.error("Failed to update track", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to update track: " + e.getMessage())
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteTrack(@PathParam("id") Long id) {
        Track track = trackRepository.findById(id);
        if (track == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // Check if the current user is the artist who owns this track's release
        UUID currentUserId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        if (!track.getRelease().getArtistId().equals(currentUserId)) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        try {
            trackRepository.delete(track);
            return Response.noContent().build();
        } catch (Exception e) {
            log.error("Failed to delete track", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to delete track: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/release/{releaseId}")
    public Response getTracksByRelease(@PathParam("releaseId") Long releaseId) {
        List<Track> tracks = trackRepository.find("release.id", releaseId).list();
        return Response.ok(tracks).build();
    }
}
