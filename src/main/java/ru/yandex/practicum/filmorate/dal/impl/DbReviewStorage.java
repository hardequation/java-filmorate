package ru.yandex.practicum.filmorate.dal.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import ru.yandex.practicum.filmorate.dal.ReviewStorage;
import ru.yandex.practicum.filmorate.dal.mappers.ReviewRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.review.Review;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static ru.yandex.practicum.filmorate.utils.ErrorMessages.REVIEW_NOT_FOUND;

public class DbReviewStorage implements ReviewStorage {

    public static final int INITIAL_RATING = 0;
    
    private final JdbcTemplate jdbcTemplate;
    
    private final ReviewRowMapper rowMapper;

    @Autowired
    public DbReviewStorage(JdbcTemplate jdbcTemplate, ReviewRowMapper rowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapper = rowMapper;
    }

    @Override
    public Review add(Review review) {
        String addFilmsql = "INSERT INTO reviews (film_id, type, assessment, rating) " +
                "VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(addFilmsql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, review.getFilmId());
            ps.setString(2, review.getType().toString());
            ps.setString(3, review.getAssessment().toString());
            ps.setInt(4, INITIAL_RATING );

            return ps;
        }, keyHolder);

        int generatedId = Objects.requireNonNull(keyHolder.getKey()).intValue();
        review.setId(generatedId);

        return review;
    }

    @Override
    public Review update(Review newReview) {
        String sql = "UPDATE reviews SET " +
                "film_id = ?, review_type = ?, assessment = ?, rating = ? " +
                "WHERE id = ?";

        int rowsAffected = jdbcTemplate.update(sql,
                newReview.getFilmId(),
                newReview.getType(),
                newReview.getAssessment(),
                newReview.getRating());

        if (rowsAffected == 0) {
            throw new NotFoundException(REVIEW_NOT_FOUND + newReview.getId());
        }
        return newReview;
    }

    @Override
    public void remove(int id) {
        String sql = "DELETE FROM reviews WHERE id = ?";

        jdbcTemplate.update(sql, id);
    }

    @Override
    public Optional<Review> findById(int id) {
        Review review = jdbcTemplate.queryForObject("SELECT * FROM reviews WHERE id = ?", rowMapper, id);
        if (review == null) {
            return Optional.empty();
        }

        return Optional.of(review);
    }

    @Override
    public List<Review> findByFilmId(int filmId, int size) {
        String sql = "SELECT * FROM reviews WHERE film_id = ? LIMIT ?";

        return jdbcTemplate.query(sql, rowMapper, filmId, size);
    }
}
