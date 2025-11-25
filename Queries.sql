SELECT * 
FROM course_instance AS ci
INNER JOIN course_layout AS cl ON ci.course_layout_id = cl.id
WHERE study_year = (SELECT EXTRACT(YEAR FROM CURRENT_DATE))
ORDER BY ci.id ASC;