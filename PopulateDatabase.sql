-- Create tables without foreign keys
\set content `cat DatabaseData/CourseLayout.json`
INSERT INTO course_layout (course_code, course_name, min_students, max_students, hp)
SELECT course_code, course_name, min_students, max_students, hp
FROM jsonb_populate_recordset(NULL::course_layout, :'content'::jsonb);

\set content `cat DatabaseData/TeachingActivities.json`
INSERT INTO teaching_activity (activity_name, factor)
SELECT activity_name, factor
FROM jsonb_populate_recordset(NULL::teaching_activity, :'content'::jsonb);

\set content `cat DatabaseData/JobTitle.json`
INSERT INTO job_title (job_title)
SELECT job_title
FROM jsonb_populate_recordset(NULL::job_title, :'content'::jsonb);

\set content `cat DatabaseData/Person.json`
INSERT INTO person (personal_number, first_name, last_name, phone_number, street, zip, city)
SELECT personal_number, first_name, last_name, phone_number, street, zip, city
FROM jsonb_populate_recordset(NULL::person, :'content'::jsonb);

-- Create tables with foreign keys
\set content `cat DatabaseData/CourseInstance.json`
INSERT INTO course_instance (instance_id, num_students, study_year, study_period, course_layout_id)
SELECT ci.instance_id, ci.num_students, ci.study_year, ci.study_period, (
    SELECT id FROM course_layout
    WHERE ci.id IS NULL  -- Dummy reference to outer table
    ORDER BY RANDOM() LIMIT 1
) AS course_layout_id
FROM jsonb_populate_recordset(NULL::course_instance, :'content'::jsonb) ci;

\set content `cat DatabaseData/PlannedActivity/PlannedActivity1.json`
INSERT INTO planned_activity (planned_hours, course_instance_id, teaching_activity_id)
SELECT pa.planned_hours, (
    SELECT id FROM course_instance
    WHERE pa.id IS NULL  -- Dummy reference to outer table
    ORDER BY RANDOM() LIMIT 1
) AS course_instance_id,
(
    SELECT id FROM teaching_activity
    WHERE pa.id IS NULL AND activity_name != 'Examination' AND activity_name != 'Admin'
    ORDER BY RANDOM() LIMIT 1
) AS teaching_activity_id
FROM jsonb_populate_recordset(NULL::planned_activity, :'content'::jsonb) pa;

\set content `cat DatabaseData/PlannedActivity/PlannedActivity2.json`
INSERT INTO planned_activity (planned_hours, course_instance_id, teaching_activity_id)
SELECT pa.planned_hours, (
    SELECT id FROM course_instance
    WHERE pa.id IS NULL  -- Dummy reference to outer table
    ORDER BY RANDOM() LIMIT 1
) AS course_instance_id,
(
    SELECT id FROM teaching_activity
    WHERE pa.id IS NULL AND activity_name != 'Examination' AND activity_name != 'Admin'
    ORDER BY RANDOM() LIMIT 1
) AS teaching_activity_id
FROM jsonb_populate_recordset(NULL::planned_activity, :'content'::jsonb) pa;

\set content `cat DatabaseData/PlannedActivity/PlannedActivity3.json`
INSERT INTO planned_activity (planned_hours, course_instance_id, teaching_activity_id)
SELECT pa.planned_hours, (
    SELECT id FROM course_instance
    WHERE pa.id IS NULL  -- Dummy reference to outer table
    ORDER BY RANDOM() LIMIT 1
) AS course_instance_id,
(
    SELECT id FROM teaching_activity
    WHERE pa.id IS NULL AND activity_name != 'Examination' AND activity_name != 'Admin'
    ORDER BY RANDOM() LIMIT 1
) AS teaching_activity_id
FROM jsonb_populate_recordset(NULL::planned_activity, :'content'::jsonb) pa;

\set content `cat DatabaseData/PlannedActivity/PlannedActivity4.json`
INSERT INTO planned_activity (planned_hours, course_instance_id, teaching_activity_id)
SELECT pa.planned_hours, (
    SELECT id FROM course_instance
    WHERE pa.id IS NULL  -- Dummy reference to outer table
    ORDER BY RANDOM() LIMIT 1
) AS course_instance_id,
(
    SELECT id FROM teaching_activity
    WHERE pa.id IS NULL AND activity_name != 'Examination' AND activity_name != 'Admin'
    ORDER BY RANDOM() LIMIT 1
) AS teaching_activity_id
FROM jsonb_populate_recordset(NULL::planned_activity, :'content'::jsonb) pa;

\set content `cat DatabaseData/Department.json`
INSERT INTO department (department_name)
SELECT department_name
FROM jsonb_populate_recordset(NULL::department, :'content'::jsonb);

