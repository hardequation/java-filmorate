package ru.yandex.practicum.filmorate.dal.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.GenreStorage;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class DbGenreStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;

    private final GenreRowMapper genreRowMapper;

    @Override
    public List<Genre> findAllGenres() {
        return jdbcTemplate.query("SELECT * FROM genres", genreRowMapper);
    }

    @Override
    public boolean containsGenre(Integer genreId) {
        String sql = "SELECT COUNT(*) FROM genres WHERE genre_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, genreId);
        return count != null && count > 0;
    }

    @Override
    public Optional<Genre> findGenreById(int id) {
        String sqlQuery = "SELECT * FROM genres WHERE genre_id = ?";
        try {
            Genre genre = jdbcTemplate.queryForObject(sqlQuery, genreRowMapper, id);
            return Optional.ofNullable(genre);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void updateGenresOfFilm(Film film) {
        String removeFilmGenre = "DELETE FROM films_genres where film_id = ?";
        jdbcTemplate.update(removeFilmGenre, film.getId());

        String addFilmGenre = "MERGE INTO films_genres (film_id, genre_id) VALUES (?, ?)";
        List<Genre> genres = film.getGenres().stream().toList();
        film.setGenres(new LinkedHashSet<>(genres));
        if (genres.isEmpty()) {
            return;
        }
        checkGenres(genres);

        Integer filmId = film.getId();
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
    }

    @Override
    public List<Genre> findGenresForFilm(int filmId) {
        String genres = "SELECT g.genre_id genre_id, g.genre genre " +
                "FROM films_genres fg " +
                "INNER JOIN genres g " +
                "ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id = ?";

        return jdbcTemplate.query(genres, genreRowMapper, filmId);
    }

    private void checkGenres(List<Genre> genres) {
        List<Integer> genreIds = genres.stream()
                .map(Genre::getId)
                .toList();

        if (genreIds.isEmpty()) {
            return;
        }

        String placeholders = String.join(",", Collections.nCopies(genreIds.size(), "?"));

        String sql = "SELECT COUNT(*) FROM genres WHERE genre_id IN (" + placeholders + ");";

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, genreIds.toArray());

        if (count == null || count < genreIds.size()) {
            throw new ValidationException("Some genres are not found in the database");
        }
    }

    @Override
    public Map<Integer, Set<Genre>> loadFilmsGenres(List<Integer> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) {
            return Map.of();
        }

        String sql = "SELECT fg.film_id, g.genre_id, g.genre " +
                "FROM films_genres fg " +
                "INNER JOIN genres g ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id IN (%s)";

        String placeholders = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        sql = String.format(sql, placeholders);


        return jdbcTemplate.query(sql, filmIds.toArray(new Object[0]), (rs, rowNum) -> {
                    Genre genre = genreRowMapper.mapRow(rs, rowNum);
                    int filmId = rs.getInt("film_id");
                    return Map.entry(filmId, new LinkedHashSet(Set.of(genre)));
                }).stream()
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                        Collectors.flatMapping(e -> e.getValue().stream(), Collectors.toCollection(LinkedHashSet::new))));
    }
}
