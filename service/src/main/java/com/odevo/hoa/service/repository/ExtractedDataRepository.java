package com.odevo.hoa.service.repository;

import com.odevo.hoa.common.entity.ExtractedData; // Using the worker's entity
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for `ExtractedData` entity.
 * This is used by the service to query the data stored by the worker.
 * Note: In a larger application, the service might query a read replica or a different data store.
 * For simplicity, both service and worker share the same entity and repository conceptually pointing to the same DB.
 */
@Repository
public interface ExtractedDataRepository extends JpaRepository<ExtractedData, UUID> {
    Optional<ExtractedData> findByPdfFileName(String pdfFileName);
}
