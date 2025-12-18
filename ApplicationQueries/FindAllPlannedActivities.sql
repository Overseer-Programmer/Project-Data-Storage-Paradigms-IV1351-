SELECT pa.id
FROM planned_activity AS pa
JOIN course_instance AS ci ON pa.course_instance_id = ci.id
ORDER BY ci.study_year, ci.study_period