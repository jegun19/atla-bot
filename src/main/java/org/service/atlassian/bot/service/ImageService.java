package org.service.atlassian.bot.service;

import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.service.atlassian.bot.config.JiraProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageService {

    private final ChatLanguageModel imageLanguageModel;
    private final JiraProperties jiraProperties;

    public String analyzeImage(String imageUrl) {
        // Fetch the Confluence page content
        RestTemplate restTemplate = new RestTemplate();

        log.info("Analyzing image from URL: {}", imageUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jiraProperties.getAuthToken());
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<byte[]> byteResponse = restTemplate.exchange(
                imageUrl,
                HttpMethod.GET,
                requestEntity,
                byte[].class
        );

        log.info("Image fetched successfully, size: {} bytes", byteResponse.getBody().length);
        String base64result = Base64.getEncoder().encodeToString(byteResponse.getBody());
        UserMessage userMessage = UserMessage.from(
                ImageContent.from(base64result, "image/png"),
                TextContent.from("""
                        Describe the steps or sequence of actions shown in the image using clear language. If the image is a flow or sequence, present the process in ordered steps.
                        """)
        );
        SystemMessage systemMessage = SystemMessage.from("""
                You are a software architect and Agile project manager assistant.

                Your primary task is to analyze UML sequence diagrams and convert them into structured, implementation-ready Jira task items using Agile best practices.

                Follow these rules:
                1. Understand the sequence diagram:
                   - Identify all participants (lifelines): client, backend services, databases, external APIs, etc.
                   - Extract each interaction (messages, method calls, conditions, loops).
                   - Note alternative or conditional paths.

                2. Break the diagram down into:
                   - **Epics** for major flows or high-level features.
                   - **User Stories** for work done by individual components or participants.
                   - **Subtasks** for specific implementation steps, validations, and error handling.

                3. Respect Agile task guidelines:
                   - Tasks must follow the INVEST principle (Independent, Negotiable, Valuable, Estimable, Small, Testable).
                   - Dependencies and execution order should be reflected if applicable.
                   - Use concise, clear, and technical but actionable language.

                4. Output format:
                   - Start with a clear **Epic title**
                   - For each User Story:
                     - Title in bold
                     - Component/owner in italics (e.g., *Frontend*, *Backend*, *Database*)
                     - Bullet list of subtasks
                   - End with a summary task table (Component | Task | Description)

                5. DO NOT include any commentary, explanation, or narrative outside of the task content.

                You will be given a diagram (image or description) representing a sequence of interactions. Analyze it and return only the structured task breakdown.
                """);
        ChatResponse chatResult = imageLanguageModel.chat(userMessage, systemMessage);
        log.info("Image converted to Base64 and processed by LLM: {}", chatResult);
        return chatResult.aiMessage().text();
    }
}