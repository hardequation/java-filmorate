package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    List<Film> findAllFilms();

    List<Genre> findAllGenres();

    List<MpaRating> findAllMpaRatings();

    Film add(Film film);

    void remove(Integer filmId);

    Film update(Film film);

    boolean containsFilm(Integer filmId);

    boolean containsRating(Integer ratingId);

    boolean containsGenre(Integer genreId);

    Optional<Film> findFilmById(int id);

    Optional<Genre> findGenreById(int id);

    Optional<MpaRating> findMpaRatingById(int id);

    void addLike(int filmId, int userId);

    void removeLike(int filmId, int userId);

    void removeAll();
}
