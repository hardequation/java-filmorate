package ru.yandex.practicum.filmorate.dal.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.FilmStorage;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.RatingRowMapper;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Repository
@Qualifier("dbFilmStorage")
public class DbFilmStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DbFilmStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Film> findAllFilms() {
        List<Film> films = jdbcTemplate.query("SELECT * FROM films", new FilmRowMapper());
        films.forEach(film -> {
            Set<Integer> likes = new HashSet<>(this.getLikesByFilmId(film.getId()));
            film.setLikedUsersID(likes);
        });
        return films;
    }

    @Override
    public List<Genre> findAllGenres() {
        return jdbcTemplate.query("SELECT * FROM genres", new GenreRowMapper());
    }

    @Override
    public List<MpaRating> findAllMpaRatings() {
        String sql = "SELECT * FROM ratings";
        return jdbcTemplate.query(sql, new RatingRowMapper());
    }

    @Override
    public Film add(Film film) {
        Integer ratingId = film.getMpaId();
        if (!this.containsRating(ratingId)) {
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
            ps.setInt(5, film.getMpaId());
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
    public boolean containsRating(Integer ratingId) {
        String sql = "SELECT COUNT(*) FROM ratings WHERE rating_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, ratingId);
        return count != null && count > 0;
    }

    @Override
    public boolean containsGenre(Integer genreId) {
        String sql = "SELECT COUNT(*) FROM genres WHERE genre_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, genreId);
        return count != null && count > 0;
    }

    @Override
    public Optional<Film> findFilmById(int id) {
        String sqlQuery = "SELECT * FROM films WHERE film_id = ?";
        Film film = jdbcTemplate.queryForObject(sqlQuery, new FilmRowMapper(), id);
        if (film == null) {
            return Optional.empty();
        }

        film.setGenres(this.findGenresForFilm(film.getId()));

        Set<Integer> likes = new HashSet<>(this.getLikesByFilmId(film.getId()));
        film.setLikedUsersID(likes);
        return Optional.ofNullable(film);
    }

    @Override
    public Optional<Genre> findGenreById(int id) {
        String sqlQuery = "SELECT * FROM genres WHERE genre_id = ?";
        try {
            Genre genre = jdbcTemplate.queryForObject(sqlQuery, new GenreRowMapper(), id);
            return Optional.ofNullable(genre);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<Genre> findGenresForFilm(int filmId) {
        String genres = "SELECT g.genre_id genre_id, g.genre genre " +
                "FROM films_genres fg " +
                "INNER JOIN genres g " +
                "ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id = ?";

        return jdbcTemplate.query(genres, new GenreRowMapper(), filmId);
    }

    @Override
    public Optional<MpaRating> findMpaRatingById(int id) {
        String sqlQuery = "SELECT * FROM ratings WHERE rating_id = ?";
        try {
            MpaRating rating = jdbcTemplate.queryForObject(sqlQuery, new RatingRowMapper(), id);
            return Optional.ofNullable(rating);
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

        List<Genre> genres = film.getGenres();

        for (Genre genre : genres) {
            Integer genreId = genre.getId();
            if (!this.containsGenre(genreId)) {
                throw new ValidationException("Genre with id " + genreId + "isn't found");
            }
            jdbcTemplate.update(addFilmGenre, film.getId(), genreId);
        }
    }
}
