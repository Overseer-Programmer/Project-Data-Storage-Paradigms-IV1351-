SELECT SUM(
        -- Compute the teaching cost for the current employee
        (e.salary / (30.0 * 24)) * (
            -- Derive planned hours for "Examination" and "Admin"
            CASE
                WHEN ta.activity_name = 'Examination' THEN 32 + 0.725 * ci.num_students
                WHEN ta.activity_name = 'Admin' THEN 2 * cl.hp + 28 + 0.2 * ci.num_students
                ELSE pa.planned_hours
            END
        ) * ta.factor / (
            -- Evenly divide the planned hour distribution for the planned activity by the allocated employees
            SELECT COUNT(*)
            FROM employee_planned_activity AS epa2
            WHERE epa2.planned_activity_id = pa.id
        )
    ) AS planned_cost,
    SUM(e.salary * epa.allocated_hours * ta.factor / (30.0 * 24)) AS actual_cost,
    cl.course_code,
    ci.instance_id,
    ci.study_period
FROM course_instance AS ci
JOIN course_layout AS cl ON ci.course_layout_id = cl.id
JOIN planned_activity AS pa ON pa.course_instance_id = ci.id
JOIN teaching_activity AS ta ON pa.teaching_activity_id = ta.id
JOIN employee_planned_activity AS epa ON epa.planned_activity_id = pa.id
JOIN employee AS e ON epa.employee_id = e.id
WHERE ci.id = ?
GROUP BY cl.course_code, ci.instance_id, ci.study_period
