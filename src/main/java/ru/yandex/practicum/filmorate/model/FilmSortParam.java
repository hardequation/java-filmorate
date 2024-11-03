package ru.yandex.practicum.filmorate.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FilmSortParam {
    FILMS_BY_RELEASE_DATE("f.release_date"),
    POPULAR_FILMS_BY_LIKES("COUNT(f.film_id) DESC");

    private final String sortParam;

}
