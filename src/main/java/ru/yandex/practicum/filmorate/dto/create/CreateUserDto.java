package ru.yandex.practicum.filmorate.dto.create;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@Data
@RequiredArgsConstructor
public class CreateUserDto {

    private String name;

    @NotBlank(message = "Login can't be empty")
    @Pattern(regexp = "^[\\S]+$", message = "The field must not contain spaces")
    private String login;

    @NotBlank
    @Email(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Email is not valid")
    private String email;

    @NotNull
    @PastOrPresent(message = "Birthday can't be in the future")
    private LocalDate birthday;
}