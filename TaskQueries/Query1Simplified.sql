CREATE OR REPLACE calc(name VARCHAR(250))
RETURN DECIMAL(10,1) AS $$
BEGIN
    IF (ta.activity_name = name) THEN
        RETURN pa.planned_hours * ta.factor;
    ELSE
        RETURN 0;
    END IF;
END;
$$ LANGUAGE plpgsql;

SELECT cl.course_code AS "Course Code",
    ci.instance_id AS "Course Instance ID",
    cl.hp AS "HP",
    ci.study_period AS "Period",
    ci.num_students AS "#Students",
    SUM(calc("Lecture")) AS "Lecture Hours",
    SUM(calc("Tutorial")) AS "Tutorial Hours",
    SUM(calc("Lab")) AS "Lab Hours",
    SUM(calc("Seminar")) AS "Seminar Hours",
    SUM(calc("Other")) AS "Other Overhead Hours",
    SUM(calc("Exam")) AS "Exam",
    SUM(calc("Admin")) AS "Admin",
    SUM(pa.planned_hours * ta.factor) AS "Total Hours"

FROM course_instance AS ci
INNER JOIN course_layout AS cl ON ci.course_layout_id = cl.id
INNER JOIN planned_activity AS pa ON pa.course_instance_id = ci.id
INNER JOIN teaching_activity AS ta ON pa.teaching_activity_id = ta.id
WHERE ci.study_year = (SELECT EXTRACT(YEAR FROM CURRENT_DATE))
GROUP BY ci.id, cl.course_code, ci.instance_id, cl.hp, ci.study_period, ci.num_students;
