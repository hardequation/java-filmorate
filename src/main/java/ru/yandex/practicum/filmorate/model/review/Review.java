package ru.yandex.practicum.filmorate.model.review;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Review {

    int id;

    int filmId;

    ReviewType type;

    ReviewAssessment assessment;

    ReviewRating rating;
}
