package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static ru.yandex.practicum.filmorate.utils.ErrorMessages.FILM_NOT_FOUND;
import static ru.yandex.practicum.filmorate.utils.ErrorMessages.USER_NOT_FOUND;

@Service
public class FilmService {
    private final FilmStorage filmStorage;

    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilm(long id) {
        return filmStorage.getFilm(id).orElseThrow(() -> new NotFoundException(FILM_NOT_FOUND + id));
    }

    public Film create(Film film) {
        filmStorage.add(film);
        return film;
    }

    public Film updateFilm(Film newFilm) {
        if (!filmStorage.contains(newFilm.getId())) {
            throw new NotFoundException(FILM_NOT_FOUND + newFilm.getId());
        }
        filmStorage.update(newFilm);
        return newFilm;
    }

    public void addLike(Long filmId, Long userId) {
        if (!userStorage.contains(userId)) {
            throw new NotFoundException(USER_NOT_FOUND + userId);
        }

        Film film = filmStorage.getFilm(filmId)
                .orElseThrow(() -> new NotFoundException(FILM_NOT_FOUND + filmId));
        Set<Long> likes = film.getLikedUsersID();
        if (likes.contains(userId)) {
            throw new ValidationException("User with id " + userId + " has already liked film with id " + filmId);
        }

        likes.add(userId);
    }

    public void removeLike(Long filmId, Long userId) {
        if (!userStorage.contains(userId)) {
            throw new NotFoundException(USER_NOT_FOUND + userId);
        }

        Film film = filmStorage.getFilm(filmId)
                .orElseThrow(() -> new NotFoundException(FILM_NOT_FOUND + filmId));
        Set<Long> likes = film.getLikedUsersID();
        if (!likes.contains(userId)) {
            throw new ValidationException("User " + userId + " hasn't liked film yet with id " + filmId);
        }

        likes.remove(userId);
    }

    public List<Film> getMostPopularFilms(int length) {
        List<Film> popularFilms = filmStorage.getFilms().stream()
                .sorted(Comparator.comparingInt((Film o) -> o.getLikedUsersID().size()).reversed())
                .toList();

        return length < popularFilms.size() ? popularFilms.subList(0, length) : popularFilms;
    }
}
