SELECT e.id AS employee_id,
    p.first_name,
    p.last_name,
    p.street,
    p.zip,
    p.city,
    e.salary
FROM employee AS e
JOIN person AS p ON e.person_id = p.id
WHERE e.id = ?