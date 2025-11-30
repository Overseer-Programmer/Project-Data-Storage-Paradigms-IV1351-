SELECT 
    employment_id AS "Employee ID",
    CONCAT(p.first_name, ' ' ,p.last_name)  AS "Teacher's Name",
    ci.study_period AS "Period",
    COUNT(DISTINCT ci.id) AS "No of courses"
FROM  employee_planned_activity AS epa
INNER JOIN planned_activity AS pa ON pa.id =  epa.planned_activity_id
INNER JOIN teaching_activity AS ta ON pa.teaching_activity_id = ta.id
INNER JOIN course_instance AS ci ON ci.id = pa.course_instance_id
INNER JOIN employee AS e ON epa.employee_id = e.id
INNER JOIN person AS p ON p.id = e.person_id
WHERE ci.study_year = (SELECT EXTRACT(YEAR FROM CURRENT_DATE)) -- Current year
AND ci.study_period = (
    -- Get the current study period (equivalent to the current yearly quarter) 
    SELECT
    CASE (SELECT EXTRACT(QUARTER FROM CURRENT_DATE))
        WHEN 1 THEN 'P1'   -- Q1
        WHEN 2 THEN 'P2'   -- Q2
        WHEN 3 THEN 'P3'   -- Q3
        WHEN 4 THEN 'P4'   -- Q4
        ELSE NULL        -- optional fallback
    END
)::academic_period
GROUP BY ci.study_period,  epa.employee_id, first_name, last_name,  employment_id
HAVING COUNT(DISTINCT ci.id) >= :min_course_instances;
