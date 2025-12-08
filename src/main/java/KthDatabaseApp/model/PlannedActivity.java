package KthDatabaseApp.model;

import java.util.List;

public class PlannedActivity {
    public final int planned_hours;
    public final int id;
    public final String activityName;
    private List<Teacher> allocatedTeachers;

    public PlannedActivity(int id, int planned_hours, String activityName) {
        this.id = id;
        this.planned_hours = planned_hours;
        this.activityName = activityName;
    }

    public void allocateTeacher(Teacher teacher) {
        allocatedTeachers.add(teacher);
    }

    public void deallocateTeacher(Teacher teacher) {
        allocatedTeachers.remove(teacher);
    }

    public TeacherDTO[] getAllocatedTeachers() {
        TeacherDTO teacherArray[] = new TeacherDTO[allocatedTeachers.size()];
        int index = 0;
        while (allocatedTeachers.get(index) != null) {
            teacherArray[index] = allocatedTeachers.get(index);
            index++;
        }
        return teacherArray;
    }
}
