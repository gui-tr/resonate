package com.resonate.api;

import com.resonate.api.dto.CreateTrackRequest;
import com.resonate.domain.model.Release;
import com.resonate.domain.model.Track;
import com.resonate.domain.media.AudioFile;
import com.resonate.infrastructure.repository.ReleaseRepository;
import com.resonate.infrastructure.repository.TrackRepository;
import com.resonate.infrastructure.repository.AudioFileRepository;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;

@Path("/api/tracks")
@Tag(name = "Track", description = "Operations related to tracks")
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

    @POST
    @Transactional
    @Operation(summary = "Create track")
    @APIResponse(responseCode = "201", description = "Track created successfully")
    @APIResponse(responseCode = "404", description = "Release or Audio File not found")
    public Response createTrack(CreateTrackRequest request, @QueryParam("releaseId") Long releaseId) {
        log.info("Creating track for release: {}", releaseId);
        
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

        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("title is required")
                    .build();
        }

        if (request.getDuration() <= 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("duration must be greater than 0")
                    .build();
        }

        if (request.getFilePath() == null || request.getFilePath().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("filePath is required")
                    .build();
        }

        Track track = new Track();
        track.setTitle(request.getTitle());
        track.setDuration(request.getDuration());
        track.setIsrc(request.getIsrc());
        track.setFilePath(request.getFilePath());
        track.setFileSize(request.getFileSize());
        track.setRelease(release);

        if (request.getAudioFileId() != null) {
            AudioFile audioFile = audioFileRepository.findById(request.getAudioFileId().longValue());
            if (audioFile == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Audio file not found")
                        .build();
            }
            track.setAudioFile(audioFile);
        }

        trackRepository.persist(track);
        return Response.status(Response.Status.CREATED).entity(track).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Get track", description = "Retrieves a track by its ID")
    @APIResponse(responseCode = "200", description = "Track retrieved successfully")
    @APIResponse(responseCode = "404", description = "Track not found")
    public Response getTrack(@PathParam("id") Long id) {
        Track track = trackRepository.findById(id);
        if (track == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Track not found").build();
        }
        return Response.ok(track).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Update track")
    @APIResponse(responseCode = "200", description = "Track updated successfully")
    @APIResponse(responseCode = "404", description = "Track not found")
    public Response updateTrack(@PathParam("id") Long id, CreateTrackRequest request) {
        Track track = trackRepository.findById(id);
        if (track == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Track not found").build();
        }

        if (request.getTitle() != null && !request.getTitle().trim().isEmpty()) {
            track.setTitle(request.getTitle());
        }

        if (request.getDuration() > 0) {
            track.setDuration(request.getDuration());
        }

        track.setIsrc(request.getIsrc());

        if (request.getFilePath() != null && !request.getFilePath().trim().isEmpty()) {
            track.setFilePath(request.getFilePath());
        }

        if (request.getFileSize() != null) {
            track.setFileSize(request.getFileSize());
        }

        if (request.getAudioFileId() != null) {
            AudioFile audioFile = audioFileRepository.findById(request.getAudioFileId().longValue());
            if (audioFile == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Audio file not found")
                        .build();
            }
            track.setAudioFile(audioFile);
        }

        trackRepository.persist(track);
        return Response.ok(track).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Delete track")
    @APIResponse(responseCode = "204", description = "Track deleted successfully")
    @APIResponse(responseCode = "404", description = "Track not found")
    public Response deleteTrack(@PathParam("id") Long id) {
        Track track = trackRepository.findById(id);
        if (track == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Track not found").build();
        }
        trackRepository.delete(track);
        return Response.noContent().build();
    }
}
