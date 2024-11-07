package ru.yandex.practicum.filmorate.dal.impl.Searching;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

@RequiredArgsConstructor
public class SearchByTitle implements SearchStrategy {
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    @Override
    public List<Film> doSearch(String query) {
        String title = "%" + query + "%";
        String sqlQuery = "SELECT f.film_id AS film_id, f.name AS film_name, f.description AS description, " +
                "f.release_date AS release_date, f.duration AS duration, " +
                "r.rating_id AS rating_id, r.rating_name AS rating_name, " +
                "g.genre AS genre, g.genre_id AS genre_id " +
                "FROM films AS f " +
                "LEFT JOIN films_genres fg ON f.film_id = fg.film_id " +
                "LEFT JOIN genres g ON g.genre_id = fg.genre_id " +
                "LEFT JOIN film_likes fl ON f.film_id = fl.film_id " +
                "JOIN ratings r ON r.rating_id = f.mpa_rating_id " +
                "LEFT JOIN films_directors fd ON f.film_id = fd.film_id " +
                "WHERE UPPER(f.name) LIKE UPPER(?) " +
                "GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, " +
                "r.rating_id, r.rating_name, g.genre " +
                "ORDER BY COUNT(fl.film_id) DESC;";
        return jdbcTemplate.query(sqlQuery, filmRowMapper, title);
    }
}
