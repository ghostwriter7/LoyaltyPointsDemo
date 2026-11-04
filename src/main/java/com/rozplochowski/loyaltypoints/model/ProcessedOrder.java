package com.rozplochowski.loyaltypoints.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;

import java.time.Instant;

@Entity
@Table(name = "processed_orders")
public class ProcessedOrder {

    @Id
    @Column(name = "order_id")
    private Long orderId;

    @NotNull
    @Positive
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "processed_at", columnDefinition = "timestamp(6) default current_timestamp(6)")
    @CreationTimestamp(source = SourceType.DB)
    private Instant processedAt;

    @NotNull
    @Positive
    @Column(name = "points_awarded")
    private Long pointsAwarded;
}
