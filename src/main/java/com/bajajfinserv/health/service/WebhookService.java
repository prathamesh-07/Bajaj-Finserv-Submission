package com.bajajfinserv.health.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import com.bajajfinserv.health.model.SolutionRequest;
import com.bajajfinserv.health.model.WebhookRequest;
import com.bajajfinserv.health.model.WebhookResponse;

@Service
public class WebhookService {
    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);

    private final RestTemplate restTemplate;
    private final String generateWebhookUrl;
    private final String jwtToken;

    public WebhookService(
            RestTemplate restTemplate,
            @Value("${webhook.generate.url}") String generateWebhookUrl,
            @Value("${jwt.token}") String jwtToken) {
        this.restTemplate = restTemplate;
        this.generateWebhookUrl = generateWebhookUrl;
        this.jwtToken = jwtToken;
    }

    public void processWebhookFlow() {
        try {
            log.info("Starting webhook flow");
            
            // Step 1: Generate webhook
            WebhookRequest request = new WebhookRequest();
            request.setName("John Doe");
            request.setRegNo("REG12348");
            request.setEmail("john@example.com");

            log.debug("Sending webhook generation request: {}", request);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(jwtToken);

            HttpEntity<WebhookRequest> requestEntity = new HttpEntity<>(request, headers);

            ResponseEntity<WebhookResponse> responseEntity = restTemplate.postForEntity(
                generateWebhookUrl,
                requestEntity,
                WebhookResponse.class
            );

            WebhookResponse response = responseEntity.getBody();
            log.debug("Received webhook response: {}", response);

            if (response == null || response.getWebhook() == null || response.getAccessToken() == null) {
                throw new RuntimeException("Invalid webhook response: " + response);
            }

            // Step 2: Solve SQL problem based on regNo
            String finalQuery = solveSqlProblem(request.getRegNo());
            log.debug("Generated SQL query: {}", finalQuery);

            // Step 3: Submit solution using the access token from the response
            submitSolution(response.getWebhook(), response.getAccessToken(), finalQuery);
            log.info("Successfully completed webhook flow");

        } catch (HttpClientErrorException e) {
            log.error("HTTP error during webhook flow: {} - {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new RuntimeException("Failed to process webhook flow: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error processing webhook flow", e);
            throw new RuntimeException("Failed to process webhook flow", e);
        }
    }

    private String solveSqlProblem(String regNo) {
        // Since we specifically want Question 2, we'll always return Query 2
        // SQL solution for Question 2 (even regNo)
        return "SELECT c.CUSTOMER_NAME, c.CITY, " +
               "COUNT(o.ORDER_ID) AS TOTAL_ORDERS, " +
               "SUM(o.TOTAL_AMOUNT) AS TOTAL_AMOUNT, " +
               "MAX(o.ORDER_DATE) AS LAST_ORDER_DATE " +
               "FROM CUSTOMER c " +
               "JOIN ORDERS o ON c.CUSTOMER_ID = o.CUSTOMER_ID " +
               "GROUP BY c.CUSTOMER_NAME, c.CITY " +
               "HAVING COUNT(o.ORDER_ID) > 5 " +
               "ORDER BY SUM(o.TOTAL_AMOUNT) DESC";
    }

    private void submitSolution(String webhookUrl, String accessToken, String finalQuery) {
        log.debug("Submitting solution to webhook URL: {} with access token", webhookUrl);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Use the accessToken directly without adding "Bearer " prefix
        headers.set("Authorization", accessToken);
        
        // Debug what headers are being sent
        log.debug("Headers being sent: {}", headers);

        SolutionRequest solutionRequest = new SolutionRequest();
        solutionRequest.setFinalQuery(finalQuery);

        HttpEntity<SolutionRequest> request = new HttpEntity<>(solutionRequest, headers);
        
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                webhookUrl,
                request,
                String.class
            );
            
            log.debug("Solution submission response: {}", response.getBody());
        } catch (HttpClientErrorException e) {
            log.error("Error submitting solution: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        }
    }
} 