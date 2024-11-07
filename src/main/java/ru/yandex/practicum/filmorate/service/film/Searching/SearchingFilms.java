package ru.yandex.practicum.filmorate.service.film.Searching;

import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Setter
@NoArgsConstructor
@Component
public class SearchingFilms {
    private SearchStrategy searchStrategy;

    public String searchFilms(String query) {
        return searchStrategy.doSearch(query);
    }
}
