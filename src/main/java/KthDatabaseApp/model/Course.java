package KthDatabaseApp.model;


public class Course implements CourseDTO {
    public final int surrogateId; // The id field of course_instance
    public final String instanceId; // The instance_id field of course_instance
    public final String courseCode;
    private String courseName;
    private int numStudents;
    private int studyYear;
    private StudyPeriod studyPeriod;
    private double hp;

    /**
     * Creates a course object that has fields from both the course_instance and
     * course_layout
     * relations of the conceptual model. The course object is identified with it's
     * surrogateId.
     * 
     * @param surrogateId The id field of course_instance
     * @param instanceId  The instance_id field of course_instance
     * @param courseCode  The course_code field of course_layout
     * @param courseName  The course_name field of course_layout
     * @param numStudents The num_students field of course_instance
     * @param studyYear   The study_year field of course_instance
     * @param studyPeriod The study_period field of course_instance
     * @param hp          The hp field of course_layout
     */
    public Course(
        int surrogateId,
        String instanceId,
        String courseCode,
        String courseName,
        int numStudents,
        int studyYear,
        StudyPeriod studyPeriod,
        double hp
    ) {
        this.surrogateId = surrogateId;
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
    public int getSurrogateId() {
        return surrogateId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getCourseCode() {
        return courseCode;
    }

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
