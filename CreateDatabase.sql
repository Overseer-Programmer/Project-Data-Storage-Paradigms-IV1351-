CREATE TABLE course_layout (
 id INT GENERATED ALWAYS AS IDENTITY NOT NULL,
 course_code CHAR(6) UNIQUE NOT NULL,
 course_name VARCHAR(250) NOT NULL,
 min_students INT NOT NULL,
 max_students INT NOT NULL,
 hp DECIMAL(10, 1) NOT NULL
);

ALTER TABLE course_layout ADD CONSTRAINT PK_course_layout PRIMARY KEY (id);


CREATE TABLE department (
 id INT GENERATED ALWAYS AS IDENTITY NOT NULL,
 department_name VARCHAR(500) UNIQUE NOT NULL,
 manager_id INT
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
 supervisor_id INT
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


CREATE TABLE teaching_activity (
 id INT GENERATED ALWAYS AS IDENTITY NOT NULL,
 activity_name VARCHAR(250) UNIQUE NOT NULL,
 factor DECIMAL(10, 1) NOT NULL
);

ALTER TABLE teaching_activity ADD CONSTRAINT PK_teaching_activity PRIMARY KEY (id);

CREATE TYPE academic_period AS ENUM ('P1', 'P2', 'P3', 'P4');   

CREATE TABLE course_instance (
 id INT GENERATED ALWAYS AS IDENTITY NOT NULL,
 instance_id VARCHAR(300) UNIQUE NOT NULL,
 num_students INT NOT NULL,
 study_year INT NOT NULL,
 study_period academic_period NOT NULL,
 course_layout_id INT NOT NULL
);

ALTER TABLE course_instance ADD CONSTRAINT PK_course_instance PRIMARY KEY (id);


CREATE TABLE planned_activity (
 id INT GENERATED ALWAYS AS IDENTITY NOT NULL,
 planned_hours INT NOT NULL,
 course_instance_id INT NOT NULL,
 teaching_activity_id INT NOT NULL
);

ALTER TABLE planned_activity ADD CONSTRAINT PK_planned_activity PRIMARY KEY (id);


CREATE TABLE employee_planned_activity (
 employee_id INT NOT NULL,
 planned_activity_id INT NOT NULL
);

ALTER TABLE employee_planned_activity ADD CONSTRAINT PK_employee_planned_activity PRIMARY KEY (employee_id,planned_activity_id);


ALTER TABLE department ADD CONSTRAINT FK_department_0 FOREIGN KEY (manager_id) REFERENCES employee (id) ON DELETE SET NULL;


ALTER TABLE employee ADD CONSTRAINT FK_employee_0 FOREIGN KEY (department_id) REFERENCES department (id) ON DELETE RESTRICT;
ALTER TABLE employee ADD CONSTRAINT FK_employee_1 FOREIGN KEY (person_id) REFERENCES person (id) ON DELETE CASCADE;
ALTER TABLE employee ADD CONSTRAINT FK_employee_2 FOREIGN KEY (job_title_id) REFERENCES job_title (id) ON DELETE RESTRICT;
ALTER TABLE employee ADD CONSTRAINT FK_employee_3 FOREIGN KEY (supervisor_id) REFERENCES employee (id) ON DELETE SET NULL;


ALTER TABLE course_instance ADD CONSTRAINT FK_course_instance_0 FOREIGN KEY (course_layout_id) REFERENCES course_layout (id) ON DELETE RESTRICT;


ALTER TABLE planned_activity ADD CONSTRAINT FK_planned_activity_0 FOREIGN KEY (course_instance_id) REFERENCES course_instance (id) ON DELETE CASCADE;
ALTER TABLE planned_activity ADD CONSTRAINT FK_planned_activity_1 FOREIGN KEY (teaching_activity_id) REFERENCES teaching_activity (id) ON DELETE RESTRICT;


ALTER TABLE employee_planned_activity ADD CONSTRAINT FK_employee_planned_activity_0 FOREIGN KEY (employee_id) REFERENCES employee (id) ON DELETE CASCADE;
ALTER TABLE employee_planned_activity ADD CONSTRAINT FK_employee_planned_activity_1 FOREIGN KEY (planned_activity_id) REFERENCES planned_activity (id) ON DELETE CASCADE;

-- On delete 

-- Triggers for Application Constraints --
-- No more than 4 course instances for a teacher/employee
/*
    Checks if an employee will is overloaded aka the employee is allocated
    to more than 4 different course instances for the same study year and study period.
*/
CREATE FUNCTION is_employee_overloaded(current_employee_id int)
RETURNS BOOLEAN AS $$
DECLARE
    most_intense_study_period record;
BEGIN
    -- Find a period where the employee is assigned to the most course instances
    SELECT COUNT(DISTINCT pa.course_instance_id) AS course_assignments, ci.study_year, ci.study_period
    INTO most_intense_study_period
    FROM employee_planned_activity AS epa
    INNER JOIN planned_activity AS pa ON epa.planned_activity_id = pa.id
    INNER JOIN course_instance AS ci ON pa.course_instance_id = ci.id
    WHERE epa.employee_id = current_employee_id
    GROUP BY ci.study_year, ci.study_period
    ORDER BY course_assignments DESC
    Limit 1;

    -- If the most intense period has more then 4 course assignments, then we know the employee is overloaded
    IF most_intense_study_period.course_assignments > 4 THEN
        RETURN true;
    ELSE
        RETURN false;
    End IF;
END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION prevent_teaching_overload_on_planned_activity_update()
RETURNS trigger AS $$
DECLARE
    affected_employee_list int[];
    affected_employee_id int;
BEGIN
    -- On update the application constraint cannot be violated if course_instance_id has not been changed
    IF NEW.course_instance_id = OLD.course_instance_id THEN
        RETURN NEW;
    ELSE
        affected_employee_list := ARRAY(
            SELECT employee_id
            FROM employee_planned_activity AS epa
            WHERE NEW.id = epa.planned_activity_id
        );
        FOREACH affected_employee_id IN ARRAY affected_employee_list LOOP
            IF is_employee_overloaded(affected_employee_id) THEN
                RAISE EXCEPTION 'Cannot update to course_instance_id=% for planned activity (id=%), employee (id=%) would be allocated to much work.', NEW.course_instance_id, NEW.id, affected_employee_id;
                RETURN OLD;
            END IF;
        END LOOP;
        RETURN NEW;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION prevent_teaching_overload_on_employee_allocation()
RETURNS trigger AS $$
BEGIN
    IF is_employee_overloaded(NEW.employee_id) THEN
        RAISE EXCEPTION 'Cannot allocate teaching activity (id=%) to employee (id=%) because that would be too much work for the employee.', NEW.planned_activity_id, NEW.employee_id;
        RETURN OLD;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER on_planned_activity_update
AFTER UPDATE
ON planned_activity
FOR EACH ROW
EXECUTE FUNCTION prevent_teaching_overload_on_planned_activity_update();

CREATE TRIGGER on_employee_allocation
AFTER INSERT OR UPDATE
ON employee_planned_activity
FOR EACH ROW
EXECUTE FUNCTION prevent_teaching_overload_on_employee_allocation();
