SELECT ci.id AS course_instance_id,
    ci.course_layout_id,
    ci.instance_id,
    cl.course_code,
    cl.course_name,
    ci.num_students,
    ci.study_year,
    ci.Study_Period,
    cl.hp,
    cl.min_students,
    cl.max_students
FROM course_instance AS ci
JOIN course_layout AS cl ON ci.course_layout_id = cl.id
WHERE ci.id = :chosen_course_id;