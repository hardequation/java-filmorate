package ru.yandex.practicum.filmorate.controller.mappers;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.CreateUserDto;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.model.User;

@Service
public class UserMapper {

    public final User map(CreateUserDto dto) {
        return User.builder()
                .name(dto.getName())
                .login(dto.getLogin())
                .email(dto.getEmail())
                .birthday(dto.getBirthday())
                .build();
    }

    public final UserDto map(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .login(user.getLogin())
                .email(user.getEmail())
                .birthday(user.getBirthday())
                .build();
    }

    public final User map(UserDto user) {
        return User.builder()
                .id(user.getId())
                .name(user.getName())
                .login(user.getLogin())
                .email(user.getEmail())
                .birthday(user.getBirthday())
                .build();
    }
}
