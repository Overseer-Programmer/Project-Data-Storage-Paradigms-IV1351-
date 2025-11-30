-- CONSTRAINT: No more than 4 course instances for a teacher/employee
DO $$
DECLARE
    planned_activities_in_same_study_period int[];
    current_planned_activity_id int;
    error_msg VARCHAR(500);
    different_period_planned_activity int;
    course_instances_in_same_study_period int[];
    current_course_instance_id int;
BEGIN
    planned_activities_in_same_study_period := ARRAY(
        SELECT pa.id
        FROM planned_activity AS pa
        INNER JOIN course_instance AS ci
        ON pa.course_instance_id = ci.id
        WHERE ci.study_year = 2025 AND ci.study_period = 'P1'
        LIMIT 50
    );
    FOREACH current_planned_activity_id IN ARRAY planned_activities_in_same_study_period LOOP
        BEGIN
            INSERT INTO employee_planned_activity (employee_id, planned_activity_id, allocated_hours)
            VALUES (1, current_planned_activity_id, 0);
        EXCEPTION
            WHEN others THEN
                GET STACKED DIAGNOSTICS error_msg = MESSAGE_TEXT;
                RAISE NOTICE 'Error inserting ID %: %', current_planned_activity_id, error_msg;
                EXIT;
        END;
    END LOOP;

    course_instances_in_same_study_period := ARRAY(
        SELECT id
        FROM course_instance
        WHERE study_year = 2025 AND study_period = 'P1'
        LIMIT 50
    );
    SELECT pa.id
    INTO different_period_planned_activity
    FROM course_instance AS ci
    INNER JOIN planned_activity AS pa ON pa.course_instance_id = ci.id
    INNER JOIN employee_planned_activity AS epa ON epa.planned_activity_id = pa.id
    WHERE (ci.study_year != 2025 OR ci.study_period != 'P1')
    AND epa.employee_id = 1
    LIMIT 1;
    FOREACH current_course_instance_id IN ARRAY course_instances_in_same_study_period LOOP
        BEGIN
            UPDATE planned_activity
            SET course_instance_id = current_course_instance_id
            WHERE id = different_period_planned_activity;
        EXCEPTION
            WHEN others THEN
                GET STACKED DIAGNOSTICS error_msg = MESSAGE_TEXT;
                RAISE NOTICE 'Error updating course instance for planned activity: %', error_msg;
                EXIT;
        END;
    END LOOP;
END $$;

SELECT epa.employee_id, ci.study_year, ci.study_period, COUNT(DISTINCT pa.course_instance_id) AS allocated_course_instances
FROM employee_planned_activity AS epa
INNER JOIN planned_activity AS pa ON epa.planned_activity_id = pa.id
INNER JOIN course_instance AS ci ON pa.course_instance_id = ci.id
WHERE epa.employee_id = 1
GROUP BY epa.employee_id, ci.study_year, ci.study_period
ORDER BY allocated_course_instances DESC;