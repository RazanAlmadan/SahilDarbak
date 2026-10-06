package com.example.sahldarbak.ExternalApi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class WhatsAppService {

    @Value("${whatsapp.phone-number-id}")
    private String phoneNumberId;

    @Value("${whatsapp.access-token}")
    private String accessToken;

    private final RestTemplate restTemplate = new RestTemplate();

    // sends a template message, failures never stop the caller
    public void sendTemplate(String toPhone, String templateName, List<String> params) {
        try {
            String url = "https://graph.facebook.com/v26.0/" + phoneNumberId + "/messages";

            List<Map<String, String>> parameters = params.stream()
                    .map(p -> Map.of("type", "text", "text", p))
                    .toList();

            Map<String, Object> body = Map.of(
                    "messaging_product", "whatsapp",
                    "to", toPhone.replace("+", ""),
                    "type", "template",
                    "template", Map.of(
                            "name", templateName,
                            "language", Map.of("code", "en"),
                            "components", List.of(Map.of("type", "body", "parameters", parameters))
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
        } catch (Exception e) {
            System.out.println("WhatsApp send failed: " + e.getMessage());
        }
    }

    // sends a plain text message, failures never stop the caller
    public void sendText(String toPhone, String text) {
        try {
            String url = "https://graph.facebook.com/v26.0/" + phoneNumberId + "/messages";

            Map<String, Object> body = Map.of(
                    "messaging_product", "whatsapp",
                    "to", toPhone.replace("+", ""),
                    "type", "text",
                    "text", Map.of("body", text)
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
        } catch (Exception e) {
            System.out.println("WhatsApp send failed: " + e.getMessage());
        }
    }
}