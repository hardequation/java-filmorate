package ru.yandex.practicum.filmorate.dal.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.FilmStorage;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static ru.yandex.practicum.filmorate.utils.ErrorMessages.FILM_NOT_FOUND;

@Repository
@Qualifier("dbFilmStorage")
public class DbFilmStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    private final FilmRowMapper filmRowMapper;

    @Autowired
    public DbFilmStorage(JdbcTemplate jdbcTemplate,
                         FilmRowMapper filmRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmRowMapper = filmRowMapper;
    }

    @Override
    public List<Film> findAllFilms() {
        return jdbcTemplate.query("SELECT " +
                        "f.film_id film_id, " +
                        "f.name film_name, " +
                        "f.description description, " +
                        "f.release_date release_date, " +
                        "f.duration duration, " +
                        "r.rating_id rating_id, " +
                        "r.rating_name rating " +
                        "FROM films f " +
                        "INNER JOIN ratings r ON f.mpa_rating_id = r.rating_id; ",
                filmRowMapper);
    }

    @Override
    public Film add(Film film) {
        String addFilmsql = "INSERT INTO films (name, description, release_date, duration, mpa_rating_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(addFilmsql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, film.getName());
                ps.setString(2, film.getDescription());
                ps.setDate(3, Date.valueOf(film.getReleaseDate()));
                ps.setLong(4, film.getDuration());
                ps.setObject(5, film.getMpa() != null ? film.getMpa().getId() : 5, Types.INTEGER);

                return ps;
            }, keyHolder);
        } catch (DataIntegrityViolationException e) {
            throw new ValidationException("Rating with id " + film.getMpa().getId() + " isn't found");
        }

        int generatedId = Objects.requireNonNull(keyHolder.getKey()).intValue();
        film.setId(generatedId);

        return film;
    }

    @Override
    public void removeFilm(Integer filmId) {
        String deleteFilmSql = "DELETE FROM films WHERE film_id = ?";
        jdbcTemplate.update(deleteFilmSql, filmId);
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET " +
                "name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? " +
                "WHERE film_id = ?";

        int rowsAffected = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        if (rowsAffected == 0) {
            throw new NotFoundException(FILM_NOT_FOUND + film.getId());
        }
        return film;
    }

    @Override
    public boolean containsFilm(Integer filmId) {
        String sql = "SELECT COUNT(*) FROM films WHERE film_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, filmId);
        return count != null && count > 0;
    }

    @Override
    public Optional<Film> findFilmById(int id) {
        String sqlQuery = "SELECT " +
                "f.film_id as film_id, " +
                "f.name as film_name, " +
                "f.description as description, " +
                "f.release_date as release_date, " +
                "f.duration as duration, " +
                "r.rating_id as rating_id, " +
                "r.rating_name as rating, " +
                "FROM films f " +
                "INNER JOIN ratings r ON f.mpa_rating_id = r.rating_id " +
                "WHERE f.film_id = ?; ";
        try {
            Film film = jdbcTemplate.queryForObject(sqlQuery, filmRowMapper, id);
            return Optional.of(film);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void addLike(int filmId, int userId) {
        String sql = "INSERT INTO film_likes (film_id, liked_user_id) VALUES (?, ?)";

        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND liked_user_id = ?";

        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public List<Film> getMostPopularFilms(int size) {
        String filmsSql = "SELECT " +
                "f.film_id AS film_id, " +
                "f.name AS film_name, " +
                "f.description AS description, " +
                "f.release_date AS release_date, " +
                "f.duration AS duration, " +
                "r.rating_id AS rating_id, " +
                "r.rating_name AS rating_name " +
                "FROM films AS f " +
                "LEFT JOIN film_likes fl ON f.film_id = fl.film_id " +
                "JOIN ratings r ON r.rating_id = f.mpa_rating_id " +
                "GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, r.rating_id, r.rating_name " +
                "ORDER BY COUNT(f.film_id) DESC " +
                "LIMIT ?";

        return jdbcTemplate.query(filmsSql, filmRowMapper, size);
    }

    @Override
    public void removeAll() {
        String removeFilmsSql = "DELETE FROM films";
        jdbcTemplate.update(removeFilmsSql);
    }

    public List<Integer> getLikesByFilmId(Integer filmId) {
        String sqlQuery = "SELECT liked_user_id FROM film_likes WHERE film_id = ?";
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> rs.getInt("liked_user_id"), filmId);
    }
}
