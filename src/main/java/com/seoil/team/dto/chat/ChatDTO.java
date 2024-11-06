package com.seoil.team.dto.chat;

import com.seoil.team.domain.member.Member;

import java.time.LocalDateTime;

public record ChatDTO(
        MessageType type,
        String roomId,
        String sender,
        String message,
        LocalDateTime timestamp) {
}
