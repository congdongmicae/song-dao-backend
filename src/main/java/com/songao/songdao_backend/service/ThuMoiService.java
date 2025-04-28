package com.songao.songdao_backend.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.songao.songdao_backend.model.ThuMoi;
import com.songao.songdao_backend.repository.ThuMoiRepository;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RequiredArgsConstructor
@Service
public class ThuMoiService {
    private final GridFSBucket gridFSBucket;
    private final ThuMoiRepository thuMoiRepo;

    public void saveThuMoi(String title, MultipartFile pdf, MultipartFile image) throws IOException {
        ObjectId pdfId = gridFSBucket.uploadFromStream(
                title + ".pdf",
                pdf.getInputStream(),
                new GridFSUploadOptions().metadata(new Document("type", pdf.getContentType()).append("title", title))
        );

        ObjectId imageId = gridFSBucket.uploadFromStream(
                title + ".jpg",
                image.getInputStream(),
                new GridFSUploadOptions().metadata(new Document("type", image.getContentType()).append("title", title))
        );

        ThuMoi entry = ThuMoi.builder()
                .title(title)
                .pdfId(pdfId.toHexString())
                .imageId(imageId.toHexString())
                .build();

        thuMoiRepo.save(entry);
    }
}
