package com.seoil.team.dto.chat;

public record ChatMessage(String sender, String content, MessageType type) {
    public enum MessageType {
        CHAT, JOIN, LEAVE
    }
}