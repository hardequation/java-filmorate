package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.film.Sorting.SortDirectorFilmsStrategy;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    List<Film> findAllFilms();

    Film add(Film film);

    void removeFilm(Integer filmId);

    Film update(Film film);

    boolean containsFilm(Integer filmId);

    Optional<Film> findFilmById(int id);

    void addLike(int filmId, int userId);

    void removeLike(int filmId, int userId);

    boolean checkLikesUserByFilmId(Integer filmId, Integer userId);

    List<Film> getMostPopularFilms(int size);

    List<Film> getFilmsByDirectorSorted(int directorId, SortDirectorFilmsStrategy sortDirectorFilmsStrategy);

    List<Film> getCommonFilms(int userId, int friendId);

    List<Film> getFilmRecommendationsForUser(int userId);

    void removeAll();

    List<Film> searchFilmsByDirector(String query);

    List<Film> searchFilmsByTitle(String query);

    List<Film> searchFilmsByTitleAndDirector(String query);

    List<Film> getPopularFilmsSortedByGenreAndYear(Integer count, Integer genreId, Integer year);

    List<Film> getPopularFilmsSortedByGenre(Integer count, Integer genreId);

    List<Film> getPopularFilmsSortedByYear(Integer count, Integer year);

}
