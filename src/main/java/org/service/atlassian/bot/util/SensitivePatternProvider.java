package org.service.atlassian.bot.util;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Pattern;

@Component
@Getter
public class SensitivePatternProvider {

    private final Map<String, Pattern> patterns = Map.of(
            "NRIC/FIN", Pattern.compile("\\b[STFG]\\d{7}[A-Z]\\b"),
            "Bank Account Number", Pattern.compile("\\b\\d{9,18}\\b"),
            "Credit Card Number", Pattern.compile("\\b\\d{16}\\b")
    );
}