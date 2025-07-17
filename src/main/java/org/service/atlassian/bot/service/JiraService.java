package org.service.atlassian.bot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Safelist;
import org.service.atlassian.bot.config.JiraProperties;
import org.service.atlassian.bot.model.enums.IssueTypeEnum;
import org.service.atlassian.bot.model.enums.Priority;
import org.service.atlassian.bot.model.jira.model.*;
import org.service.atlassian.bot.model.jira.request.*;
import org.service.atlassian.bot.model.response.*;
import org.service.atlassian.bot.util.HttpClientHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class JiraService {

    private final JiraProperties jiraProperties;
    private final ObjectMapper objectMapper;
    private final ImageService imageService;
    private final HttpClientHelper httpClientHelper;

    private Map<String, String> createJiraHeader() {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Authorization", jiraProperties.getAuthToken());
        headerMap.put("Content-Type", "application/json");
        return headerMap;
    }

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

    @Tool("Check what fields need to be filled for a specific issue type")
    public Issue getFieldsForIssueType(@P("Name of the issue type") String issueType) {
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
        String searchUrl = jiraProperties.getUrl() + "/rest/api/2/project/search?query=" + projectKey;
        GetProjectResponse response;
        try {
            response = httpClientHelper.get(searchUrl, GetProjectResponse.class, createJiraHeader());
        } catch (RestClientException e) {
            log.error("Error searching Jira projects: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        return response.getValues();
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

        String searchUrl = jiraProperties.getUrl() + "/rest/api/2/search/jql?jql=" + jqlQuery + "&fields=" + fields;
        SearchIssueResponse response;
        try {
            response = httpClientHelper.get(searchUrl, SearchIssueResponse.class, createJiraHeader());
        } catch (RestClientException e) {
            log.error("Error searching JIRA issues: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        return response;
    }

    @Tool("Get issue types for a project using the project ID or key")
    public List<IssueType> getIssueTypesForProject(@P("Project ID or Key") String projectIdOrKey) {
        log.info("Fetching issue types for project ID: {}", projectIdOrKey);

        String url = jiraProperties.getUrl() + "/rest/api/2/issue/createmeta/" + projectIdOrKey + "/issuetypes";

        GetIssueTypesResponse response;
        try {
            response = httpClientHelper.get(url, GetIssueTypesResponse.class, createJiraHeader());
        } catch (RestClientException e) {
            log.error("Error fetching issue types for project ID {}: {}", projectIdOrKey, e.getMessage());
            throw new RuntimeException(e);
        }
        return response.getIssueTypes();
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
        String url = jiraProperties.getUrl() + "/rest/api/2/issue";
        CreateIssueResponse response;
        try {
            response = httpClientHelper.post(url, issuePayload, CreateIssueResponse.class, createJiraHeader());
        } catch (RestClientException e) {
            log.error("Error creating JIRA issue: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        return jiraProperties.getUrl() + "/browse/" + response.getKey();
    }

    @Tool("Parse Confluence Document from URL")
    public String parseConfluenceDocumentFromUrl(@P("Confluence Page URL") String url) {
        log.info("Extracting Page ID from Confluence URL: {}", url);
        
        // Validate domain first
        if (!isValidConfluenceDomain(url)) {
            log.error("Invalid Confluence domain. URL must be from: {}", jiraProperties.getUrl());
            throw new IllegalArgumentException("Confluence URL must be from your organization's domain: " + jiraProperties.getUrl());
        }
        
        // Extract page ID from URL pattern: /pages/{pageId}/
        String pageId = extractPageIdFromUrl(url);
        if (pageId == null) {
            log.error("Could not extract page ID from URL: {}", url);
            throw new IllegalArgumentException("Invalid Confluence URL format");
        }
        
        log.info("Extracted Page ID: {}", pageId);
        
        // Call the existing parseConfluenceDocument method
        return parseConfluenceDocument(pageId);
    }

    @Tool("Parse Confluence Document by ID")
    public String parseConfluenceDocument(@P("Page ID") String pageId) {
        log.info("Parsing Confluence Document with Page ID: {}", pageId);
        String url = jiraProperties.getUrl() + "/wiki/rest/api/content/" + pageId + "?expand=body.storage";

        ConfluencePageResponse response;
        try {
            response = httpClientHelper.get(url, ConfluencePageResponse.class, createJiraHeader());
        } catch (RestClientException e) {
            log.error("Error parsing Confluence document: {}", e.getMessage());
            throw new RuntimeException(e);
        }
        String rawHtml = response.getBody().getStorage().getValue();
        return cleanHtmlForLLM(rawHtml, pageId);
    }

    @Tool("Find Confluence Page ID by Title")
    public String findPageIdByTitle(@P("Page Title") String title) {
        log.info("Finding Confluence Page ID by title: {}", title);
        String cql = "title~\"" + title + "\"";
        String url = UriComponentsBuilder.fromUriString(jiraProperties.getUrl() + "/wiki/rest/api/content/search")
                .queryParam("cql", cql)
                .build()
                .toUriString();

        ConfluenceSearchResponse response;
        try {
            response = httpClientHelper.get(url, ConfluenceSearchResponse.class, createJiraHeader());
        } catch (RestClientException e) {
            log.error("Error finding Confluence Page ID: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        List<ConfluenceSearchResponse.ConfluencePageResult> results = response.getResults();
        if (results != null && !results.isEmpty()) {
            String id = results.get(0).getId();
            log.info("Found Confluence Page ID: {}", id);
            return id;
        }
        return null;
    }

    private String cleanHtmlForLLM(String html, String pageId) {
        // Parse as XML to support both HTML and Confluence macros
        Document document = Jsoup.parse(html, Parser.xmlParser());

        // Handle Confluence <ac:image> macros
        document.select("ac|image").forEach(acImage -> {
            Element attachment = acImage.selectFirst("ri|attachment");
            if (attachment != null) {
                String filename = attachment.attr("ri:filename");
                String imageUrl = jiraProperties.getUrl() + "/wiki/download/attachments/" + pageId + "/" + filename;
                String imageAnalysisResult = imageService.analyzeImage(imageUrl);
                log.info("Image analysis result for {} is finished!", filename);
                acImage.replaceWith(new TextNode(imageAnalysisResult));
            }
        });

        document.select(".conf-macro, .wysiwyg-macro, script, style").remove();

        // Clean HTML to safe plain text
        String plainText = Jsoup.clean(document.html(), "", Safelist.none(), new org.jsoup.nodes.Document.OutputSettings().prettyPrint(false));

        return plainText.replaceAll("\\n{2,}", "\n").trim();
    }

    private boolean isValidConfluenceDomain(String url) {
        try {
            // Get the base domain from jiraProperties (remove trailing slash if present)
            String expectedDomain = jiraProperties.getUrl().replaceAll("/$", "");
            
            // Check if the URL starts with our expected domain
            return url.startsWith(expectedDomain + "/wiki/");
        } catch (Exception e) {
            log.error("Error validating Confluence domain: {}", e.getMessage());
            return false;
        }
    }

    private String extractPageIdFromUrl(String url) {
        // Pattern: https://domain.atlassian.net/wiki/spaces/SPACE/pages/PAGEID/TITLE
        try {
            String[] parts = url.split("/pages/");
            if (parts.length < 2) {
                return null;
            }
            
            String afterPages = parts[1];
            String[] idParts = afterPages.split("/");
            if (idParts.length > 0) {
                return idParts[0]; // This should be the page ID
            }
            
            return null;
        } catch (Exception e) {
            log.error("Error extracting page ID from URL: {}", e.getMessage());
            return null;
        }
    }
}