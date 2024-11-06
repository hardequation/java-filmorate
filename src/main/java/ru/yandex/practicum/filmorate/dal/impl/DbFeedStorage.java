package ru.yandex.practicum.filmorate.dal.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.FeedStorage;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DbFeedStorage implements FeedStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Feed> getFeedByUserId(Integer userId) {
        String sqlQuery = "SELECT * FROM feed WHERE user_id = ? ORDER BY time_stamp ASC";

        return jdbcTemplate.query(sqlQuery, this::mapRowToFeed, userId);
    }

    @Override
    public void addFeed(Integer entityId, Integer userId, EventType eventType, Operation operation) {
        String sqlQuery = "INSERT INTO feed (entity_id, user_id, time_stamp, event_type, operation) " +
                "VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.update(sqlQuery, entityId, userId, Timestamp.valueOf(LocalDateTime.now()), eventType.toString(),
                operation.toString());
    }

    private Feed mapRowToFeed(ResultSet resultSet, int rowNum) throws SQLException {
        return Feed.builder()
                .eventId(resultSet.getInt("event_id"))
                .entityId(resultSet.getInt("entity_id"))
                .userId(resultSet.getInt("user_id"))
                .timestamp(resultSet.getTimestamp("time_stamp").getTime())
                .eventType(EventType.valueOf(resultSet.getString("event_type")))
                .operation(Operation.valueOf(resultSet.getString("operation")))
                .build();
    }
}
