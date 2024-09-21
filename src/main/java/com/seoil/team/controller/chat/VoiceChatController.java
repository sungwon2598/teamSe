package com.seoil.team.controller.chat;

import com.seoil.team.dto.chat.VoiceMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class VoiceChatController {

    @MessageMapping("/voice.send")
    @SendTo("/topic/voice")
    public VoiceMessage send(VoiceMessage message) {
        return message;
    }
}
