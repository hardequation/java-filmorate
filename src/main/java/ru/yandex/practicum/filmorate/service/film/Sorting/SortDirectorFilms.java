package ru.yandex.practicum.filmorate.service.film.Sorting;

import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Setter
@NoArgsConstructor
@Component
public class SortDirectorFilms {
    private SortDirectorFilmsStrategy searchStrategy;

    public String searchFilms(Integer directorId) {
        return searchStrategy.getSortSQL(directorId);
    }
}
