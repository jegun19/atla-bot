package org.service.atlassian.bot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.atlassian.bot.config.JiraProperties;
import org.service.atlassian.bot.model.enums.IssueTypeEnum;
import org.service.atlassian.bot.model.enums.Priority;
import org.service.atlassian.bot.model.jira.model.*;
import org.service.atlassian.bot.model.jira.request.*;
import org.service.atlassian.bot.model.response.CreateIssueResponse;
import org.service.atlassian.bot.model.response.GetIssueTypesResponse;
import org.service.atlassian.bot.model.response.GetProjectResponse;
import org.service.atlassian.bot.model.response.SearchIssueResponse;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class JiraService {

    private final JiraProperties jiraProperties;
    private final ObjectMapper objectMapper;

    @Tool("Get Reporter ID. Do not make any modification to the ID, use it as it is.")
    public String getReporterId() {
        log.info("Fetching reporter ID");
        String reporterId = jiraProperties.getReporterId();
        if (reporterId == null || reporterId.isEmpty()) {
            throw new RuntimeException("Reporter ID is not set in the properties.");
        }
        return reporterId;
    }

    @Tool("Check if issue type is supported")
    public String getIssueType(@P("Issue type to check for") String issueType) {
        log.info("Checking for issueType: {}", issueType);
        try {
            IssueTypeEnum.valueOf(issueType.toUpperCase());
            return "Yes, this issue type is supported.";
        } catch (IllegalArgumentException e) {
            return "No, this issue type is not supported.";
        }
    }

    @Tool("Check what fields do the user need to provide")
    public Issue getFieldsForIssueType(@P("Issue type") String issueType) {
        log.info("Getting fields for issueType: {}", issueType);
        try {
            IssueTypeEnum issueTypeEnum = IssueTypeEnum.valueOf(issueType.toUpperCase());
            return switch (issueTypeEnum) {
                case TASK -> new Task();
                case STORY -> new Story();
                case EPIC -> new Epic();
                case BUG -> new Bug();
            };
        } catch (IllegalArgumentException e) {
            log.error("Error getting fields for issue type: {}", issueType, e);
            throw new RuntimeException(e);
        }
    }

    @Tool("Search Jira project by key or name, returns a list of projects")
    public List<Project> searchProjectByKey(@P(value = "Project key OR name") String projectKey) {
        log.info("Searching Jira projects with key: {}", projectKey);

        RestTemplate restTemplate = new RestTemplate();
        String authToken = jiraProperties.getAuthToken();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);
        headers.set("Content-Type", "application/json");

        String searchUrl = jiraProperties.getUrl() + "/rest/api/2/project/search?query=" + projectKey;

        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<GetProjectResponse> response;
        try {
            response = restTemplate.exchange(
                    searchUrl,
                    HttpMethod.GET,
                    request,
                    GetProjectResponse.class
            );
        } catch (RestClientException e) {
            log.error("Error searching Jira projects: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        log.info("Response: {}", response.getBody());
        log.info("Values: {}", Objects.isNull(response.getBody()) ? List.of() : response.getBody().getValues().toString());

        return Objects.isNull(response.getBody()) ? List.of() : response.getBody().getValues();
    }

    @Tool("Search JIRA issues")
    public SearchIssueResponse searchJiraIssues(
            @P(value = "Issue type to search for", required = false) String type,
            @P(value = "Keyword to search for in Summary", required = false) String summary
    ) {
        log.info("Searching JIRA issues with type: {} and summary: {}", type, summary);
        String fields = "summary,description";
        StringBuilder jqlBuilder = new StringBuilder();
        if (type != null && !type.isEmpty()) {
            jqlBuilder.append("type = '").append(type).append("'");
        }
        if (summary != null && !summary.isEmpty()) {
            if (!jqlBuilder.isEmpty()) {
                jqlBuilder.append(" AND ");
            }
            jqlBuilder.append("summary ~ '").append(summary).append("'");
        }
        String jqlQuery = jqlBuilder.toString();
        log.info("JQL Query: {}", jqlQuery);

        RestTemplate restTemplate = new RestTemplate();
        String authToken = jiraProperties.getAuthToken();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);
        headers.set("Content-Type", "application/json");

        String searchUrl = jiraProperties.getUrl() + "/rest/api/2/search/jql?jql=" + jqlQuery + "&fields=" + fields;

        log.info("Search URL: {}", searchUrl);
        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<SearchIssueResponse> response;
        try {
            response = restTemplate.exchange(
                    searchUrl,
                    HttpMethod.GET,
                    request,
                    SearchIssueResponse.class
            );
        } catch (RestClientException e) {
            log.error("Error searching JIRA issues: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        log.info("Response: {}", response.getBody());
        return response.getBody();
    }

    @Tool("Get issue types for a project using the project ID or key")
    public List<IssueType> getIssueTypesForProject(@P("Project ID or Key") String projectIdOrKey) {
        log.info("Fetching issue types for project ID: {}", projectIdOrKey);

        RestTemplate restTemplate = new RestTemplate();
        String authToken = jiraProperties.getAuthToken();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);
        headers.set("Content-Type", "application/json");

        String url = jiraProperties.getUrl() + "/rest/api/2/issue/createmeta/" + projectIdOrKey + "/issuetypes";

        HttpEntity<String> request = new HttpEntity<>(headers);

        ResponseEntity<GetIssueTypesResponse> response;
        try {
            response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    GetIssueTypesResponse.class
            );
        } catch (RestClientException e) {
            log.error("Error fetching issue types for project ID {}: {}", projectIdOrKey, e.getMessage());
            throw new RuntimeException(e);
        }

        log.info("Fetched issue types: {}", response.getBody());
        return Objects.isNull(response.getBody()) ? List.of() : response.getBody().getIssueTypes();
    }

    @Tool("Create a Bug issue")
    public String createBugIssue(
            @P("Fields for the bug issue") BugFields bugFields
    ) throws JsonProcessingException {
        log.info("Creating Bug issue with fields: {}", bugFields.toString());
        String priorityId = Priority.valueOf(bugFields.getPriority().toUpperCase()).getId();
        CreateIssueRequestPayload.Fields fields = CreateIssueRequestPayload.Fields.builder()
                .priority(CreateIssueRequestPayload.PriorityPayload.builder().id(priorityId).build())
                .description(bugFields.getDescription())
                .environment(bugFields.getEnvironment())
                .summary(bugFields.getSummary())
                .project(CreateIssueRequestPayload.ProjectPayload.builder().id(bugFields.getProjectId()).build())
                .reporter(CreateIssueRequestPayload.ReporterPayload.builder().id(bugFields.getReporterId()).build())
                .issueType(CreateIssueRequestPayload.IssueTypePayload.builder().id(bugFields.getIssueTypeId()).build())
                .build();
        CreateIssueRequestPayload payload = CreateIssueRequestPayload.builder().fields(fields).build();
        String stringified = objectMapper.writeValueAsString(payload);
        log.info("Creating Bug issue with payload: {}", stringified);
        return createJiraIssue(stringified);
    }

    @Tool("Create a Task issue")
    public String createTaskIssue(
            @P("Fields for the task issue") TaskFields taskFields
    ) throws JsonProcessingException {
        log.info("Creating Task issue with fields: {}", taskFields.toString());
        String priorityId = Priority.valueOf(taskFields.getPriority().toUpperCase()).getId();
        CreateIssueRequestPayload.Fields fields = CreateIssueRequestPayload.Fields.builder()
                .priority(CreateIssueRequestPayload.PriorityPayload.builder().id(priorityId).build())
                .description(taskFields.getDescription())
                .summary(taskFields.getSummary())
                .project(CreateIssueRequestPayload.ProjectPayload.builder().id(taskFields.getProjectId()).build())
                .reporter(CreateIssueRequestPayload.ReporterPayload.builder().id(taskFields.getReporterId()).build())
                .issueType(CreateIssueRequestPayload.IssueTypePayload.builder().id(taskFields.getIssueTypeId()).build())
                .build();

        CreateIssueRequestPayload payload = CreateIssueRequestPayload.builder().fields(fields).build();
        String stringified = objectMapper.writeValueAsString(payload);
        log.info("Creating Task issue with payload: {}", stringified);
        return createJiraIssue(stringified);
    }

    @Tool("Create a Story issue")
    public String createStoryIssue(
            @P("Fields for the story issue") StoryFields storyFields
    ) throws JsonProcessingException {
        log.info("Creating Story issue with fields: {}", storyFields.toString());
        String priorityId = Priority.valueOf(storyFields.getPriority().toUpperCase()).getId();
        CreateIssueRequestPayload.Fields fields = CreateIssueRequestPayload.Fields.builder()
                .priority(CreateIssueRequestPayload.PriorityPayload.builder().id(priorityId).build())
                .description(storyFields.getDescription())
                .summary(storyFields.getSummary())
                .project(CreateIssueRequestPayload.ProjectPayload.builder().id(storyFields.getProjectId()).build())
                .reporter(CreateIssueRequestPayload.ReporterPayload.builder().id(storyFields.getReporterId()).build())
                .issueType(CreateIssueRequestPayload.IssueTypePayload.builder().id(storyFields.getIssueTypeId()).build())
                .acceptanceCriteria(storyFields.getAcceptanceCriteria())
                .build();
        CreateIssueRequestPayload payload = CreateIssueRequestPayload.builder().fields(fields).build();
        String stringified = objectMapper.writeValueAsString(payload);
        log.info("Creating Story issue with payload: {}", stringified);
        return createJiraIssue(stringified);
    }

    @Tool("Create an Epic issue")
    public String createEpicIssue(
            @P("Fields for the epic issue") EpicFields epicFields
    ) throws JsonProcessingException {
        log.info("Creating Epic issue with fields: {}", epicFields.toString());
        String priorityId = Priority.valueOf(epicFields.getPriority().toUpperCase()).getId();
        CreateIssueRequestPayload.Fields fields = CreateIssueRequestPayload.Fields.builder()
                .priority(CreateIssueRequestPayload.PriorityPayload.builder().id(priorityId).build())
                .description(epicFields.getDescription())
                .summary(epicFields.getSummary())
                .project(CreateIssueRequestPayload.ProjectPayload.builder().id(epicFields.getProjectId()).build())
                .reporter(CreateIssueRequestPayload.ReporterPayload.builder().id(epicFields.getReporterId()).build())
                .issueType(CreateIssueRequestPayload.IssueTypePayload.builder().id(epicFields.getIssueTypeId()).build())
                .build();
        CreateIssueRequestPayload payload = CreateIssueRequestPayload.builder().fields(fields).build();
        String stringified = objectMapper.writeValueAsString(payload);
        return createJiraIssue(stringified);
    }

    public String createJiraIssue(String issuePayload) {
        RestTemplate restTemplate = new RestTemplate();
        String authToken = jiraProperties.getAuthToken();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authToken);
        headers.set("Content-Type", "application/json");

        HttpEntity<String> request = new HttpEntity<>(issuePayload, headers);

        ResponseEntity<CreateIssueResponse> response;
        try {
            response = restTemplate.exchange(
                    jiraProperties.getUrl() + "/rest/api/2/issue",
                    HttpMethod.POST,
                    request,
                    CreateIssueResponse.class
            );
        } catch (RestClientException e) {
            log.error("Error creating JIRA issue: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        return jiraProperties.getUrl() + "/browse/" + response.getBody().getKey();
    }
}