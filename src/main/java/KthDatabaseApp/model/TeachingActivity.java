package KthDatabaseApp.model;

import java.util.Objects;

public class TeachingActivity implements TeachingActivityDTO {
    private final String activityName;
    private double multiplicationFactor;

    public TeachingActivity(String activityName, double multiplicationFactor) {
        this.activityName = Objects.requireNonNull(activityName);
        setMultiplicationFactor(multiplicationFactor);
    }

    public void setMultiplicationFactor(double multiplicationFactor) {
        this.multiplicationFactor = Objects.requireNonNull(multiplicationFactor);
    }

    public String getActivityName() {
        return activityName;
    }

    public double getMultiplicationFactor() {
        return multiplicationFactor;
    }
}
