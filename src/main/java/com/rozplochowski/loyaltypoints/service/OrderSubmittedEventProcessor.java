package com.rozplochowski.loyaltypoints.service;

import com.rozplochowski.loyaltypoints.model.OrderSubmittedEvent;
import com.rozplochowski.loyaltypoints.model.UserPoints;
import com.rozplochowski.loyaltypoints.repository.UserPointsRepository;
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
    private final UserPointsRepository userPointsRepository;

    @Transactional
    void processOrderSubmittedEvent(OrderSubmittedEvent event) {
        var userPoints = userPointsRepository.findById(event.userId())
                .orElseGet(() -> {
                    var newUserPoints = new UserPoints();
                    newUserPoints.setNickname("User-" + event.userId());
                    newUserPoints.setUserId(event.userId());
                    return newUserPoints;
                });
        var points = event.total().divide(BigDecimal.TEN, RoundingMode.HALF_UP);

        userPoints.addPoints(points.longValue());

        userPointsRepository.save(userPoints);

        log.info("User points updated for user id: {}, new points: {}", event.userId(), userPoints.getPoints());
    }
}
