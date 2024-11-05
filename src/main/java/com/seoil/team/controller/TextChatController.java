package com.seoil.team.controller;

import com.seoil.team.domain.member.Member;
import com.seoil.team.dto.chat.ChatDTO;
import com.seoil.team.dto.chat.MessageType;
import com.seoil.team.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;

@Slf4j
@Controller
@RequiredArgsConstructor
public class TextChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;

    /**
     * 사용자가 방에 입장할 때 호출되는 메서드
     * @param chatDTO 입장 메시지 정보
     * @param member 현재 인증된 사용자 (자동 주입)
     */
    @MessageMapping("/chat/join")
    public void joinRoom(@Payload ChatDTO chatDTO, @AuthenticationPrincipal Member member) {
        ChatDTO joinMessage = new ChatDTO(
                chatDTO.type(),
                chatDTO.roomId(),
                chatDTO.sender(),
                member.getUsername() + "님이 입장하셨습니다.",
                LocalDateTime.now()
        );

        messagingTemplate.convertAndSend("/sub/chat/room/" + chatDTO.roomId(), joinMessage);
    }

    /**
     * 일반 채팅 메시지를 전송하는 메서드
     * @param chatDTO 채팅 메시지 정보
     */
    @MessageMapping("/chat/sendMessage")
    public void sendMessage(@Payload ChatDTO chatDTO) {
        ChatDTO sendMessage = new ChatDTO(
                chatDTO.type(),
                chatDTO.roomId(),
                chatDTO.sender(),
                chatDTO.message(),
                LocalDateTime.now()
        );

        messagingTemplate.convertAndSend("/sub/chat/room/" + chatDTO.roomId(), sendMessage);
    }

    /**
     * 사용자가 방을 나갈 때 호출되는 메서드
     * @param chatDTO 퇴장 메시지 정보
     * @param member 현재 인증된 사용자 (자동 주입)
     */
    @MessageMapping("/chat/leave")
    public void leaveRoom(@Payload ChatDTO chatDTO, @AuthenticationPrincipal Member member) {
        chatService.minusUserCnt(chatDTO.roomId());
        chatService.delUser(chatDTO.roomId(), member.getId().toString());

        ChatDTO leaveMessage = new ChatDTO(
                chatDTO.type(),
                chatDTO.roomId(),
                member.getName(),
                member.getName() + "님이 퇴장하셨습니다.",
                LocalDateTime.now()
        );

        messagingTemplate.convertAndSend("/sub/chat/room/" + chatDTO.roomId(), leaveMessage);
    }
}
