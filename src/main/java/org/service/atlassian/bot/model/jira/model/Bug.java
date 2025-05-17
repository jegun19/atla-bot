package org.service.atlassian.bot.model.jira.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.langchain4j.model.output.structured.Description;

@Description("This object represents information that user provides to create a Bug issue in JIRA.")
public class Bug extends Issue {
    @JsonProperty(required = true)
    @Description("Environment in which the bug was found")
    private String environment;

    @Override
    public String toString() {
        return "Bug{" +
                "environment='" + environment + '\'' +
                ", project=" + getProject() +
                ", description='" + getDescription() + '\'' +
                ", summary='" + getSummary() + '\'' +
                ", priority=" + getPriority() +
                ", assignee='" + getAssignee() + '\'' +
                '}';
    }
}
