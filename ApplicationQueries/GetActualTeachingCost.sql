SELECT SUM(e.salary * epa.allocated_hours * ta.factor / (30.0 * 24)) AS cost
FROM course_instance AS ci
JOIN planned_activity AS pa ON pa.course_instance_id = ci.id
JOIN teaching_activity AS ta ON pa.teaching_activity_id = ta.id
JOIN employee_planned_activity AS epa ON epa.planned_activity_id = pa.id
JOIN employee AS e ON epa.employee_id = e.id
WHERE ci.id = ?