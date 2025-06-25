package com.odevo.hoa.worker.repository;

import com.odevo.hoa.common.entity.VectorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for `VectorData` entity.
 * Currently, vector data is stored within `ExtractedData` for simplicity.
 * This repository is a placeholder if a separate vector table/store becomes necessary.
 */
@Repository
public interface VectorDataRepository extends JpaRepository<VectorData, UUID> {
    Optional<VectorData> findByPdfFileName(String pdfFileName);
}
