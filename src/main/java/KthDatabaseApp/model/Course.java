package KthDatabaseApp.model;


public class Course {
    public final int courseInstanceId; // The id field of course_instance
    public final String instanceId; // The instance_id field of course_instance
    public final String courseCode;
    private String courseName;
    private int numStudents;
    private int studyYear;
    private StudyPeriod studyPeriod;
    private double hp;

    public Course(
        int courseInstanceId,
        String instanceId,
        String courseCode,
        String courseName,
        int numStudents,
        int studyYear,
        StudyPeriod studyPeriod,
        double hp
    ) {
        this.courseInstanceId = courseInstanceId;
        this.instanceId = instanceId;
        this.courseCode = courseCode;
    }

    // Setters
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public void setStudentCount(int newCount) {
        numStudents = newCount;
    }

    public void setStudyTime(int studyYear, StudyPeriod studyPeriod) {
        this.studyYear = studyYear;
        this.studyPeriod = studyPeriod;
    }

    public void setHp(double hp) {
        this.hp = hp;
    }

    // Getters
    public String getCourseName() {
        return courseName;
    }

    public int getStudentCount() {
        return numStudents;
    }

    public int getStudyYear() {
        return studyYear;
    }

    public StudyPeriod getStudyPeriod() {
        return studyPeriod;
    }

    public double getHp() {
        return hp;
    }
}
