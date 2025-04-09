package com.resonate.api;

import com.resonate.domain.media.AudioFile;
import com.resonate.infrastructure.repository.AudioFileRepository;
import com.resonate.storage.BackblazeStorageService;
import io.quarkus.security.Authenticated;
import io.quarkus.security.identity.SecurityIdentity;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Path("/api/audio-files")
@Authenticated
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AudioFileResource {

    @Inject
    SecurityIdentity securityIdentity;

    @Inject
    BackblazeStorageService storageService;

    @Inject
    AudioFileRepository audioFileRepository;

    @GET
    @Path("/upload")
    @Operation(summary = "Generate a signed URL for file upload", description = "Returns a signed URL that allows the client to directly upload a file to Backblaze B2")
    @APIResponse(responseCode = "200", description = "Signed URL generated successfully")
    public Response getSignedUploadUrl(@QueryParam("fileName") String fileName, @QueryParam("contentType") String contentType) {
        String safeContentType = contentType != null ? contentType : "audio/mpeg";
        Map<String, String> uploadInfo = storageService.generateUploadUrl(fileName, safeContentType);
        return Response.ok(uploadInfo).build();
    }

    @POST
    @Path("/register")
    @Transactional
    @Operation(summary = "Register an uploaded audio file", description = "After a successful upload to Backblaze B2, register the file metadata")
    @APIResponse(responseCode = "201", description = "Audio file registration successful")
    public Response registerAudioFile(AudioFileRegistration registration) {
        // Create a streaming URL
        String streamingUrl = storageService.generateDownloadUrl(registration.fileKey);

        // Create and persist the audio file entity
        AudioFile audioFile = AudioFile.builder()
                .fileIdentifier(registration.fileKey)
                .fileUrl(streamingUrl)
                .fileSize(registration.fileSize)
                .checksum(registration.checksum)
                .build();

        audioFileRepository.persist(audioFile);

        return Response.status(Response.Status.CREATED).entity(audioFile).build();
    }

    @GET
    @Path("/{id}/stream")
    @Operation(summary = "Get streaming URL", description = "Generates a temporary signed URL for streaming an audio file")
    @APIResponse(responseCode = "200", description = "Streaming URL generated successfully")
    @APIResponse(responseCode = "404", description = "Audio file not found")
    public Response getStreamingUrl(@PathParam("id") Long id) {
        AudioFile audioFile = audioFileRepository.findById(id);
        if (audioFile == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Audio file not found").build();
        }

        // Generate fresh streaming URL to ensure it's valid
        String streamingUrl = storageService.generateDownloadUrl(audioFile.getFileIdentifier());

        return Response.ok(Map.of("streamingUrl", streamingUrl)).build();
    }

    public static class AudioFileRegistration {
        public String fileKey;
        public Long fileSize;
        public String checksum;
    }
}
