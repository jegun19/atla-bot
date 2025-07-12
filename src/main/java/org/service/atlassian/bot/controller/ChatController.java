package org.service.atlassian.bot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.atlassian.bot.model.request.ChatRequest;
import org.service.atlassian.bot.service.ChatService;
import org.service.atlassian.bot.util.SensitiveContentChecker;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final SensitiveContentChecker sensitiveContentChecker;

    @PostMapping("/chat")
    public String chat(@RequestBody ChatRequest chatRequest) {
        sensitiveContentChecker.validate(chatRequest.getUserPrompt());
        return chatService.chat(chatRequest.getUserPrompt(), chatRequest.getUserId());
    }
}
