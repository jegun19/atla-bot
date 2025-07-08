package org.service.atlassian.bot.util;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
@Getter
public class SensitivePatternProvider {

    private final List<Pattern> patterns = List.of(
            // NRIC / FIN: Starts with S, T, F, G + 7 digits + 1 letter
            Pattern.compile("\\b[STFG]\\d{7}[A-Z]\\b"),
            // Bank account numbers: 9 to 18 digits
            Pattern.compile("\\b\\d{9,18}\\b"),
            // Credit card numbers: Basic 16-digit sequence
            Pattern.compile("\\b\\d{16}\\b")
    );
}
