package com.fitness.aiservice.service;

import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import com.fitness.aiservice.repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityMessageListener {

    @Autowired
    private ActivityAIService activityAIService;

    @Autowired
    private RecommendationRepository recommendationRepository;

    @RabbitListener(queues = "activity.queue")
    public void processActivity(Activity activity) {
        try {
            log.info("Received activity for processing: {}", activity.getId());
            Recommendation recommendation = activityAIService.generateRecommendation(activity);
            recommendationRepository.save(recommendation);
        } catch (Exception e) {
            log.error("Error while processing activity {}: {}", activity.getId(), e.getMessage(), e);
        }
    }
}
