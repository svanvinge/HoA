package com.odevo.hoa.worker.consumer;

import com.odevo.hoa.common.dto.PdfProcessRequest;
import com.odevo.hoa.common.util.Constants;
import com.odevo.hoa.worker.service.PdfProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ message consumer for PDF processing requests.
 * Listens to the defined queue and triggers PDF processing.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MessageConsumer {

    private final PdfProcessingService pdfProcessingService;

    /**
     * Listens for messages on the PDF processing queue.
     * When a message (PdfProcessRequest) is received, it triggers the PDF processing.
     *
     * @param request The PdfProcessRequest received from the queue.
     */
    @RabbitListener(queues = Constants.RABBITMQ_QUEUE_NAME)
    public void receivePdfProcessRequest(PdfProcessRequest request) {
        log.info("Received PDF processing request from RabbitMQ: {}", request);
        try {
            pdfProcessingService.processPdf(request);
            log.info("Successfully processed PDF: {}", request.getPdfFileName());
        } catch (Exception e) {
            log.error("Error processing PDF request for file {}: {}", request.getPdfFileName(), e.getMessage(), e);
            // In a real application, you might want to send to a dead-letter queue or retry.
        }
    }
}
