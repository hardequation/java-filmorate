package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.validators.AfterDate;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Film.
 */

@Data
public class Film {

    private static final String FIRST_FILM_BIRTHDAY = LocalDate.of(1895, 12, 28).toString();

    private Long id;

    @JsonIgnore
    private Set<Long> likedUsersID = new HashSet<>();

    @NotBlank(message = "Film name can't be blank")
    private String name;

    @NotBlank
    @Size(max = 200, message = "Description is too long")
    private String description;

    @NotNull
    @AfterDate(value = "1895-12-28", message = "Release date should after 1st film birthday")
    private LocalDate releaseDate;

    @Positive(message = "Film duration should be positive")
    private long duration;

    private List<String> genres;

    @Size(max = 5)
    private String mpaRating;
}
