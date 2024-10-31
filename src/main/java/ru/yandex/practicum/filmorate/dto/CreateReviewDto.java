package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.review.ReviewAssessment;
import ru.yandex.practicum.filmorate.model.review.ReviewRating;
import ru.yandex.practicum.filmorate.model.review.ReviewType;

@Data
@Builder
public class CreateReviewDto {

    @NotNull
    Integer filmId;

    @NotNull
    ReviewType type;

    @NotNull
    ReviewAssessment assessment;

    @NotNull
    ReviewRating rating;
}
