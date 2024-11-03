package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.LinkedHashSet;

/**
 * Film.
 */

@Data
@Builder
public class Film {

    private int id;

    private String name;

    private String description;

    private MpaRating mpa;

    private LocalDate releaseDate;

    private long duration;

    private LinkedHashSet<Genre> genres;

    private LinkedHashSet<Director> directors;

}
