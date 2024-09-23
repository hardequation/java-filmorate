package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {

    private Long id;

    private String name;

    @JsonIgnore
    private Set<Long> friends = new HashSet<>();

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
