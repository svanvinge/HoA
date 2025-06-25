package com.odevo.hoa.service.service;

import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * Service for interacting with MinIO (local S3 equivalent).
 * Handles uploading and downloading files to/from the configured bucket.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MinioService {

    private final MinioClient minioClient;

    /**
     * Uploads a file to a specified MinIO bucket.
     * If the bucket does not exist, it will be created.
     *
     * @param bucketName   The name of the bucket.
     * @param objectName   The name of the object (file) in the bucket.
     * @param inputStream  The input stream of the file to upload.
     * @param contentType  The content type of the file (e.g., "application/pdf").
     * @throws Exception if an error occurs during upload.
     */
    public void uploadFile(String bucketName, String objectName, InputStream inputStream, String contentType) throws Exception {
        // Check if the bucket exists; if not, create it
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            log.info("MinIO bucket '{}' created.", bucketName);
        } else {
            log.info("MinIO bucket '{}' already exists.", bucketName);
        }

        // Upload the object
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(inputStream, inputStream.available(), -1)
                        .contentType(contentType)
                        .build()
        );
        log.info("File '{}' uploaded successfully to bucket '{}'.", objectName, bucketName);
    }

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
}
