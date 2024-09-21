package com.seoil.team.dto.chat;

//매우 유동적
public record WhiteboardData(
        String user,
        String action,
        double x,
        double y,
        String color,
        int size) {
}
