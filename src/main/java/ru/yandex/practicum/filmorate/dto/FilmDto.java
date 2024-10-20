package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.validators.AfterDate;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class FilmDto {

    private int id;

    @NotBlank(message = "Film name can't be blank")
    private String name;

    @NotBlank
    @Size(max = 200, message = "Description is too long")
    private String description;

    private MpaRating mpa;

    @NotNull
    @AfterDate(value = "1895-12-28", message = "Release date should after 1st film birthday")
    private LocalDate releaseDate;

    @Positive(message = "Film duration should be positive")
    private long duration;

    private List<Genre> genres;
}
