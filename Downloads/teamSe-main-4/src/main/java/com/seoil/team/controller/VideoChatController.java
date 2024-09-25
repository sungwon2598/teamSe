package com.seoil.team.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VideoChatController {

    @GetMapping("/video")
    public String index() {
        return "vid";
    }
}