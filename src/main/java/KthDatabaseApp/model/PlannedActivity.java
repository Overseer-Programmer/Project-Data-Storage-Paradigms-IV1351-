package KthDatabaseApp.model;


public class PlannedActivity implements PlannedActivityDTO {
    private final int id;
    private final String activityName;
    private int plannedHours;

    public PlannedActivity(int id, String activityName) {
        this.id = id;
        this.activityName = activityName;
    }

    public void setPlannedHours(int plannedHours) {
        this.plannedHours = plannedHours;
    }

    public int getId() {
        return id;
    }

    public String getActivityName() {
        return activityName;
    }

    public int getPlannedHours() {
        return plannedHours;
    }
}
