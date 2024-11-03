package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface DirectorStorage {
    List<Director> findAllDirectors();

    Optional<Director> findDirectorById(int id);

    boolean containsDirector(Integer directorId);

    Director createDirector(Director director);

    Director updateDirector(Director director);

    void deleteDirector(int directorId);

    void addDirectorOfFilm(Film film);

    List<Director> findDirectorForFilm(int filmId);
}