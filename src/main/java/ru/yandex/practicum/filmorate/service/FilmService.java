package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.*;

import java.util.LinkedHashSet;
import java.util.List;

import static ru.yandex.practicum.filmorate.utils.ErrorMessages.*;

@Service
public class FilmService {
    private final FilmStorage filmStorage;

    private final UserStorage userStorage;

    private final RatingStorage ratingStorage;

    private final GenreStorage genreStorage;

    private final DirectorStorage directorStorage;

    @Autowired
    public FilmService(FilmStorage dbFilmStorage,
                       UserStorage dbUserStorage,
                       RatingStorage ratingStorage,
                       GenreStorage genreStorage,
                       DirectorStorage directorStorage) {
        this.filmStorage = dbFilmStorage;
        this.userStorage = dbUserStorage;
        this.ratingStorage = ratingStorage;
        this.genreStorage = genreStorage;
        this.directorStorage = directorStorage;
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

    public List<Director> getDirectors() {
        return directorStorage.findAllDirectors();
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

    public Director findDirectorById(int id) {
        return directorStorage.findDirectorById(id).orElseThrow(() -> new NotFoundException(DIRECTOR_NOT_FOUND + id));
    }

    public LinkedHashSet<Genre> findGenresForFilm(int id) {
        return new LinkedHashSet<>(genreStorage.findGenresForFilm(id));
    }

    public void addGenresForFilm(Film film) {
        genreStorage.addGenresOfFilm(film);
    }

    public LinkedHashSet<Director> findDirectorsForFilm(int id) {
        return new LinkedHashSet<>(directorStorage.findDirectorForFilm(id));
    }

    public void addDirectorsForFilm(Film film) {
        directorStorage.addDirectorOfFilm(film);
    }

    public Director createDirector(Director director) {
        return directorStorage.createDirector(director);
    }

    public Director updateDirector(Director director) {
        return directorStorage.updateDirector(director);
    }

    public void deleteDirector(Integer id) {
        directorStorage.deleteDirector(id);
    }

    public Film create(Film film) {
        return filmStorage.add(film);
    }

    public void removeFilm(Integer id) {
        filmStorage.removeFilm(id);
    }

    public Film updateFilm(Film newFilm) {
        genreStorage.addGenresOfFilm(newFilm);
        directorStorage.addDirectorOfFilm(newFilm);
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

    public List<Film> getFilmsByDirectorSorted(int directorId, FilmSortParam sortParam) {
        return filmStorage.getFilmsByDirectorSorted(directorId, sortParam);
    }
}
