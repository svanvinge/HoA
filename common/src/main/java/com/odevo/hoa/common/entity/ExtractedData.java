package com.odevo.hoa.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity for storing extracted JSON data from PDFs.
 * The `jsonData` field uses `jsonb` type in PostgreSQL.
 */
@Entity
@Table(name = "extracted_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExtractedData {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "pdf_file_name", nullable = false, unique = true)
    private String pdfFileName;

    @Column(name = "original_file_name")
    private String originalFileName;

    // Stores JSON data as a JSONB type in PostgreSQL
    @Column(name = "json_data", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON) // Hibernate 6 way to map JSONB
    private String jsonData; // Storing as String for direct JSON content

    @Column(name = "extraction_timestamp", nullable = false)
    private LocalDateTime extractionTimestamp;

    // Optional: Reference to VectorData if it's a one-to-one relationship
    // For simplicity, we'll keep vector_data directly in this table as a String for now
    @Column(name = "vector_data")
    private String vectorData;
}
