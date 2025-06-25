package com.odevo.hoa.service.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odevo.hoa.common.dto.ExtractedDataDto;
import com.odevo.hoa.service.repository.ExtractedDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for querying extracted data from the database.
 * This service interacts with the ExtractedDataRepository (which is shared conceptually with worker,
 * though in a real-world scenario, you might have a dedicated read-only view or a separate query service).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataQueryService {

    private final ExtractedDataRepository extractedDataRepository;
    private final ObjectMapper objectMapper; // For converting JSONB to Map and vice-versa

    /**
     * Retrieves all extracted data from the database and maps it to DTOs.
     *
     * @return A list of ExtractedDataDto objects.
     */
    public List<ExtractedDataDto> getAllExtractedData() {
        log.info("Fetching all extracted data from the database.");
        return extractedDataRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves extracted data by PDF file name from the database.
     *
     * @param pdfFileName The file name of the PDF.
     * @return The ExtractedDataDto if found, otherwise null.
     */
    public ExtractedDataDto getExtractedDataByPdfFileName(String pdfFileName) {
        log.info("Fetching extracted data for PDF file name: {}", pdfFileName);
        return extractedDataRepository.findByPdfFileName(pdfFileName)
                .map(this::convertToDto)
                .orElse(null);
    }

    /**
     * Retrieves the original file name for a given PDF file name (UUID).
     *
     * @param pdfFileName The unique file name (UUID) of the PDF.
     * @return An Optional containing the original file name if found, otherwise empty.
     */
    public Optional<String> getOriginalFileNameByPdfFileName(String pdfFileName) {
        log.info("Fetching original file name for PDF file name: {}", pdfFileName);
        return extractedDataRepository.findByPdfFileName(pdfFileName)
                .map(com.odevo.hoa.common.entity.ExtractedData::getOriginalFileName);
    }

    /**
     * Helper method to convert the entity to DTO.
     * This assumes the `jsonData` in the entity is stored as a JSON string or byte array that can be mapped to Map.
     * Since we are using JPA and a `jsonb` type, Spring Data JPA might automatically handle the mapping if the entity field is `Map<String, Object>`.
     * If not, manual conversion from String (or byte[]) to Map might be needed.
     */
    private ExtractedDataDto convertToDto(com.odevo.hoa.common.entity.ExtractedData entity) {
        Map<String, Object> jsonDataMap = null;
        try {
            // Assuming jsonData is stored as a JSON string in the entity for simplicity with JPA.
            // If using jsonb type, JPA might handle it directly, otherwise manual parsing.
            if (entity.getJsonData() != null) {
                jsonDataMap = objectMapper.readValue(entity.getJsonData(), new TypeReference<Map<String, Object>>() {});
            }
        } catch (Exception e) {
            log.error("Error converting JSON data from entity to Map: {}", e.getMessage(), e);
            // Handle error, perhaps return an empty map or null depending on desired behavior
        }
        return new ExtractedDataDto(
                entity.getId().toString(),
                entity.getPdfFileName(),
                entity.getOriginalFileName(),
                jsonDataMap,
                entity.getVectorData()
        );
    }
}
