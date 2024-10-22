package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dal.UserStorage;
import ru.yandex.practicum.filmorate.dal.impl.DbUserStorage;
import ru.yandex.practicum.filmorate.dto.CreateUserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.yandex.practicum.filmorate.utils.ErrorMessages.USER_NOT_FOUND;

@JdbcTest
class UserServiceTest {
    private Validator validator;
    private UserService service;
    private UserStorage storage;

    @Autowired
    private JdbcTemplate template;

    @BeforeEach
    void setUp() {
        storage = new DbUserStorage(template);
        service = new UserService(storage);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testCreateUserSuccess() {
        String login = "dolore";
        String name = "Nick Name";
        String email = "mail@mail.ru";
        LocalDate birthday = LocalDate.of(1946, 8, 20);
        User user = User.builder()
                .login(login)
                .name(name)
                .email(email)
                .birthday(birthday)
                .build();
        User createdUser = service.create(user);

        assertNotNull(createdUser.getId(), "User ID should be generated");
        assertEquals(login, createdUser.getLogin());
        assertEquals(name, createdUser.getName());
        assertEquals(email, createdUser.getEmail());
        assertEquals(birthday, createdUser.getBirthday());
    }

    @Test
    void testCreateUserWithInvalidLogin() {
        CreateUserDto user = new CreateUserDto();
        user.setLogin("dolore ullamco");
        user.setEmail("mail@mail.ru");
        user.setBirthday(LocalDate.of(1946, 8, 20));

        Set<ConstraintViolation<CreateUserDto>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());

        ConstraintViolation<CreateUserDto> exception = violations.stream().findFirst().get();
        assertEquals("The field must not contain spaces", exception.getMessage());
    }

    @Test
    void testCreateUserWithoutLogin() {
        CreateUserDto user = new CreateUserDto();
        user.setName("dolore ullamco");
        user.setEmail("mail@mail.ru");
        user.setBirthday(LocalDate.of(1946, 8, 20));

        Set<ConstraintViolation<CreateUserDto>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());

        ConstraintViolation<CreateUserDto> exception = violations.stream().findFirst().get();
        assertEquals("Login can't be empty", exception.getMessage());
    }

    @Test
    void testCreateUserWithWrongEmail() {
        CreateUserDto user = new CreateUserDto();
        user.setLogin("doloreullamco");
        user.setName("");
        user.setEmail("mail.ru");
        user.setBirthday(LocalDate.of(1946, 8, 20));

        Set<ConstraintViolation<CreateUserDto>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());

        ConstraintViolation<CreateUserDto> exception = violations.stream().findFirst().get();
        assertEquals("Email is not valid", exception.getMessage());
    }

    @Test
    void testCreateUserWithFutureBirthday() {
        CreateUserDto user = new CreateUserDto();
        user.setLogin("dolore");
        user.setName("");
        user.setEmail("test@mail.ru");
        user.setBirthday(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<CreateUserDto>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());

        ConstraintViolation<CreateUserDto> exception = violations.stream().findFirst().get();
        assertEquals("Birthday can't be in the future", exception.getMessage());
    }

    @Test
    void testCreateUserWithoutName() {
        User user = User.builder()
                .login("dolore")
                .email("test@mail.ru")
                .birthday(LocalDate.of(1946, 8, 20))
                .build();

        User createdUser = service.create(user);
        assertEquals("dolore", createdUser.getName());
    }

    @Test
    void testUpdateUserSuccess() {
        User user = User.builder()
                .login("testlogin")
                .name("Test Name")
                .email("test@example.com")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        service.create(user);

        User updatedUser = User.builder()
                .id(user.getId())
                .login("newlogin")
                .name("New Name")
                .email("new@example.com")
                .build();
        updatedUser.setBirthday(LocalDate.of(1994, 1, 1));

        User result = service.updateUser(updatedUser);

        assertEquals("newlogin", result.getLogin());
        assertEquals("New Name", result.getName());
        assertEquals("new@example.com", result.getEmail());
        assertEquals(LocalDate.of(1994, 1, 1), result.getBirthday());
    }

    @Test
    void testUpdateUserNotFound() {
        User updatedUser = User.builder()
                .id(999) // Non-existent ID
                .login("newlogin")
                .name("New Name")
                .email("new@example.com")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        NotFoundException exception = assertThrows(NotFoundException.class, () -> service.updateUser(updatedUser));

        assertEquals(USER_NOT_FOUND + 999, exception.getMessage());
    }

    @Test
    void testFindAllUsers() {
        User user1 = User.builder()
                .login("testlogin1")
                .name("Test Name 1")
                .email("test1@example.com")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        service.create(user1);

        User user2 = User.builder()
                .login("testlogin2")
                .name("Test Name 2")
                .email("test2@example.com")
                .birthday(LocalDate.of(1995, 5, 5))
                .build();
        service.create(user2);

        Collection<User> users = service.getUsers();

        assertEquals(2, users.size(), "There should be 2 users in the collection");
        assertTrue(users.contains(user1), "Collection should contain user1");
        assertTrue(users.contains(user2), "Collection should contain user2");
    }
}
