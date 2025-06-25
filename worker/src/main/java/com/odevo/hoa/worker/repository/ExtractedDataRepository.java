package com.odevo.hoa.worker.repository;

import com.odevo.hoa.common.entity.ExtractedData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for `ExtractedData` entity, used by the worker to save and retrieve extracted information.
 */
@Repository
public interface ExtractedDataRepository extends JpaRepository<ExtractedData, UUID> {
    Optional<ExtractedData> findByPdfFileName(String pdfFileName);
}
