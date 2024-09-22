package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Collection<Film> getFilms();

    Film add(Film film);

    Film update(Film film);

    boolean contains(Long filmId);

    Film getFilm(long id);
}
