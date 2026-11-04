package com.rozplochowski.loyaltypoints.service.dto;

public record UserResultWithNeighbours(
        String nickname,
        long points,
        long rank,
        UserResult before,
        UserResult after
) {
}
