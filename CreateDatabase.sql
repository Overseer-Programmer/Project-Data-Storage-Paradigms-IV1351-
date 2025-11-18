CREATE TABLE course_layout (
 id INT GENERATED ALWAYS AS IDENTITY NOT NULL,
 course_code CHAR(6) UNIQUE NOT NULL,
 course_name VARCHAR(250) NOT NULL,
 min_students INT NOT NULL,
 max_students INT NOT NULL,
 hp DECIMAL(10) NOT NULL
);

ALTER TABLE course_layout ADD CONSTRAINT PK_course_layout PRIMARY KEY (id);


CREATE TABLE department (
 id INT GENERATED ALWAYS AS IDENTITY NOT NULL,
 department_name VARCHAR(500) UNIQUE NOT NULL,
 manager_id INT NOT NULL
);

ALTER TABLE department ADD CONSTRAINT PK_department PRIMARY KEY (id);


CREATE TABLE employee (
 id INT GENERATED ALWAYS AS IDENTITY NOT NULL,
 employment_id VARCHAR(300) UNIQUE NOT NULL,
 skill_set VARCHAR(1000) NOT NULL,
 salary INT NOT NULL,
 department_id INT NOT NULL,
 person_id INT NOT NULL,
 job_title_id INT NOT NULL,
 supervisor_id INT NOT NULL
);

ALTER TABLE employee ADD CONSTRAINT PK_employee PRIMARY KEY (id);


CREATE TABLE job_title (
 id INT GENERATED ALWAYS AS IDENTITY NOT NULL,
 job_title VARCHAR(500) UNIQUE NOT NULL
);

ALTER TABLE job_title ADD CONSTRAINT PK_job_title PRIMARY KEY (id);


CREATE TABLE person (
 id INT GENERATED ALWAYS AS IDENTITY NOT NULL,
 personal_number CHAR(12) UNIQUE NOT NULL,
 first_name VARCHAR(500) NOT NULL,
 last_name VARCHAR(500) NOT NULL,
 phone_number VARCHAR(100) NOT NULL,
 street VARCHAR(500) NOT NULL,
 zip VARCHAR(100) NOT NULL,
 city VARCHAR(500) NOT NULL
);

ALTER TABLE person ADD CONSTRAINT PK_person PRIMARY KEY (id);


CREATE TABLE study_period (
 id INT GENERATED ALWAYS AS IDENTITY NOT NULL,
 start_time TIMESTAMP(10) NOT NULL,
 end_time TIMESTAMP(10) NOT NULL
);

ALTER TABLE study_period ADD CONSTRAINT PK_study_period PRIMARY KEY (id);


CREATE TABLE teaching_activity (
 id INT GENERATED ALWAYS AS IDENTITY NOT NULL,
 activity_name VARCHAR(250) UNIQUE NOT NULL,
 factor DECIMAL(10) NOT NULL
);

ALTER TABLE teaching_activity ADD CONSTRAINT PK_teaching_activity PRIMARY KEY (id);


CREATE TABLE course_instance (
 id INT GENERATED ALWAYS AS IDENTITY NOT NULL,
 instance_id VARCHAR(300) UNIQUE NOT NULL,
 num_students INT NOT NULL,
 study_year INT NOT NULL,
 course_layout_id INT
);

ALTER TABLE course_instance ADD CONSTRAINT PK_course_instance PRIMARY KEY (id);


CREATE TABLE course_instance_study_period (
 course_instance_id INT NOT NULL,
 study_period_id INT NOT NULL
);

ALTER TABLE course_instance_study_period ADD CONSTRAINT PK_course_instance_study_period PRIMARY KEY (course_instance_id,study_period_id);


CREATE TABLE planned_activity (
 id INT GENERATED ALWAYS AS IDENTITY NOT NULL,
 planned_hours INT NOT NULL,
 course_instace_id INT NOT NULL,
 teaching_activity_id INT NOT NULL
);

ALTER TABLE planned_activity ADD CONSTRAINT PK_planned_activity PRIMARY KEY (id);


CREATE TABLE employee_planned_activity (
 employee_id INT NOT NULL,
 planned_activity_id INT NOT NULL
);

ALTER TABLE employee_planned_activity ADD CONSTRAINT PK_employee_planned_activity PRIMARY KEY (employee_id,planned_activity_id);


ALTER TABLE department ADD CONSTRAINT FK_department_0 FOREIGN KEY (manager_id) REFERENCES employee (id);


ALTER TABLE employee ADD CONSTRAINT FK_employee_0 FOREIGN KEY (department_id) REFERENCES department (id);
ALTER TABLE employee ADD CONSTRAINT FK_employee_1 FOREIGN KEY (person_id) REFERENCES person (id);
ALTER TABLE employee ADD CONSTRAINT FK_employee_2 FOREIGN KEY (job_title_id) REFERENCES job_title (id);
ALTER TABLE employee ADD CONSTRAINT FK_employee_3 FOREIGN KEY (supervisor_id) REFERENCES employee (id);


ALTER TABLE course_instance ADD CONSTRAINT FK_course_instance_0 FOREIGN KEY (course_layout_id) REFERENCES course_layout (id);


ALTER TABLE course_instance_study_period ADD CONSTRAINT FK_course_instance_study_period_0 FOREIGN KEY (course_instance_id) REFERENCES course_instance (id);
ALTER TABLE course_instance_study_period ADD CONSTRAINT FK_course_instance_study_period_1 FOREIGN KEY (study_period_id) REFERENCES study_period (id);


ALTER TABLE planned_activity ADD CONSTRAINT FK_planned_activity_0 FOREIGN KEY (course_instace_id) REFERENCES course_instance (id);
ALTER TABLE planned_activity ADD CONSTRAINT FK_planned_activity_1 FOREIGN KEY (teaching_activity_id) REFERENCES teaching_activity (id);


ALTER TABLE employee_planned_activity ADD CONSTRAINT FK_employee_planned_activity_0 FOREIGN KEY (employee_id) REFERENCES employee (id);
ALTER TABLE employee_planned_activity ADD CONSTRAINT FK_employee_planned_activity_1 FOREIGN KEY (planned_activity_id) REFERENCES planned_activity (id);


