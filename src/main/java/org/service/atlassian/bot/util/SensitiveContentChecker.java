package org.service.atlassian.bot.util;

import lombok.RequiredArgsConstructor;
import org.service.atlassian.bot.exception.SensitiveContentException;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class SensitiveContentChecker {

    private final SensitivePatternProvider patternProvider;

    public void validate(String input) {
        if (input == null || input.isBlank()) return;

        // Pattern match
        for (Pattern pattern : patternProvider.getPatterns()) {
            if (pattern.matcher(input).find()) {
                throw new SensitiveContentException("Request contains sensitive data matching pattern: " + pattern);
            }
        }
    }
}

