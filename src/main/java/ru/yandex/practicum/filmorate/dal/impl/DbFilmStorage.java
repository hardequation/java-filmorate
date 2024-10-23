package ru.yandex.practicum.filmorate.dal.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.FilmStorage;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static ru.yandex.practicum.filmorate.utils.ErrorMessages.FILM_NOT_FOUND;

@Repository
@Qualifier("dbFilmStorage")
public class DbFilmStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    private final FilmRowMapper filmRowMapper;

    private final GenreRowMapper genreRowMapper;

    @Autowired
    public DbFilmStorage(JdbcTemplate jdbcTemplate,
                         GenreRowMapper genreRowMapper,
                         FilmRowMapper filmRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreRowMapper = genreRowMapper;
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
                        "INNER JOIN ratings r ON f.mpa_rating_id = r.rating_id; "
                , filmRowMapper);
    }

    @Override
    public Film add(Film film) {
        String addFilmsql = "INSERT INTO films (name, description, release_date, duration, mpa_rating_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        String addFilmGenre = "MERGE INTO films_genres (film_id, genre_id) VALUES (?, ?)";

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

        Integer generatedId = Objects.requireNonNull(keyHolder.getKey()).intValue();
        film.setId(generatedId);

        List<Genre> genres = film.getGenres().stream().toList();
        if (genres.isEmpty()) {
            return film;
        }

        Integer filmId = film.getId();
        try {
            jdbcTemplate.batchUpdate(addFilmGenre,
                    new BatchPreparedStatementSetter() {
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            ps.setInt(1, filmId);
                            ps.setInt(2, genres.get(i).getId());
                        }

                        public int getBatchSize() {
                            return genres.size();
                        }

                    });
        } catch (DataIntegrityViolationException e) {
            throw new ValidationException("Genres of films should present in genres table");
        }

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
        Film film = jdbcTemplate.queryForObject(sqlQuery, filmRowMapper, id);
        if (film == null) {
            return Optional.empty();
        }

        String genres = "SELECT g.genre_id genre_id, g.genre genre " +
                "FROM films_genres fg " +
                "INNER JOIN genres g " +
                "ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id = ?";

        List<Genre> genresList = jdbcTemplate.query(genres, genreRowMapper, film.getId());
        film.setGenres(new LinkedHashSet<>(genresList));

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
        String filmsSql = "SELECT " +
                "f.film_id AS film_id, " +
                "f.name AS film_name, " +
                "f.description AS description, " +
                "f.release_date AS release_date, " +
                "f.duration AS duration, " +
                "r.rating_id AS rating_id, " +
                "r.rating_name AS rating_name, " +
                "g.genre_id AS genre_id, " +
                "g.genre AS genre_name " +
                "FROM films AS f " +
                "JOIN film_likes fl ON f.film_id = fl.film_id " +
                "JOIN ratings r ON r.rating_id = f.mpa_rating_id " +
                "JOIN films_genres fg ON f.film_id = fg.film_id " +
                "JOIN genres g ON fg.genre_id = g.genre_id " +
                "GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, r.rating_id, r.rating_name, g.genre_id, g.genre " +
                "ORDER BY COUNT(fl.liked_user_id) DESC " +
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
