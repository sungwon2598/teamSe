package com.seoil.team.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SignalingHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Map<String, WebSocketSession>> rooms = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("New WebSocket connection: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            Map<String, Object> msg = objectMapper.readValue(message.getPayload(), Map.class);
            String type = (String) msg.get("type");
            String roomId = (String) msg.get("roomId");

            if (roomId == null) {
                System.out.println("Room ID is null. Ignoring message.");
                return;
            }

            if ("join".equals(type)) {
                handleJoinRoom(roomId, session);
            } else {
                broadcastToRoom(roomId, session, message);
            }
        } catch (Exception e) {
            System.out.println("Error handling message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleJoinRoom(String roomId, WebSocketSession session) {
        rooms.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>()).put(session.getId(), session);
        System.out.println("Session " + session.getId() + " joined room " + roomId);
    }

    private void broadcastToRoom(String roomId, WebSocketSession sender, TextMessage message) throws IOException {
        Map<String, WebSocketSession> room = rooms.get(roomId);
        if (room != null) {
            for (WebSocketSession webSocketSession : room.values()) {
                if (webSocketSession.isOpen() && !sender.getId().equals(webSocketSession.getId())) {
                    webSocketSession.sendMessage(message);
                }
            }
        } else {
            System.out.println("Room " + roomId + " not found");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("WebSocket connection closed: " + session.getId());
        rooms.values().forEach(room -> room.remove(session.getId()));
    }
}