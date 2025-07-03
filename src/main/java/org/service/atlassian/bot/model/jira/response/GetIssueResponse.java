package org.service.atlassian.bot.model.jira.response;

import lombok.Data;

@Data
public class GetIssueResponse {

    private String id;
    private String key;
    private Object fields;
}
