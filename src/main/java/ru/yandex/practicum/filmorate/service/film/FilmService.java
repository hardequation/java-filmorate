package ru.yandex.practicum.filmorate.service.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.dal.DirectorStorage;
import ru.yandex.practicum.filmorate.dal.FilmStorage;
import ru.yandex.practicum.filmorate.dal.GenreStorage;
import ru.yandex.practicum.filmorate.dal.RatingStorage;
import ru.yandex.practicum.filmorate.dal.UserStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.service.film.Searching.*;
import ru.yandex.practicum.filmorate.service.film.Sorting.SortDirectorFilmsByDate;
import ru.yandex.practicum.filmorate.service.film.Sorting.SortDirectorFilmsByLikes;
import ru.yandex.practicum.filmorate.service.film.Sorting.SortDirectorFilmsStrategy;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import static ru.yandex.practicum.filmorate.model.enums.EventType.LIKE;
import static ru.yandex.practicum.filmorate.model.enums.Operation.ADD;
import static ru.yandex.practicum.filmorate.model.enums.Operation.REMOVE;
import static ru.yandex.practicum.filmorate.utils.ErrorMessages.DIRECTOR_NOT_FOUND;
import static ru.yandex.practicum.filmorate.utils.ErrorMessages.FILM_NOT_FOUND;
import static ru.yandex.practicum.filmorate.utils.ErrorMessages.GENRE_NOT_FOUND;
import static ru.yandex.practicum.filmorate.utils.ErrorMessages.RATING_NOT_FOUND;
import static ru.yandex.practicum.filmorate.utils.ErrorMessages.USER_NOT_FOUND;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final RatingStorage ratingStorage;
    private final GenreStorage genreStorage;
    private final DirectorStorage directorStorage;
    private static final Map<String, SortDirectorFilmsStrategy> SORT_DIRECTOR_FILMS_STRATEGIES = Map.of(
            "year", new SortDirectorFilmsByDate(),
            "likes", new SortDirectorFilmsByLikes()
    );
    private static final Map<Set<String>, SearchStrategy> SEARCH__FILMS_STRATEGIES = Map.of(
            Set.of("director"), new SearchByDirector(),
            Set.of("title"), new SearchByTitle(),
            Set.of("director", "title"), new SearchByDirectorAndTitle()
    );

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
        }
        userStorage.addFeed(filmId, userId, LIKE, ADD);
    }

    public void removeLike(Integer filmId, Integer userId) {
        if (!userStorage.contains(userId)) {
            throw new NotFoundException(USER_NOT_FOUND + userId);
        }

        if (!filmStorage.containsFilm(filmId)) {
            throw new NotFoundException(FILM_NOT_FOUND + filmId);
        }

        filmStorage.removeLike(filmId, userId);
        userStorage.addFeed(filmId, userId, LIKE, REMOVE);
    }

    public List<Film> getMostPopularFilms(int size) {
        return filmStorage.getMostPopularFilms(size);
    }

    public List<Film> searchFilms(String query, Set<String> by) {
        List<Film> foundedFilms;
        if (by == null || by.isEmpty()) {
            throw new ValidationException("Передано некорректное число параметров");
        } else {
            if (SEARCH__FILMS_STRATEGIES.containsKey(by)) {
                foundedFilms = filmStorage.searchFilmsBy(query, SEARCH__FILMS_STRATEGIES.get(by));
            } else {
                throw new NotFoundException("Неверно указан параметр поиска");
            }
        }
        for (Film film : foundedFilms) {
            film.setGenres(findGenresForFilm(film.getId()));
            film.setDirectors(findDirectorsForFilm(film.getId()));
        }
        return foundedFilms;
    }

    public ResponseEntity<Object> getFilmsByDirectorSorted(int directorId, String sortParam, FilmMapper filmMapper) {
        try {
            List<Film> films;
            if (SORT_DIRECTOR_FILMS_STRATEGIES.containsKey(sortParam)) {
                films = filmStorage.getFilmsByDirectorSorted(directorId, SORT_DIRECTOR_FILMS_STRATEGIES.get(sortParam.toLowerCase()));
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Invalid sortBy parameter: '" + sortParam +
                        "'. Allowed values - year, likes");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            for (Film film : films) {
                film.setGenres(findGenresForFilm(film.getId()));
                film.setDirectors(findDirectorsForFilm(film.getId()));
            }
            if (films.isEmpty()) {
                return ResponseEntity.notFound().build();
            } else {
                return ResponseEntity.ok(films.stream()
                        .map(filmMapper::map)
                        .toList());
            }
        } catch (IllegalArgumentException e) {
            log.error(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.internalServerError().body("Internal Server Error");
        }
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
