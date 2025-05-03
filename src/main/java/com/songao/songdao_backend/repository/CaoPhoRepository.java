package com.songao.songdao_backend.repository;

import com.songao.songdao_backend.model.CaoPho;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CaoPhoRepository extends MongoRepository<CaoPho, String> {
}
