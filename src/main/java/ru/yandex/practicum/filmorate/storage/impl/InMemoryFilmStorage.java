package ru.yandex.practicum.filmorate.storage.impl;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();

    private long nextId = 1;

    @Override
    public Collection<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film add(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        films.replace(film.getId(), film);
        return film;
    }

    @Override
    public boolean contains(Long filmId) {
        return films.containsKey(filmId);
    }

    @Override
    public Optional<Film> getFilm(long id) {
        return Optional.ofNullable(films.get(id));
    }

    private long getNextId() {
        return nextId++;
    }
}
