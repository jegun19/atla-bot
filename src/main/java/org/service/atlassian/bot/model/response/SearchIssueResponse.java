package org.service.atlassian.bot.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.langchain4j.model.output.structured.Description;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchIssueResponse {
    @JsonProperty("issues")
    @Description("This field represents the list of issues returned by the JIRA search API.")
    private List<Issues> issues;

    @Data
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Issues {
        @JsonProperty("id")
        private String id;

        @JsonProperty("self")
        private String self;

        @JsonProperty("key")
        private String key;

        @JsonProperty("fields")
        private Map<String, Object> fields; // Use a Map to store dynamic fields
    }
}
