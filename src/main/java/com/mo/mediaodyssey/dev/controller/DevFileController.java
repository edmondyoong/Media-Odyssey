package com.mo.mediaodyssey.dev.controller;

import com.mo.mediaodyssey.dev.dto.DevFileApiResponse;
import com.mo.mediaodyssey.shared.services.ObjectStorageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/dev/file")
public class DevFileController {

    @Autowired
    private ObjectStorageService objectStorageService;

    @Value("${DEV_MODE:FALSE}")
    private String devMode;

    /**
     * API endpoint to upload a file to object storage. For developmental use only.
     * Must be enabled via environment variable. Once enabled, access is
     * unrestricted.
     * 
     * @param file Multipart file to be uploaded. Must be sent as form data with key
     *             "file".
     * @return URL of the uploaded file in body upon success
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DevFileApiResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (!devMode.toLowerCase().equals("TRUE".toLowerCase())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(DevFileApiResponse.error("DEV_FILE_DISABLED",
                                "Developmental file upload API endpoint is disabled."));
            } else {
                String url = objectStorageService.uploadFile(file);
                return ResponseEntity
                        .ok(DevFileApiResponse.success("DEV_FILE_UPLOAD_SUCCESS", "File uploaded successfully.", url));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(DevFileApiResponse.error("DEV_FILE_UPLOAD_ERROR",
                    "An error occurred while processing the request."));
        }
    }
}
