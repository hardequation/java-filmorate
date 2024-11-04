package ru.yandex.practicum.filmorate.dal.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.ReviewStorage;
import ru.yandex.practicum.filmorate.dal.UserStorage;
import ru.yandex.practicum.filmorate.dal.mappers.ReviewRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static ru.yandex.practicum.filmorate.model.enums.EventType.REVIEW;
import static ru.yandex.practicum.filmorate.model.enums.Operation.ADD;
import static ru.yandex.practicum.filmorate.model.enums.Operation.REMOVE;
import static ru.yandex.practicum.filmorate.model.enums.Operation.UPDATE;
import static ru.yandex.practicum.filmorate.utils.ErrorMessages.REVIEW_NOT_FOUND;

@Repository
@RequiredArgsConstructor
public class DbReviewStorage implements ReviewStorage {

    private final JdbcTemplate jdbcTemplate;

    private final ReviewRowMapper rowMapper;

    private final UserStorage userStorage;

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

        String addFilmSql = "INSERT INTO reviews (film_id, user_id, is_positive, useful, content) " +
                "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(addFilmSql, new String[]{"REVIEW_ID"});
                ps.setInt(1, filmId);
                ps.setInt(2, userId);
                ps.setBoolean(3, review.isPositive());
                ps.setInt(4, review.getUseful());
                ps.setString(5, review.getContent());

                return ps;
            }, keyHolder);

            review.setReviewId(Objects.requireNonNull(keyHolder.getKey()).intValue());
            userStorage.addFeed(review.getReviewId(), review.getUserId(), REVIEW, ADD);
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
        userStorage.addFeed(newReview.getReviewId(), newReview.getUserId(), REVIEW, UPDATE);
        return findById(newReview.getReviewId()).stream().findFirst().orElse(null);
    }

    @Override
    public void remove(int id) {
        Optional<Review> review = findById(id);
        if (review.isPresent()) {
            Integer userId = review.get().getUserId();
            String sql = "DELETE FROM reviews WHERE review_id = ?";

            jdbcTemplate.update(sql, id);

            userStorage.addFeed(id, userId, REVIEW, REMOVE);
        }
    }

    @Override
    public void removeAll() {
        String sql = "DELETE FROM reviews";

        jdbcTemplate.update(sql);
    }

    @Override
    public List<Review> findAll() {
        return jdbcTemplate.query("SELECT * FROM reviews ORDER BY useful DESC", rowMapper);
    }

    @Override
    public Optional<Review> findById(int id) {
        try {
            Review review = jdbcTemplate.queryForObject("SELECT * FROM reviews WHERE review_id = ?", rowMapper, id);
            return Optional.of(review);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Review> findByFilmId(int filmId, int size) {
        String sql = "SELECT * FROM reviews WHERE film_id = ? LIMIT ?";

        return jdbcTemplate.query(sql, rowMapper, filmId, size);
    }

    @Override
    public void addRating(int reviewId, int userId, boolean isLike) {
        String sql = "MERGE INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, ?)";
        try {
            jdbcTemplate.update(sql, reviewId, userId, isLike);
        } catch (DataIntegrityViolationException e) {
            throw new ValidationException(e.getMessage());
        }

        String updateSql = "UPDATE reviews r SET useful = ( " +
                "SELECT COUNT(CASE WHEN is_like = true THEN 1 END) - COUNT(CASE WHEN is_like = false THEN 1 END) " +
                "FROM review_likes rl " +
                "WHERE rl.review_id = r.review_id and rl.review_id = ?);";

        try {
            jdbcTemplate.update(updateSql, reviewId);
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

        String increaseRating = "UPDATE reviews SET useful = useful + 1 WHERE review_id = ?";
        String decreaseRating = "UPDATE reviews SET useful = useful - 1 WHERE review_id = ?";

        try {
            if (isLike) {
                jdbcTemplate.update(decreaseRating, reviewId);
            } else {
                jdbcTemplate.update(increaseRating, reviewId);
            }
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
