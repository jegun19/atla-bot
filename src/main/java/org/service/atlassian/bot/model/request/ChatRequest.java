package org.service.atlassian.bot.model.request;

import lombok.Data;

@Data
public class ChatRequest {
    private Integer userId;
    private String userPrompt;
}
