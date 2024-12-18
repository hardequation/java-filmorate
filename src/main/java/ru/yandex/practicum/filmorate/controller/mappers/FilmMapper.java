package ru.yandex.practicum.filmorate.controller.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.CreateFilmDto;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Film;

@Component
@RequiredArgsConstructor
public class FilmMapper {

    private final GenreMapper genreMapper;

    private final MpaRatingMapper ratingMapper;

    public Film map(CreateFilmDto dto) {
        return Film.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .mpa(ratingMapper.map(dto.getMpa()))
                .releaseDate(dto.getReleaseDate())
                .duration(dto.getDuration())
                .genres(genreMapper.mapToGenreList(dto.getGenres()))
                .build();
    }

    public Film map(FilmDto dto) {
        return Film.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .mpa(ratingMapper.map(dto.getMpa()))
                .releaseDate(dto.getReleaseDate())
                .duration(dto.getDuration())
                .genres(genreMapper.mapToGenreList(dto.getGenres()))
                .build();
    }

    public FilmDto map(Film film) {
        return FilmDto.builder()
                .id(film.getId())
                .name(film.getName())
                .description(film.getDescription())
                .mpa(ratingMapper.map(film.getMpa()))
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .genres(genreMapper.mapToGenreDtoList(film.getGenres()))
                .build();
    }
}
