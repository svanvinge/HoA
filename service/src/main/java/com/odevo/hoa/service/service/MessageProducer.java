package com.odevo.hoa.service.service;

import com.odevo.hoa.common.dto.PdfProcessRequest;
import com.odevo.hoa.common.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for sending messages to RabbitMQ.
 * This producer will send PdfProcessRequest objects to the worker queue.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Sends a PDF processing request message to the RabbitMQ queue.
     * The message is sent to a topic exchange with a specific routing key.
     *
     * @param request The PdfProcessRequest object containing details of the PDF to process.
     */
    public void sendMessage(PdfProcessRequest request) {
        log.info("Sending message to RabbitMQ: {}", request);
        rabbitTemplate.convertAndSend(Constants.RABBITMQ_EXCHANGE_NAME, Constants.RABBITMQ_ROUTING_KEY, request);
    }
}
