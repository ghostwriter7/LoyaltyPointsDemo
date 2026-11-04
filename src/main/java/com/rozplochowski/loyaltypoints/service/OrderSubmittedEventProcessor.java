package com.rozplochowski.loyaltypoints.service;

import com.rozplochowski.loyaltypoints.model.OrderSubmittedEvent;
import com.rozplochowski.loyaltypoints.model.UserPoints;
import com.rozplochowski.loyaltypoints.model.outbox.OutboxRecord;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class OrderSubmittedEventProcessor {

    @PersistenceContext
    private final EntityManager manager;
    private final ObjectMapper objectMapper;

    private static final String USER_ID = "userId";

    @Transactional
    void processOrderSubmittedEvent(OrderSubmittedEvent event) {
        var points = event.total().divide(BigDecimal.TEN, RoundingMode.HALF_UP);

        var result = manager.createNativeQuery("insert ignore into processed_orders (order_id, user_id, points_awarded) values (:orderId, :userId, :pointsAwarded)")
                .setParameter("orderId", event.orderId())
                .setParameter(USER_ID, event.userId())
                .setParameter("pointsAwarded", points.longValue())
                .executeUpdate();

        if (result == 0) {
            log.info("Order already processed: {}", event.orderId());
            return;
        }

        manager.createNativeQuery("""
                        insert into user_points (user_id, points, nickname) values (:userId, :points, :nickname) as new
                        on duplicate key update user_points.points = user_points.points + new.points, user_points.version = user_points.version + 1""")
                .setParameter(USER_ID, event.userId())
                .setParameter("points", points.longValue())
                .setParameter("nickname", "User-" + event.userId())
                .executeUpdate();

        var userPoints = manager.createQuery("select up from UserPoints up where up.userId = :userId", UserPoints.class)
                .setParameter(USER_ID, event.userId())
                .getSingleResult();

        var userPointsChange = new UserPointsChange(userPoints.getNickname(), event.userId(), userPoints.getPoints(), userPoints.getVersion());

        var outbox = OutboxRecord.create(
                userPoints.getUserId(), UserPoints.class.getSimpleName(), UserPointsChange.class.getSimpleName(),
                objectMapper.writeValueAsString(userPointsChange),
                "user.points.change"
        );

        manager.persist(outbox);

        log.info("User [{}] points increased by {}", event.userId(), points.longValue());
    }
}
