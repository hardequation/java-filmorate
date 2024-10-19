package ru.yandex.practicum.filmorate.dal.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.FilmStorage;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@Qualifier("dbFilmStorage")
public class DbFilmStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DbFilmStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    @Override
    public Collection<Film> findAll() {
        return jdbcTemplate.query("SELECT * FROM films", new FilmRowMapper());
    }

    @Override
    public Film add(Film film) {
        String addFilmsql = "INSERT INTO films (name, description, release_date, duration, mpa_rating_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(addFilmsql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setLong(4, film.getDuration());
            ps.setInt(5, film.getMpaRatingId());
            return ps;
        }, keyHolder);

        Integer generatedId = Objects.requireNonNull(keyHolder.getKey()).intValue();
        film.setId(generatedId);

        updateGenres(film);

        return film;
    }

    @Override
    public void remove(Integer filmId) {
        String deleteLikesSql = "DELETE FROM films_likes WHERE film_id = ?";
        jdbcTemplate.update(deleteLikesSql, filmId);

        String deleteGenresSql = "DELETE FROM films_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteGenresSql, filmId);

        String deleteFilmSql = "DELETE FROM films WHERE film_id = ?";
        jdbcTemplate.update(deleteFilmSql, filmId);
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET " +
                "name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? " +
                "WHERE film_id = ?";

        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpaRatingId(),
                film.getId());

        return film;
    }

    @Override
    public boolean contains(Integer filmId) {
        String sql = "SELECT COUNT(*) FROM films WHERE film_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, filmId);
        return count != null && count > 0;
    }

    @Override
    public Optional<Film> findById(int id) {
        String sqlQuery = "SELECT * FROM films WHERE film_id = ?";
        try {
            Film film = jdbcTemplate.queryForObject(sqlQuery, new FilmRowMapper(), id);
            return Optional.ofNullable(film);
        } catch (Exception e) {
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
    public void removeAll() {
        String removeFilmsSql = "DELETE FROM films";
        String removeGenresSql = "DELETE FROM films_genres";
        String removeLikesSql = "DELETE FROM film_likes";
        jdbcTemplate.update(removeGenresSql);
        jdbcTemplate.update(removeLikesSql);
        jdbcTemplate.update(removeFilmsSql);
    }

    public List<Integer> getLikesByFilmId(Integer filmId) {
        String sqlQuery = "SELECT liked_user_id FROM film_likes WHERE film_id = ?";
        return jdbcTemplate.query(sqlQuery, (rs, rowNum) -> rs.getInt("liked_user_id"), filmId);
    }

    private void updateGenres(Film film) {
        String addFilmGenre = "MERGE INTO films_genres (film_id, genre_id) VALUES (?, ?)";
        String genreIdQuery = "SELECT genre_id FROM genres WHERE genre = ?";

        List<Integer> genreIds = new ArrayList<>();
        List<String> genres = film.getGenres();
        if (genres == null) {
            return;
        }

        for (String genre : genres) {
            Integer genreId = jdbcTemplate.queryForObject(genreIdQuery, new Object[]{genre}, Integer.class);
            if (genreId != null) {
                genreIds.add(genreId);
            }
        }

        for (Integer genreId : genreIds) {
            jdbcTemplate.update(addFilmGenre, film.getId(), genreId);
        }
    }
}
