package ru.yandex.practicum.filmorate.dal.impl.Searching;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface SearchStrategy {

    List<Film> doSearch(String query);
}
