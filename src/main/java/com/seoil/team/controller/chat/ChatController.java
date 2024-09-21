package com.seoil.team.controller.chat;

import com.seoil.team.dto.chat.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Controller
public class ChatController {

    private List<String> users = new ArrayList<>();

    // 팝업 채팅 페이지를 제공하는 GET 매핑 추가
    @GetMapping("/popupchat")
    public String popupChat() {
        return "popupchat";
    }

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(ChatMessage message) {
        if (message.type() == ChatMessage.MessageType.JOIN){
            users.add(message.sender());
            updateUserList();
            return new ChatMessage(message.sender(), "님이 채팅방에 입장했습니다.", ChatMessage.MessageType.JOIN);
        } else if (message.type() == ChatMessage.MessageType.LEAVE) {
            users.remove(message.sender());
            updateUserList();
            return new ChatMessage(message.sender(), "님이 채팅방에서 나갔습니다.", ChatMessage.MessageType.LEAVE);
        }
        return message;
    }

    @MessageMapping("/chat.updateUsers")
    @SendTo("/topic/users")
    public List<String> updateUserList() {
        return users;
    }
}
