package KthDatabaseApp.model;

public class TeacherAllocation {
    public final PlannedActivityDTO plannedActivity;
    public final int allocatedHours;

    public TeacherAllocation(PlannedActivityDTO plannedActivity, int allocatedHours) {
        this.plannedActivity = plannedActivity;
        this.allocatedHours = allocatedHours;
    }
}
