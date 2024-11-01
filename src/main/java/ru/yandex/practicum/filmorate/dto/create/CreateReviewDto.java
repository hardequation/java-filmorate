package ru.yandex.practicum.filmorate.dto.create;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class CreateReviewDto {

    @NotNull
    Integer filmId;

    @NotNull
    Integer userId;

    @NotNull
    boolean isPositive;

    @NotNull
    boolean useful;

    String content;

}
