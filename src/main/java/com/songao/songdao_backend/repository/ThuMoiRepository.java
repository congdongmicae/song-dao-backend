package com.songao.songdao_backend.repository;

import com.songao.songdao_backend.model.ThuMoi;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ThuMoiRepository extends MongoRepository<ThuMoi, String> {
}
