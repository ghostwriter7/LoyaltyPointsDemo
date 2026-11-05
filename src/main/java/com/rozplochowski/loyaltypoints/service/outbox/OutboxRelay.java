package com.rozplochowski.loyaltypoints.service.outbox;

import com.rozplochowski.loyaltypoints.model.outbox.OutboxRecord;
import com.rozplochowski.loyaltypoints.model.outbox.OutboxStatus;
import com.rozplochowski.loyaltypoints.repository.OutboxRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class OutboxRelay {

    private final OutboxRepository outboxRepository;
    private final KafkaOperations<String, String> kafkaOperations;

    @Scheduled(fixedDelay = 1_000)
    @Transactional
    void relay() {
        var outboxRecords = outboxRepository.getUnprocessedRecords();

        List<CompletableFuture<RelayResult>> relayResults = outboxRecords.stream()
                .map(outboxRecord -> {
                    log.info("Attempting to relay outbox record for {} [{}] due to {}", outboxRecord.getAggregateType(), outboxRecord.getAggregateId(), outboxRecord.getEventName());
                    return kafkaOperations.send(outboxRecord.getTopic(), outboxRecord.getAggregateId().toString(), outboxRecord.getPayload())
                            .handle((result, exception) -> new RelayResult(outboxRecord, result, exception));
                })
                .toList();

        CompletableFuture.allOf(relayResults.toArray(CompletableFuture[]::new)).join();

        relayResults.stream()
                .map(CompletableFuture::join)
                .forEach(this::handleRelayResult);
    }

    private void handleRelayResult(RelayResult relayResult) {
        var outboxRecord = relayResult.outboxRecord();
        if (relayResult.failed()) {
            log.error(
                    "Failed to relay outbox record [{}] for {} [ID: {}, {}]",
                    outboxRecord.getUuid(),
                    outboxRecord.getAggregateType(),
                    outboxRecord.getAggregateId(),
                    outboxRecord.getEventName(),
                    relayResult.exception()
            );
        } else {
            var metadata = relayResult.sendResult().getRecordMetadata();
            log.info("Successfully relayed outbox record [{}] for {} [ID: {}, {}] to topic: {}, partition: {}, offset: {}",
                    outboxRecord.getUuid(), outboxRecord.getAggregateType(), outboxRecord.getAggregateId(), outboxRecord.getEventName(), metadata.topic(), metadata.partition(), metadata.offset());
            relayResult.outboxRecord().setStatus(OutboxStatus.PUBLISHED);
        }
    }

    private record RelayResult(OutboxRecord outboxRecord, SendResult<String, String> sendResult, Throwable exception) {
        private boolean failed() {
            return exception != null;
        }
    }
}
