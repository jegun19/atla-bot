package org.service.atlassian.bot;

import dev.langchain4j.service.Result;
import dev.langchain4j.service.tool.ToolExecution;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.service.atlassian.bot.agent.JiraAssistant;
import org.service.atlassian.bot.service.JiraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@SpringBootTest
@Slf4j
class BotServiceIntegrationTest {

    @Autowired
    JiraAssistant jiraAssistant;

    @MockitoBean
    JiraService jiraService;

    @Test
    @DisplayName("Test Jira Assistant with a user message to find a page in Confluence")
    void confluenceParsingTest() {
        String userMessage = "Hi, can you help me to find a page in Confluence?";
        when(jiraService.findPageIdByTitle(anyString())).thenReturn("1234567890");
        when(jiraService.parseConfluenceDocument(anyString())).thenReturn(
                "As part of our initiative to improve user engagement and reduce churn, we are introducing a comprehensive onboarding and profile management system for new users joining our SaaS platform. Our analytics have shown that users who complete onboarding are 3x more likely to convert to paying customers and stay active over 6 months.");

        Result<String> result = jiraAssistant.chat(userMessage);
        log.info("jiraAssistant first reply: {}", result.content());

        String userMessageTwo = "The title should contain 'User Onboarding'";
        Result<String> resultTwo = jiraAssistant.chat(userMessageTwo);
        log.info("jiraAssistant second reply: {}", resultTwo.content());

        // assert that correct tools were called
        List<ToolExecution> toolExecutionList = resultTwo.toolExecutions();
        assertEquals(2, toolExecutionList.size());
        toolExecutionList.stream().filter(t -> t.request().name().equals("findPageIdByTitle"))
                .findFirst()
                .ifPresentOrElse(tool -> log.info("Tool findPageIdByTitle executed: {}", tool),
                        () -> fail("Tool findPageIdByTitle not executed"));

        toolExecutionList.stream().filter(t -> t.request().name().equals("parseConfluenceDocument"))
                .findFirst().ifPresentOrElse(
                        tool -> log.info("Tool parseConfluenceDocument executed: {}", tool),
                        () -> fail("Tool parseConfluenceDocument not executed"));

        // assert that tools were called with correct parameters
        Mockito.verify(jiraService, times(1)).findPageIdByTitle("User Onboarding");
        Mockito.verify(jiraService, times(1)).parseConfluenceDocument("1234567890");
    }

    @Test
    @DisplayName("Test Jira Assistant with Confluence URL parsing")
    void confluenceUrlParsingTest() {
        String userMessage = "Hi, can you help me read a Confluence page?";
        String confluenceUrl =
                "https://myacmecompany.atlassian.net/wiki/spaces/SD/pages/8880129/Order+Management+System+System+Analysis+Sample";

        when(jiraService.parseConfluenceDocumentFromUrl(anyString())).thenReturn(
                "Order Management System Requirements: The system should handle customer orders, inventory management, payment processing, and order fulfillment. Key features include order creation, status tracking, inventory updates, and integration with payment gateways.");

        Result<String> result = jiraAssistant.chat(userMessage);
        log.info("jiraAssistant first reply: {}", result.content());

        String userMessageTwo = "Please read this Confluence page: " + confluenceUrl;
        Result<String> resultTwo = jiraAssistant.chat(userMessageTwo);
        log.info("jiraAssistant second reply: {}", resultTwo.content());

        // assert that correct tool was called
        List<ToolExecution> toolExecutionList = resultTwo.toolExecutions();
        assertEquals(1, toolExecutionList.size());

        toolExecutionList.stream()
                .filter(t -> t.request().name().equals("parseConfluenceDocumentFromUrl"))
                .findFirst().ifPresentOrElse(
                        tool -> log.info("Tool parseConfluenceDocumentFromUrl executed: {}", tool),
                        () -> fail("Tool parseConfluenceDocumentFromUrl not executed"));

        // assert that tool was called with correct URL parameter
        Mockito.verify(jiraService, times(1)).parseConfluenceDocumentFromUrl(confluenceUrl);
    }

}
