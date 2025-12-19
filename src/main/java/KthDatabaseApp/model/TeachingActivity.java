package KthDatabaseApp.model;

public class TeachingActivity implements TeachingActivityDTO {
    private final String activityName;
    private double multiplicationFactor;

    public TeachingActivity(String activityName, double multiplicationFactor) {
        this.activityName = activityName;
        setMultiplicationFactor(multiplicationFactor);
    }

    public void setMultiplicationFactor(double multiplicationFactor) {
        this.multiplicationFactor = multiplicationFactor;
    }

    public String getActivityName() {
        return activityName;
    }

    public double getMultiplicationFactor() {
        return multiplicationFactor;
    }
}
