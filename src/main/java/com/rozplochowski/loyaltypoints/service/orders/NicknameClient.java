package com.rozplochowski.loyaltypoints.service.orders;

import org.springframework.web.service.annotation.GetExchange;

import java.util.List;

public interface NicknameClient {

    @GetExchange("/api?inc=name&results=100")
    Results getNicknames();


    record Results(List<UserName> results) {}

    record UserName(Name name) {}

    record Name(String title, String first, String last) {}
}
