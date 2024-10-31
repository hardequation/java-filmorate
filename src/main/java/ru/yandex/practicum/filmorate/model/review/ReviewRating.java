package ru.yandex.practicum.filmorate.model.review;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewRating {

    @NotNull
    Integer value;
}
