package com.songao.songdao_backend.repository;

import com.songao.songdao_backend.model.Admin;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AdminRepository extends MongoRepository<Admin, String> {
    Optional<Admin> findAdminByEmail(String email);
}
