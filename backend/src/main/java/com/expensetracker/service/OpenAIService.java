package com.expensetracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

/**
 * OpenAIService - Integrates with OpenAI's GPT API to generate budget advice.
 *
 * Flow:
 * 1. Receive expense summary (category-wise totals)
 * 2. Build a structured prompt
 * 3. Send to OpenAI API
 * 4. Parse the response
 * 5. Return the AI-generated advice
 *
 * We use Java's built-in HttpClient (Java 11+) — no extra dependency needed.
 */
@Service
@RequiredArgsConstructor
public class OpenAIService {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Value("${openai.api.url}")
    private String openAiApiUrl;

    @Value("${openai.model}")
    private String openAiModel;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Generate budget advice based on the user's expense summary.
     *
     * @param expenseSummary - Category-wise expense totals as a string
     * @return AI-generated financial advice
     */
    public String generateBudgetAdvice(String expenseSummary) {
        try {
            // Step 1: Build the prompt
            String prompt = buildPrompt(expenseSummary);

            // Step 2: Build the request body (JSON)
            // OpenAI API expects: model, messages, max_tokens
            Map<String, Object> requestBody = Map.of(
                    "model", openAiModel,
                    "messages", List.of(
                            Map.of(
                                    "role", "system",
                                    "content", "You are a helpful personal finance advisor. Provide clear, actionable budget advice based on the user's expense data. Keep advice practical and encouraging. Format your response with clear sections."
                            ),
                            Map.of(
                                    "role", "user",
                                    "content", prompt
                            )
                    ),
                    "max_tokens", 600,
                    "temperature", 0.7  // Creativity: 0=predictable, 1=creative
            );

            // Step 3: Convert to JSON string
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);

            // Step 4: Build HTTP request
            HttpClient httpClient = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(openAiApiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + openAiApiKey) // OpenAI auth
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();

            // Step 5: Send request and get response
            HttpResponse<String> httpResponse = httpClient.send(
                    httpRequest,
                    HttpResponse.BodyHandlers.ofString()
            );

            // Step 6: Check for errors
            if (httpResponse.statusCode() != 200) {
                return "AI service is temporarily unavailable. Please try again later. (Status: " + httpResponse.statusCode() + ")";
            }

            // Step 7: Parse OpenAI response
            // Response structure: { choices: [ { message: { content: "..." } } ] }
            JsonNode responseJson = objectMapper.readTree(httpResponse.body());
            String advice = responseJson
                    .path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText();

            return advice.isEmpty() ? "Could not generate advice at this time." : advice;

        } catch (Exception e) {
            e.printStackTrace();
            return "Unable to connect to AI service. Please check your API key configuration and try again.";
        }
    }

    /**
     * Build a well-structured prompt for the AI.
     * The quality of the prompt directly affects the quality of advice!
     */
    private String buildPrompt(String expenseSummary) {
        return """
                Analyze my expense data and provide personalized budget advice:
                
                %s
                
                Please provide:
                1. 📊 SPENDING ANALYSIS: Comment on my spending patterns across categories
                2. ⚠️  AREAS OF CONCERN: Highlight categories where I might be overspending
                3. 💡 SAVINGS TIPS: Give 3-4 specific, actionable ways to reduce expenses
                4. 🎯 BUDGET RECOMMENDATION: Suggest an ideal percentage allocation for each category
                5. ✅ POSITIVE HABITS: Mention what I'm doing well (if anything looks good)
                
                Keep the advice practical, specific to my data, and motivating. Use emojis to make it readable.
                """.formatted(expenseSummary);
    }
}
