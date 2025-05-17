package org.service.atlassian.bot.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.langchain4j.model.output.structured.Description;
import lombok.Data;
import org.service.atlassian.bot.model.jira.model.IssueType;

import java.util.List;

@Description("Response object for getting issue types from JIRA.")
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class GetIssueTypesResponse {
    @Description("List of issue types")
    private List<IssueType> issueTypes;
}
