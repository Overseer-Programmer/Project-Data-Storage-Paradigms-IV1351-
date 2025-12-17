package KthDatabaseApp.model;

import java.util.List;

public interface CourseDTO {
    /**
     * @return the surrogate "id" Primary key for the course_instance table
     * (Not the instance_id field).
     */
    public int getSurrogateId();
    /**
     * @return the instance_id field of the course_instance table (Not the id field).
     */
    public String getInstanceId();
    public String getCourseName();
    public String getCourseCode();
    public int getStudentCount();
    public int getStudyYear();
    public StudyPeriod getStudyPeriod();
    public double getHp();
    public int getMinStudents();
    public int getMaxStudents();
    public int getCourseLayoutId();
}
