package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewDto {

    @NotNull
    Integer reviewId;

    @NotNull
    Integer filmId;

    @NotNull
    Integer userId;

    @NotNull
    boolean isPositive;

    @NotNull
    Integer useful;

    String content;
}
