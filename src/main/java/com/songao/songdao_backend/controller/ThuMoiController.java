package com.songao.songdao_backend.controller;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.songao.songdao_backend.model.ThuMoi;
import com.songao.songdao_backend.repository.ThuMoiRepository;
import com.songao.songdao_backend.service.ThuMoiService;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.Normalizer;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "https://songdao.de"})
@RequestMapping("/thumoi")
public class ThuMoiController {
    @Autowired
    private ThuMoiService thuMoiService;

    @Autowired
    private ThuMoiRepository thuMoiRepo;

    @Autowired
    private GridFSBucket gridFSBucket;

    @PostMapping("/saveEntry")
    public ResponseEntity<String> entry(
            @RequestParam("title") String title,
            @RequestParam("pdf") MultipartFile pdfFile,
            @RequestParam("image") MultipartFile imageFile) {

        try {
            thuMoiService.saveThuMoi(title, pdfFile, imageFile);
            return ResponseEntity.ok("Files uploaded successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/entry")
    public List<ThuMoi> getEntries() {
        return thuMoiRepo.findAll();
    }

    @PutMapping("/entry/{id}")
    public ResponseEntity<String> editEntry(@PathVariable String id,
                                            @RequestParam("title") String title,
                                            @RequestParam(value = "pdf", required = false) MultipartFile pdfFile,
                                            @RequestParam(value = "image", required = false) MultipartFile imageFile) {
        try {
            Optional<ThuMoi> optionalThuMoi = thuMoiRepo.findById(id);
            if (optionalThuMoi.isEmpty()) {
                return ResponseEntity.status(404).body("Thu Moi not found");
            }

            ThuMoi thuMoi = optionalThuMoi.get();
            boolean titleChanged = !title.equals(thuMoi.getTitle());
            thuMoi.setTitle(title);

            if (pdfFile != null && !pdfFile.isEmpty()) {
                gridFSBucket.delete(new ObjectId(thuMoi.getPdfId()));
                ObjectId newPdfId = gridFSBucket.uploadFromStream(
                        sanitizeFileName(title, ".pdf"),
                        pdfFile.getInputStream(),
                        new GridFSUploadOptions().metadata(
                                new Document("type", pdfFile.getContentType()).append("title", title)
                        )
                );
                thuMoi.setPdfId(newPdfId.toHexString());

            } else if (titleChanged) {
                GridFSDownloadStream oldStream = gridFSBucket.openDownloadStream(new ObjectId(thuMoi.getPdfId()));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                IOUtils.copy(oldStream, baos);
                oldStream.close();
                gridFSBucket.delete(new ObjectId(thuMoi.getPdfId()));

                ObjectId newPdfId = gridFSBucket.uploadFromStream(
                        sanitizeFileName(title, ".pdf"),
                        new ByteArrayInputStream(baos.toByteArray()),
                        new GridFSUploadOptions().metadata(
                                new Document("type", "application/pdf").append("title", title)
                        )
                );
                thuMoi.setPdfId(newPdfId.toHexString());
            }

            if (imageFile != null && !imageFile.isEmpty()) {
                gridFSBucket.delete(new ObjectId(thuMoi.getImageId()));
                ObjectId newImageId = gridFSBucket.uploadFromStream(
                        sanitizeFileName(title, ".jpg"),
                        imageFile.getInputStream(),
                        new GridFSUploadOptions().metadata(
                                new Document("type", imageFile.getContentType()).append("title", title)
                        )
                );
                thuMoi.setImageId(newImageId.toHexString());

            } else if (titleChanged) {
                GridFSDownloadStream oldStream = gridFSBucket.openDownloadStream(new ObjectId(thuMoi.getImageId()));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                IOUtils.copy(oldStream, baos);
                oldStream.close();
                gridFSBucket.delete(new ObjectId(thuMoi.getImageId()));

                ObjectId newImageId = gridFSBucket.uploadFromStream(
                        sanitizeFileName(title, ".jpg"),
                        new ByteArrayInputStream(baos.toByteArray()),
                        new GridFSUploadOptions().metadata(
                                new Document("type", "image/jpeg").append("title", title)
                        )
                );
                thuMoi.setImageId(newImageId.toHexString());
            }

            thuMoiRepo.save(thuMoi);
            return ResponseEntity.ok("Thu Moi was updated successfully");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error (Update Thu Moi): " + e.getMessage());
        }
    }

    @DeleteMapping("/entry/{id}")
    public ResponseEntity<String> deleteEntry(@PathVariable String id) {
        try {
            Optional<ThuMoi> optionalThuMoi = thuMoiRepo.findById(id);
            if (optionalThuMoi.isEmpty()) {
                return ResponseEntity.status(404).body("Thu Moi not found");
            }

            ThuMoi thuMoi = optionalThuMoi.get();
            gridFSBucket.delete(new ObjectId(thuMoi.getPdfId()));
            gridFSBucket.delete(new ObjectId(thuMoi.getImageId()));
            thuMoiRepo.deleteById(id);
            return ResponseEntity.ok("Thu Moi entry was deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error (Delete Entry): " + e.getMessage());
        }
    }

    private String sanitizeFileName(String rawTitle, String extension) {
        String normalized = Normalizer.normalize(rawTitle, Normalizer.Form.NFD);
        String noDiacritics = Pattern.compile("\\p{InCombiningDiacriticalMarks}+")
                .matcher(normalized)
                .replaceAll("");
        String cleaned = noDiacritics.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
        return cleaned + extension;
    }
}
