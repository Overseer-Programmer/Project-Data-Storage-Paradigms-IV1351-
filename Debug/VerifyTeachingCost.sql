SELECT ci.id, epa.employee_id
FROM course_instance AS ci
JOIN course_layout AS cl ON ci.course_layout_id = cl.id
JOIN planned_activity AS pa ON pa.course_instance_id = ci.id
JOIN teaching_activity AS ta ON pa.teaching_activity_id = ta.id
JOIN employee_planned_activity AS epa ON epa.planned_activity_id = pa.id
JOIN employee AS e ON epa.employee_id = e.id
WHERE ci.id = 100