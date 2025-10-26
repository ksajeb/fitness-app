package com.fitness.aiservice.model;

import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

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
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String activityId;
    private String userId;
    private String activityType;

    @Column(columnDefinition = "TEXT")
    private String recommendation;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<String> improvements;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<String> suggestions;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private List<String> safety;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
