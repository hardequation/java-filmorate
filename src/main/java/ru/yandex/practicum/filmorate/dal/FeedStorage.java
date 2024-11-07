package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.enums.EventType;
import ru.yandex.practicum.filmorate.model.enums.Operation;

import java.util.List;

public interface FeedStorage {

    List<Feed> getFeedByUserId(Integer userId);

    void addFeed(Integer entityId, Integer userId, EventType eventType, Operation operation);

}
