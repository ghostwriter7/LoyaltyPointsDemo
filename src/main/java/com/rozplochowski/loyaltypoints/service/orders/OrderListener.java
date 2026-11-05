package com.rozplochowski.loyaltypoints.service.orders;

import com.rozplochowski.loyaltypoints.model.OrderSubmittedEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class OrderListener {
    private final OrderProcessor eventProcessor;

    @KafkaListener(topics = "order.submitted", groupId = "loyalty-points")
    void processOrderSubmittedEvent(OrderSubmittedEvent event) {
      log.info("Received order submitted event with order id: {}, user id: {}, total: {}", event.orderId(), event.userId(), event.total());
      eventProcessor.processOrderSubmittedEvent(event);
    }

    @KafkaListener(topics = "order.submitted.dead-letter", groupId = "loyalty-points-dlt")
    void processOrderSubmittedEventDeadLetter(OrderSubmittedEvent event) {
      log.error("Received order submitted event from dead letter queue with order id: {}, user id: {}, total: {}", event.orderId(), event.userId(), event.total());
    }
}
