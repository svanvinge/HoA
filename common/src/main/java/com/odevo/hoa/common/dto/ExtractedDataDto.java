package com.odevo.hoa.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * DTO representing extracted data from a PDF, including JSON and a vector.
 * This can be used for querying from the service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtractedDataDto implements Serializable {
    private String id; // Unique ID for the extracted data (e.g., linked to pdfFileName)
    private String pdfFileName;
    private String originalFileName;
    private Map<String, Object> jsonData; // For the JSON document
    private String vectorData;           // For the vector (as a string for simplicity, could be byte array)
}
