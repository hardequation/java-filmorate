package ru.yandex.practicum.filmorate.dal.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.RatingStorage;
import ru.yandex.practicum.filmorate.dal.mappers.RatingRowMapper;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;
import java.util.Optional;

@Repository
public class DbRatingStorage implements RatingStorage {

    private final JdbcTemplate jdbcTemplate;

    private final RatingRowMapper ratingRowMapper;

    @Autowired
    public DbRatingStorage(JdbcTemplate jdbcTemplate, RatingRowMapper ratingRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.ratingRowMapper = ratingRowMapper;
    }

    @Override
    public List<MpaRating> findAllMpaRatings() {
        String sql = "SELECT * FROM ratings";
        return jdbcTemplate.query(sql, ratingRowMapper);
    }

    @Override
    public boolean containsRating(Integer ratingId) {
        String sql = "SELECT COUNT(*) FROM ratings WHERE rating_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, ratingId);
        return count != null && count > 0;
    }

    @Override
    public Optional<MpaRating> findMpaRatingById(int id) {
        String sqlQuery = "SELECT * FROM ratings WHERE rating_id = ?";
        try {
            MpaRating rating = jdbcTemplate.queryForObject(sqlQuery, ratingRowMapper, id);
            return Optional.ofNullable(rating);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
