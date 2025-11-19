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
INSERT INTO course_instance (instance_id, num_students, study_year, course_layout_id)
SELECT ci.instance_id, ci.num_students, ci.study_year, (
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
    WHERE pa.id IS NULL  -- Dummy reference to outer table
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
    WHERE pa.id IS NULL  -- Dummy reference to outer table
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
    WHERE pa.id IS NULL  -- Dummy reference to outer table
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
    WHERE pa.id IS NULL  -- Dummy reference to outer table
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

-- Create final employee_planned_activity cross reference table
DO $$
BEGIN
    FOR i IN 1..1000 LOOP
        INSERT INTO employee_planned_activity (employee_id, planned_activity_id)
        VALUES (
            (
            SELECT id FROM employee
            ORDER BY RANDOM() LIMIT 1
            ),
            (
                SELECT id FROM planned_activity
                ORDER BY RANDOM() LIMIT 1
            )
        );
    END LOOP;
END;
$$ LANGUAGE plpgsql

