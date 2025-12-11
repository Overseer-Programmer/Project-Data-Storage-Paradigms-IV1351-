package KthDatabaseApp.model;

public class TeachingCostDTO {
    public final String courseCode;
    public final String instanceId;
    public final StudyPeriod studyPeriod;
    public final long plannedCost;
    public final long actualCost;

    public TeachingCostDTO(
        String courseCode,
        String instanceId,
        StudyPeriod studyPeriod,
        long plannedCost,
        long actualCost
    ) {
        this.courseCode = courseCode;
        this.instanceId = instanceId;
        this.studyPeriod = studyPeriod;
        this.plannedCost = plannedCost;
        this.actualCost = actualCost;
    }

    
}
