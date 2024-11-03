package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.validators.AfterDate;

import java.time.LocalDate;
import java.util.LinkedHashSet;

@Data
@Builder
public class FilmDto {

    private int id;

    @NotBlank(message = "Film name can't be blank")
    private String name;

    @NotBlank
    @Size(max = 200, message = "Description is too long")
    private String description;

    @NotNull
    private MpaRatingDto mpa;

    @NotNull
    @AfterDate(value = "1895-12-28", message = "Release date should after 1st film birthday")
    private LocalDate releaseDate;

    @Positive(message = "Film duration should be positive")
    private long duration;

    private LinkedHashSet<GenreDto> genres;

    private LinkedHashSet<DirectorDto> directors;
}
