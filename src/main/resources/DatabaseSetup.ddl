CREATE TABLE DEPARTMENT (
	ID INTEGER NOT NULL,
	CREATED TIMESTAMP NOT NULL,
	LAST_UPDATED TIMESTAMP NOT NULL,
	LAST_UPDATED_BY VARCHAR,
	LOCATION VARCHAR(20),
	NAME VARCHAR(20) NOT NULL UNIQUE,
	VERSION INTEGER,
	PRIMARY KEY (ID));
CREATE SEQUENCE DEPARTMENT_SEQ INCREMENT BY 50 START WITH 50;

CREATE TABLE EMPLOYEE (
	ID INTEGER NOT NULL,
	EMAIL_ADDRESS VARCHAR(255) NOT NULL,
	FAVOURITE_DRINK VARCHAR(20),
	NAME VARCHAR(20) NOT NULL,
	DEPARTMENT_ID INTEGER NOT NULL,
	PRIMARY KEY (ID));
ALTER TABLE EMPLOYEE ADD CONSTRAINT FK_EMPLOYEE_DEPARTMENT_ID FOREIGN KEY (DEPARTMENT_ID) REFERENCES DEPARTMENT (ID);
CREATE SEQUENCE EMPLOYEE_SEQ INCREMENT BY 50 START WITH 50;
