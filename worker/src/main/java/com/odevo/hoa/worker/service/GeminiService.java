package com.odevo.hoa.worker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.odevo.hoa.common.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

/**
 * Service for interacting with the Gemini API to extract data.
 * This is a simplified placeholder. In a real application, you'd handle:
 * - PDF content extraction (e.g., convert PDF to text/images)
 * - Constructing the prompt with the extracted content
 * - Handling Gemini API responses (parsing JSON, extracting relevant parts)
 * - Error handling and retry mechanisms.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper; // To build JSON requests and parse responses

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    /**
     * Calling the Gemini API to extract data from provided text content.
     *
     * @param pdfInputStream The pdf content as an inputstream.
     * @return A map containing extracted JSON data and a simulated vector.
     */
    public JsonNode extractDataFromPdfContent(InputStream pdfInputStream) throws IOException {
        log.info("Calling Global Gemini API for PDF content analysis (Base64 inline)...");

        // Convert InputStream to byte array and then to Base64
        byte[] pdfBytes = pdfInputStream.readAllBytes();
        String base64EncodedPdf = Base64.getEncoder().encodeToString(pdfBytes);
        log.debug("PDF size (bytes): {}", pdfBytes.length);
        log.debug("Base64 encoded length: {}", base64EncodedPdf.length());

        // Define your JSON schema as a string (this will be part of your prompt)
        String jsonSchema = """
                {
                  "type": "object",
                  "properties": {
                    "title": { "type": "string" },
                    "auditor": { "type": "string", "nullable": true },
                    "summary": { "type": "string" },
                    "keywords": { "type": "array", "items": { "type": "string" } },
                    "board_members": {
                      "type": "array",
                      "items": {
                        "type": "object",
                        "properties": {
                          "name": { "type": "string" },
                          "role": { "type": "string", "nullable": true } // Role might not always be available
                        },
                        "required": ["name"] // Name is required, role is optional
                      }
                    },
                    "financial_year": { "type": "string", "example": "2021-2022" },
                    "loans": {
                      "type": "array",
                      "items": {
                        "type": "object",
                        "properties": {
                          "debt": { "type": "string" },
                          "interrest_rate": { "type": "string", "nullable": true } // Role might not always be available
                        },
                        "required": ["debt"]
                      }
                    }
                  },
                  "required": ["title", "summary", "keywords", "board_members", "financial_year", "loans"]
                }
                """;

        // Build the request body for Gemini API (direct JSON construction)
        ObjectNode rootNode = objectMapper.createObjectNode();
        ArrayNode contentsArray = objectMapper.createArrayNode();
        ObjectNode userContent = objectMapper.createObjectNode();
        ArrayNode partsArray = objectMapper.createArrayNode();

        // Part 1: Text instruction with the JSON schema
        partsArray.add(objectMapper.createObjectNode().put("text", "Extract key information (specifically: title, auditor, summary, keywords, board_members, financial_year, loans) " + "from the following annual report PDF. Provide the output as a JSON object strictly adhering to this schema:\n\n" + "```json\n" + jsonSchema + "\n```\n\n" + "Here is the PDF content:"));

        // Part 2: Inline Base64 encoded PDF data
        ObjectNode inlineData = objectMapper.createObjectNode();
        inlineData.put("mime_type", "application/pdf");
        inlineData.put("data", base64EncodedPdf);
        partsArray.add(objectMapper.createObjectNode().set("inline_data", inlineData));

        userContent.set("parts", partsArray);
        userContent.put("role", "user");
        contentsArray.add(userContent);
        rootNode.set("contents", contentsArray);

        // Generation config for JSON output type
        ObjectNode generationConfig = objectMapper.createObjectNode();
        // Note: For direct API calls, use snake_case for config properties
        generationConfig.put("response_mime_type", "application/json");
        rootNode.set("generation_config", generationConfig);

        String requestBody = rootNode.toString();
        log.trace("Gemini API Request: {}", requestBody);

        // Use your global base URL here
        String geminiApiUrl = String.format("%s/models/%s:generateContent?key=%s", Constants.GEMINI_API_BASE_URL, Constants.GEMINI_MODEL, geminiApiKey);

        Mono<String> responseMono = webClient.post().uri(geminiApiUrl).contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromValue(requestBody)).retrieve().bodyToMono(String.class).doOnError(e -> log.error("Error during Gemini API call: {}", e.getMessage(), e));

        String responseBody = responseMono.block(); // Blocking call for simplicity
        log.debug("Gemini API Response: {}", responseBody);

        // Parse the response
        try {
            JsonNode responseJson = objectMapper.readTree(responseBody);
            // The JSON from Gemini should now be directly at /candidates/0/content/parts/0/text
            String extractedJsonString = responseJson.at("/candidates/0/content/parts/0/text").asText("");

            if (extractedJsonString.isEmpty()) {
                log.warn("Gemini returned empty JSON string.");
                return objectMapper.createObjectNode();
            }

            JsonNode jsonData = objectMapper.readTree(extractedJsonString);
            log.debug("JSON Response: {}", jsonData.toPrettyString());
            return jsonData;
        } catch (Exception e) {
            log.error("Failed to parse Gemini API response or extract data: {}", e.getMessage(), e);
            return objectMapper.createObjectNode();
        }
    }
}
