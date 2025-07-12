package org.service.atlassian.bot.model.jira.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditIssuePayload {

    private Map<String, Details> fields = new HashMap<>();

    @Data
    public static class Details{
        private String id;
        private String key;
    }

    public void constructAllEditPayload(String type, String id, String key){
        Details details = new Details();
        details.setId(id);
        details.setKey(key);

        this.fields.put(type, details);
    }

}
