package com.rozplochowski.loyaltypoints.repository;

import com.rozplochowski.loyaltypoints.model.UserPoints;
import org.springframework.data.repository.CrudRepository;

public interface UserPointsRepository extends CrudRepository<UserPoints, Long> {
}
