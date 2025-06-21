package org.service.atlassian.bot.service;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.Result;
import lombok.extern.slf4j.Slf4j;
import org.service.atlassian.bot.agent.JiraAssistant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ChatService {
    @Autowired
    private JiraAssistant jiraAssistant;

    public String chat(String userPrompt, Integer userId) {
        Result<String> result = jiraAssistant.chat(userPrompt);
        return result.content();
    }
}
