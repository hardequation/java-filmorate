package ru.yandex.practicum.filmorate.dto.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    Boolean isPositive;

    @NotBlank
    @Size(max = 400, message = "Content is too long")
    String content;

}
