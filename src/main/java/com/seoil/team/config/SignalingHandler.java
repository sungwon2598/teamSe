package com.seoil.team.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SignalingHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Map<String, WebSocketSession>> rooms = new ConcurrentHashMap<>();//redis변환//연결할때만 사용된다.

    private static final String MSG_OFFER = "offer";
    private static final String MSG_ANSWER = "answer";
    private static final String MSG_ICE = "ice";
    private static final String MSG_JOIN = "join";
    private static final String MSG_LEAVE = "leave";

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("New WebSocket connection: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            Map<String, Object> msg = objectMapper.readValue(message.getPayload(), Map.class);
            String type = (String) msg.get("type");
            String roomId = (String) msg.get("roomId");

            if (roomId == null) {
                System.out.println("Room id is null");
                return;
            }

            switch (type) {

                case MSG_JOIN://방을 들어올 때
                    handleJoinRoom(roomId, session);
                    break;

                case MSG_OFFER:
                case MSG_ANSWER:
                case MSG_ICE:
                    broadcastToRoom(roomId, session, message);
                    break;

                case MSG_LEAVE://방을 나갈 때
                    handleLeaveRoom(roomId, session);
                    break;

                default:
                    log.info("Unknown message type: {}", type);
                    break;
            }
        } catch (Exception e) {
            log.info("Error handling Message: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleJoinRoom(String roomId, WebSocketSession session) {
        //해당 방이 있으면 접속, 없으면 생성 및 접속
        rooms.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>()).put(session.getId(), session);
        log.info("Session {} joined room {}", session.getId(), roomId);
    }

    private void broadcastToRoom(String roomId, WebSocketSession sender, TextMessage message) {
        // Null 체크 및 사전 검증
        if (roomId == null || message == null) {
            log.info("Invalid roomId or message. Broadcast aborted.");
            return;
        }

        // 해당 roomId에 대한 방이 없으면 리턴
        Map<String, WebSocketSession> room = rooms.get(roomId);
        if (room == null) {
            log.info("Room {} not found.", roomId);
            return;
        }

        // 방에 있는 다른 모든 세션에게 메시지 전송
        room.values().forEach(webSocketSession -> {
            // 보낸 사람(sender)에게는 메시지를 전송하지 않음
            if (webSocketSession.isOpen() && !sender.getId().equals(webSocketSession.getId())) {
                try {
                    webSocketSession.sendMessage(message);
                } catch (IOException e) {
                    log.info("Failed to send message to session {}: {}", webSocketSession.getId(), e.getMessage());
                }
            }
        });
    }

    private void handleLeaveRoom(String roomId, WebSocketSession session) {
        Map<String, WebSocketSession> room = rooms.get(roomId);
        if (room != null) {
            room.remove(session.getId());
            log.info("Session removed: {} left room id: {}", session.getId(), roomId);
        }
    }

    /*세션이 끊겼을 때 != 방을 나갈 때
      방을 나가도 세션은 유지 다른 동작들 가능
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket connection closed: {}", session.getId());
        rooms.values().forEach(room -> room.remove(session.getId()));
    }
}