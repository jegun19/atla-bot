package org.service.atlassian.bot.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.service.atlassian.bot.exception.SensitiveContentException;

import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SensitiveContentCheckerTest {

    @Mock
    private SensitivePatternProvider patternProvider;

    @InjectMocks
    private SensitiveContentChecker sensitiveContentChecker;

    @Test
    void shouldThrowExceptionWhenSensitiveDataIsFound() {
        // Arrange
        Map<String, Pattern> mockPatterns = Map.of(
                "Credit Card Number", Pattern.compile("\\b\\d{16}\\b")
        );
        when(patternProvider.getPatterns()).thenReturn(mockPatterns);

        String input = "My credit card number is 1234567812345678";

        // Act & Assert
        assertThrows(SensitiveContentException.class, () -> sensitiveContentChecker.validate(input));
    }

    @Test
    void shouldNotThrowExceptionWhenNoSensitiveDataIsFound() {
        // Arrange
        Map<String, Pattern> mockPatterns = Map.of(
                "Credit Card Number", Pattern.compile("\\b\\d{16}\\b")
        );
        when(patternProvider.getPatterns()).thenReturn(mockPatterns);

        String input = "This is a safe message.";

        // Act & Assert
        sensitiveContentChecker.validate(input); // No exception should be thrown
    }

    @Test
    void shouldNotThrowExceptionForNullOrBlankInput() {
        // Act & Assert
        sensitiveContentChecker.validate(null); // No exception should be thrown
        sensitiveContentChecker.validate("");   // No exception should be thrown
    }
}