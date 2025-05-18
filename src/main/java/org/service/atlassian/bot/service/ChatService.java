package org.service.atlassian.bot.service;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.service.atlassian.bot.agent.JiraAssistant;
import org.service.atlassian.bot.config.GeminiProperties;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ChatService {
    private final JiraAssistant jiraAssistant;
    private final JiraService jiraService;

    public ChatService(JiraService jiraService, GeminiProperties geminiProperties) {
        ChatLanguageModel gemini = GoogleAiGeminiChatModel.builder()
                .apiKey(geminiProperties.getApiKey())
                .temperature(0.0)
                .logRequestsAndResponses(true)
                .modelName(geminiProperties.getModel()).build();

        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(20);
        this.jiraService = jiraService;
        this.jiraAssistant = AiServices
                .builder(JiraAssistant.class)
                .chatLanguageModel(gemini)
                .tools(jiraService)
                .chatMemoryProvider(memoryId -> chatMemory).build();
    }

    public String chat(String userPrompt, Integer userId) {
        return jiraAssistant.chat(userPrompt);
    }
}
