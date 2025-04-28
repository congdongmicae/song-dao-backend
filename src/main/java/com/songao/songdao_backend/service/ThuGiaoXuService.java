package com.songao.songdao_backend.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.songao.songdao_backend.model.ThuGiaoXu;
import com.songao.songdao_backend.repository.ThuGiaoXuRepository;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RequiredArgsConstructor
@Service
public class ThuGiaoXuService {
    private final GridFSBucket gridFSBucket;
    private final ThuGiaoXuRepository thuGiaoXuRepo;

    public void saveThuGiaoXu(String title, MultipartFile pdf, MultipartFile image) throws IOException {
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

        ThuGiaoXu entry = ThuGiaoXu.builder()
                .title(title)
                .pdfId(pdfId.toHexString())
                .imageId(imageId.toHexString())
                .build();

        thuGiaoXuRepo.save(entry);
    }
}