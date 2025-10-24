package com.fitness.aiservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "recommendation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recommendation {
    @Id
    private String id;
    private String activityId;
    private String userId;
    private String recommendation;

    @Column(columnDefinition = "jsonb")
    private List<String> improvements;

    @Column(columnDefinition = "jsonb")
    private List<String> suggestions;

    @Column(columnDefinition = "jsonb")
    private List<String> safety;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
