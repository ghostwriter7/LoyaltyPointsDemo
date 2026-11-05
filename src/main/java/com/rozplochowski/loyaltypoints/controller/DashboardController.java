package com.rozplochowski.loyaltypoints.controller;

import com.rozplochowski.loyaltypoints.service.DashboardManager;
import com.rozplochowski.loyaltypoints.service.dto.UserResult;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class DashboardController {

    private final DashboardManager dashboardManager;

    @GetMapping("/top")
    List<UserResult> getTopUsers() {
        return dashboardManager.getTopUsers();
    }

    @GetMapping("/{userId}")
    List<UserResult> getUserResult(@PathVariable long userId) {
        return dashboardManager.getUserResultWithNeighbours(userId);
    }
}
