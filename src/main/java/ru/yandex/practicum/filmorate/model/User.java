package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class User {

    private Integer id;

    private String name;

    private String login;

    private String email;

    private LocalDate birthday;

}
