package ru.yandex.practicum.filmorate.service.film.Sorting;

public interface SortDirectorFilmsStrategy {
    String getSortSQL(int directorId);
}
