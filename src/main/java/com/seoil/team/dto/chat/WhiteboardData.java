package com.seoil.team.dto.chat;

import java.util.List;
//매우 유동적
public record WhiteboardData(
        String user,
        String action,
        double x,
        double y,
        String color,
        int size) {
}
