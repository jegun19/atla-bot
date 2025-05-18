package org.service.atlassian.bot.model.enums;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Priority {
    HIGH("2"),
    MEDIUM("3"),
    LOW("4");

    private final String id;
}
