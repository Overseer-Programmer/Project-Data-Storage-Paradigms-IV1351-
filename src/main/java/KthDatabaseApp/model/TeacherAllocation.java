package KthDatabaseApp.model;

public class TeacherAllocation {
    public final Teacher teacher;
    public final PlannedActivity plannedActivity;
    public final int allocatedHours;

    public TeacherAllocation(Teacher teacher, PlannedActivity plannedActivity, int allocatedHours) {
        this.teacher = teacher;
        this.plannedActivity = plannedActivity;
        this.allocatedHours = allocatedHours;
    }
}
