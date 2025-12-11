SELECT planned_activity_id
FROM employee_planned_activity
WHERE employee_id = :chosen_employee_id
FOR UPDATE;