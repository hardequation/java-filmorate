package ru.yandex.practicum.filmorate.dal.impl;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.FilmStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Integer, Film> films = new HashMap<>();

    private int nextId = 1;

    @Override
    public List<Film> findAllFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public List<Genre> findAllGenres() {
        return new ArrayList<>();
    }

    @Override
    public List<MpaRating> findAllMpaRatings() {
        return new ArrayList<>();
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
    public boolean containsFilm(Integer filmId) {
        return films.containsKey(filmId);
    }

    @Override
    public boolean containsRating(Integer ratingId) {
        return false;
    }

    @Override
    public boolean containsGenre(Integer genreId) {
        return false;
    }

    @Override
    public Optional<Film> findFilmById(int id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Optional<Genre> findGenreById(int id) {
        return Optional.empty();
    }

    @Override
    public Optional<MpaRating> findMpaRatingById(int id) {
        return Optional.empty();
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
