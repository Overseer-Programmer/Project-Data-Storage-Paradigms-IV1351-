CREATE VIEW course_data AS
    SELECT cl.hp,
        cl.course_code,
        cl.course_name,
        cl.min_students,
        cl.max_students,
        ci.instance_id,
        ci.num_students,
        ci.study_year,
        ci.study_period,
        ci.course_layout_id,
        pa.planned_hours,
        pa.course_instance_id,
        ta.activity_name,
        ta.factor,
        pa.id AS planned_activity_id
    FROM course_instance AS ci
    INNER JOIN course_layout AS cl ON ci.course_layout_id = cl.id
    INNER JOIN planned_activity AS pa ON pa.course_instance_id = ci.id
    INNER JOIN teaching_activity AS ta ON pa.teaching_activity_id = ta.id;

SELECT cd.course_code AS "Course Code",
    cd.instance_id AS "Course Instance ID",
    cd.hp AS "HP",
    cd.study_period AS "Period",
    cd.num_students AS "#Students",
    SUM(CASE WHEN cd.activity_name = 'Lecture' THEN cd.planned_hours * cd.factor ELSE 0 END) AS "Lecture Hours",
    SUM(CASE WHEN cd.activity_name = 'Tutorial' THEN cd.planned_hours * cd.factor ELSE 0 END) AS "Tutorial Hours",
    SUM(CASE WHEN cd.activity_name = 'Lab' THEN cd.planned_hours * cd.factor ELSE 0 END) AS "Lab Hours",
    SUM(CASE WHEN cd.activity_name = 'Seminar' THEN cd.planned_hours * cd.factor ELSE 0 END) AS "Seminar Hours",
    SUM(CASE WHEN cd.activity_name = 'Other' THEN cd.planned_hours * cd.factor ELSE 0 END) AS "Other Overhead Hours",
    SUM(CASE WHEN cd.activity_name = 'Examination' THEN cd.planned_hours ELSE 0 END) AS "Exam",
    SUM(CASE WHEN cd.activity_name = 'Admin' THEN cd.planned_hours ELSE 0 END) AS "Admin",
    SUM(cd.planned_hours * cd.factor) AS "Total Hours"

FROM course_data AS cd
WHERE cd.study_year = (SELECT EXTRACT(YEAR FROM CURRENT_DATE))
GROUP BY cd.course_instance_id, cd.course_code, cd.instance_id, cd.hp, cd.study_period, cd.num_students;
