package com.seoil.team.controller;

import com.seoil.team.dto.request.ai.WeeklyRoadmapResponse;
import com.seoil.team.service.WeeklyRoadmapGeneratorService;
import com.seoil.team.dto.request.ai.RoadmapRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Controller
@RequestMapping("/roadmap")
public class RoadmapController {
    private final WeeklyRoadmapGeneratorService roadmapService;

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
        WeeklyRoadmapResponse response = roadmapService.generateWeeklyRoadmap(request);
        model.addAttribute("roadmap", response);
        return "roadmap-result";
    }
}