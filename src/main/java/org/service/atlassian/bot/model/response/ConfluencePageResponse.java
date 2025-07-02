package org.service.atlassian.bot.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ConfluencePageResponse {
    private String id;
    private String title;
    private String type;
    private Body body;
    @JsonProperty("_links")
    private Links _links;

    @Data
    public static class Body {
        private Storage storage;
    }

    @Data
    public static class Storage {
        private String value;
        private String representation;
    }

    @Data
    public static class Links {
        private String webui;
    }
}
