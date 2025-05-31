package org.service.atlassian.bot.agent.config;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import lombok.RequiredArgsConstructor;
import org.service.atlassian.bot.config.GeminiProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GeminiAgentConfig {

    private final GeminiProperties geminiProperties;

    @Bean
    public ChatLanguageModel geminiLanguageModel() {
        return GoogleAiGeminiChatModel.builder()
                .apiKey(geminiProperties.getApiKey())
                .temperature(0.0)
                .logRequestsAndResponses(true)
                .modelName(geminiProperties.getModel()).build();
    }

    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> MessageWindowChatMemory.withMaxMessages(20);
    }
}
