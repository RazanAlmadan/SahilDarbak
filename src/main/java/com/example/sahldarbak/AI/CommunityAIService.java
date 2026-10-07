package com.example.sahldarbak.AI;

import com.example.sahldarbak.Api.ApiException;
import com.example.sahldarbak.DTO.Post.CommunityModerationDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommunityAIService {

    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com")
            .build();

    public CommunityModerationDTO moderatePost(
            String title,
            String content
    ) {

        try {

            String prompt = """
                    You are the AI moderator for Sahl Darbak,
                    a travel application community.

                    Your job is to check whether a community post
                    is appropriate to publish.

                    APPROVE the post if:
                    - It is related to travel, tourism, destinations,
                      experiences, hotels, food, activities, transportation,
                      or other travel-related topics.
                    - It is normal and appropriate user-generated content.

                    REJECT the post if:
                    - It contains hate speech.
                    - It contains harassment or threats.
                    - It contains sexual or extremely inappropriate content.
                    - It contains dangerous or illegal instructions.
                    - It is spam or clearly unrelated to the travel community.

                    Return ONLY valid JSON.

                    Use EXACTLY this structure:

                    {
                      "approved": true,
                      "reason": "Short explanation"
                    }

                    POST TITLE:
                    """ + title + """

                    POST CONTENT:
                    """ + content;

            String response = callGemini(prompt);

            return objectMapper.readValue(response, CommunityModerationDTO.class);

        } catch (ApiException e) {

            throw e;

        } catch (Exception e) {

            throw new ApiException("failed to moderate community post: " + e.getMessage());
        }
    }

    private String callGemini(String prompt) throws Exception {

        Map<String, Object> generationConfig =
                Map.of(
                        "responseMimeType",
                        "application/json"
                );

        Map<String, Object> requestBody =
                Map.of(
                        "contents",
                        List.of(
                                Map.of(
                                        "parts",
                                        List.of(
                                                Map.of(
                                                        "text",
                                                        prompt
                                                )
                                        )
                                )
                        ),
                        "generationConfig",
                        generationConfig
                );

        Map response =
                restClient.post()
                        .uri(
                                "/v1beta/models/"
                                        + "gemini-3.5-flash-lite:"
                                        + "generateContent"
                        )
                        .header(
                                "x-goog-api-key",
                                apiKey
                        )
                        .contentType(
                                MediaType.APPLICATION_JSON
                        )
                        .body(requestBody)
                        .retrieve()
                        .body(Map.class);

        if (response == null) {
            throw new ApiException(
                    "Gemini returned an empty response"
            );
        }

        List<Map<String, Object>> candidates =
                (List<Map<String, Object>>)
                        response.get("candidates");

        if (candidates == null
                || candidates.isEmpty()) {

            throw new ApiException(
                    "Gemini did not return a moderation result"
            );
        }

        Map<String, Object> content =
                (Map<String, Object>)
                        candidates
                                .get(0)
                                .get("content");

        List<Map<String, Object>> parts =
                (List<Map<String, Object>>)
                        content.get("parts");

        if (parts == null || parts.isEmpty()) {

            throw new ApiException(
                    "invalid Gemini moderation response"
            );
        }

        return ((String) parts.get(0).get("text"))
                .replace("```json", "")
                .replace("```", "")
                .trim();
    }
}
