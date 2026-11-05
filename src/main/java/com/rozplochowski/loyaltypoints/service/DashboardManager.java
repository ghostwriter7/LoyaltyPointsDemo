package com.rozplochowski.loyaltypoints.service;

import com.rozplochowski.loyaltypoints.repository.UserPointsRepository;
import com.rozplochowski.loyaltypoints.service.dto.UserResult;
import com.rozplochowski.loyaltypoints.service.dto.UserResultWithNeighbours;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DashboardManager {

    private final ConcurrentMap<Long, PointsVersion> pointsByUserId = new ConcurrentHashMap<>();
    private final UserPointsRepository userPointsRepository;
    private final AtomicReference<Stats> stats = new AtomicReference<>(new Stats(List.of(), Map.of()));

    @PostConstruct
    void init() {
        userPointsRepository.findAll()
                .forEach(userPoints -> pointsByUserId.put(userPoints.getUserId(), new PointsVersion(userPoints.getVersion(), userPoints.getPoints())));
    }

    public UserResultWithNeighbours getUserResult(long userId) {
        var statistics = stats.get();
        var userPosition = statistics.positionByUser().get(userId);

        if (userPosition == null) {
            throw new NoSuchElementException("User with id " + userId + " does not exist");
        }

        var ranking = statistics.ranking();
        var before = userPosition > 0 ? ranking.get(userPosition - 1) : null;
        var after = userPosition < ranking.size() - 1 ? ranking.get(userPosition + 1) : null;
        var userPoints = ranking.get(userPosition);

        return new UserResultWithNeighbours(
                "User-" + userPoints.userId(),
                userPoints.points(),
                userPosition + 1L,
                before != null ? toUserResult(before, userPosition - 1) : null,
                after != null ? toUserResult(after, userPosition + 1) : null);
    }

    public List<UserResult> getTopUsers() {
        var statistics = stats.get();

        var top20 = statistics.ranking().subList(0, Math.min(20, statistics.ranking().size()));

        List<UserResult> results = new ArrayList<>(top20.size());

        for (var i = 0; i < top20.size(); i++) {
            var userPoints = top20.get(i);
            results.add(toUserResult(userPoints, i));
        }

        return results;
    }

    private UserResult toUserResult(UserPoints userPoints, int position) {
        return new UserResult("User-" + userPoints.userId(), userPoints.points(), position + 1L);
    }

    @KafkaListener(topics = "user.points.change", groupId = "dashboard")
    void updateDashboard(UserPointsChange event) {
        log.info("Updating dashboard for user {} [{}], version {}, points {}", event.nickname(), event.userId(), event.version(), event.points());

        pointsByUserId.merge(event.userId(), new PointsVersion(event.version(), event.points()),
                (oldValue, newValue) -> oldValue.version() < newValue.version() ? newValue : oldValue);
    }

    @Scheduled(fixedDelay = 60_000)
    void refreshCache() {
        List<UserPoints> ranking = pointsByUserId.entrySet()
                .stream()
                .map(e -> new UserPoints(e.getKey(), e.getValue().points()))
                .sorted(Comparator.comparing(UserPoints::points)
                        .reversed()
                        .thenComparing(UserPoints::userId))
                .toList();

        Map<Long, Integer> positionByUser = HashMap.newHashMap(ranking.size());

        for (var i = 0; i < ranking.size(); i++) {
            positionByUser.put(ranking.get(i).userId(), i);
        }

        stats.set(new Stats(ranking, positionByUser));

        log.info("Dashboard refreshed");
    }

    record Stats(List<UserPoints> ranking, Map<Long, Integer> positionByUser) {
    }

    record UserPoints(Long userId, Long points) {
    }

    record PointsVersion(int version, long points) {
    }

}
