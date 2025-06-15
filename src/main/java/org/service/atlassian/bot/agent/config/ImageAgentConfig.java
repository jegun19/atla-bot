package org.service.atlassian.bot.agent.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import lombok.RequiredArgsConstructor;
import org.service.atlassian.bot.config.GeminiProperties;
import org.service.atlassian.bot.config.ImageProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
@RequiredArgsConstructor
public class ImageAgentConfig {

    private final ImageProperties imageProperties;

    private final GeminiProperties geminiProperties;

    @Bean
    public ChatLanguageModel imageLanguageModel() {
        String imageApiKey = Objects.isNull(imageProperties.getApiKey())
                ? geminiProperties.getApiKey()
                : imageProperties.getApiKey();
        String imageModel = Objects.isNull(imageProperties.getModel())
                ? geminiProperties.getModel()
                : imageProperties.getModel();

        return GoogleAiGeminiChatModel.builder()
                .apiKey(imageApiKey)
                .temperature(0.0)
                .logRequestsAndResponses(true)
                .modelName(imageModel).build();
    }
}
