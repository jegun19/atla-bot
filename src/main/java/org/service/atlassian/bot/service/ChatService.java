package org.service.atlassian.bot.service;

import dev.langchain4j.service.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.atlassian.bot.agent.JiraAssistant;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {
    private final JiraAssistant jiraAssistant;

    public String chat(String userPrompt, Integer userId) {
        Result<String> result = jiraAssistant.chat(userPrompt);
        return result.content();
    }
}
