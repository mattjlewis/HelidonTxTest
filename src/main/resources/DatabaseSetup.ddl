CREATE TABLE DEPARTMENT (
	ID NUMBER(10) NOT NULL,
	NAME VARCHAR2(20) NOT NULL,
	LOCATION VARCHAR2(20) NULL,
	VERSION NUMBER(10) NULL,
	PRIMARY KEY (ID));
CREATE SEQUENCE DepartmentSeq INCREMENT BY 50 START WITH 50;

CREATE TABLE EMPLOYEE (
	ID NUMBER(10) NOT NULL,
	EMAILADDRESS VARCHAR2(20) NOT NULL,
	FAVOURITEDRINK VARCHAR2(20) NULL,
	NAME VARCHAR2(20) NOT NULL,
	DEPARTMENT_ID NUMBER(10) NOT NULL,
	PRIMARY KEY (ID));
ALTER TABLE EMPLOYEE ADD CONSTRAINT FK_EMPLOYEE_DEPARTMENT_ID FOREIGN KEY (DEPARTMENT_ID) REFERENCES DEPARTMENT (ID);
CREATE SEQUENCE EmployeeSeq INCREMENT BY 50 START WITH 50;
