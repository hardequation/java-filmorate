package ru.yandex.practicum.filmorate.dal.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.UserStorage;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static ru.yandex.practicum.filmorate.utils.ErrorMessages.USER_NOT_FOUND;

@Repository
@RequiredArgsConstructor
public class DbUserStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    private final UserRowMapper userRowMapper;

    @Override
    public List<User> findAll() {
        return jdbcTemplate.query("SELECT * from USERS", userRowMapper);
    }

    @Override
    public User add(User user) {
        String sql = "INSERT INTO users (name, login, email, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getName());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getEmail());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        Integer generatedId = Objects.requireNonNull(keyHolder.getKey()).intValue();
        user.setId(generatedId);

        return user;
    }

    @Override
    public void removeUser(Integer id) {
        String deleteUserSql = "DELETE FROM users WHERE user_id = ?";
        jdbcTemplate.update(deleteUserSql, id);
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET " +
                "name = ?, login = ?, email = ?, birthday = ? " +
                "WHERE user_id = ?";

        int rowsAffected = jdbcTemplate.update(sql,
                user.getName(),
                user.getLogin(),
                user.getEmail(),
                Date.valueOf(user.getBirthday()),
                user.getId());

        if (rowsAffected == 0) {
            throw new NotFoundException(USER_NOT_FOUND + user.getId());
        }
        return user;
    }

    @Override
    public boolean contains(Integer id) {
        String sql = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    public Optional<User> findById(int id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, id);
            return Optional.of(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<User> getFriendsbyUserId(int userId) {
        if (!this.contains(userId)) {
            throw new NotFoundException("Can't find friends of non-existing user");
        }

        String friends = "SELECT * FROM users " +
                "WHERE user_id IN (SELECT friend_id from FRIENDSHIP where user_id = ?);";

        return jdbcTemplate.query(friends, userRowMapper, userId);
    }

    @Override
    public List<User> getCommonFriends(int userId, int friendId) {
        String sql = "SELECT u.* " +
                "FROM users AS u " +
                "JOIN friendship AS fs1 ON u.user_id = fs1.friend_id " +
                "JOIN friendship AS fs2 ON u.user_id = fs2.friend_id " +
                "WHERE fs1.user_id = ? AND fs2.user_id = ?;";

        return jdbcTemplate.query(sql, userRowMapper, userId, friendId);
    }

    @Override
    public void addFriendship(Integer userId, Integer friendId) {
        String sql = "INSERT INTO friendship (user_id, friend_id) VALUES (?, ?)";

        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public void removeFriendship(Integer userId, Integer friendId) {
        String sql = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";

        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public void removeAll() {
        String sql = "DELETE FROM users";
        jdbcTemplate.update(sql);
    }
}
