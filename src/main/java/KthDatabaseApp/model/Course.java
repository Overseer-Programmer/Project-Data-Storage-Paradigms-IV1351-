package KthDatabaseApp.model;

public class Course implements CourseDTO {
    private final int surrogateId; // The id field of course_instance
    private final int courseLayoutId;
    private final String instanceId; // The instance_id field of course_instance
    private final String courseCode;
    private String courseName;
    private int numStudents;
    private int studyYear;
    private StudyPeriod studyPeriod;
    private double hp;
    private int minStudents;
    private int maxStudents;
    private List<TeacherAllocation> teacherAllocations;

    /**
     * Creates a course object that has all fields from both the course_instance and
     * course_layout relations of the conceptual model. The course object is
     * identified
     * with it's surrogateId. All planned activities that belong to the course are
     * also stored.
     * 
     * @param surrogateId    The id field of course_instance
     * @param courseLayoutId The course_layout_id field of course_layout
     * @param instanceId     The instance_id field of course_instance
     * @param courseCode     The course_code field of course_layout
     * @param courseName     The course_name field of course_layout
     * @param numStudents    The num_students field of course_instance
     * @param studyYear      The study_year field of course_instance
     * @param studyPeriod    The study_period field of course_instance
     * @param hp             The hp field of course_layout
     * @param minStudents    The min_students field of course_layout
     * @param maxStudents    The max_students field of course_layout
     * @throws InvalidRangeException
     */
    public Course(
        int surrogateId,
        int courseLayoutId,
        String instanceId,
        String courseCode,
        String courseName,
        int numStudents,
        int studyYear,
        StudyPeriod studyPeriod,
        double hp,
        int minStudents,
        int maxStudents
    ) throws InvalidRangeException {
        this.surrogateId = surrogateId;
        this.courseLayoutId = courseLayoutId;
        this.instanceId = instanceId;
        this.courseCode = courseCode;
        setStudentCount(numStudents);
        setCourseName(courseName);
        setStudyTime(studyYear, studyPeriod);
        setHp(hp);
        setStudentRange(minStudents, maxStudents);
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

    /**
     * Set the max and min amount of students the course can have.
     * @param maxStudents
     * @param minStudents
     * @throws InvalidRangeException
     */
    public void setStudentRange(int minStudents, int maxStudents) throws InvalidRangeException {
        if (maxStudents < minStudents) {
            throw new InvalidRangeException("The max student count must be equal or larger than the min count");
        }
        this.minStudents = minStudents;
        this.maxStudents = maxStudents;
    }

    // Getters
    public int getSurrogateId() {
        return surrogateId;
    }

    public int getCourseLayoutId() {
        return courseLayoutId;
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

    public int getMinStudents() {
        return minStudents;
    }

    public int getMaxStudents() {
        return maxStudents;
    }
}
