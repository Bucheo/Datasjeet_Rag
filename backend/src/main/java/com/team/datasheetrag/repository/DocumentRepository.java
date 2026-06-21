package com.team.datasheetrag.repository;

import com.team.datasheetrag.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, String> {
    List<Document> findAllByOrderByUploadedAtDesc();
}
