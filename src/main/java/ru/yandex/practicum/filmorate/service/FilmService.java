package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.dal.FilmStorage;
import ru.yandex.practicum.filmorate.dal.UserStorage;

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
    public FilmService(@Qualifier("dbFilmStorage") FilmStorage filmStorage,
                       @Qualifier("dbUserStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<Film> getFilms() {
        return filmStorage.findAll();
    }

    public Film getFilm(int id) {
        return filmStorage.findById(id).orElseThrow(() -> new NotFoundException(FILM_NOT_FOUND + id));
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

    public void addLike(Integer filmId, Integer userId) {
        if (!userStorage.contains(userId)) {
            throw new NotFoundException(USER_NOT_FOUND + userId);
        }

        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException(FILM_NOT_FOUND + filmId));
        Set<Integer> likes = film.getLikedUsersID();
        if (likes.contains(userId)) {
            throw new ValidationException("User with id " + userId + " has already liked film with id " + filmId);
        }

        likes.add(userId);
        film.setLikedUsersID(likes);
        filmStorage.update(film);
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        if (!userStorage.contains(userId)) {
            throw new NotFoundException(USER_NOT_FOUND + userId);
        }

        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException(FILM_NOT_FOUND + filmId));
        Set<Integer> likes = film.getLikedUsersID();

        if (!likes.contains(userId)) {
            throw new ValidationException("User " + userId + " hasn't liked film yet with id " + filmId);
        }

        likes.remove(userId);
        film.setLikedUsersID(likes);
        filmStorage.update(film);
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getMostPopularFilms(int length) {
        List<Film> popularFilms = filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt((Film o) -> o.getLikedUsersID().size()).reversed())
                .toList();

        return length < popularFilms.size() ? popularFilms.subList(0, length) : popularFilms;
    }
}
