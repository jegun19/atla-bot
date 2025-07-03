package org.service.atlassian.bot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.service.atlassian.bot.config.JiraProperties;
import org.service.atlassian.bot.model.jira.request.EditIssuePayload;
import org.service.atlassian.bot.model.jira.response.EditIssueResponse;
import org.service.atlassian.bot.model.jira.response.GetIssueResponse;
import org.service.atlassian.bot.util.HttpClientHelper;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class JiraServiceTest {

    @Mock
    private JiraProperties jiraProperties;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private ImageService imageService;
    @Mock
    private HttpClientHelper httpClientHelper;

    @InjectMocks
    private JiraService jiraService;

    @Test
    void attachToEpic() {
        EditIssueResponse rs = new EditIssueResponse();
        rs.setId("10001");
        rs.setKey("ECS-123");

        GetIssueResponse issue = new GetIssueResponse();
        issue.setId("10002");
        issue.setKey("ECS-123");

        GetIssueResponse epic = new GetIssueResponse();
        epic.setId("10002");
        epic.setKey("ECS-8");

        Mockito.lenient().when(jiraProperties.getUrl()).thenReturn("url-example");
        Mockito.when(httpClientHelper.put(eq("url-example/rest/api/2/issue/ECS-123?returnIssue=true"), any(EditIssuePayload.class), any(), any())).thenReturn(rs);
        Mockito.lenient().when(httpClientHelper.get(eq("url-example/rest/api/2/issue/ECS-123"), any(), any())).thenReturn(issue);
        Mockito.lenient().when(httpClientHelper.get(eq("url-example/rest/api/2/issue/ECS-8"), any(), any())).thenReturn(epic);

        String message = jiraService.attachToEpic("ECS-8", "ECS-123", false);

        assertEquals("Success attaching Issue to Epic!", message);
    }

    @Test
    void attachToEpic_EpicNotReplaced() {
        EditIssueResponse rs = new EditIssueResponse();
        rs.setId("10001");
        rs.setKey("ECS-123");

        GetIssueResponse issue = new GetIssueResponse();
        issue.setId("10002");
        issue.setKey("ECS-123");

        Map<String, Object> fields = new HashMap<>();
        Map<String, String> parent = new HashMap<>();
        parent.put("key","ECS-1234");
        fields.put("parent", parent);

        issue.setFields(fields);

        GetIssueResponse epic = new GetIssueResponse();
        epic.setId("10002");
        epic.setKey("ECS-8");

        Mockito.lenient().when(jiraProperties.getUrl()).thenReturn("url-example");
        Mockito.lenient().when(httpClientHelper.get(eq("url-example/rest/api/2/issue/ECS-123"), any(), any())).thenReturn(issue);
        Mockito.lenient().when(httpClientHelper.get(eq("url-example/rest/api/2/issue/ECS-8"), any(), any())).thenReturn(epic);

        String message = jiraService.attachToEpic("ECS-8", "ECS-123", false);

        assertEquals("Issue has epic attached, it's ECS-1234", message);
    }

    @Test
    void attachToEpic_cantFindEpicKey() {
        Mockito.lenient().when(jiraProperties.getUrl()).thenReturn("url-example");
        Mockito.lenient().when(httpClientHelper.get(eq("url-example/rest/api/2/issue/ECS-8"), any(), any())).thenReturn(null);

        String message = jiraService.attachToEpic("ECS-8", "ECS-123", false);

        assertEquals("can't find epic key", message);
    }

    @Test
    void attachToEpic_cantFindIssueKey() {

        GetIssueResponse epic = new GetIssueResponse();
        epic.setId("10002");
        epic.setKey("ECS-8");

        Mockito.lenient().when(jiraProperties.getUrl()).thenReturn("url-example");
        Mockito.lenient().when(httpClientHelper.get(eq("url-example/rest/api/2/issue/ECS-123"), any(), any())).thenReturn(null);
        Mockito.lenient().when(httpClientHelper.get(eq("url-example/rest/api/2/issue/ECS-8"), any(), any())).thenReturn(epic);

        String message = jiraService.attachToEpic("ECS-8", "ECS-123", false);

        assertEquals("can't find issue key", message);
    }

    @Test
    void attachToEpic_restClientExceptionGetIssue() {

        GetIssueResponse epic = new GetIssueResponse();
        epic.setId("10002");
        epic.setKey("ECS-8");

        Mockito.lenient().when(jiraProperties.getUrl()).thenReturn("url-example");
        Mockito.lenient().when(httpClientHelper.get(eq("url-example/rest/api/2/issue/ECS-123"), any(), any())).thenThrow(new RuntimeException("Error"));
        Mockito.lenient().when(httpClientHelper.get(eq("url-example/rest/api/2/issue/ECS-8"), any(), any())).thenReturn(epic);

        String message = jiraService.attachToEpic("ECS-8", "ECS-123", false);

        assertEquals("can't find issue key", message);

    }

    @Test
    void attachToEpic_failed() {
        EditIssueResponse rs = new EditIssueResponse();
        rs.setId("10001");
        rs.setKey("ECS-123");

        GetIssueResponse issue = new GetIssueResponse();
        issue.setId("10002");
        issue.setKey("ECS-123");

        GetIssueResponse epic = new GetIssueResponse();
        epic.setId("10002");
        epic.setKey("ECS-8");

        Mockito.lenient().when(jiraProperties.getUrl()).thenReturn("url-example");
        Mockito.when(httpClientHelper.put(eq("url-example/rest/api/2/issue/ECS-123?returnIssue=true"), any(EditIssuePayload.class), any(), any())).thenThrow(new RuntimeException("failed to attach epic to project"));
        Mockito.lenient().when(httpClientHelper.get(eq("url-example/rest/api/2/issue/ECS-123"), any(), any())).thenReturn(issue);
        Mockito.lenient().when(httpClientHelper.get(eq("url-example/rest/api/2/issue/ECS-8"), any(), any())).thenReturn(epic);

        String message = jiraService.attachToEpic("ECS-8", "ECS-123", false);

        assertEquals("failed to attach epic to project", message);
    }


}