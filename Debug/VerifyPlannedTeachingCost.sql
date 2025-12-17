SELECT e.id AS employee_id,
(e.salary / (30.0 * 24)) AS hourly_salary,
(
    CASE
        WHEN ta.activity_name = 'Examination' THEN 32 + 0.725 * ci.num_students
        WHEN ta.activity_name = 'Admin' THEN 2 * cl.hp + 28 + 0.2 * ci.num_students
        ELSE pa.planned_hours
    END
) * ta.factor AS planned_hours,
(
    SELECT COUNT(*)
    FROM employee_planned_activity AS epa2
    WHERE epa2.planned_activity_id = pa.id
) AS allocated_employees
FROM course_instance AS ci
JOIN course_layout AS cl ON ci.course_layout_id = cl.id
JOIN planned_activity AS pa ON pa.course_instance_id = ci.id
JOIN teaching_activity AS ta ON pa.teaching_activity_id = ta.id
JOIN employee_planned_activity AS epa ON epa.planned_activity_id = pa.id
JOIN employee AS e ON epa.employee_id = e.id
WHERE ci.id = :chosen_course_instance_id