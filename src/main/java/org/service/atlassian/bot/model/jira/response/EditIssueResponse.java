package org.service.atlassian.bot.model.jira.response;

import lombok.Data;

@Data
public class EditIssueResponse {

    private String id;
    private String key;
    private Object fields;
}
