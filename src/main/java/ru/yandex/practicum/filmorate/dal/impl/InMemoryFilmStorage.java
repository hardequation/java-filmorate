package ru.yandex.practicum.filmorate.dal.impl;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.FilmStorage;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Integer, Film> films = new HashMap<>();

    private int nextId = 1;

    @Override
    public Collection<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film add(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public void remove(Integer filmId) {
        films.remove(filmId);
    }

    @Override
    public Film update(Film film) {
        films.replace(film.getId(), film);
        return film;
    }

    @Override
    public boolean contains(Integer filmId) {
        return films.containsKey(filmId);
    }

    @Override
    public Optional<Film> findById(int id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public void addLike(int filmId, int userId) {
        films.get(filmId).getLikedUsersID().add(userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        films.get(filmId).getLikedUsersID().remove(userId);
    }

    @Override
    public void removeAll() {
        films.clear();
    }

    private int getNextId() {
        return nextId++;
    }
}
