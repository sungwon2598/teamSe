package com.seoil.team.dto.request.ai;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RoadmapRequest {
    @NotBlank(message = "Topic is required")
    private String topic;

    @NotBlank(message = "Goal level is required")
    private String goalLevel;

    private String currentLevel;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 week")
    private Integer duration; // 주 단위

    // 기본 생성자
    public RoadmapRequest() {
    }

    // toString 메소드 오버라이드
    @Override
    public String toString() {
        return "RoadmapRequest{" +
                "topic='" + topic + '\'' +
                ", goalLevel='" + goalLevel + '\'' +
                ", duration=" + duration +
                '}';
    }
}