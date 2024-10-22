package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
public class UserDto {

    private Integer id;

    private String name;

    //    @NotBlank(message = "Login can't be empty")
    @Pattern(regexp = "^[\\S]+$", message = "The field must not contain spaces")
    private String login;

    //    @NotBlank
    @Email(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Email is not valid")
    private String email;

    //    @NotNull
    @PastOrPresent(message = "Birthday can't be in the future")
    private LocalDate birthday;

    private Map<Integer, Boolean> friendshipStatuses;
}
