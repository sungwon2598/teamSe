package com.seoil.team.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seoil.team.dto.request.ai.RoadmapRequest;
import com.seoil.team.dto.request.ai.WeeklyRoadmapResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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

    public WeeklyRoadmapResponse generateWeeklyRoadmap(RoadmapRequest request) {
        String prompt = createPrompt(request);
        String apiResponse = callOpenAIAPI(prompt);
        return parseResponse(apiResponse);
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
        requestBody.put("model", "gpt-4o");
        requestBody.put("messages", new Object[]{
                Map.of("role", "system", "content", "You are a project management expert."),
                Map.of("role", "user", "content", prompt)
        });
        requestBody.put("temperature", 0.7);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        Map<String, Object> response = restTemplate.postForObject(apiUrl, request, Map.class);
        return ((Map<String, String>) ((Map<String, Object>) ((java.util.ArrayList<?>) response.get("choices")).get(
                0)).get("message")).get("content");
    }

    private WeeklyRoadmapResponse parseResponse(String apiResponse) {
        try {
            Map<String, Object> responseMap = objectMapper.readValue(apiResponse, new TypeReference<Map<String, Object>>() {});
            List<Map<String, Object>> weeklyPlans = (List<Map<String, Object>>) responseMap.get("weeklyPlans");
            List<String> overallTips = (List<String>) responseMap.get("overallTips");
            String curriculumEvaluation = (String) responseMap.get("curriculumEvaluation");

            Map<Integer, String> weeklyDescriptions = new HashMap<>();
            for (Map<String, Object> plan : weeklyPlans) {
                int week = (int) plan.get("week");
                String description = (String) plan.get("description");
                weeklyDescriptions.put(week, description);
            }

            return new WeeklyRoadmapResponse(weeklyDescriptions, overallTips, curriculumEvaluation);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse API response", e);
        }
    }
}