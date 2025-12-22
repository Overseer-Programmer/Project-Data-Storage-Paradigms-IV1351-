package KthDatabaseApp.model;

public class TeacherAllocationDTO {
    private final Teacher teacher;
    private final PlannedActivity plannedActivity;
    private final int allocatedHours;

    /**
     * Represents a a teacher who is allocated to a planned activity along with the
     * amount of allocated hours the teacher is allocated to the planned activity.
     * 
     * @param teacher         The Teacher that is allocated a planned activity.
     * @param plannedActivity The PlannedActivity that the Teacher is allocated to
     * @param allocatedHours  The amount of hours the teacher is allocated to the
     *                        planned activity.
     */
    public TeacherAllocationDTO(Teacher teacher, PlannedActivity plannedActivity, int allocatedHours) {
        this.teacher = teacher;
        this.plannedActivity = plannedActivity;
        this.allocatedHours = allocatedHours;
    }

    public int getPlannedActivityId() {
        return plannedActivity.getId();
    }

    /**
     * Get the planned hours for the allocated planned activity.
     */
    public int getPlannedHours() {
        return plannedActivity.getPlannedHours();
    }

    /**
     * Get the teaching activity name for the allocated planned activity.
     */
    public String getActivityName() {
        return plannedActivity.getActivityName();
    }

    public int getTeacherId() {
        return teacher.getEmployeeId();
    }

    public String getTeacherFullName() {
        return teacher.getFullName();
    }

    public int getTeacherSalary() {
        return teacher.getSalary();
    }

    /**
     * Get the hours the teacher is allocated to the planned activity.
     */
    public int getAllocatedHours() {
        return allocatedHours;
    }

    /**
     * Get the surrogate id for the Course the teacher is indirectly allocated to
     * via the PlannedActivity.
     */
    public int getAllocatedCourseSurrogateId() {
        return plannedActivity.getCourseSurrogateId();
    }

    /**
     * Get the study year for the Course the teacher is indirectly allocated to
     * via the PlannedActivity.
     */
    public int getAllocatedCourseStudyYear() {
        return plannedActivity.getCourseStudyYear();
    }

    /**
     * Get the study period for the Course the teacher is indirectly allocated to
     * via the PlannedActivity.
     */
    public StudyPeriod getAllocatedCourseStudyPeriod() {
        return plannedActivity.getCourseStudyPeriod();
    }

    /**
     * Get the name for the Course the teacher is indirectly allocated to
     * via the PlannedActivity.
     */
    public String getAllocatedCourseName() {
        return plannedActivity.getCourseName();
    }
}
