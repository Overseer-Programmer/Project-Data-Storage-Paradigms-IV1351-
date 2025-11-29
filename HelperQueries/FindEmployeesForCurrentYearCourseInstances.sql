SELECT e.id AS employee_id,
    CONCAT(p.first_name, ' ', p.last_name) AS employee_name,
    COUNT(DISTINCT ci.id) AS assigned_course_instances
FROM employee AS e
INNER JOIN person AS p ON e.person_id = p.id
INNER JOIN employee_planned_activity AS epa ON epa.employee_id = e.id
INNER JOIN planned_activity AS pa ON epa.planned_activity_id = pa.id
INNER JOIN course_instance AS ci ON pa.course_instance_id = ci.id
WHERE ci.study_year = (SELECT EXTRACT(YEAR FROM CURRENT_DATE)) -- Current year
GROUP BY e.id, p.first_name, p.last_name
ORDER BY assigned_course_instances DESC