package ru.yandex.practicum.filmorate.dal.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.ReviewStorage;
import ru.yandex.practicum.filmorate.dal.mappers.ReviewRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static ru.yandex.practicum.filmorate.utils.ErrorMessages.REVIEW_NOT_FOUND;

@Repository
@RequiredArgsConstructor
public class DbReviewStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;

    private final ReviewRowMapper rowMapper;

    @Override
    public Review add(Review review) {

        int userId = review.getUserId();
        int filmId = review.getFilmId();
        if (!containsFilm(filmId)) {
            throw new NotFoundException("Film id " + filmId + " isn't found");
        }
        if (!containsUser(userId)) {
            throw new NotFoundException("User id " + userId + " isn't found");
        }

        String addFilmSql = "INSERT INTO reviews (film_id, user_id, is_positive, content) " +
                "VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(addFilmSql, new String[]{"REVIEW_ID"});
                ps.setInt(1, filmId);
                ps.setInt(2, userId);
                ps.setBoolean(3, review.isPositive());
                ps.setString(4, review.getContent());

                return ps;
            }, keyHolder);

            review.setReviewId(Objects.requireNonNull(keyHolder.getKey()).intValue());
            return review;
        } catch (DataIntegrityViolationException e) {
            throw new NotFoundException(e.getMessage());
        }
    }

    @Override
    public Review update(Review newReview) {
        String sql = "UPDATE reviews SET " +
                "is_positive = ?, content = ? " +
                "WHERE review_id = ?";

        int rowsAffected = jdbcTemplate.update(sql,
                newReview.isPositive(),
                newReview.getContent(),
                newReview.getReviewId());

        if (rowsAffected == 0) {
            throw new NotFoundException(REVIEW_NOT_FOUND + newReview.getReviewId());
        }
        return findById(newReview.getReviewId()).stream().findFirst().orElse(null);
    }

    @Override
    public void remove(int id) {
        String sql = "DELETE FROM reviews WHERE review_id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void removeAll() {
        String sql = "DELETE FROM reviews";

        jdbcTemplate.update(sql);
    }

    @Override
    public List<Review> findAll() {
        String sql = "SELECT r.*, COALESCE(SUM(rl.is_like), 0) as useful " +
                "FROM reviews r " +
                "LEFT JOIN review_likes rl ON r.review_id = rl.review_id " +
                "GROUP BY r.review_id ORDER BY useful DESC";
        return jdbcTemplate.query(sql, rowMapper);
    }

    @Override
    public Optional<Review> findById(int id) {
        try {
            String sql = "SELECT r.*, COALESCE(SUM(rl.is_like), 0) as useful " +
                    "FROM reviews r " +
                    "LEFT JOIN review_likes rl ON r.review_id = rl.review_id " +
                    "WHERE r.review_id = ? " +
                    "GROUP BY rl.review_id";
            Review review = jdbcTemplate.queryForObject(sql, rowMapper, id);
            return Optional.of(review);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Review> findByFilmId(int filmId, int size) {
        String sql = "SELECT r.*, COALESCE(SUM(rl.is_like), 0) as useful " +
                "FROM reviews r " +
                "LEFT JOIN review_likes rl ON r.review_id = rl.review_id " +
                "WHERE r.film_id = ? " +
                "GROUP BY r.review_id " +
                "ORDER BY useful DESC LIMIT ?";

        return jdbcTemplate.query(sql, rowMapper, filmId, size);
    }

    @Override
    public void addRating(int reviewId, int userId, boolean isLike) {
        String sql = "MERGE INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, ?)";
        try {
            jdbcTemplate.update(sql, reviewId, userId, isLike ? 1 : -1);
        } catch (DataIntegrityViolationException e) {
            throw new ValidationException(e.getMessage());
        }
    }

    @Override
    public void removeRating(int reviewId, int userId, boolean isLike) {
        String sql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?";
        try {
            jdbcTemplate.update(sql, reviewId, userId);
        } catch (DataIntegrityViolationException e) {
            throw new ValidationException(e.getMessage());
        }
    }

    public boolean containsUser(Integer userId) {
        String sql = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null && count > 0;
    }

    public boolean containsFilm(Integer filmId) {
        String sql = "SELECT COUNT(*) FROM films WHERE film_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, filmId);
        return count != null && count > 0;
    }

}
