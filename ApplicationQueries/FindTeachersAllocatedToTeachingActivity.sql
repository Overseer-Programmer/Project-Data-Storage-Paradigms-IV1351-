SELECT epa.employee_id
FROM employee_planned_activity AS epa
JOIN planned_activity AS pa ON epa.planned_activity_id = pa.id
JOIN teaching_activity AS ta ON pa.teaching_activity_id = ta.id
WHERE ta.activity_name = ?