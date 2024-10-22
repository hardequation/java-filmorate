package ru.yandex.practicum.filmorate.dal.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.FilmStorage;
import ru.yandex.practicum.filmorate.dal.GenreStorage;
import ru.yandex.practicum.filmorate.dal.RatingStorage;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@Qualifier("dbFilmStorage")
public class DbFilmStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    private final FilmRowMapper filmRowMapper;

    private final GenreStorage genreStorage;

    private final RatingStorage ratingStorage;

    @Autowired
    public DbFilmStorage(JdbcTemplate jdbcTemplate, GenreStorage genreStorage, RatingStorage ratingStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreStorage = genreStorage;
        this.ratingStorage = ratingStorage;
        this.filmRowMapper = new FilmRowMapper();
    }

    @Override
    public List<Film> findAllFilms() {
        return jdbcTemplate.query("SELECT * FROM films", filmRowMapper);
    }

    @Override
    public Film add(Film film) {
        Integer ratingId = film.getMpaId();
        if (!ratingStorage.containsRating(ratingId)) {
            throw new ValidationException("Rating with id " + ratingId + "isn't found");
        }

        String addFilmsql = "INSERT INTO films (name, description, release_date, duration, mpa_rating_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(addFilmsql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setLong(4, film.getDuration());
            ps.setObject(5, film.getMpaId() != null ? film.getMpaId() : 5, Types.INTEGER);

            return ps;
        }, keyHolder);

        Integer generatedId = Objects.requireNonNull(keyHolder.getKey()).intValue();
        film.setId(generatedId);

        genreStorage.addGenresOfFilm(film);

        return film;
    }

    @Override
    public void remove(Integer filmId) {
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
                film.getMpaId(),
                film.getId());

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
        String sqlQuery = "SELECT * FROM films WHERE film_id = ?";
        Film film = jdbcTemplate.queryForObject(sqlQuery, filmRowMapper, id);
        if (film == null) {
            return Optional.empty();
        }

        film.setGenres(new LinkedHashSet<>(genreStorage.findGenresForFilm(id)));

        return Optional.of(film);
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
        String films = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id " +
                "FROM films AS f " +
                "LEFT JOIN film_likes AS fl ON f.film_id = fl.film_id " +
                "GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id " +
                "ORDER BY COUNT(fl.liked_user_id) DESC " +
                "LIMIT ?;";

        return jdbcTemplate.query(films, filmRowMapper, size);
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
}
