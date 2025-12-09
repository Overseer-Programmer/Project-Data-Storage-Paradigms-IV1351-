SELECT pa.id AS planned_activity_id, ta.activity_name, pa.planned_hours
FROM course_instance AS ci
JOIN planned_activity AS pa ON pa.course_instance_id = ci.id
JOIN teaching_activity AS ta ON pa.teaching_activity_id = ta.id
WHERE ci.id = :target_course_instance;
