SELECT ci.id AS course_instance_id, cl.course_code, COUNT(DISTINCT epa.employee_id) AS assigned_employees
FROM course_instance AS ci
INNER JOIN course_layout AS cl ON ci.course_layout_id = cl.id
INNER JOIN planned_activity AS pa ON pa.course_instance_id = ci.id
LEFT JOIN employee_planned_activity AS epa ON epa.planned_activity_id = pa.id
GROUP BY ci.id, cl.course_code
ORDER BY assigned_employees ASC;
