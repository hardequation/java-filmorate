package ru.yandex.practicum.filmorate.service.film.Sorting;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SortDirectorFilmsByLikes implements SortDirectorFilmsStrategy {

    @Override
    public String getSortSQL(int directorId) {
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
                "LEFT JOIN films_directors fd on f.film_id = fd.film_id " +
                "WHERE fd.director_id = ? " +
                "GROUP BY f.film_id, f.name, f.description, f.release_date, f.duration, r.rating_id, r.rating_name " +
                "ORDER BY COUNT(f.film_id) DESC";
        return filmsSql;
    }
}
