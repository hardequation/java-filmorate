package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    List<Film> findAllFilms();

    Film add(Film film);

    void remove(Integer filmId);

    Film update(Film film);

    boolean containsFilm(Integer filmId);

    Optional<Film> findFilmById(int id);

    void addLike(int filmId, int userId);

    void removeLike(int filmId, int userId);

    List<Film> getMostPopularFilms(int size);

    void removeAll();
}
