package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface GenreStorage {
    List<Genre> findAllGenres();

    boolean containsGenre(Integer genreId);

    Optional<Genre> findGenreById(int id);

    void updateGenresOfFilm(Film film);

    List<Genre> findGenresForFilm(int filmId);

    Map<Integer, Set<Genre>> loadFilmsGenres(List<Integer> filmIds);
}
