SELECT cl.course_code AS "Course Code",
    ci.instance_id AS "Course Instance ID",
    cl.hp AS "HP",
    CONCAT(p.first_name, ' ', p.last_name) AS "Teacher's Name",
    jt.job_title AS "Designation",
    SUM(CASE WHEN ta.activity_name = 'Lecture' THEN epa.allocated_hours * ta.factor ELSE 0 END) AS "Lecture Hours",
    SUM(CASE WHEN ta.activity_name = 'Tutorial' THEN epa.allocated_hours * ta.factor ELSE 0 END) AS "Tutorial Hours",
    SUM(CASE WHEN ta.activity_name = 'Lab' THEN epa.allocated_hours * ta.factor ELSE 0 END) AS "Lab Hours",
    SUM(CASE WHEN ta.activity_name = 'Seminar' THEN epa.allocated_hours * ta.factor ELSE 0 END) AS "Seminar Hours",
    SUM(CASE WHEN ta.activity_name = 'Other' THEN epa.allocated_hours * ta.factor ELSE 0 END) AS "Other Overhead Hours",
    SUM(CASE WHEN ta.activity_name = 'Examination' THEN epa.allocated_hours * ta.factor ELSE 0 END) AS "Exam",
    SUM(CASE WHEN ta.activity_name = 'Admin' THEN epa.allocated_hours * ta.factor ELSE 0 END) AS "Admin",
    SUM(epa.allocated_hours * ta.factor) AS "Total Hours"

FROM (
    -- Select a current year's course instance
    SELECT *
    FROM course_instance
    WHERE study_year = (SELECT EXTRACT(YEAR FROM CURRENT_DATE))
    LIMIT 1
) AS ci
INNER JOIN course_layout AS cl ON ci.course_layout_id = cl.id
INNER JOIN planned_activity AS pa ON pa.course_instance_id = ci.id
INNER JOIN teaching_activity AS ta ON pa.teaching_activity_id = ta.id
INNER JOIN employee_planned_activity AS epa ON epa.planned_activity_id = pa.id
INNER JOIN employee AS e ON epa.employee_id = e.id
INNER JOIN person AS p ON e.person_id = p.id
INNER JOIN job_title AS jt ON e.job_title_id = jt.id
GROUP BY ci.id, cl.course_code, ci.instance_id, cl.hp, p.first_name, p.last_name, jt.job_title;
