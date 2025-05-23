package com.songao.songdao_backend.service;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.songao.songdao_backend.model.CaoPho;
import com.songao.songdao_backend.repository.CaoPhoRepository;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RequiredArgsConstructor
@Service
public class CaoPhoService {
    private final GridFSBucket gridFSBucket;
    private final CaoPhoRepository caoPhoRepo;

    public void saveCaoPho(String title, String birth, String death, MultipartFile pdf, MultipartFile image) throws IOException {
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

        CaoPho entry = CaoPho.builder()
                .title(title)
                .birth(birth)
                .death(death)
                .pdfId(pdfId.toHexString())
                .imageId(imageId.toHexString())
                .build();

        caoPhoRepo.save(entry);
    }
}
