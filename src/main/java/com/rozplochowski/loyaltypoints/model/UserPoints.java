package com.rozplochowski.loyaltypoints.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_points")
@Getter
@Setter
public class UserPoints {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @NotNull
    private Long points = 0L;

    @NotBlank
    @Size(max = 100)
    private String nickname;

    @Version
    @Column(columnDefinition = "int default 0")
    private Integer version = 0;

    public void addPoints(Long points) {
        this.points += points;
    }
}
