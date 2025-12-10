package KthDatabaseApp.model;

public interface PlannedActivityDTO {
    public int getId();
    public Course getCourse();
    public String getActivityName();

    /**
     * @return the planned hours without multiplication factor.
     */
    public int getPlannedHours();

    /**
     * Gets the total hours that a given amount of hours for the planned activity
     * would result in when accounting for the multiplication factor.
     * 
     * @param hours
     * @return
     */
    public double getTotalHours(int hours);
}
