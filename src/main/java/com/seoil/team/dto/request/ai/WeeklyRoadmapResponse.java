package com.seoil.team.dto.request.ai;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class WeeklyRoadmapResponse {
    private Map<Integer, String> weeklyDescriptions;
    private List<String> overallTips;
    private String curriculumEvaluation;
}
