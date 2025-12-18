package KthDatabaseApp.model;

public class PlannedActivity implements PlannedActivityDTO {
    private final int id;
    private final CourseDTO course; // A reference to the course object the planned activity belongs to
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
     */
    public PlannedActivity(int id, CourseDTO course, String activityName, double multiplicationFactor) {
        this.id = id;
        this.course = course;
        this.activityName = activityName;
        setTeachingActivityFactor(multiplicationFactor);
    }

    public PlannedActivity(CourseDTO course, String activityName, double multiplicationFactor) {
        this.id = -1; // id does not exist here
        this.course = course;
        this.activityName = activityName;
        setTeachingActivityFactor(multiplicationFactor);
    }

    /**
     * @param plannedHours the planned_hours attribute of the planned_activity table
     */
    public void setPlannedHours(int plannedHours) {
        this.plannedHours = plannedHours;
    }

    public void setTeachingActivityFactor(double multiplicationFactor) {
        this.multiplicationFactor = multiplicationFactor;
    }

    public int getId() {
        return id;
    }

    public CourseDTO getCourse() {
        return course;
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
