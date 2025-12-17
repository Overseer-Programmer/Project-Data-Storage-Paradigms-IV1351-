SELECT pa.id AS planned_activity_id, pa.planned_hours, ta.activity_name, ta.factor
FROM planned_activity AS pa
JOIN teaching_activity AS ta ON pa.teaching_activity_id = ta.id
WHERE pa.course_instance_id = ?