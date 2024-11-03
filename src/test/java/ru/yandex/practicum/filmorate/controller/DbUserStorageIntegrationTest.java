package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dal.impl.DbUserStorage;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@Import({DbUserStorage.class, UserRowMapper.class})
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class DbUserStorageIntegrationTest {

    @Autowired
    private final JdbcTemplate template;

    private DbUserStorage storage;

    private User user;
    private User user2;

    @BeforeEach
    void setUp() {
        UserRowMapper userRowMapper = new UserRowMapper();
        storage = new DbUserStorage(template, userRowMapper);
        user = User.builder()
                .name("Name")
                .login("Login")
                .email("a@abc.com")
                .birthday(LocalDate.of(1980, 10, 1))
                .build();

        user2 = User.builder()
                .name("Name2")
                .login("Login2")
                .email("a2@abc.com")
                .birthday(LocalDate.of(1984, 11, 11))
                .build();
    }

    @AfterEach
    void finish() {
        storage.removeAll();
    }

    @Test
    void testAddUser() {
        assertTrue(storage.findAll().isEmpty());

        User addedUser = storage.add(user);

        assertEquals(1, storage.findAll().size());
        assertEquals(user.getName(), addedUser.getName());
    }

    @Test
    void testRemoveUser() {
        User addedUser = storage.add(user);
        int id = addedUser.getId();
        assertFalse(storage.findById(id).isEmpty());

        storage.removeUser(id);

        assertEquals(Optional.empty(), storage.findById(id));
    }

    @Test
    void testUpdateUser() {
        storage.add(user);

        String newName = "NewName";
        user.setName(newName);
        storage.update(user);

        assertEquals(newName, user.getName());
    }

    @Test
    void testContains() {
        User addedUser = storage.add(user);

        assertTrue(storage.contains(addedUser.getId()));
        assertFalse(storage.contains(addedUser.getId() + 1));
    }

    @Test
    void testFindUserById() {
        User addedUser = storage.add(user);

        Optional<User> userOptional = storage.findById(addedUser.getId());

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(u ->
                        assertThat(u).hasFieldOrPropertyWithValue("id", addedUser.getId())
                );
    }

    @Test
    void testRemoveAll() {
        storage.add(user);
        assertEquals(1, storage.findAll().size());

        storage.removeAll();

        assertTrue(storage.findAll().isEmpty());
    }

    @Test
    void shouldGetFeedByUserId() {
        storage.add(user);
        storage.add(user2);
        storage.addFriendship(user.getId(), user2.getId());
        storage.addFeed(1, 2, EventType.REVIEW, Operation.ADD);

        assertNotNull(storage.getFeedByUserId(user2.getId()));
    }
}
