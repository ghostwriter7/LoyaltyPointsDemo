package com.rozplochowski.loyaltypoints.service.orders;

import com.rozplochowski.loyaltypoints.model.OrderSubmittedEvent;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class OrderProducer {
    private final KafkaOperations<String, String> kafkaTemplate;
    private final ObjectMapper mapper;
    private final NicknameClient nicknameClient;

    private final Random random = new Random();
    private final AtomicLong orderCounter = new AtomicLong(0);

    private List<User> users;

    @PostConstruct
    void init() {
        log.info("Fetching nicknames");

        var nicknames = nicknameClient.getNicknames()
                .results()
                .stream()
                .map(NicknameClient.UserName::name)
                .map(name -> new User(name.first(), getRandomId()))
                .toList();

        users = nicknames;

        log.info("Fetched nicknames: {}", nicknames);
    }

    @Scheduled(fixedRate = 500)
    void produceOrder() {
        var total = BigDecimal.valueOf(random.nextDouble(1, 10_000));
        var orderId = orderCounter.incrementAndGet();
        var user = getRandomUser();
        var event = new OrderSubmittedEvent(orderId, user.id(), user.nickname(), total);

        kafkaTemplate.send("order.submitted", String.valueOf(user.id()), mapper.writeValueAsString(event));
    }

    private User getRandomUser() {
        if (users == null || users.isEmpty()) {
            var id = getRandomId();
            return new User("User-" + id, id);
        }

        var index = random.nextInt(users.size());
        return users.get(index);
    }

    private long getRandomId() {
        return random.nextLong(1, 40_000_000);
    }

    record User(String nickname, long id) {}
}
