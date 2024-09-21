package com.seoil.team.controller;

import com.seoil.team.dto.request.ai.RoadmapRequest;
import com.seoil.team.dto.request.ai.WeeklyRoadmapResponse;
import com.seoil.team.service.WeeklyRoadmapGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Controller
@RequestMapping("/roadmap")
public class RoadmapController {
    private final WeeklyRoadmapGeneratorService roadmapService;
    private final Map<Long, WeeklyRoadmapResponse> roadmapCache = new ConcurrentHashMap<>();

    public RoadmapController(WeeklyRoadmapGeneratorService roadmapService) {
        this.roadmapService = roadmapService;
    }

    @GetMapping
    public String showRoadmapForm(Model model) {
        model.addAttribute("roadmapRequest", new RoadmapRequest());
        return "roadmap-form";
    }

    @PostMapping("/generate")
    public String generateRoadmap(@ModelAttribute RoadmapRequest request, Model model) {
        Long requestId = System.currentTimeMillis();
        model.addAttribute("generating", true);
        model.addAttribute("requestId", requestId);

        roadmapService.generateWeeklyRoadmapAsync(request)
                .thenAccept(response -> roadmapCache.put(requestId, response));

        return "roadmap-processing";
    }

    @GetMapping("/status/{requestId}")
    @ResponseBody
    public ResponseEntity<?> checkStatus(@PathVariable Long requestId) {
        if (roadmapCache.containsKey(requestId)) {
            return ResponseEntity.ok(Map.of("status", "COMPLETED"));
        } else {
            return ResponseEntity.ok(Map.of("status", "PROCESSING"));
        }
    }

    @GetMapping("/result/{requestId}")
    public String showResult(@PathVariable Long requestId, Model model) {
        WeeklyRoadmapResponse response = roadmapCache.get(requestId);
        if (response != null) {
            model.addAttribute("roadmap", response);
            roadmapCache.remove(requestId);  // 결과를 반환한 후 캐시에서 제거
            return "roadmap-result";
        } else {
            return "redirect:/roadmap";  // 결과가 없으면 폼 페이지로 리다이렉트
        }
    }
}