package org.service.atlassian.bot.model.jira.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import dev.langchain4j.model.output.structured.Description;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
@Description("This is an object representing a JIRA project.")
public class Project {

    @Description("ID of the project")
    private String id;

    @Description("Key of the project")
    private String key;

    @Description("Name of the project")
    private String name;

    @Override
    public String toString() {
        return "Project{" +
                "id='" + id + '\'' +
                ", key='" + key + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
