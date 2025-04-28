package com.songao.songdao_backend.model;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "admin-login")
public class Admin {
    private String email;
    private String password;
}
