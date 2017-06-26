CREATE TABLE DNA_COMM (
	ID INT,
	NUMBER VARCHAR(16),
	LINK VARCHAR(256),
	RELEASE_DATE DATE,
	VALUE_EUR MONEY,
	VALUE_RON MONEY,
	CRIME_DATE DATE,
	TITLE VARCHAR(256),
	CONTENT NTEXT,
	TYPE VARCHAR(64)
)

CREATE TABLE DNA_RULING (
	ID INT,
	NUMBER VARCHAR(16),
	LINK VARCHAR(256),
	RELEASE_DATE DATE,
	RULING_DATE DATE,

)