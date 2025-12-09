package KthDatabaseApp.model;

public interface PlannedActivityDTO {
    public int getId();
    public Course getCourse();
    public String getActivityName();
    
    /**
     * @return the total planned hours with the multiplication factor.
     */
    public int getTotalPlannedHours();
}
