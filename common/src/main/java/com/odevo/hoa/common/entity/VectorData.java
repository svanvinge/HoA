package com.odevo.hoa.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * JPA Entity for storing vector data.
 * This entity is separate but linked to ExtractedData by pdfFileName (or a direct foreign key if needed).
 * For simplicity in this template, we decided to embed `vectorData` directly in `ExtractedData` entity.
 * This file is kept as a placeholder to show the intent of having a separate vector representation.
 * If you need a proper vector database integration, this entity would be used.
 *
 * For now, `vector_data` is a column in `extracted_data` table.
 */
@Entity
@Table(name = "vector_data") // This table won't be created with current V1__Initial_Schema.sql
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VectorData {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "pdf_file_name", nullable = false, unique = true)
    private String pdfFileName; // Link to the original PDF / ExtractedData

    @Column(name = "vector_value", columnDefinition = "TEXT") // Store vector as a string/text
    private String vectorValue; // Example: "0.1,0.2,0.3,..." or a JSON string of array
}
