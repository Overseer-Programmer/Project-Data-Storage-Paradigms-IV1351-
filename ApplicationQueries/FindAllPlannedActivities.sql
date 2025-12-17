SELECT pa.course_instance_id
FROM planned_activity AS pa
JOIN teaching_activity AS ta ON pa.teaching_activity_id = ta.id