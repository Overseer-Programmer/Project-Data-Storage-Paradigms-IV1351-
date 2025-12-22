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
 planned_hours INT,
 course_instance_id INT NOT NULL,
 teaching_activity_id INT NOT NULL
);

ALTER TABLE planned_activity ADD CONSTRAINT PK_planned_activity PRIMARY KEY (id);


CREATE TABLE employee_planned_activity (
 employee_id INT NOT NULL,
 planned_activity_id INT NOT NULL,
 allocated_hours INT NOT NULL
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

-- Triggers for Application Constraints --
-- No more than 4 course instances for a teacher/employee

-- Return the amount of course assignments for the period where the employee has the most course assignments
CREATE FUNCTION get_max_teaching_allocation(current_employee_id int)
RETURNS int AS $$
DECLARE
    most_intense_study_period record;
BEGIN
    SELECT COUNT(DISTINCT pa.course_instance_id) AS course_assignments, ci.study_year, ci.study_period
    INTO most_intense_study_period
    FROM employee_planned_activity AS epa
    INNER JOIN planned_activity AS pa ON epa.planned_activity_id = pa.id
    INNER JOIN course_instance AS ci ON pa.course_instance_id = ci.id
    WHERE epa.employee_id = current_employee_id
    GROUP BY ci.study_year, ci.study_period
    ORDER BY course_assignments DESC
    Limit 1;
    IF most_intense_study_period IS NULL THEN
        RETURN 0;
    ELSE
        RETURN most_intense_study_period.course_assignments;
    END IF;
END;
$$ LANGUAGE plpgsql;

/*
    Checks if an employee will is overloaded aka the employee is allocated
    to more than 4 different course instances for the same study year and study period.
*/
CREATE FUNCTION is_employee_overloaded(current_employee_id int)
RETURNS BOOLEAN AS $$
BEGIN
    -- If the most intense period has more then 4 course assignments, then we know the employee is overloaded
    IF get_max_teaching_allocation(current_employee_id) > 4 THEN
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
        RAISE EXCEPTION 'Cannot allocate planned activity (id=%) to employee (id=%) because that would be too much work for the employee.', NEW.planned_activity_id, NEW.employee_id;
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

-- One immutable "Examination" and one immutable "Admin" planned_activity for each course instance
CREATE FUNCTION add_exam_and_admin()
RETURNS trigger AS $$
DECLARE
    exam_teaching_activity_id int;
    admin_teaching_activity_id int;
    course_hp DECIMAL(10, 1);
BEGIN
    -- Assert that "Examination" and "Admin" teaching activities exists
    SELECT id
    INTO exam_teaching_activity_id
    FROM teaching_activity
    WHERE activity_name = 'Examination' and factor = 1;
    SELECT id
    INTO admin_teaching_activity_id
    FROM teaching_activity
    WHERE activity_name = 'Admin' and factor = 1;
    IF exam_teaching_activity_id IS NULL OR admin_teaching_activity_id IS NULL THEN
        RAISE EXCEPTION 'A "Examination" and "Admin" teaching activity must exist and have factor 1 in order to create a course instance.';
        RETURN OLD;
    END IF;

    -- Create the planned activities
    SELECT cl.hp
    INTO course_hp
    FROM course_instance AS ci
    INNER JOIN course_layout AS cl
    ON ci.course_layout_id = cl.id
    WHERE ci.id = NEW.id;
    INSERT INTO planned_activity (planned_hours, course_instance_id, teaching_activity_id)
    VALUES (NULL, NEW.id, exam_teaching_activity_id);
    INSERT INTO planned_activity (planned_hours, course_instance_id, teaching_activity_id)
    VALUES (NULL, NEW.id, admin_teaching_activity_id);

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER on_course_instance_added
AFTER INSERT
ON course_instance
FOR EACH ROW
EXECUTE FUNCTION add_exam_and_admin();

CREATE FUNCTION assert_legal_planned_activity_modified()
RETURNS trigger AS $$
DECLARE

    teaching_activity_name VARCHAR(250);
BEGIN
    SELECT ta.activity_name
    INTO teaching_activity_name
    FROM planned_activity AS pa
    JOIN teaching_activity AS ta ON pa.teaching_activity_id = ta.id
    WHERE pa.id = OLD.id;
    IF teaching_activity_name = 'Examination' OR teaching_activity_name = 'Admin' THEN
        RAISE EXCEPTION 'Cannot modify the teaching activities of "Examination" or "Admin"';
        RETURN OLD;
    END IF;
    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    ELSE
        RETURN NEW;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER on_planned_activity_changed
BEFORE INSERT OR UPDATE OR DELETE
ON planned_activity
FOR EACH ROW
EXECUTE FUNCTION assert_legal_planned_activity_modified();

-- Teaching activity "Exercise" business constraint
CREATE FUNCTION handle_teaching_activity_addition_request()
RETURNS trigger AS $$
DECLARE
    target_course_instance_id int;
    target_employee_id int;
    target_hours int := 10;
    create_planned_activity_id int;
BEGIN
    IF NEW.activity_name != 'Exercise' THEN
        RETURN NEW;
    END IF;

    -- Find a course instance for the new planned activity
    SELECT id
    INTO target_course_instance_id
    FROM course_instance
    ORDER BY RANDOM()
    LIMIT 1;
    IF target_course_instance_id IS NULL THEN
        RAISE EXCEPTION 'Unable to find course instance with at least one employee allocated to it for teaching activity "Exercise"';
    END IF;


    --Create a planned activity
    INSERT INTO planned_activity (planned_hours, course_instance_id, teaching_activity_id)
    VALUES(target_hours, target_course_instance_id, NEW.id)
    RETURNING id INTO  create_planned_activity_id;
    
    -- Find a teacher
    SELECT id 
    INTO  target_employee_id
    FROM employee
    ORDER BY get_max_teaching_allocation(id) ASC
    LIMIT 1;
    IF target_employee_id IS NULL THEN
        RAISE EXCEPTION 'Could not find employee allocate to teacher activity "Exercise"';
    END IF;

    -- Connect employee to the created planned_activity
    INSERT INTO employee_planned_activity(employee_id, planned_activity_id, allocated_hours)
    VALUES (target_employee_id, create_planned_activity_id, target_hours);

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

/*
    Check If all planned activities of type "Exercise" have at least one
    teacher allocated them and that the teaching activity exercise is
    associated with at least one course instance, otherwise raise exception.
*/
CREATE FUNCTION check_exercise_teaching_activity_constraint()
RETURNS trigger AS $$
BEGIN
    -- If teaching activity "Exercise" does not exist, nothing has to be done.
    IF EXISTS (
        SELECT *
        FROM teaching_activity
        WHERE activity_name = 'Exercise'
    )
    THEN
        -- Assert teaching activity "Exercise" is associated with at least one course instance
        IF NOT EXISTS (
            SELECT *
            FROM planned_activity AS pa
            JOIN teaching_activity AS ta ON pa.teaching_activity_id = ta.id
            WHERE ta.activity_name = 'Exercise'
        )
        THEN
            RAISE EXCEPTION 'Teaching activity "Exercise" must be associated with at least one course instance.';
        END IF;

        -- Assert all planned activities of type "Exercise" have a teacher allocated to them
        IF EXISTS (
            SELECT *
            FROM planned_activity pa
            JOIN teaching_activity ta ON pa.teaching_activity_id = ta.id
            LEFT JOIN employee_planned_activity emp ON emp.planned_activity_id = pa.id
            WHERE ta.activity_name = 'Exercise' AND emp.employee_id IS NULL
            LIMIT 1
        )
        THEN 
            RAISE EXCEPTION 'All planned activities of type "Exercise" must have a teacher allocated to them.';
        END if;
    END IF;

    -- Allow deletes to pass through
    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    ELSE
        RETURN NEW;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Make sure a planned activity of type "Exercise" is created when a teaching activity of type "Exercise" is created.
CREATE TRIGGER on_teaching_activity_added
AFTER INSERT
ON teaching_activity
FOR EACH ROW
EXECUTE FUNCTION handle_teaching_activity_addition_request();

/*
    Make sure planned activities of type "Exercise" always has a teacher allocated to them.
    This can be violated when planned activities are deleted, inserted and updated and when
    allocations for teachers are updated or deleted which is why these triggers exist.
    However they are constraint trigger of type DEFERRABLE INITIALLY DEFERRED which makes
    it possible to create a transaction that both creates a planned activity of type
    "Exercise" and assigns a teacher to them, making sure "Exercise" planned activities
    are not blocked from being created.
*/
CREATE CONSTRAINT TRIGGER on_planned_activity_changed_2
AFTER INSERT OR DELETE OR UPDATE
ON planned_activity
DEFERRABLE INITIALLY DEFERRED
FOR EACH ROW
EXECUTE FUNCTION check_exercise_teaching_activity_constraint();
CREATE CONSTRAINT TRIGGER on_teaching_de_or_reallocation
AFTER UPDATE OR DELETE
ON employee_planned_activity
DEFERRABLE INITIALLY DEFERRED
FOR EACH ROW
EXECUTE FUNCTION check_exercise_teaching_activity_constraint();