\set content `cat DatabaseData/Employee.json`
INSERT INTO employee (employment_id, skill_set, salary, department_id, person_id, job_title_id)
SELECT e.employment_id, e.skill_set, e.salary, (
    SELECT id FROM department
    WHERE e.id IS NULL  -- Dummy reference to outer table
    ORDER BY RANDOM() LIMIT 1
) AS department_id,
(
    SELECT id FROM person
    WHERE e.id IS NULL  -- Dummy reference to outer table
    ORDER BY RANDOM() LIMIT 1
) AS person_id,
(
    SELECT id FROM job_title
    WHERE e.id IS NULL  -- Dummy reference to outer table
    ORDER BY RANDOM() LIMIT 1
) AS job_title_id
FROM jsonb_populate_recordset(NULL::employee, :'content'::jsonb) e;

-- Assign managers for employees and departments
\set content `cat DatabaseData/Department.json`
UPDATE department AS d
SET manager_id = (
    SELECT id FROM employee
    WHERE d.id IS NOT NULL  -- Dummy reference to outer table
    ORDER BY RANDOM() LIMIT 1
);

\set content `cat DatabaseData/Employee.json`
UPDATE employee AS e
SET supervisor_id = (
    SELECT id FROM employee
    WHERE e.id IS NOT NULL  -- Dummy reference to outer table
    ORDER BY RANDOM() LIMIT 1
);

-- Fill the employee_planned_activity cross reference table and assign employees to planned activities
DO $$
DECLARE
    employee_list int[];
    current_employee_id int;
    all_course_instances int[];
    current_course_instance_id int;
    course_instance_belonging_planned_activities int[];
    chosen_planned_activity_id int;
    existing_allocation int;
BEGIN
    employee_list := ARRAY(SELECT id FROM employee);
    all_course_instances := ARRAY(SELECT id FROM course_instance);
    FOREACH current_employee_id IN ARRAY employee_list LOOP
        RAISE NOTICE 'employee=% assigned % planned activities', current_employee_id, (1 + floor(random() * 4)::int);
        FOR i IN 1..(1 + floor(random() * 4)::int) LOOP
            -- Find a course instance that has planned activities
            course_instance_belonging_planned_activities := '{}';
            WHILE cardinality(course_instance_belonging_planned_activities) = 0 LOOP
                -- Find a random course instance to assign planned activities from
                SELECT id
                INTO current_course_instance_id
                FROM course_instance
                WHERE id = all_course_instances[1 + floor(random() * cardinality(all_course_instances))::int];
                
                -- Locate all the planned activities in that course instance
                course_instance_belonging_planned_activities := ARRAY(
                    SELECT id
                    FROM planned_activity
                    WHERE course_instance_id = current_course_instance_id
                );
            END LOOP;

            -- Assign the employee to a number of the planned activities for the course
            FOR j IN 1..(1 + floor(random() * cardinality(course_instance_belonging_planned_activities))::int) LOOP
                chosen_planned_activity_id := course_instance_belonging_planned_activities[1 + floor(random() * cardinality(course_instance_belonging_planned_activities))];
                SELECT planned_activity_id
                INTO existing_allocation
                FROM employee_planned_activity
                WHERE planned_activity_id = chosen_planned_activity_id AND employee_id = current_employee_id;
                IF existing_allocation IS NULL THEN
                    INSERT INTO employee_planned_activity (employee_id, planned_activity_id, allocated_hours)
                    VALUES (current_employee_id, chosen_planned_activity_id, 0);
                END IF;
            END LOOP;
        END LOOP;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- Allocate a number of hours to each employee for each planned activity
DO $$
DECLARE
    assigned_activity record;
    current_allocated_hours int;
    max_allocated_hours int;
BEGIN
    FOR assigned_activity IN SELECT * FROM employee_planned_activity LOOP
        -- Get the current amount of allocated hours for the planned activity
        SELECT SUM(allocated_hours)
        INTO current_allocated_hours
        FROM employee_planned_activity
        WHERE planned_activity_id = assigned_activity.planned_activity_id;

        /*
            Get the maximum amount of allocated hours the planned activity can have.
            This is the planned hours.
        */
        SELECT pa.planned_hours * ta.factor
        INTO max_allocated_hours
        FROM planned_activity AS pa
        INNER JOIN teaching_activity AS ta
        ON pa.teaching_activity_id = ta.id
        WHERE pa.id = assigned_activity.planned_activity_id;

        /*
            Assign the current employee a random number of allocated hours to the current
            planned activity such that the total allocated hours does not exceed the
            maximum amount of hours.
        */
        UPDATE employee_planned_activity
        SET allocated_hours = floor(random() * (max_allocated_hours - current_allocated_hours + 1))::int
        WHERE planned_activity_id = assigned_activity.planned_activity_id AND employee_id = assigned_activity.employee_id;
    END LOOP;
END;
$$ LANGUAGE plpgsql;
