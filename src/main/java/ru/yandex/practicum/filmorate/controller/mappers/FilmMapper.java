package ru.yandex.practicum.filmorate.controller.mappers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.CreateFilmDto;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.ArrayList;

@Service
public class FilmMapper {

    @Autowired
    FilmService filmService;


    public Film map(CreateFilmDto dto) {
        return Film.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .mpaId(dto.getMpa().getId())
                .releaseDate(dto.getReleaseDate())
                .duration(dto.getDuration())
                .genres(dto.getGenres() == null ? new ArrayList<>() : dto.getGenres())
                .build();
    }

    public Film map(FilmDto dto) {
        return Film.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .mpaId(dto.getMpa().getId())
                .releaseDate(dto.getReleaseDate())
                .duration(dto.getDuration())
                .genres(dto.getGenres() == null ? new ArrayList<>() : dto.getGenres())
                .build();
    }

    public FilmDto map(Film dto) {
        return FilmDto.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .mpa(filmService.findMpaRatingById(dto.getMpaId()))
                .releaseDate(dto.getReleaseDate())
                .duration(dto.getDuration())
                .genres(dto.getGenres())
                .build();
    }
}
