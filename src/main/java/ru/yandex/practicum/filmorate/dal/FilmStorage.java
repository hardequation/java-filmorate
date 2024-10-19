package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Collection<Film> findAll();

    Film add(Film film);

    void remove(Integer filmId);

    Film update(Film film);

    boolean contains(Integer filmId);

    Optional<Film> findById(int id);

    void addLike(int filmId, int userId);

    void removeLike(int filmId, int userId);

    void removeAll();
}
