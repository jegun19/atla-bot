package org.service.atlassian.bot.model.jira.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

@Description("JIRA issue object which will be extended by other issue types. Do not use this class directly, use the subclass instead")
@Data
public class Issue {
    @JsonProperty(required = true)
    @Description("the project in which the issue will be created")
    private Project project;

    @JsonProperty(required = true)
    @Description("description of the task")
    private String description;

    @JsonProperty(required = true)
    @Description("summary of the task")
    private String summary;

    @JsonProperty(required = false)
    @Description("priority of the task")
    private String priority;

    @JsonProperty(required = false)
    @Description("assignee of the task")
    private String assignee;

    @Override
    public String toString() {
        return "Issue{" +
                "project=" + project +
                ", description='" + description + '\'' +
                ", summary='" + summary + '\'' +
                ", priority=" + priority +
                ", assignee='" + assignee + '\'' +
                '}';
    }
}