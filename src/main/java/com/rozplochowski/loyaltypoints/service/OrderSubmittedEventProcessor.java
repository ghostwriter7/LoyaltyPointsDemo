package com.rozplochowski.loyaltypoints.service;

import com.rozplochowski.loyaltypoints.model.OrderSubmittedEvent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class OrderSubmittedEventProcessor {

    @PersistenceContext
    private final EntityManager manager;


    @Transactional
    void processOrderSubmittedEvent(OrderSubmittedEvent event) {
        var points = event.total().divide(BigDecimal.TEN, RoundingMode.HALF_UP);

        var result = manager.createNativeQuery("insert ignore into processed_orders (order_id, user_id, points_awarded) values (:orderId, :userId, :pointsAwarded)")
                .setParameter("orderId", event.orderId())
                .setParameter("userId", event.userId())
                .setParameter("pointsAwarded", points.longValue())
                .executeUpdate();

        if (result == 0) {
            log.info("Order already processed: {}", event.orderId());
            return;
        }

        manager.createNativeQuery("""
                        insert into user_points (user_id, points, nickname) values (:userId, :points, :nickname) as new
                        on duplicate key update user_points.points = user_points.points + new.points""")
                .setParameter("userId", event.userId())
                .setParameter("points", points.longValue())
                .setParameter("nickname", "User-" + event.userId())
                .executeUpdate();

        log.info("User [{}] points increased by {}", event.userId(), points.longValue());
    }
}
