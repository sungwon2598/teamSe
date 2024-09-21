package com.seoil.team.controller.chat;

import com.seoil.team.dto.chat.WhiteboardData;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WhiteboardController {

    @GetMapping("/whiteboard")//첫 입장 페이지
    public String showWhiteboard() {
        return "whiteboard";
    }

    @MessageMapping("/whiteboard.update")
    @SendTo("/topic/whiteboard")
    public WhiteboardData updateWhiteboard (WhiteboardData data) {
        return data;
    }
}
