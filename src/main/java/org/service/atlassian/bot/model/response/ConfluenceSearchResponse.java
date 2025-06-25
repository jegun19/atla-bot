package org.service.atlassian.bot.model.response;


import lombok.Data;

import java.util.List;

@Data
public class ConfluenceSearchResponse {
    private List<ConfluencePageResult> results;
    private int start;
    private int limit;
    private int size;

    @Data
    public static class ConfluencePageResult {
        private String id;
        private String title;
        private String type;
        private Body body;
    }

    @Data
    public static class Body {
        private Storage storage;
    }

    @Data
    public static class Storage {
        private String value;
        private String representation;
    }
}
