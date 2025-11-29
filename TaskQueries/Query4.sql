SELECT 
    e.employment_id AS "Employee ID",
    CONCAT(p.first_name, ' ' ,p.last_name )  AS "Teacher's Name",
    ci.study_period AS "Period",
    COUNT(DISTINCT ci.id) AS "No of courses"

FROM course_instance AS ci
INNER JOIN course_layout AS cl ON ci.course_layout_id = cl.id
INNER JOIN planned_activity AS pa ON pa.course_instance_id = ci.id
INNER JOIN teaching_activity AS ta ON pa.teaching_activity_id = ta.id
INNER JOIN employee_planned_activity AS epa ON epa.planned_activity_id = pa.id
INNER JOIN employee AS e ON epa.employee_id = e.id
INNER JOIN person AS p ON p.id = e.person_id
WHERE ci.study_year = (SELECT EXTRACT(YEAR FROM CURRENT_DATE)) -- Current year
GROUP BY ci.study_period, p.first_name, p.last_name, e.id,  e.employment_id;
