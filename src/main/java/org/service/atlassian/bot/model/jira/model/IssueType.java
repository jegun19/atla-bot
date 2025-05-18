package org.service.atlassian.bot.model.jira.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Description("This is an object representing a JIRA issue type.")
@Data
public class IssueType {

    @Description("Name of the issue type")
    private String name;

    @Description("ID of the issue type")
    private String id;
}
