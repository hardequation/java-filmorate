package ru.yandex.practicum.filmorate.controller.mappers;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class GenreMapper {

    public GenreDto map(Genre genre) {
        return GenreDto.builder()
                .id(genre.getId())
                .name(genre.getName())
                .build();
    }

    public Genre map(GenreDto genre) {
        return Genre.builder()
                .id(genre.getId())
                .name(genre.getName())
                .build();
    }

    public LinkedHashSet<Genre> mapToGenreList(Set<GenreDto> genreDtos) {
        if (genreDtos == null) {
            return new LinkedHashSet<>();
        }
        return genreDtos.stream()
                .map(this::map)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public LinkedHashSet<GenreDto> mapToGenreDtoList(Set<Genre> genres) {
        if (genres == null) {
            return new LinkedHashSet<>();
        }
        return genres.stream()
                .map(this::map)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

}
