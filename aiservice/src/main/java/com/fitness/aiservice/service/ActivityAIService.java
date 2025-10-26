package com.fitness.aiservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class ActivityAIService {

    @Autowired
    private GeminiService geminiService;

    public Recommendation generateRecommendation(Activity activity) {
        String prompt = createPromptForActivity(activity);
        String aiResponse = geminiService.getAnswer(prompt);
        log.info("Response FROM AI :{}", aiResponse);

        return processAIResponse(activity, aiResponse);
    }

    private Recommendation processAIResponse(Activity activity, String aiResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String cleanedResponse = aiResponse;
            if (cleanedResponse.contains("{") && cleanedResponse.contains("}")) {
                cleanedResponse = cleanedResponse.substring(
                        cleanedResponse.indexOf("{"),
                        cleanedResponse.lastIndexOf("}") + 1
                );
            }

            JsonNode rootNode = mapper.readTree(cleanedResponse);

            // Safe checks for candidates and parts
            JsonNode candidates = rootNode.path("candidates");
            if (!candidates.isArray() || candidates.size() == 0) {
                log.error("No candidates found in AI response: {}", aiResponse);
                return createDefaultRecommendation(activity);
            }

            JsonNode parts = candidates.get(0).path("content").path("parts");
            if (!parts.isArray() || parts.size() == 0) {
                log.error("No parts found in AI response: {}", aiResponse);
                return createDefaultRecommendation(activity);
            }

            JsonNode textNode = parts.get(0).path("text");
            String jsonContent = textNode.asText()
                    .replaceAll("```json\\n", "")
                    .replaceAll("\\n```", "")
                    .trim();

            if (!jsonContent.startsWith("{")) {
                log.error("Invalid JSON format from AI: {}", jsonContent);
                return createDefaultRecommendation(activity);
            }

            JsonNode analysisJson = mapper.readTree(jsonContent);
            JsonNode analysisNode = analysisJson.path("analysis");

            StringBuilder fullAnalysis = new StringBuilder();
            addAnalysisSection(fullAnalysis, analysisNode, "overall", "Overall:");
            addAnalysisSection(fullAnalysis, analysisNode, "heartRate", "HeartRate:");
            addAnalysisSection(fullAnalysis, analysisNode, "caloriesBurned", "CaloriesBurned:");

            List<String> improvements = extractImprovements(analysisJson.path("improvements"));
            List<String> suggestions = extractSuggestions(analysisJson.path("suggestions"));
            List<String> safety = extractSafety(analysisJson.path("safety"));
            if (safety.isEmpty()) {
                safety = Arrays.asList("Always warm up before exercise","Stay hydrated","Listen to your body");
            }


            return Recommendation.builder()
                    .activityId(activity.getId())
                    .userId(activity.getUserId())
                    .activityType(activity.getType())
                    .recommendation(fullAnalysis.toString().trim())
                    .improvements(improvements)
                    .suggestions(suggestions)
                    .safety(safety)
                    .createdAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Failed to process AI response", e);
            return createDefaultRecommendation(activity);
        }
    }


    private Recommendation createDefaultRecommendation(Activity activity) {
        return Recommendation.builder()
                .activityId(activity.getId())
                .userId(activity.getUserId())
                .activityType(activity.getType())
                .recommendation("Unable to generate detailed analysis")
                .improvements(Collections.singletonList("Continue with your current routine"))
                .suggestions(Collections.singletonList("Consider consulting a fitness professional"))
                .safety(Arrays.asList(
                        "Always warm up before exercise",
                        "Stay hydrated",
                        "Listen to your body"
                ))
                .createdAt(LocalDateTime.now())
                .build();
    }

    private List<String> extractSuggestions(JsonNode suggestionsNode) {
        List<String> suggestions = new ArrayList<>();
        if (suggestionsNode.isArray()) {
            suggestionsNode.forEach(s -> {
                String workout = s.path("workout").asText();
                String description = s.path("description").asText();
                suggestions.add(String.format("%s: %s", workout, description));
            });
        }
        return suggestions.isEmpty() ? Collections.singletonList("No specific suggestions provided") : suggestions;
    }

    private List<String> extractImprovements(JsonNode improvementsNode) {
        List<String> improvements = new ArrayList<>();
        if (improvementsNode.isArray()) {
            improvementsNode.forEach(improvement -> {
                String area = improvement.path("area").asText();
                String detail = improvement.path("recommendation").asText();
                improvements.add(String.format("%s: %s", area, detail));
            });
        }
        return improvements.isEmpty() ? Collections.singletonList("No specific improvements provided") : improvements;
    }

    private List<String> extractSafety(JsonNode safetyNode) {
        List<String> safety = new ArrayList<>();
        if (safetyNode.isArray()) {
            for (JsonNode item : safetyNode) {
                if (item.isTextual()) {
                    safety.add(item.asText());
                } else if (item.has("advice")) {
                    safety.add(item.path("advice").asText());
                }
            }
        }
        // Default safety tips if AI doesn't provide any
        if (safety.isEmpty()) {
            safety = Arrays.asList(
                    "Always warm up before exercise",
                    "Stay hydrated",
                    "Listen to your body"
            );
        }
        return safety;
    }

    private void addAnalysisSection(StringBuilder fullAnalysis, JsonNode analysisNode, String key, String prefix) {
        if (!analysisNode.path(key).isMissingNode()) {
            fullAnalysis.append(prefix)
                    .append(analysisNode.path(key).asText())
                    .append("\n\n");
        }
    }

    private String createPromptForActivity(Activity activity) {
        return String.format("""
        You are a fitness AI assistant. 
        Analyze the given workout data and respond ONLY in JSON format as follows:

        {
          "analysis": {
            "overall": "...",
            "heartRate": "...",
            "caloriesBurned": "..."
          },
          "improvements": [
            { "area": "...", "recommendation": "..." }
          ],
          "suggestions": [
            { "workout": "...", "description": "..." }
          ]
        }

        Input data:
        {
          "type": "%s",
          "duration": %d,
          "caloriesBurned": %d,
          "additionalMetrics": {
            "avgSpeed": "%s",
            "distance": "%s",
            "maxHeartRate": "%s"
          }
        }
        """,
                activity.getType(),
                activity.getDuration(),
                activity.getCalorieBurned(),
                activity.getAdditionalMetrics().get("avgSpeed"),
                activity.getAdditionalMetrics().get("distance"),
                activity.getAdditionalMetrics().get("maxHeartRate")
        );
    }

}
