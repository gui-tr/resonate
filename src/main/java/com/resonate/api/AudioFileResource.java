package com.resonate.api;

import com.resonate.domain.media.AudioFile;
import com.resonate.infrastructure.repository.AudioFileRepository;
import com.resonate.storage.BackblazeStorageService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Path("/api/audio-files")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AudioFileResource {

    @Inject
    BackblazeStorageService storageService;

    @Inject
    AudioFileRepository audioFileRepository;

    @GET
    @Path("/upload")
    @Operation(summary = "Generate a signed URL for file upload")
    @APIResponse(responseCode = "200", description = "Signed URL generated successfully")
    public Response getSignedUploadUrl(@QueryParam("fileName") String fileName, @QueryParam("contentType") String contentType) {
        String safeContentType = contentType != null ? contentType : "audio/mpeg";
        Map<String, String> uploadInfo = storageService.generateUploadUrl(fileName, safeContentType);
        return Response.ok(uploadInfo).build();
    }

    @POST
    @Path("/register")
    @Transactional
    @Operation(summary = "Register an uploaded audio file")
    @APIResponse(responseCode = "201", description = "Audio file registration successful")
    public Response registerAudioFile(AudioFileRegistration registration) {
        String streamingUrl = storageService.generateDownloadUrl(registration.fileKey);

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
    @Operation(summary = "Get streaming URL")
    @APIResponse(responseCode = "200", description = "Streaming URL generated successfully")
    @APIResponse(responseCode = "404", description = "Audio file not found")
    public Response getStreamingUrl(@PathParam("id") Long id) {
        AudioFile audioFile = audioFileRepository.findById(id);
        if (audioFile == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Audio file not found").build();
        }

        String streamingUrl = storageService.generateDownloadUrl(audioFile.getFileIdentifier());

        return Response.ok(Map.of("streamingUrl", streamingUrl)).build();
    }

    public static class AudioFileRegistration {
        public String fileKey;
        public Long fileSize;
        public String checksum;
    }
}
