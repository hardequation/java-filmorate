MERGE INTO ratings (rating_id, rating_name) VALUES (1, 'G');
MERGE INTO ratings (rating_id, rating_name) VALUES (2, 'PG');
MERGE INTO ratings (rating_id, rating_name) VALUES (3, 'PG-13');
MERGE INTO ratings (rating_id, rating_name) VALUES (4, 'R');
MERGE INTO ratings (rating_id, rating_name) VALUES (5, 'NC-17');

MERGE INTO genres (genre_id, genre) VALUES (1, 'Комедия');
MERGE INTO genres (genre_id, genre) VALUES (2, 'Драма');
MERGE INTO genres (genre_id, genre) VALUES (3, 'Мультфильм');
MERGE INTO genres (genre_id, genre) VALUES (4, 'Триллер');
MERGE INTO genres (genre_id, genre) VALUES (5, 'Документальный');
MERGE INTO genres (genre_id, genre) VALUES (6, 'Боевик');