CREATE TABLE IF NOT EXISTS genres (
    genre_id SERIAL PRIMARY KEY,
    genre VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS ratings (
    rating_id SERIAL PRIMARY KEY,
    rating_name VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS films (
    film_id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    release_date DATE NOT NULL,
    duration BIGINT NOT NULL,
    mpa_rating_id INTEGER NOT NULL,
    CONSTRAINT fk_mpa_rating_id FOREIGN KEY (mpa_rating_id) REFERENCES ratings (rating_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS users (
    user_id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    login VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    birthday DATE NOT NULL
);

CREATE UNIQUE index IF NOT EXISTS USER_EMAIL_UINDEX ON USERS (email);
CREATE UNIQUE index IF NOT EXISTS USER_LOGIN_UINDEX ON USERS (login);

CREATE TABLE IF NOT EXISTS friendship (
    user_id INTEGER NOT NULL,
    friend_id INTEGER NOT NULL,
    CONSTRAINT fk_user_id FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT fk_friend_id FOREIGN KEY (friend_id) REFERENCES users (user_id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, friend_id)
);

CREATE TABLE IF NOT EXISTS film_likes (
    film_id INTEGER NOT NULL,
    liked_user_id INTEGER NOT NULL,
    CONSTRAINT fk_film_id1 FOREIGN KEY (film_id) REFERENCES films (film_id) ON DELETE CASCADE,
    CONSTRAINT fk_liked_user_id FOREIGN KEY (liked_user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, liked_user_id)
);

CREATE TABLE IF NOT EXISTS films_genres (
    film_id INTEGER NOT NULL,
    genre_id INTEGER NOT NULL,
    CONSTRAINT fk_film_id2 FOREIGN KEY (film_id) REFERENCES films (film_id) ON DELETE CASCADE,
    CONSTRAINT fk_genre_id FOREIGN KEY (genre_id) REFERENCES genres (genre_id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, genre_id)
);