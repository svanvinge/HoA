package com.odevo.hoa.worker.service;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * Service for interacting with MinIO (local S3 equivalent) from the worker.
 * Handles downloading and deleting files from the configured bucket.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MinioService {

    private final MinioClient minioClient;

    /**
     * Downloads a file from a specified MinIO bucket.
     *
     * @param bucketName The name of the bucket.
     * @param objectName The name of the object (file) to download.
     * @return An InputStream of the downloaded file.
     * @throws Exception if an error occurs during download.
     */
    public InputStream downloadFile(String bucketName, String objectName) throws Exception {
        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            log.info("File '{}' downloaded successfully from bucket '{}'.", objectName, bucketName);
            return stream;
        } catch (MinioException e) {
            log.error("MinIO error while downloading file {} from bucket {}: {}", objectName, bucketName, e.getMessage(), e);
            throw new RuntimeException("MinIO download failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error downloading file {} from bucket {}: {}", objectName, bucketName, e.getMessage(), e);
            throw new RuntimeException("File download failed: " + e.getMessage(), e);
        }
    }

    /**
     * Removes a file from a specified MinIO bucket.
     *
     * @param bucketName The name of the bucket.
     * @param objectName The name of the object (file) to remove.
     * @throws Exception if an error occurs during removal.
     */
    public void removeFile(String bucketName, String objectName) throws Exception {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            log.info("File '{}' removed successfully from bucket '{}'.", objectName, bucketName);
        } catch (MinioException e) {
            log.error("MinIO error while removing file {} from bucket {}: {}", objectName, bucketName, e.getMessage(), e);
            throw new RuntimeException("MinIO removal failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error removing file {} from bucket {}: {}", objectName, bucketName, e.getMessage(), e);
            throw new RuntimeException("File removal failed: " + e.getMessage(), e);
        }
    }
}
