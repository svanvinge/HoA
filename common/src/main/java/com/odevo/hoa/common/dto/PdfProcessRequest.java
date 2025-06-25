package com.odevo.hoa.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO representing a request to process a PDF.
 * This will be sent from the service to the worker via RabbitMQ.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PdfProcessRequest implements Serializable {
    private String pdfFileName;
    private String originalFileName; // To keep track of the file's original name
    private String bucketName;
}
