package ru.yandex.practicum.filmorate.controller.mappers;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.dto.create.CreateReviewDto;
import ru.yandex.practicum.filmorate.model.Review;

@Component
public class ReviewMapper {

    public static final int INITIAL_RATING = 0;

    public final Review map(CreateReviewDto dto) {
        return Review.builder()
                .filmId(dto.getFilmId())
                .userId(dto.getUserId())
                .isPositive(dto.getIsPositive())
                .useful(INITIAL_RATING)
                .content(dto.getContent())
                .build();
    }

    public final Review map(ReviewDto dto) {
        return Review.builder()
                .reviewId(dto.getReviewId())
                .filmId(dto.getFilmId())
                .userId(dto.getUserId())
                .isPositive(dto.getIsPositive())
                .useful(dto.getUseful())
                .content(dto.getContent())
                .build();
    }

    public final ReviewDto map(Review review) {
        return ReviewDto.builder()
                .reviewId(review.getReviewId())
                .filmId(review.getFilmId())
                .userId(review.getUserId())
                .isPositive(review.isPositive())
                .useful(review.getUseful())
                .content(review.getContent())
                .build();
    }
}
