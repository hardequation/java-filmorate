package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.review.Review;
import ru.yandex.practicum.filmorate.model.review.ReviewAssessment;
import ru.yandex.practicum.filmorate.model.review.ReviewRating;
import ru.yandex.practicum.filmorate.model.review.ReviewType;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ReviewRowMapper implements RowMapper<Review> {

    @Override
    public Review mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Review.builder()
                .id(rs.getInt("id"))
                .filmId(rs.getInt("film_id"))
                .type(ReviewType.valueOf(rs.getString("review_type")))
                .assessment(ReviewAssessment.valueOf(rs.getString("assessment")))
                .rating(ReviewRating.builder()
                        .value(rs.getInt("rating"))
                        .build())
                .build();
    }
}
