package ru.yandex.practicum.filmorate.dto.create;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateDirectorDto {

    private Integer id;

    @NotBlank
    @Size(max = 100, message = "Name of director is too long")
    private String name;
}
