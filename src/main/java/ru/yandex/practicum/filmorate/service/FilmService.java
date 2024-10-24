package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FilmStorage;
import ru.yandex.practicum.filmorate.dal.GenreStorage;
import ru.yandex.practicum.filmorate.dal.RatingStorage;
import ru.yandex.practicum.filmorate.dal.UserStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.LinkedHashSet;
import java.util.List;

import static ru.yandex.practicum.filmorate.utils.ErrorMessages.FILM_NOT_FOUND;
import static ru.yandex.practicum.filmorate.utils.ErrorMessages.GENRE_NOT_FOUND;
import static ru.yandex.practicum.filmorate.utils.ErrorMessages.RATING_NOT_FOUND;
import static ru.yandex.practicum.filmorate.utils.ErrorMessages.USER_NOT_FOUND;

@Service
public class FilmService {
    private final FilmStorage filmStorage;

    private final UserStorage userStorage;

    private final RatingStorage ratingStorage;

    private final GenreStorage genreStorage;

    @Autowired
    public FilmService(FilmStorage dbFilmStorage,
                       UserStorage dbUserStorage,
                       RatingStorage ratingStorage,
                       GenreStorage genreStorage) {
        this.filmStorage = dbFilmStorage;
        this.userStorage = dbUserStorage;
        this.ratingStorage = ratingStorage;
        this.genreStorage = genreStorage;
    }

    public List<Film> getFilms() {
        return filmStorage.findAllFilms();
    }

    public List<Genre> getGenres() {
        return genreStorage.findAllGenres();
    }

    public List<MpaRating> getRatings() {
        return ratingStorage.findAllMpaRatings();
    }

    public Film findFilmById(int id) {
        return filmStorage.findFilmById(id).orElseThrow(() -> new NotFoundException(FILM_NOT_FOUND + id));
    }

    public Genre findGenreById(int id) {
        return genreStorage.findGenreById(id).orElseThrow(() -> new NotFoundException(RATING_NOT_FOUND + id));
    }

    public MpaRating findMpaRatingById(int id) {
        return ratingStorage.findMpaRatingById(id).orElseThrow(() -> new NotFoundException(GENRE_NOT_FOUND + id));
    }

    public LinkedHashSet<Genre> findGenresForFilm(int id) {
        return new LinkedHashSet<>(genreStorage.findGenresForFilm(id));
    }

    public void addGenresForFilm(Film film) {
        genreStorage.addGenresOfFilm(film);
    }

    public Film create(Film film) {
        return filmStorage.add(film);
    }

    public Film updateFilm(Film newFilm) {
        genreStorage.addGenresOfFilm(newFilm);
        return filmStorage.update(newFilm);
    }

    public void addLike(Integer filmId, Integer userId) {
        if (!userStorage.contains(userId)) {
            throw new NotFoundException(USER_NOT_FOUND + userId);
        }

        if (!filmStorage.containsFilm(filmId)) {
            throw new NotFoundException(FILM_NOT_FOUND + filmId);
        }

        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Integer filmId, Integer userId) {
        if (!userStorage.contains(userId)) {
            throw new NotFoundException(USER_NOT_FOUND + userId);
        }

        if (!filmStorage.containsFilm(filmId)) {
            throw new NotFoundException(FILM_NOT_FOUND + filmId);
        }

        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getMostPopularFilms(int size) {
        return filmStorage.getMostPopularFilms(size);
    }
}
