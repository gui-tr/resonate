package com.resonate.api;

import com.resonate.domain.model.Release;
import com.resonate.domain.model.Track;
import com.resonate.domain.media.AudioFile;
import com.resonate.infrastructure.repository.ReleaseRepository;
import com.resonate.infrastructure.repository.TrackRepository;
import com.resonate.infrastructure.repository.AudioFileRepository;
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

@Authenticated
@Path("/api/tracks")
@Tag(name = "Track", description = "Operations related to tracks")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TrackResource {

    @Inject
    TrackRepository trackRepository;

    @Inject
    ReleaseRepository releaseRepository;

    @Inject
    AudioFileRepository audioFileRepository;

    @Inject
    SecurityIdentity securityIdentity;

    @POST
    @Transactional
    @Operation(
            summary = "Create track",
            description = "Creates a new track for a given release. " +
                    "The release must belong to the authenticated artist."
    )
    @APIResponse(responseCode = "201", description = "Track created successfully")
    @APIResponse(responseCode = "404", description = "Release or Audio File not found")
    @APIResponse(responseCode = "403", description = "Not authorized to add track to this release")
    public Response createTrack(Track track,
                                @QueryParam("releaseId") Long releaseId,
                                @QueryParam("audioFileId") Long audioFileId) {
        Release release = releaseRepository.findById(releaseId);
        if (release == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Release not found").build();
        }
        UUID userId = UUID.fromString(securityIdentity.getPrincipal().getName());
        if (!release.getArtistId().equals(userId)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Not authorized to add track to this release").build();
        }
        track.setRelease(release);
        // If an audioFileId is provided, fetch the AudioFile and set it on the track.
        if (audioFileId != null) {
            AudioFile audioFile = audioFileRepository.findById(audioFileId);
            if (audioFile == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Audio file not found").build();
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
    @Operation(summary = "Update track", description = "Updates an existing track for the authenticated artist.")
    @APIResponse(responseCode = "200", description = "Track updated successfully")
    @APIResponse(responseCode = "403", description = "Not authorized to update this track")
    @APIResponse(responseCode = "404", description = "Track not found")
    public Response updateTrack(@PathParam("id") Long id, Track updatedTrack, @QueryParam("audioFileId") Long audioFileId) {
        Track track = trackRepository.findById(id);
        if (track == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Track not found").build();
        }
        UUID userId = UUID.fromString(securityIdentity.getPrincipal().getName());
        if (!track.getRelease().getArtistId().equals(userId)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Not authorized to update this track").build();
        }
        track.setTitle(updatedTrack.getTitle());
        track.setDuration(updatedTrack.getDuration());
        track.setIsrc(updatedTrack.getIsrc());
        track.setFilePath(updatedTrack.getFilePath());
        track.setFileSize(updatedTrack.getFileSize());

        if (audioFileId != null) {
            AudioFile audioFile = audioFileRepository.findById(audioFileId);
            if (audioFile == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Audio file not found").build();
            }
            track.setAudioFile(audioFile);
        }
        trackRepository.persist(track);
        return Response.ok(track).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Delete track", description = "Deletes a track for the authenticated artist")
    @APIResponse(responseCode = "200", description = "Track deleted successfully")
    @APIResponse(responseCode = "403", description = "Not authorized to delete this track")
    @APIResponse(responseCode = "404", description = "Track not found")
    public Response deleteTrack(@PathParam("id") Long id) {
        Track track = trackRepository.findById(id);
        if (track == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Track not found").build();
        }
        UUID userId = UUID.fromString(securityIdentity.getPrincipal().getName());
        if (!track.getRelease().getArtistId().equals(userId)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Not authorized to delete this track").build();
        }
        trackRepository.delete(track);
        return Response.ok("Track deleted successfully").build();
    }

}
