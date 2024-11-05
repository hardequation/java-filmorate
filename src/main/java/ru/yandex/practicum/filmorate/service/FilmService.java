package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.DirectorStorage;
import ru.yandex.practicum.filmorate.dal.FilmStorage;
import ru.yandex.practicum.filmorate.dal.GenreStorage;
import ru.yandex.practicum.filmorate.dal.RatingStorage;
import ru.yandex.practicum.filmorate.dal.UserStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.FilmSortParam;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.LinkedHashSet;
import java.util.List;

import static ru.yandex.practicum.filmorate.model.enums.EventType.LIKE;
import static ru.yandex.practicum.filmorate.model.enums.Operation.ADD;
import static ru.yandex.practicum.filmorate.utils.ErrorMessages.DIRECTOR_NOT_FOUND;
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

    public void updateGenresForFilm(Film film) {
        genreStorage.updateGenresOfFilm(film);
    }

    public LinkedHashSet<Director> findDirectorsForFilm(int id) {
        return new LinkedHashSet<>(directorStorage.findDirectorForFilm(id));
    }

    public void updateDirectorsForFilm(Film film) {
        directorStorage.updateDirectorOfFilm(film);
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
        genreStorage.updateGenresOfFilm(newFilm);
        directorStorage.updateDirectorOfFilm(newFilm);
        return filmStorage.update(newFilm);
    }

    public void addLike(Integer filmId, Integer userId) {
        if (!userStorage.contains(userId)) {
            throw new NotFoundException(USER_NOT_FOUND + userId);
        }

        if (!filmStorage.containsFilm(filmId)) {
            throw new NotFoundException(FILM_NOT_FOUND + filmId);
        }
        if (!filmStorage.checkLikesUserByFilmId(filmId, userId)) {
            filmStorage.addLike(filmId, userId);
        } else {
            userStorage.addFeed(filmId, userId, LIKE, ADD);
        }
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

    public List<Film> searchFilms(String query, List<String> by) {
        List<Film> foundedFilms;
        if (by == null || by.isEmpty()) {
            throw new ValidationException("Передано некорректное число параметров");
        } else {
            if (by.size() == 2 && by.contains("title") && by.contains("director")) {
                foundedFilms = filmStorage.searchFilmsByTitleAndDirector(query);
            } else if (by.size() == 1) {
                if (by.getFirst().equalsIgnoreCase("title")) {
                    foundedFilms = filmStorage.searchFilmsByTitle(query);
                } else if (by.getFirst().equalsIgnoreCase("director")) {
                    foundedFilms = filmStorage.searchFilmsByDirector(query);
                } else {
                    throw new NotFoundException("Неверно указан параметр поиска");
                }
            } else {
                throw new ValidationException("Передано некорректное число параметров");
            }
        }
        for (Film film : foundedFilms) {
            film.setGenres(findGenresForFilm(film.getId()));
            film.setDirectors(findDirectorsForFilm(film.getId()));
        }
        return foundedFilms;
    }

    public List<Film> getFilmsByDirectorSorted(int directorId, FilmSortParam sortParam) {
        return filmStorage.getFilmsByDirectorSorted(directorId, sortParam);
    }

    public List<Film> getPopularFilmsSortedByGenreAndYear(Integer count, Integer genreId, Integer year) {
        return filmStorage.getPopularFilmsSortedByGenreAndYear(count, genreId, year);
    }

    public List<Film> getPopularFilmsSortedByGenre(Integer count, Integer genreId) {
        return filmStorage.getPopularFilmsSortedByGenre(count, genreId);
    }

    public List<Film> getPopularFilmsSortedByYear(Integer count, Integer year) {
        return filmStorage.getPopularFilmsSortedByYear(count, year);
    }

    public List<Film> getCommonFilms(int userId, int friendId) {
        return filmStorage.getCommonFilms(userId, friendId);
    }

    public List<Film> getFilmRecommendationsForUser(int userId) {
        return filmStorage.getFilmRecommendationsForUser(userId);
    }
}
