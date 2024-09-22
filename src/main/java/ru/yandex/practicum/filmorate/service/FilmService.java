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

@Service
public class FilmService {
    FilmStorage filmStorage;

    UserStorage userStorage;

    private long nextId = 1;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilm(long id) {
        if (!filmStorage.contains(id)) {
            throw new NotFoundException("There is no film with such id:" + id);
        }
        return filmStorage.getFilm(id);
    }

    public Film create(Film film) {
        film.setId(getNextId());
        filmStorage.add(film);
        return film;
    }

    public Film updateFilm(Film newFilm) {
        if (!filmStorage.contains(newFilm.getId())) {
            throw new NotFoundException("Film with id = " + newFilm.getId() + " isn't found");
        }
        filmStorage.update(newFilm);
        return newFilm;
    }

    public void addLike(Long filmId, Long userId) {
        if (!filmStorage.contains(filmId)) {
            throw new NotFoundException("Unable to find film with id " + filmId);
        }

        if (!userStorage.contains(userId)) {
            throw new NotFoundException("Unable to find user with id " + userId);
        }

        Set<Long> likes = filmStorage.getFilm(filmId).getLikedUsersID();
        if (likes.contains(userId)) {
            throw new ValidationException("User has already liked this film");
        }

        likes.add(userId);
    }

    public void removeLike(Long filmId, Long userId) {
        if (!filmStorage.contains(filmId)) {
            throw new NotFoundException("Unable to find film with id " + filmId);
        }

        if (!userStorage.contains(userId)) {
            throw new NotFoundException("Unable to find user with id " + userId);
        }

        Set<Long> likes = filmStorage.getFilm(filmId).getLikedUsersID();
        if (!likes.contains(userId)) {
            throw new ValidationException("User hasn't liked this film yet");
        }

        likes.remove(userId);
    }

    public List<Film> getMostPopularFilms(int length) {
        List<Film> popularFilms = filmStorage.getFilms().stream()
                .sorted(Comparator.comparingInt((Film o)-> o.getLikedUsersID().size()).reversed())
                .toList();

        return length < popularFilms.size() ? popularFilms.subList(0, length) : popularFilms ;
    }

    private long getNextId() {
        return nextId++;
    }
}
