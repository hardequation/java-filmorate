package ru.yandex.practicum.filmorate.dal.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.DirectorStorage;
import ru.yandex.practicum.filmorate.dal.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static ru.yandex.practicum.filmorate.utils.ErrorMessages.DIRECTOR_NOT_FOUND;

@Repository
@RequiredArgsConstructor
public class DbDirectorStorage implements DirectorStorage {

    private final JdbcTemplate jdbcTemplate;

    private final DirectorRowMapper directorRowMapper;

    @Override
    public List<Director> findAllDirectors() {
        return jdbcTemplate.query("SELECT * FROM directors", directorRowMapper);
    }

    @Override
    public Optional<Director> findDirectorById(int id) {
        String sql = "SELECT * FROM directors WHERE director_id = ?";
        try {
            Director director = jdbcTemplate.queryForObject(sql, directorRowMapper, id);
            return Optional.ofNullable(director);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean containsDirector(Integer directorId) {
        String sql = "SELECT COUNT(*) FROM directors WHERE director_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, directorId);
        return count != null && count > 0;
    }

    @Override
    public Director createDirector(Director director) {
        String sql = "INSERT INTO directors (director_name) VALUES (?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

        int generatedId = Objects.requireNonNull(keyHolder.getKey()).intValue();
        director.setId(generatedId);

        return director;
    }

    @Override
    public Director updateDirector(Director director) {
        String sql = "UPDATE directors SET director_name = ? WHERE director_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, director.getName(), director.getId());

        if (rowsAffected == 0) {
            throw new NotFoundException(DIRECTOR_NOT_FOUND + director.getId());
        }
        return director;
    }

    @Override
    public void deleteDirector(int id) {
        String sql = "DELETE FROM directors WHERE director_id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void addDirectorOfFilm(Film film) {
        String addFilmDirector = "MERGE INTO films_directors (film_id, director_id) VALUES (?, ?)";

        List<Director> directors = film.getDirectors().stream().toList();
        if (directors.isEmpty()) {
            return;
        }
        checkDirectors(directors);

        Integer filmId = film.getId();
        jdbcTemplate.batchUpdate(addFilmDirector,
                new BatchPreparedStatementSetter() {
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, filmId);
                        ps.setInt(2, directors.get(i).getId());
                    }

                    public int getBatchSize() {
                        return directors.size();
                    }

                });
    }

    private void checkDirectors(List<Director> directors) {
        List<Integer> directorIds = directors.stream()
                .map(Director::getId)
                .toList();

        if (directorIds.isEmpty()) {
            return;
        }

        String placeholders = String.join(",", Collections.nCopies(directorIds.size(), "?"));

        String sql = "SELECT COUNT(*) FROM directors WHERE director_id IN (" + placeholders + ");";

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, directorIds.toArray());

        if (count == null || count < directorIds.size()) {
            throw new ValidationException("Some directors are not found in the database");
        }
    }

    @Override
    public List<Director> findDirectorForFilm(int filmId) {
        String sql = "SELECT d.director_id, d.director_name " +
                "FROM films_directors fd " +
                "INNER JOIN directors d " +
                "ON fd.director_id = d.director_id " +
                "WHERE fd.film_id = ?";

        return jdbcTemplate.query(sql, directorRowMapper, filmId);
    }
}
