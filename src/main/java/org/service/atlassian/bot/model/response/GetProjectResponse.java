package org.service.atlassian.bot.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.service.atlassian.bot.model.jira.model.Project;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetProjectResponse {
    private List<Project> values;
}