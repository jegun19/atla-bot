package org.service.atlassian.bot.model.jira.request;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateIssueRequestPayload {

    @JsonProperty("fields")
    private Fields fields;

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Fields {
        @JsonProperty("issuetype")
        private IssueTypePayload issueType;

        @JsonProperty("project")
        private ProjectPayload project;

        @JsonProperty("reporter")
        private ReporterPayload reporter;

        @JsonProperty("summary")
        private String summary;

        @JsonProperty("priority")
        private PriorityPayload priority;

        @JsonProperty("description")
        private String description;

        @JsonProperty("customfield_10037")
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String acceptanceCriteria;

        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String environment;
    }

    @Data
    @Builder
    public static class ProjectPayload {
        private String id;
    }

    @Data
    @Builder
    public static class ReporterPayload {
        private String id;
    }

    @Data
    @Builder
    public static class IssueTypePayload {
        private String id;
    }

    @Data
    @Builder
    public static class PriorityPayload {
        private String id;
    }
}
