package ru.yandex.practicum.filmorate.dal.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.UserStorage;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Repository
@Qualifier("dbUserStorage")
public class DbUserStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DbUserStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Collection<User> findAll() {
        return jdbcTemplate.query("SELECT * from USERS", new UserRowMapper());
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
    public User update(User user) {
        String sql = "UPDATE users SET " +
                "name = ?, login = ?, email = ?, birthday = ? " +
                "WHERE user_id = ?";

        jdbcTemplate.update(sql,
                user.getName(),
                user.getLogin(),
                user.getEmail(),
                Date.valueOf(user.getBirthday()),
                user.getId());

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
        String findUser = "SELECT * FROM users WHERE user_id = ?";
        String getFriendshipStatuses = "SELECT friend_id, confirmed FROM friendship_statuses WHERE user_id = ?";
        Map<Integer, Boolean> statuses = new HashMap<>();
        jdbcTemplate.query(getFriendshipStatuses, new Object[]{id}, (rs, rowNum) -> {
            statuses.put(rs.getInt("friend_id"), rs.getBoolean("confirmed"));
            return null;
        });

        try {
            User user = jdbcTemplate.queryForObject(findUser, new UserRowMapper(), id);
            if (user != null) user.setFriendshipStatuses(statuses);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void addFriendship(Integer userId, Integer friendId, boolean confirmed) {
        String sql = "INSERT INTO friendship_statuses (user_id, friend_id, confirmed) VALUES (?, ?, ?)";

        jdbcTemplate.update(sql, userId, friendId, confirmed);
    }

    @Override
    public void removeFriendship(Integer userId, Integer friendId) {
        String sql = "DELETE FROM friendship_statuses WHERE user_id = ? AND friend_id = ?";

        jdbcTemplate.update(sql, userId, friendId);
        jdbcTemplate.update(sql, friendId, userId);
    }

    @Override
    public void removeAll() {
        String sql = "DELETE FROM users";
        jdbcTemplate.update(sql);
    }
}