package org.service.atlassian.bot.config;

import dev.langchain4j.agent.tool.Tool;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "jira")
@Getter
@Setter
@Slf4j
public class JiraProperties {
    private String url;
    private String authToken;
    private String reporterId;
}
