package org.service.atlassian.bot.exception;

public class SensitiveContentException extends RuntimeException {
    public SensitiveContentException(String message) {
        super(message);
    }
}