package ru.yandex.practicum.filmorate.controller.mappers;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.CreateReviewDto;
import ru.yandex.practicum.filmorate.dto.ReviewDto;
import ru.yandex.practicum.filmorate.model.review.Review;

@Component
public class ReviewMapper {

    public final Review map(CreateReviewDto dto) {
        return Review.builder()
                .filmId(dto.getFilmId())
                .type(dto.getType())
                .assessment(dto.getAssessment())
                .rating(dto.getRating())
                .build();
    }

    public final Review map(ReviewDto dto) {
        return Review.builder()
                .id(dto.getId())
                .filmId(dto.getFilmId())
                .type(dto.getType())
                .assessment(dto.getAssessment())
                .rating(dto.getRating())
                .build();
    }

    public final ReviewDto map(Review review) {
        return ReviewDto.builder()
                .id(review.getId())
                .filmId(review.getFilmId())
                .type(review.getType())
                .assessment(review.getAssessment())
                .rating(review.getRating())
                .build();
    }
}
