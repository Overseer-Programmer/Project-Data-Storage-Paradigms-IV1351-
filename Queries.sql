
SELECT cl.course_code AS "Course Code",
    ci.instance_id AS "Course Instance ID",
    cl.hp AS "HP",
    ci.study_period AS "Period",
    ci.num_students AS "#Students",
    SUM(CASE WHEN ta.activity_name = 'Lecture' THEN pa.planned_hours * ta.factor ELSE 0 END) AS "Lecture Hours",
    SUM(CASE WHEN ta.activity_name = 'Tutorial' THEN pa.planned_hours * ta.factor ELSE 0 END) AS "Tutorial Hours",
    SUM(CASE WHEN ta.activity_name = 'Lab' THEN pa.planned_hours * ta.factor ELSE 0 END) AS "Lab Hours",
    SUM(CASE WHEN ta.activity_name = 'Seminar' THEN pa.planned_hours * ta.factor ELSE 0 END) AS "Seminar Hours",
    SUM(CASE WHEN ta.activity_name = 'Other' THEN pa.planned_hours * ta.factor ELSE 0 END) AS "Other Overhead Hours",
    SUM((2*cl.hp) + 28 + (0.2 * ci.num_students)) AS "Admin",
    SUM(32+ (0.752 * ci.num_students)) AS "Exam",
    SUM(pa.planned_hours * ta.factor + (2*cl.hp) + 28 + (0.2 * ci.num_students) + 32 + (0.752 * ci.num_students)) AS "Total_Hours"

    FROM course_instance AS ci
    INNER JOIN course_layout AS cl ON ci.course_layout_id = cl.id
    INNER JOIN planned_activity AS pa ON pa.course_instance_id = ci.id
    INNER JOIN teaching_activity AS ta ON pa.teaching_activity_id = ta.id
    WHERE ci.study_year = (SELECT EXTRACT(YEAR FROM CURRENT_DATE))
GROUP BY ci.id,course_code,instance_id,hp,
    study_period,
    num_students;
