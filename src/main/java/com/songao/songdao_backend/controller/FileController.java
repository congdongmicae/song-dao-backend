package com.songao.songdao_backend.controller;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.text.Normalizer;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/files")
@CrossOrigin(origins = "http://localhost:5173")
public class FileController {

    private final GridFSBucket gridFSBucket;

    @Autowired
    public FileController(GridFSBucket gridFSBucket) {
        this.gridFSBucket = gridFSBucket;
    }

    @GetMapping("/image/{id}")
    public ResponseEntity<?> getImage(@PathVariable String id) {
        try {
            GridFSDownloadStream downloadStream = gridFSBucket.openDownloadStream(new ObjectId(id));
            GridFSFile gridFSFile = downloadStream.getGridFSFile();

            String contentType = "image/jpeg";
            if (gridFSFile.getMetadata() != null && gridFSFile.getMetadata().getString("type") != null) {
                contentType = gridFSFile.getMetadata().getString("type");
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .body(new InputStreamResource(downloadStream));
        } catch (Exception e) {
            return ResponseEntity.status(404).body("Image not found.");
        }
    }

    @GetMapping("/pdf/{id}")
    public ResponseEntity<?> viewPdf(@PathVariable String id) {
        try {
            GridFSDownloadStream downloadStream = gridFSBucket.openDownloadStream(new ObjectId(id));
            GridFSFile gridFSFile = downloadStream.getGridFSFile();

            String contentType = "application/pdf";
            if (gridFSFile.getMetadata() != null && gridFSFile.getMetadata().getString("type") != null) {
                contentType = gridFSFile.getMetadata().getString("type");
            }

            String filename = gridFSFile.getFilename();

            String normalized = Normalizer.normalize(filename, Normalizer.Form.NFD);
            String cleanName = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
                    .matcher(normalized)
                    .replaceAll("");

            cleanName = cleanName.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + cleanName + "\"")
                    .body(new InputStreamResource(downloadStream));

        } catch (Exception e) {
            return ResponseEntity.status(404).body("PDF not found.");
        }
    }

    @GetMapping("/pdf/download/{id}")
    public ResponseEntity<?> downloadPdf(@PathVariable String id) {
        try {
            GridFSDownloadStream stream = gridFSBucket.openDownloadStream(new ObjectId(id));
            GridFSFile file = stream.getGridFSFile();

            String contentType = "application/pdf";
            if (file.getMetadata() != null && file.getMetadata().getString("type") != null) {
                contentType = file.getMetadata().getString("type");
            }

            String rawTitle = file.getMetadata() != null ? file.getMetadata().getString("title") : "file";

            String normalized = Normalizer.normalize(rawTitle, Normalizer.Form.NFD);
            String cleanTitle = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
                    .matcher(normalized)
                    .replaceAll("");
            cleanTitle = cleanTitle.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");

            String finalFilename = cleanTitle + ".pdf";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + finalFilename + "\"")
                    .body(new InputStreamResource(stream));

        } catch (Exception e) {
            return ResponseEntity.status(404).body("PDF not found.");
        }
    }
}