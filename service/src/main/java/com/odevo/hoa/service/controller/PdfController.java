package com.odevo.hoa.service.controller;

import com.odevo.hoa.common.dto.ExtractedDataDto;
import com.odevo.hoa.common.dto.PdfProcessRequest;
import com.odevo.hoa.common.util.Constants;
import com.odevo.hoa.service.service.DataQueryService;
import com.odevo.hoa.service.service.MessageProducer;
import com.odevo.hoa.service.service.MinioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * REST Controller for handling PDF operations.
 * Provides endpoints for uploading PDFs and querying extracted data.
 */
@RestController
@RequestMapping("/api/pdfs")
@RequiredArgsConstructor
@Slf4j
public class PdfController {

    private final MinioService minioService;
    private final MessageProducer messageProducer;
    private final DataQueryService dataQueryService;

    /**
     * Endpoint to upload a PDF file.
     * The file is uploaded to MinIO, and a message is sent to RabbitMQ for processing.
     *
     * @param file The PDF file to upload.
     * @return ResponseEntity indicating success or failure.
     */
    @PostMapping("/upload")
    public CompletableFuture<ResponseEntity<String>> uploadPdf(@RequestParam("file") MultipartFile file) {
        return CompletableFuture.supplyAsync(() -> {
            if (file.isEmpty()) {
                return new ResponseEntity<>("Please select a file to upload.", HttpStatus.BAD_REQUEST);
            }
            if (!"application/pdf".equals(file.getContentType())) {
                return new ResponseEntity<>("Only PDF files are allowed.", HttpStatus.BAD_REQUEST);
            }

            try {
                String fileName = UUID.randomUUID().toString() + ".pdf";
                // Upload PDF to MinIO
                minioService.uploadFile(Constants.MINIO_BUCKET_NAME, fileName, file.getInputStream(), file.getContentType());
                log.info("PDF uploaded to MinIO: {}/{}", Constants.MINIO_BUCKET_NAME, fileName);

                // Send message to RabbitMQ for processing
                PdfProcessRequest request = new PdfProcessRequest(fileName, file.getOriginalFilename(), Constants.MINIO_BUCKET_NAME);
                messageProducer.sendMessage(request);
                log.info("PDF processing request sent to RabbitMQ for file: {}", fileName);

                return new ResponseEntity<>("PDF uploaded and queued for processing: " + file.getOriginalFilename(), HttpStatus.OK);
            } catch (Exception e) {
                log.error("Error uploading PDF or sending message to queue: {}", e.getMessage(), e);
                return new ResponseEntity<>("Failed to upload PDF or queue for processing: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        });
    }

    /**
     * Endpoint to download a PDF file by its stored file name (UUID).
     *
     * @param pdfFileName The unique file name (UUID) of the PDF to download.
     * @return ResponseEntity containing the PDF file as a stream.
     */
    @GetMapping("/download/{pdfFileName}")
    public CompletableFuture<ResponseEntity<InputStreamResource>> downloadPdf(@PathVariable String pdfFileName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                InputStream pdfStream = minioService.downloadFile(Constants.MINIO_BUCKET_NAME, pdfFileName);
                InputStreamResource resource = new InputStreamResource(pdfStream);

                HttpHeaders headers = new HttpHeaders();
                // We try to fetch the original file name from the database to suggest it for download
                String originalFileName = dataQueryService.getOriginalFileNameByPdfFileName(pdfFileName)
                        .orElse(pdfFileName); // Fallback to pdfFileName if not found

                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + originalFileName + "\"");
                headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_PDF_VALUE);

                return ResponseEntity.ok()
                        .headers(headers)
                        .contentLength(pdfStream.available()) // This might not be accurate for all streams
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(resource);
            } catch (Exception e) {
                log.error("Error downloading PDF {}: {}", pdfFileName, e.getMessage(), e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
        });
    }

    /**
     * Endpoint to get all extracted data.
     *
     * @return List of ExtractedDataDto.
     */
    @GetMapping("/data")
    public CompletableFuture<ResponseEntity<List<ExtractedDataDto>>> getAllExtractedData() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<ExtractedDataDto> data = dataQueryService.getAllExtractedData();
                return new ResponseEntity<>(data, HttpStatus.OK);
            } catch (Exception e) {
                log.error("Error retrieving extracted data: {}", e.getMessage(), e);
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        });
    }

    /**
     * Endpoint to get extracted data by PDF file name.
     *
     * @param pdfFileName The name of the PDF file.
     * @return ExtractedDataDto if found, otherwise 404.
     */
    @GetMapping("/data/{pdfFileName}")
    public CompletableFuture<ResponseEntity<ExtractedDataDto>> getExtractedDataByPdfFileName(@PathVariable String pdfFileName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ExtractedDataDto data = dataQueryService.getExtractedDataByPdfFileName(pdfFileName);
                if (data != null) {
                    return new ResponseEntity<>(data, HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }
            } catch (Exception e) {
                log.error("Error retrieving extracted data for {}: {}", pdfFileName, e.getMessage(), e);
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        });
    }
}
