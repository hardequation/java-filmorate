package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.validators.AfterDate;

import java.time.LocalDate;

/**
 * Film.
 */

@Data
public class Film {

    private static final String FIRST_FILM_BIRTHDAY = LocalDate.of(1895, 12, 28).toString();

    private Long id;

    @NotNull
    @NotBlank(message = "Film name can't be blank")
    private String name;

    @Size(max = 200, message = "Description is too long")
    private String description;

    @NotNull
    @AfterDate(value = "1895-12-28", message = "Release date should after 1st film birthday")
    private LocalDate releaseDate;

    @Positive(message = "Film duration should be positive")
    private Long duration;
}
