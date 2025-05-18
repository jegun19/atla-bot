package org.service.atlassian.bot.model.jira.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.langchain4j.model.output.structured.Description;

@Description("This object represents information that user provides to create a Story issue in JIRA.")
public class Story extends Issue{

    @JsonProperty(required = true)
    @Description("Acceptance criteria of the story")
    public String acceptanceCriteria;
}
