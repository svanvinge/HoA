package com.odevo.hoa.worker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odevo.hoa.common.dto.PdfProcessRequest;
import com.odevo.hoa.common.entity.ExtractedData;
import com.odevo.hoa.worker.repository.ExtractedDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDateTime;

/**
 * Service responsible for the end-to-end PDF processing workflow.
 * This includes downloading from MinIO, extracting text, calling Gemini,
 * and saving the extracted data to PostgreSQL.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PdfProcessingService {

    private final MinioService minioService;
    private final GeminiService geminiService;
    private final ExtractedDataRepository extractedDataRepository;
    private final ObjectMapper objectMapper; // For converting Map to JSON string

    /**
     * Processes a PDF file based on the received request.
     * Workflow:
     * 1. Download PDF from MinIO.
     * 2. Call Gemini API with the pdf to get structured data and vector.
     * 3. Store the extracted JSON and vector data in PostgreSQL.
     *
     * @param request The PdfProcessRequest containing PDF details.
     */
    @Transactional
    public void processPdf(PdfProcessRequest request) throws Exception {
        String pdfFileName = request.getPdfFileName();
        String originalFileName = request.getOriginalFileName();
        String bucketName = request.getBucketName();

        log.info("Starting processing for PDF: {} from bucket: {}", pdfFileName, bucketName);

        InputStream pdfInputStream = null;
        try {
            // 1. Download PDF from MinIO
            pdfInputStream = minioService.downloadFile(bucketName, pdfFileName);
            log.info("PDF file {} downloaded from MinIO.", pdfFileName);


            // 2. Call Gemini API to extract structured data and vector
            JsonNode extractedGeminiData = geminiService.extractDataFromPdfContent(pdfInputStream);
            String jsonData = extractedGeminiData.toString();
            String vectorData = "";
            log.info("Data extracted by Gemini for {}. JSON size: {}, Vector length: {}",
                    pdfFileName, jsonData.length(), vectorData.length());


            // 3. Store the extracted JSON and vector data in PostgreSQL
            ExtractedData extracted = ExtractedData.builder()
                    .pdfFileName(pdfFileName)
                    .originalFileName(originalFileName)
                    .jsonData(jsonData)
                    .vectorData(vectorData)
                    .extractionTimestamp(LocalDateTime.now())
                    .build();

            extractedDataRepository.save(extracted);
            log.info("Extracted data saved to database for PDF: {}", pdfFileName);
        } catch (Exception e) {
            log.error("Failed to process PDF {}: {}", pdfFileName, e.getMessage(), e);
            throw e; // Re-throw to indicate failure, allowing potential dead-letter queue handling
        } finally {
            if (pdfInputStream != null) {
                try {
                    pdfInputStream.close();
                } catch (Exception e) {
                    log.warn("Failed to close PDF input stream: {}", e.getMessage());
                }
            }
        }
    }
}
