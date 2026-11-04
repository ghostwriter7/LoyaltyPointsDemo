package com.rozplochowski.loyaltypoints;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LoyaltyPointsApplication {

    static void main(String[] args) {
        SpringApplication.run(LoyaltyPointsApplication.class, args);
    }

}
