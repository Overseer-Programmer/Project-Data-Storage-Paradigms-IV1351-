package KthDatabaseApp.model;

import java.util.List;

public class TeachingActivity implements TeachingActivityDTO {
    private final String activityName;
    private int multiplicationFactor;
    private List<PlannedActivityDTO> plannedActivities;
    private List<Course> allCourses;
    //private PlannedActivity plannedActivity;

    // lista av alla planned activity

    public TeachingActivity(String activityName, int multiplicationFactor, List <PlannedActivityDTO> plannedActivities ,List<Course> allCourses) throws BusinessConstraintException {
        this.activityName = activityName;
        setMultiplicationFactor(multiplicationFactor);
        boolean foundExercise = false;
        boolean teacherAllocation = false;

        this.plannedActivities = plannedActivities;
        this.allCourses = allCourses;

      
       

        if (activityName.equals("Exercise") && plannedActivities == null || plannedActivities.isEmpty) {           

            Course course = findCourseWithAtLeastOneTeacher(allCourses);
            
            if(course != null){

                plannedActiviti.add(PlannedActivity(course,activityName, multiplicationFactor));
                
            }else{
                throw new BusinessConstraintException("teaching activity Exercise must be associated with at least one course layout/instance and must have one teacher allocated for the same");
            }

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

    private boolean associatedToAnyCourse(List<Course> allCourses){

        for (Course course : allCourses) {

            for (PlannedActivityDTO plannedActivity : course.getPlannedActivities()) {

                if (activityName.equals(plannedActivity.getActivityName())) {

                  return true;

                }
            }
        }
        return false;

    }


    private  Course findCourseWithAtLeastOneTeacher(List<Course> allCourses){

        for (Course course : allCourses) {

            for (PlannedActivityDTO plannedActivity : course.getPlannedActivities()) {

                 if(plannedActivity.getTeacherAllocations().isEmpty()) {

                    return course;
                }
            }
        }
    }
}
