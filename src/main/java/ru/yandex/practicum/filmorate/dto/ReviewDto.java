package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewDto {

    @NotNull
    Integer reviewId;

    @NotNull
    Integer filmId;

    @NotNull
    Integer userId;

    @NotNull
    @JsonProperty("isPositive")
    boolean isPositive;

    int useful;

    String content;
}
