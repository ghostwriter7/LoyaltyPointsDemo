package com.rozplochowski.loyaltypoints.service.domain;

public record UserPointsChange(
        String nickname,
        long userId,
        long points,
        int version
) {
}
