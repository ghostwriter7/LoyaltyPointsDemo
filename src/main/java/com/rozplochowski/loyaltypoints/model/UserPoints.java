package com.rozplochowski.loyaltypoints.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "user_points")
public class UserPoints {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @NotNull
    private Long points;

    @NotBlank
    @Max(100)
    private String nickname;
}
