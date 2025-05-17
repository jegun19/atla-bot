package org.service.atlassian.bot.model.response;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

@Data
@Description("This object represents the response received after creating a new JIRA issue.")
public class CreateIssueResponse {
    @Description("This field represents the ID of the JIRA issue.")
    private String id;
    @Description("This field represents the key of the JIRA issue.")
    private String key;
    @Description("This field represents the self link of the JIRA issue.")
    private String self;
}
