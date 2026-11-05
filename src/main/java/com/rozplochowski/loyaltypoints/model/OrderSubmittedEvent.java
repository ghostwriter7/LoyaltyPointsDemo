package com.rozplochowski.loyaltypoints.model;

import java.math.BigDecimal;

public record OrderSubmittedEvent(Long orderId, Long userId, String nickname, BigDecimal total) {
}
