package ru.yandex.practicum.filmorate.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MpaRatingDto {
    private int id;

    private String name;
}