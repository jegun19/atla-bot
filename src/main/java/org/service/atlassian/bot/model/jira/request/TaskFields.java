package org.service.atlassian.bot.model.jira.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

@Description("This object represents a payload which will be used to create a new Task in JIRA")
@Data
public class TaskFields {
    @JsonProperty(required = true)
    @Description("ID of the issue type")
    private String issueTypeId;

    @JsonProperty(required = true)
    @Description("ID of the reporter")
    private String reporterId;

    @JsonProperty(required = true)
    @Description("ID of the project")
    private String projectId;

    @JsonProperty(required = true)
    @Description("description of the issue")
    private String description;

    @JsonProperty(required = true)
    @Description("summary of the issue")
    private String summary;

    @JsonProperty(required = true)
    @Description("priority of the issue")
    private String priority;
}
