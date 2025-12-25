package KthDatabaseApp.model;

import java.util.Objects;

public class TeacherAllocation implements TeacherAllocationDTO {
    public final Teacher teacher;
    public final PlannedActivity plannedActivity;
    public final int allocatedHours;

    /**
     * Represents a a teacher who is allocated to a planned activity along with the
     * amount of allocated hours the teacher is allocated to the planned activity.
     * 
     * @param teacher         The Teacher that is allocated a planned activity.
     * @param plannedActivity The PlannedActivity that the Teacher is allocated to
     * @param allocatedHours  The amount of hours the teacher is allocated to the
     *                        planned activity.
     */
    public TeacherAllocation(Teacher teacher, PlannedActivity plannedActivity, int allocatedHours) {
        this.teacher = teacher;
        this.plannedActivity = Objects.requireNonNull(plannedActivity);;
        this.allocatedHours = Objects.requireNonNull(allocatedHours);;
    }

    /**
     * Creates a teacher allocation without the teacher
     * @param plannedActivity
     * @param allocatedHours
     */
    public TeacherAllocation(PlannedActivity plannedActivity, int allocatedHours) {
        this(null, plannedActivity, allocatedHours);
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
        assertTeacherExists();
        return teacher.getEmployeeId();
    }

    public String getTeacherFullName() {
        assertTeacherExists();
        return teacher.getFullName();
    }

    public int getTeacherSalary() {
        assertTeacherExists();
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

    private void assertTeacherExists() {
        if (teacher == null) {
            throw new NullPointerException("Teacher is not specified.");
        }
    }
}
