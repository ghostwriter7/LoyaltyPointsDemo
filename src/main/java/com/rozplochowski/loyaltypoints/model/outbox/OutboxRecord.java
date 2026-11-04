package com.rozplochowski.loyaltypoints.model.outbox;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_records")
@Getter
@Setter
public class OutboxRecord {

    @Id @GeneratedValue
    private UUID uuid;

    @NotNull
    private Long aggregateId;

    @NotBlank
    private String aggregateType;

    @NotBlank
    private String eventName;

    @NotBlank
    private String payload;

    @NotBlank
    private String topic;

    @CreationTimestamp
    private Instant createdAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    private OutboxStatus status = OutboxStatus.PENDING;

    public static OutboxRecord create(
            Long aggregateId,
            String aggregateType,
            String eventName,
            String payload,
            String topic
    ) {
        var outboxRecord = new OutboxRecord();
        outboxRecord.setAggregateId(aggregateId);
        outboxRecord.setAggregateType(aggregateType);
        outboxRecord.setEventName(eventName);
        outboxRecord.setPayload(payload);
        outboxRecord.setTopic(topic);
        return outboxRecord;
    }

}
