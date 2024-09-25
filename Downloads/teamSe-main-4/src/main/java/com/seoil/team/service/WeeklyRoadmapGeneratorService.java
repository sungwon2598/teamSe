package com.seoil.team.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seoil.team.dto.request.ai.RoadmapRequest;
import com.seoil.team.dto.request.ai.WeeklyRoadmapResponse;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class WeeklyRoadmapGeneratorService {

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public WeeklyRoadmapGeneratorService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Async
    public CompletableFuture<WeeklyRoadmapResponse> generateWeeklyRoadmapAsync(RoadmapRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            String prompt = createPrompt(request);
            String apiResponse = callOpenAIAPI(prompt);
            log.info("Raw API Response (first 1000 characters): {}",
                    apiResponse.length() > 1000 ? apiResponse.substring(0, 1000) + "..." : apiResponse);
            return parseResponse(apiResponse);
        });
    }

    private String createPrompt(RoadmapRequest request) {
        return String.format(
                "다음 프로젝트에 대한 주차별 로드맵을 JSON 형식으로 생성해주세요:\n" +
                        "주제: %s\n" +
                        "현재 수준: %s\n" +
                        "목표 수준: %s\n" +
                        "기간: %s주\n\n" +
                        "로드맵은 다음 구조를 따라주세요:\n" +
                        "{\n" +
                        "  \"weeklyPlans\": [\n" +
                        "    {\n" +
                        "      \"week\": 1,\n" +
                        "      \"description\": \"첫 주 계획에 대한 상세 설명. 목표, 작업, 마일스톤 등을 포함.\"\n" +
                        "    },\n" +
                        "    ...\n" +
                        "  ],\n" +
                        "  \"overallTips\": [\"전체 프로젝트에 대한 주의사항 및 팁\"],\n" +
                        "  \"curriculumEvaluation\": \"전체 커리큘럼의 내용과 기간의 적합성에 대한 평가\"\n" +
                        "}\n" +
                        "각 주차별 설명은 하나의 긴 문자열로 작성해주세요. 현재 수준과 목표 수준을 고려하여 로드맵을 생성해주세요.",
                request.getTopic(), request.getCurrentLevel(), request.getGoalLevel(), request.getDuration()
        );
    }

    private String callOpenAIAPI(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", "You are a project management expert."),
                Map.of("role", "user", "content", prompt)
        ));
        requestBody.put("temperature", 0.7);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else {
            log.error("API call failed. Status: {}, Body: {}", response.getStatusCode(), response.getBody());
            throw new RuntimeException("API call failed with status: " + response.getStatusCode());
        }
    }

    private String preprocessResponse(String apiResponse) {
        // 백틱과 다른 잠재적으로 문제가 될 수 있는 문자 제거
        String preprocessed = apiResponse.replaceAll("[`\u0000-\u001F]", "").trim();
        log.info("Preprocessed response (first 1000 characters): {}",
                preprocessed.length() > 1000 ? preprocessed.substring(0, 1000) + "..." : preprocessed);
        return preprocessed;
    }

    private boolean isValidJson(String json) {
        try {
            objectMapper.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    private WeeklyRoadmapResponse parseResponse(String apiResponse) {
        try {
            JsonNode rootNode = objectMapper.readTree(apiResponse);
            String content = rootNode.path("choices").get(0).path("message").path("content").asText();

            JsonNode contentNode = objectMapper.readTree(content);

            List<Map<String, Object>> weeklyPlans = objectMapper.convertValue(contentNode.get("weeklyPlans"), new TypeReference<List<Map<String, Object>>>() {});
            List<String> overallTips = objectMapper.convertValue(contentNode.get("overallTips"), new TypeReference<List<String>>() {});
            String curriculumEvaluation = contentNode.get("curriculumEvaluation").asText();

            Map<Integer, String> weeklyDescriptions = new HashMap<>();
            for (Map<String, Object> plan : weeklyPlans) {
                int week = ((Integer) plan.get("week")).intValue();
                String description = (String) plan.get("description");
                weeklyDescriptions.put(week, description);
            }

            return new WeeklyRoadmapResponse(weeklyDescriptions, overallTips, curriculumEvaluation);
        } catch (Exception e) {
            log.error("Error parsing API response: {}", apiResponse, e);
            throw new RuntimeException("Failed to parse API response", e);
        }
    }
}