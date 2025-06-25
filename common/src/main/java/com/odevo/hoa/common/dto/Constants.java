package com.odevo.hoa.common.util;

/**
 * Constants for the application - to be moved to application.yaml or secret vault.
 */
public final class Constants {

    private Constants() {
    }

    public static final String RABBITMQ_EXCHANGE_NAME = "pdf-processing-exchange";
    public static final String RABBITMQ_QUEUE_NAME = "pdf-processing-queue";
    public static final String RABBITMQ_ROUTING_KEY = "pdf.process";

    public static final String MINIO_BUCKET_NAME = "pdf-uploads";

    public static final String GEMINI_API_BASE_URL = "https://generativelanguage.googleapis.com/v1beta";
    public static final String GEMINI_MODEL = "gemini-2.0-flash"; // Or other suitable model
}
