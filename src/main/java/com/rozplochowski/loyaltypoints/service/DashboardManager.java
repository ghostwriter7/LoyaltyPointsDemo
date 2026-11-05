package com.rozplochowski.loyaltypoints.service;

import com.rozplochowski.loyaltypoints.repository.UserPointsRepository;
import com.rozplochowski.loyaltypoints.service.dto.UserResult;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class DashboardManager {

    private final UserPointsRepository userPointsRepository;

    private final StringRedisTemplate redisTemplate;

    private static final String RANKING_KEY = "{dashboard}:ranking";
    private static final String VERSIONS_KEY = "{dashboard}:versions";

    /// Arguments
    /// * `ARGV[1]` = userId
    /// * `ARGV[2]` = version
    /// * `ARGV[3]` = absolute points balance
    /// {dashboard}:ranking (member = userID, score = points) Sorted Set
    /// {dashboard}:versions (field = userID, value = version) Hash
    private static final DefaultRedisScript<Long> UPDATE_POINTS = new DefaultRedisScript<>(
            """
            local currentVersion = redis.call('HGET', KEYS[2], ARGV[1])
            
            if currentVersion
                and tonumber(currentVersion) >= tonumber(ARGV[2]) then
                return 0
            end
            
            redis.call('ZADD', KEYS[1], ARGV[3], ARGV[1])
            redis.call('HSET', KEYS[2], ARGV[1], ARGV[2])
            
            return 1
            """, Long.class);

    @PostConstruct
    void init() {
        userPointsRepository.findAll()
                .forEach(userPoints -> applyPoints(userPoints.getUserId(), userPoints.getVersion(), userPoints.getPoints()));
    }

    public List<UserResult> getUserResultWithNeighbours(long userId) {
        Long position = redisTemplate.opsForZSet().reverseRank(RANKING_KEY, String.valueOf(userId));
        var start = Math.max(0, position - 2);
        Set<ZSetOperations.TypedTuple<String>> neighbours = redisTemplate.opsForZSet()
                .reverseRangeWithScores(RANKING_KEY, start, position + 2);

        var rank = start + 1;
        List<UserResult> results = new ArrayList<>(neighbours.size());

        for (var tuple : neighbours) {
            var id = Long.parseLong(Objects.requireNonNull(tuple.getValue()));
            var points = Objects.requireNonNull(tuple.getScore()).longValue();
            results.add(new UserResult("User-" + id, points, rank++));
        }

        return results;
    }

    public List<UserResult> getTopUsers() {
        var entries = redisTemplate.opsForZSet()
                .reverseRangeWithScores(RANKING_KEY, 0, 19);

        List<UserResult> results = new ArrayList<>(entries.size());

        var rank = 1;
        for (var entry : entries) {
            long userId = Long.parseLong(Objects.requireNonNull(entry.getValue()));
            long points = Objects.requireNonNull(entry.getScore()).longValue();

            results.add(new UserResult("User-" + userId, points, rank++));
        }

        return results;
    }

    @KafkaListener(topics = "user.points.change", groupId = "dashboard")
    void updateDashboard(UserPointsChange event) {
        log.info("Updating dashboard for user {} [{}], version {}, points {}", event.nickname(), event.userId(), event.version(), event.points());

        applyPoints(event.userId(), event.version(), event.points());
    }

    private void applyPoints(long userId, int version, long points) {
        if (points < -9_007_199_254_740_992L || points > 9_007_199_254_740_992L) {
            throw new IllegalArgumentException("Points exceed exact Redis score range");
        }

        var result = redisTemplate.execute(UPDATE_POINTS,
                List.of(RANKING_KEY, VERSIONS_KEY),
                Long.toString(userId),
                Integer.toString(version),
                Long.toString(points));

        if (Long.valueOf(1L).equals(result)) {
            log.info("Updated dashboard points for user {}, version {}, points {}", userId, version, points);
        } else {
            log.info("Skipped stale dashboard points update for user {}, version {}, points {}", userId, version, points);
        }
    }
}
