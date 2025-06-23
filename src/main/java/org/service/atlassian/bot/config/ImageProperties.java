package org.service.atlassian.bot.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "image")
@Getter
@Setter
@Slf4j
public class ImageProperties {
    private String apiKey;
    private String model;
}
