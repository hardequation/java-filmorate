package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Film.
 */

@Data
@Builder
public class Film {

    private int id;

    private Set<Integer> likedUsersID;

    private String name;

    private String description;

    private Integer mpaId;

    private LocalDate releaseDate;

    private long duration;

    private List<Genre> genres;

}
