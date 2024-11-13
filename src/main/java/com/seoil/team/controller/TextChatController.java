package com.seoil.team.controller;

import com.seoil.team.dto.chat.ChatDTO;
import com.seoil.team.dto.chat.MessageType;
import com.seoil.team.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Slf4j
@Controller
@RequiredArgsConstructor
public class TextChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    @MessageMapping("/chat/join")
    public void joinRoom(@Payload ChatDTO chatDTO) {
        ChatDTO joinMessage = new ChatDTO(
                MessageType.JOIN,
                chatDTO.roomId(),
                chatDTO.sender(),
                chatDTO.sender() + "님이 입장하셨습니다.",
                LocalDateTime.now()
        );

        messagingTemplate.convertAndSend("/sub/chat/room/" + chatDTO.roomId(), joinMessage);
    }

    @MessageMapping("/chat/sendMessage")
    public void sendMessage(@Payload ChatDTO chatDTO) {
        ChatDTO sendMessage = new ChatDTO(
                MessageType.CHAT,
                chatDTO.roomId(),
                chatDTO.sender(),
                chatDTO.message(),
                LocalDateTime.now()
        );

        messagingTemplate.convertAndSend("/sub/chat/room/" + chatDTO.roomId(), sendMessage);
    }

    @MessageMapping("/chat/leave")
    public void leaveRoom(@Payload ChatDTO chatDTO) {
        chatService.minusUserCnt(chatDTO.roomId());
        chatService.delUser(chatDTO.roomId(), chatDTO.sender());

        ChatDTO leaveMessage = new ChatDTO(
                MessageType.LEAVE,
                chatDTO.roomId(),
                chatDTO.sender(),
                chatDTO.sender() + "님이 퇴장하셨습니다.",
                LocalDateTime.now()
        );

        messagingTemplate.convertAndSend("/sub/chat/room/" + chatDTO.roomId(), leaveMessage);
    }
}