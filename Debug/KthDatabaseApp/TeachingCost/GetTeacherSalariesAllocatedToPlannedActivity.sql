SELECT e.salary
FROM employee_planned_activity AS epa
JOIN employee AS e ON epa.employee_id = e.id
WHERE epa.planned_activity_id = :chosen_planned_activity;