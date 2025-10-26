package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Activity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ActivityAIService {

    @Autowired
    private GeminiService geminiService;

    public String generateRecommendation(Activity activity){
        String prompt=createPromptForActivity(activity);
        String aiResponse =geminiService.getAnswer(prompt);
        log.info("Response FROM AI :{}",aiResponse);
        return aiResponse;
    }

    private String createPromptForActivity(Activity activity) {
        return String.format("""
                [
                  {
                    "analysis": {
                      "overall": "Good endurance and consistent performance during running.",
                      "pace": "Average pace was steady at 6 min/km, slightly above optimal.",
                      "heartRate": "Heart rate stayed between 120–145 bpm, indicating aerobic efficiency.",
                      "caloriesBurned": "Approximately 320 calories burned during session."
                    },
                    "improvements": [
                      {
                        "area": "Pace control",
                        "recommendation": "Try interval training to gradually reduce your average pace."
                      },
                      {
                        "area": "Hydration",
                        "recommendation": "Drink water every 20 minutes to maintain energy levels."
                      }
                    ]
                  },
                  {
                    "analysis": {
                      "overall": "Excellent flexibility improvement from yoga session.",
                      "pace": "Smooth transitions between poses observed.",
                      "heartRate": "Heart rate stayed around 90–100 bpm, ideal for relaxation.",
                      "caloriesBurned": "Roughly 150 calories burned."
                    },
                    "improvements": [
                      {
                        "area": "Breathing",
                        "recommendation": "Maintain steady breathing throughout each pose."
                      }
                    ]
                  },
                  {
                    "analysis": {
                      "overall": "Good c
               
                """,activity.getType(),
                    activity.getDuration(),
                    activity.getCalorieBurned(),
                    activity.getAdditionalMetrics()
                );
    }

}
