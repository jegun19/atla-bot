package org.service.atlassian.bot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.atlassian.bot.model.request.ChatRequest;
import org.service.atlassian.bot.service.ChatService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping("/chat")
    public String chat(@RequestBody ChatRequest chatRequest) {
        return chatService.chat(chatRequest.getUserPrompt(), chatRequest.getUserId());
    }
}
