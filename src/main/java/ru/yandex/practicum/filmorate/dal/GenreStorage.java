package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

public interface GenreStorage {
    List<Genre> findAllGenres();

    boolean containsGenre(Integer genreId);

    Optional<Genre> findGenreById(int id);

    void updateGenresOfFilm(Film film);

    List<Genre> findGenresForFilm(int filmId);
}
