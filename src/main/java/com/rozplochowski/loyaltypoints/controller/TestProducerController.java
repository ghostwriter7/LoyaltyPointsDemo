package com.rozplochowski.loyaltypoints.controller;

import com.rozplochowski.loyaltypoints.model.OrderSubmittedEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Profile("test")
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class TestProducerController {

    private final KafkaOperations<String, String> kafkaOperations;
    private final ObjectMapper objectMapper;

    @PostMapping
    void mockOrderSubmittedEvent(@RequestBody OrderSubmittedEvent event) {
        var future = kafkaOperations.send("order.submitted", objectMapper.writeValueAsString(event));
        future.thenAcceptAsync(result -> {
            var metadata = result.getRecordMetadata();
            log.info("Order submitted event sent to Kafka to partition {}", metadata.partition());
        });
    }
}
