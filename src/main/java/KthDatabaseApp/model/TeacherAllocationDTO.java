package KthDatabaseApp.model;

public interface TeacherAllocationDTO {
    public int getPlannedActivityId();
    public int getPlannedHours();
    public String getActivityName();
    public int getTeacherId();
    public String getTeacherFullName();
    public int getTeacherSalary();
    public int getAllocatedHours();
    public int getAllocatedCourseSurrogateId();
    public int getAllocatedCourseStudyYear();
    public StudyPeriod getAllocatedCourseStudyPeriod();
    public String getAllocatedCourseName();
}
