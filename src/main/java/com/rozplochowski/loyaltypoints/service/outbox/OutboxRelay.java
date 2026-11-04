package com.rozplochowski.loyaltypoints.service.outbox;

import com.rozplochowski.loyaltypoints.model.outbox.OutboxStatus;
import com.rozplochowski.loyaltypoints.repository.OutboxRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class OutboxRelay {

    private final OutboxRepository outboxRepository;
    private final KafkaOperations<String, Object> kafkaOperations;

    @Scheduled(fixedDelay = 20_000)
    @Transactional
    void relay() {

        var outboxRecords = outboxRepository.getUnprocessedRecords();

        for (var outboxRecord : outboxRecords) {

            kafkaOperations.send(outboxRecord.getTopic(), outboxRecord.getPayload());
            outboxRecord.setStatus(OutboxStatus.PUBLISHED);

        }

    }
}
