package com.songao.songdao_backend.model;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@Document(collection = "cao-pho")
public class CaoPho {
    @Id
    private String id;

    private String title;
    private String birth;
    private String death;
    private String pdfId;
    private String imageId;
}