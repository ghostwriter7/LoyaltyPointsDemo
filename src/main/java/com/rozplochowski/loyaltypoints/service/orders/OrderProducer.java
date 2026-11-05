package com.rozplochowski.loyaltypoints.service.orders;

import com.rozplochowski.loyaltypoints.model.OrderSubmittedEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class OrderProducer {
    private final KafkaOperations<String, String> kafkaTemplate;
    private final ObjectMapper mapper;

    private final Random random = new Random();
    private final AtomicLong orderCounter = new AtomicLong(0);

    @Scheduled(fixedRate = 500)
    void produceOrder() {
        var userId = random.nextLong(1, 40_000_000);
        var total = BigDecimal.valueOf(random.nextDouble(1, 10_000));
        var orderId = orderCounter.incrementAndGet();
        var event = new OrderSubmittedEvent(orderId, userId, total);

        kafkaTemplate.send("order.submitted", String.valueOf(userId), mapper.writeValueAsString(event));
    }
}
