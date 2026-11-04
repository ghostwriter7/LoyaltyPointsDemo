package com.rozplochowski.loyaltypoints.service;

public record UserPointsChange(
        String nickname,
        long userId,
        long points,
        int version
) {
}
