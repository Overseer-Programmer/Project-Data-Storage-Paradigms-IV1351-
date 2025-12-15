package KthDatabaseApp.model;

import java.util.List;

public class TeachingActivity implements TeachingActivityDTO {
    private final String activityName;
    private int multiplicationFactor;

    public TeachingActivity(String activityName, int multiplicationFactor, List<Course> allCourses) throws BusinessConstraintException {
        this.activityName = activityName;
        setMultiplicationFactor(multiplicationFactor);

        // Satisfy "Exercise" teaching activity business constraint
        if (activityName == "Exercise") {
            // if a valid course is not found, throw BusinessConstraintException
        }
    }

    public void setMultiplicationFactor(int multiplicationFactor) {
        this.multiplicationFactor = multiplicationFactor;
    }

    public String getActivityName() {
        return activityName;
    }

    public int getMultiplicationFactor() {
        return multiplicationFactor;
    }
}
