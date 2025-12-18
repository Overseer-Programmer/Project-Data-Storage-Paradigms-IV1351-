SELECT ci.id,
    cl.course_code,
    cl.course_name,
    concat(p.first_name, ' ', p.last_name) AS Full_name,
    ta.activity_name,
    epa.employee_id
FROM course_instance AS ci
    JOIN course_layout AS cl ON ci.course_layout_id = cl.id
    JOIN planned_activity AS pa ON ci.id = pa.course_instance_id
    JOIN teaching_activity AS ta ON pa.teaching_activity_id = ta.id
    JOIN employee_planned_activity AS epa ON epa.planned_activity_id = pa.id
    JOIN employee AS e ON epa.employee_id = e.id
    JOIN person AS p ON e.person_id = p.id
WHERE ta.activity_name = 'Exercise';