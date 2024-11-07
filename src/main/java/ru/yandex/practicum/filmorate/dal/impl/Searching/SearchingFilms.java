package ru.yandex.practicum.filmorate.dal.impl.Searching;

import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

@Setter
@NoArgsConstructor
@Component
public class SearchingFilms {
    private SearchStrategy searchStrategy;

    public List<Film> searchFilms(String query) {
        return searchStrategy.doSearch(query);
    }
}
