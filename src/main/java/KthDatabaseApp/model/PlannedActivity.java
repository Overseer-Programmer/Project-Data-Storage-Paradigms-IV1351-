package KthDatabaseApp.model;

import java.util.Objects;

public class PlannedActivity implements PlannedActivityDTO {
    private final int id;
    private final Course course; // A reference to the course object the planned activity belongs to
    private final String activityName;
    private int plannedHours; // The planned_hours attribute of the planned_activity table
    private double multiplicationFactor;

    /**
     * Creates a planned activity object which contains the planned_activity
     * and teaching_activity attributes from the conceptual model
     * 
     * @param id                   The id attribute of the planned_activity table
     * @param activityName         The activity_name attribute of the
     *                             teaching_activity table
     * @param multiplicationFactor The multiplication_factor attribute of the
     *                             teaching_activity table
     * @param plannedHours         The amount of hours which is planned for the
     *                             planned activity. Will be ignored if the planned
     *                             activity is of a type that has planned hours as a
     *                             derived attribute.
     */
    public PlannedActivity(int id, Course course, String activityName, double multiplicationFactor, int plannedHours) {
        this.id = Objects.requireNonNull(id);
        this.course = Objects.requireNonNull(course);
        this.activityName = Objects.requireNonNull(activityName);
        setTeachingActivityFactor(multiplicationFactor);

        // Set the planned hours for derived and non derived teaching activity types
        if (activityName.equals("Examination")) {
            setPlannedHours(
                    (int) Math.round(32 + 0.725 * course.getStudentCount()));
        } else if (activityName.equals("Admin")) {
            setPlannedHours(
                    (int) Math.round(2 * course.getHp() + 28 + 0.2 * course.getStudentCount()));
        } else {
            setPlannedHours(plannedHours);
        }
    }

    /**
     * @param plannedHours the planned_hours attribute of the planned_activity table
     */
    public void setPlannedHours(int plannedHours) {
        this.plannedHours = Objects.requireNonNull(plannedHours);
    }

    public void setTeachingActivityFactor(double multiplicationFactor) {
        this.multiplicationFactor = Objects.requireNonNull(multiplicationFactor);
    }

    public int getId() {
        return id;
    }

    public int getCourseSurrogateId() {
        return course.getSurrogateId();
    }

    public int getCourseStudyYear() {
        return course.getStudyYear();
    }

    public StudyPeriod getCourseStudyPeriod() {
        return course.getStudyPeriod();
    }

    public String getCourseName() {
        return course.getCourseName();
    }

    public String getActivityName() {
        return activityName;
    }

    public int getPlannedHours() {
        return plannedHours;
    }

    public double getTotalHours(int hours) {
        return hours * multiplicationFactor;
    }
}
