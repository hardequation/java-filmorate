package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Review {

    Integer reviewId;

    Integer filmId;

    Integer userId;

    boolean isPositive;

    boolean useful;

    String content;

    Integer rating;
}
