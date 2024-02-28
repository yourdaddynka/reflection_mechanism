DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
DROP TABLE if exists simple_user;
CREATE TABLE simple_user(
	id	BIGSERIAL PRIMARY KEY,
	firstName	VARCHAR NOT NULL,
	lastName	VARCHAR NOT NULL,
	age	INT NOT NULL
);
INSERT INTO simple_user VALUES (1,
                                'Azat',
                                'Malikov',
                                170);
UPDATE simple_user SET firstName = 'Azatos',
                       lastName = 'Malikov',
                       age = 170
WHERE id = 1