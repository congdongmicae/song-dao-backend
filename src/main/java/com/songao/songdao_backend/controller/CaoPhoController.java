package com.songao.songdao_backend.controller;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.songao.songdao_backend.model.CaoPho;
import com.songao.songdao_backend.repository.CaoPhoRepository;
import com.songao.songdao_backend.service.CaoPhoService;
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
@CrossOrigin(origins = {"http://localhost:5173", "https://songdao.de, https://www.songdao.de"})
@RequestMapping("/caopho")
public class CaoPhoController {
    @Autowired
    private CaoPhoService caoPhoService;

    @Autowired
    private CaoPhoRepository caoPhoRepo;

    @Autowired
    private GridFSBucket gridFSBucket;

    @PostMapping("/saveEntry")
    public ResponseEntity<String> entry(
            @RequestParam("title") String title,
            @RequestParam("birth") String birth,
            @RequestParam("death") String death,
            @RequestParam("pdf") MultipartFile pdfFile,
            @RequestParam("image") MultipartFile imageFile) {

        try {
            caoPhoService.saveCaoPho(title, birth, death, pdfFile, imageFile);
            return ResponseEntity.ok("Files uploaded successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/entry")
    public List<CaoPho> getEntries() {
        return caoPhoRepo.findAll();
    }

    @PutMapping("/entry/{id}")
    public ResponseEntity<String> editEntry(@PathVariable String id,
                                            @RequestParam("title") String title,
                                            @RequestParam("birth") String birth,
                                            @RequestParam("death") String death,
                                            @RequestParam(value = "pdf", required = false) MultipartFile pdfFile,
                                            @RequestParam(value = "image", required = false) MultipartFile imageFile) {
        try {
            Optional<CaoPho> optionalCaoPho = caoPhoRepo.findById(id);
            if (optionalCaoPho.isEmpty()) {
                return ResponseEntity.status(404).body("Cao Pho not found");
            }

            CaoPho caoPho = optionalCaoPho.get();
            boolean titleChanged = !title.equals(caoPho.getTitle());
            caoPho.setTitle(title);
            caoPho.setBirth(birth);
            caoPho.setDeath(death);

            if (pdfFile != null && !pdfFile.isEmpty()) {
                gridFSBucket.delete(new ObjectId(caoPho.getPdfId()));
                ObjectId newPdfId = gridFSBucket.uploadFromStream(
                        sanitizeFileName(title, ".pdf"),
                        pdfFile.getInputStream(),
                        new GridFSUploadOptions().metadata(
                                new Document("type", pdfFile.getContentType()).append("title", title)
                        )
                );
                caoPho.setPdfId(newPdfId.toHexString());

            } else if (titleChanged) {
                GridFSDownloadStream oldStream = gridFSBucket.openDownloadStream(new ObjectId(caoPho.getPdfId()));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                IOUtils.copy(oldStream, baos);
                oldStream.close();
                gridFSBucket.delete(new ObjectId(caoPho.getPdfId()));

                ObjectId newPdfId = gridFSBucket.uploadFromStream(
                        sanitizeFileName(title, ".pdf"),
                        new ByteArrayInputStream(baos.toByteArray()),
                        new GridFSUploadOptions().metadata(
                                new Document("type", "application/pdf").append("title", title)
                        )
                );
                caoPho.setPdfId(newPdfId.toHexString());
            }

            if (imageFile != null && !imageFile.isEmpty()) {
                gridFSBucket.delete(new ObjectId(caoPho.getImageId()));
                ObjectId newImageId = gridFSBucket.uploadFromStream(
                        sanitizeFileName(title, ".jpg"),
                        imageFile.getInputStream(),
                        new GridFSUploadOptions().metadata(
                                new Document("type", imageFile.getContentType()).append("title", title)
                        )
                );
                caoPho.setImageId(newImageId.toHexString());

            } else if (titleChanged) {
                GridFSDownloadStream oldStream = gridFSBucket.openDownloadStream(new ObjectId(caoPho.getImageId()));
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                IOUtils.copy(oldStream, baos);
                oldStream.close();
                gridFSBucket.delete(new ObjectId(caoPho.getImageId()));

                ObjectId newImageId = gridFSBucket.uploadFromStream(
                        sanitizeFileName(title, ".jpg"),
                        new ByteArrayInputStream(baos.toByteArray()),
                        new GridFSUploadOptions().metadata(
                                new Document("type", "image/jpeg").append("title", title)
                        )
                );
                caoPho.setImageId(newImageId.toHexString());
            }

            caoPhoRepo.save(caoPho);
            return ResponseEntity.ok("Cao Pho was updated successfully");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error (Update Cao Pho): " + e.getMessage());
        }
    }

    @DeleteMapping("/entry/{id}")
    public ResponseEntity<String> deleteEntry(@PathVariable String id) {
        try {
            Optional<CaoPho> optionalCaoPho = caoPhoRepo.findById(id);
            if (optionalCaoPho.isEmpty()) {
                return ResponseEntity.status(404).body("Cao Pho not found");
            }

            CaoPho caoPho = optionalCaoPho.get();
            gridFSBucket.delete(new ObjectId(caoPho.getPdfId()));
            gridFSBucket.delete(new ObjectId(caoPho.getImageId()));
            caoPhoRepo.deleteById(id);
            return ResponseEntity.ok("Cao Pho entry was deleted successfully");
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