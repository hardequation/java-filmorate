package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.dal.UserStorage;
import ru.yandex.practicum.filmorate.dal.impl.InMemoryUserStorage;
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

class UserServiceTest {
    private Validator validator;
    private UserService service;
    private UserStorage storage;

    @BeforeEach
    void setUp() {
        storage = new InMemoryUserStorage();
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
        User user = new User();
        user.setLogin(login);
        user.setName(name);
        user.setEmail(email);
        user.setBirthday(birthday);
        User createdUser = service.create(user);

        assertNotNull(createdUser.getId(), "User ID should be generated");
        assertEquals(login, createdUser.getLogin());
        assertEquals(name, createdUser.getName());
        assertEquals(email, createdUser.getEmail());
        assertEquals(birthday, createdUser.getBirthday());
    }

    @Test
    void testCreateUserWithInvalidLogin() {
        User user = new User();
        user.setLogin("dolore ullamco"); // login with space
        user.setEmail("mail@mail.ru");
        user.setBirthday(LocalDate.of(1946, 8, 20));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());

        ConstraintViolation<User> exception = violations.stream().findFirst().get();
        assertEquals("The field must not contain spaces", exception.getMessage());
    }

    @Test
    void testCreateUserWithoutLogin() {
        User user = new User();
        user.setName("dolore ullamco");
        user.setEmail("mail@mail.ru");
        user.setBirthday(LocalDate.of(1946, 8, 20));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());

        ConstraintViolation<User> exception = violations.stream().findFirst().get();
        assertEquals("Login can't be empty", exception.getMessage());
    }

    @Test
    void testCreateUserWithWrongEmail() {
        User user = new User();
        user.setLogin("doloreullamco");
        user.setName("");
        user.setEmail("mail.ru");
        user.setBirthday(LocalDate.of(1946, 8, 20));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());

        ConstraintViolation<User> exception = violations.stream().findFirst().get();
        assertEquals("Email is not valid", exception.getMessage());
    }

    @Test
    void testCreateUserWithFutureBirthday() {
        User user = new User();
        user.setLogin("dolore");
        user.setName("");
        user.setEmail("test@mail.ru");
        user.setBirthday(LocalDate.now().plusDays(1)); // future date

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());

        ConstraintViolation<User> exception = violations.stream().findFirst().get();
        assertEquals("Birthday can't be in the future", exception.getMessage());
    }

    @Test
    void testCreateUserWithoutName() {
        User user = new User();
        user.setLogin("dolore");
        user.setEmail("test@mail.ru");
        user.setBirthday(LocalDate.of(1946, 8, 20));

        User createdUser = service.create(user);
        assertEquals("dolore", createdUser.getName());
    }

    @Test
    void testUpdateUserSuccess() {
        User user = new User();
        user.setLogin("testlogin");
        user.setName("Test Name");
        user.setEmail("test@example.com");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        service.create(user);

        User updatedUser = new User();
        updatedUser.setId(user.getId());
        updatedUser.setLogin("newlogin");
        updatedUser.setName("New Name");
        updatedUser.setEmail("new@example.com");
        updatedUser.setBirthday(LocalDate.of(1994, 1, 1));

        User result = service.updateUser(updatedUser);

        assertEquals("newlogin", result.getLogin());
        assertEquals("New Name", result.getName());
        assertEquals("new@example.com", result.getEmail());
        assertEquals(LocalDate.of(1994, 1, 1), result.getBirthday());
    }

    @Test
    void testUpdateUserNotFound() {
        User updatedUser = new User();
        updatedUser.setId(999); // Non-existent ID
        updatedUser.setLogin("newlogin");
        updatedUser.setName("New Name");
        updatedUser.setEmail("new@example.com");
        updatedUser.setBirthday(LocalDate.of(1990, 1, 1));

        NotFoundException exception = assertThrows(NotFoundException.class, () -> service.updateUser(updatedUser));

        assertEquals(USER_NOT_FOUND + 999, exception.getMessage());
    }

    @Test
    void testFindAllUsers() {
        User user1 = new User();
        user1.setLogin("testlogin1");
        user1.setName("Test Name 1");
        user1.setEmail("test1@example.com");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        service.create(user1);

        User user2 = new User();
        user2.setLogin("testlogin2");
        user2.setName("Test Name 2");
        user2.setEmail("test2@example.com");
        user2.setBirthday(LocalDate.of(1995, 5, 5));
        service.create(user2);

        Collection<User> users = service.getUsers();

        assertEquals(2, users.size(), "There should be 2 users in the collection");
        assertTrue(users.contains(user1), "Collection should contain user1");
        assertTrue(users.contains(user2), "Collection should contain user2");
    }
}
