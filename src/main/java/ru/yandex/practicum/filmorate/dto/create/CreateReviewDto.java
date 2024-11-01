package ru.yandex.practicum.filmorate.dto.create;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
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
