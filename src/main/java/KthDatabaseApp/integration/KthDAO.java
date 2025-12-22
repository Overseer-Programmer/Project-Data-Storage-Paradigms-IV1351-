package KthDatabaseApp.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection; // help us connect to the database
import java.sql.DriverManager; // help us manage the connection
import java.sql.PreparedStatement; // help us create prepared statements
import java.sql.ResultSet;
import java.sql.SQLException; // help us handle SQL exceptions
import java.util.ArrayList;
import java.util.List;

import KthDatabaseApp.model.*;

public class KthDAO {
    public final String database_name = "kth_group15";

    // connection DB
    private final String url = "jdbc:postgresql://localhost:5432/" + database_name;
    private Connection connection;

    // A PreparedStatement is a precompiled SQL statement
    private PreparedStatement findCourseStatement;
    private PreparedStatement findCourseLockingStatement;
    private PreparedStatement findTeacherStatement;
    private PreparedStatement findPlannedActivityStatement;
    private PreparedStatement findAllCoursesStatement;
    private PreparedStatement findAllTeachersStatement;
    private PreparedStatement findAllPlannedActivitiesStatement;
    private PreparedStatement getPlannedActivitiesForTeacherStatement;

    // Task 1 Compute teaching cost
    private PreparedStatement getTeachingCostStatement;

    // Task 2 Modify course instance
    private PreparedStatement updateStudentsInCourseStatement;

    // Task 3 Teacher allocation
    private PreparedStatement allocateTeacherStatement;
    private PreparedStatement deallocateTeacherStatement;

    // Task 4 Create teaching activity and read results from it's creation
    private PreparedStatement createTeachingActivityStatement;
    private PreparedStatement findTeachersAllocatedToTeachingActivityStatement;

    public void connectToDatabase(String dbUsername, String dbUserPassword) throws DBException {
        try {
            connectToDB(dbUsername, dbUserPassword);
            prepareStatements();
        } catch (ClassNotFoundException | SQLException | IOException e) {
            throw new DBException("failed to connect to database", e);
        }
    }

    public void commit() throws DBException {
        try {
            connection.commit();
        } catch (SQLException e) {
            handleException("commit failed", e);
        }
    }

    /**
     * Finds a course and all its related information.
     * 
     * @param courseInstanceId The id column of the course_instance relation which
     *                         you want to gather information from.
     * @param lockExclusive    Whether an exclusive lock should be placed the table
     *                         rows selected from the database to obtain this
     *                         course.
     *                         This will also stop the transaction from committing,
     *                         requiring it to be committed elsewhere.
     * @return a Course object or null if it was not found.
     * @throws DBException
     */
    public Course findCourse(int courseInstanceId, boolean lockExclusive) throws DBException {
        final String failureMessage = "Could not find course";
        Course course = null;
        try {
            course = findCourseInternal(courseInstanceId, lockExclusive);
            if (!lockExclusive) {
                connection.commit();
            }
        } catch (SQLException | BusinessConstraintException e) {
            handleException(failureMessage, e);
        }

        return course;
    }

    /**
     * Finds a teacher using their employee id.
     * 
     * @param employeeId The employeeId from the employee table
     * @return A Teacher object or null if it does not exist
     * @throws DBException
     */
    public Teacher findTeacher(int employeeId) throws DBException {
        final String failureMessage = "Could not find teacher";
        Teacher teacher = null;
        try {
            teacher = findTeacherInternal(employeeId);
            connection.commit();
        } catch (SQLException | InvalidRangeException | BusinessConstraintException e) {
            handleException(failureMessage, e);
        }

        return teacher;
    }

    /**
     * Finds a planned activity from the planned_activity table
     * 
     * @param plannedActivityId The id of the planned activity to find
     * @return A PlannedActivity object or null if it does not exist
     * @throws DBException
     */
    public PlannedActivity findPlannedActivity(int plannedActivityId) throws DBException {
        final String failureMessage = "Could not find planned activity";
        PlannedActivity plannedActivity = null;
        try {
            plannedActivity = findPlannedActivityInternal(plannedActivityId);
            connection.commit();
        } catch (SQLException | BusinessConstraintException e) {
            handleException(failureMessage, e);
        }
        return plannedActivity;
    }

    public List<Course> findAllCourses() throws DBException {
        final String failureMessage = "Could not find courses";
        List<Course> courses = new ArrayList<>();
        ResultSet result = null;
        try {
            result = findAllCoursesStatement.executeQuery();
            while (result.next()) {
                Course course = findCourseInternal(result.getInt("id"), false);
                if (course != null) {
                    courses.add(course);
                }
            }
            connection.commit();
        } catch (SQLException | BusinessConstraintException e) {
            handleException(failureMessage, e);
        } finally {
            closeResultSet(failureMessage, result);
        }
        return courses;
    }

    public List<Teacher> findAllTeachers() throws DBException {
        final String failureMessage = "Could not find teachers";
        List<Teacher> teachers = new ArrayList<>();
        ResultSet result = null;
        try {
            result = findAllTeachersStatement.executeQuery();
            while (result.next()) {
                Teacher teacher = findTeacherInternal(result.getInt("id"));
                if (teacher != null) {
                    teachers.add(teacher);
                }
            }
            connection.commit();
        } catch (SQLException | InvalidRangeException | BusinessConstraintException e) {
            handleException(failureMessage, e);
        } finally {
            closeResultSet(failureMessage, result);
        }
        return teachers;
    }

    public List<PlannedActivity> findAllPlannedActivities() throws DBException {
        final String failureMessage = "Could not find planned activities";
        List<PlannedActivity> plannedActivities = new ArrayList<>();
        ResultSet result = null;
        try {
            result = findAllPlannedActivitiesStatement.executeQuery();
            while (result.next()) {
                int plannedActivityId = result.getInt("id");
                PlannedActivity plannedActivity = findPlannedActivityInternal(plannedActivityId);
                if (plannedActivity != null) {
                    plannedActivities.add(plannedActivity);
                }
            }
            connection.commit();
        } catch (SQLException | BusinessConstraintException e) {
            handleException(failureMessage, e);
        } finally {
            closeResultSet(failureMessage, result);
        }
        return plannedActivities;
    }

    /**
     * Finds the teaching cost for the specified course.
     * 
     * @param courseInstanceId The course_instance_id of course
     * @return A TeachingCostDTO object unless an exception was thrown.
     * @throws DBException
     */
    public TeachingCostDTO findTeachingCost(int courseInstanceId) throws DBException {
        final String failureMessage = "Could not find teaching cost";
        ResultSet result = null;
        TeachingCostDTO teachingCost = null;
        try {
            getTeachingCostStatement.setInt(1, courseInstanceId);
            result = getTeachingCostStatement.executeQuery();
            boolean courseExists = result.next();
            if (!courseExists) {
                throw new DBException(String.format("Course with course_instance_id=%d not found", courseInstanceId));
            }
            teachingCost = new TeachingCostDTO(
                    result.getString("course_code"),
                    result.getString("instance_id"),
                    StudyPeriod.valueOf(result.getString("study_period")),
                    result.getInt("planned_cost"),
                    result.getInt("actual_cost"));
            connection.commit();
        } catch (SQLException e) {
            handleException(failureMessage, e);
        } finally {
            closeResultSet(failureMessage, result);
        }
        return teachingCost;
    }

    /**
     * Returns a list of all teachers who are allocated to planned activities of
     * with the specified teaching activity name.
     * 
     * @param activityName
     * @return
     * @throws DBException
     */
    public List<Teacher> findTeachersAllocatedToTeachingActivity(String activityName) throws DBException {
        final String failureMessage = "Could not find teachers allocated to teaching activity: " + activityName;
        List<Teacher> teachers = new ArrayList<>();
        ResultSet result = null;
        try {
            findTeachersAllocatedToTeachingActivityStatement.setString(1, activityName);
            result = findTeachersAllocatedToTeachingActivityStatement.executeQuery();
            while (result.next()) {
                Teacher teacher = findTeacherInternal(result.getInt("employee_id"));
                if (teacher != null) {
                    teachers.add(teacher);
                }
            }
            connection.commit();
        } catch (SQLException | BusinessConstraintException | InvalidRangeException e) {
            handleException(failureMessage, e);
        } finally {
            closeResultSet(failureMessage, result);
        }
        return teachers;
    }

    /**
     * Creates an allocation in the employee_planned_activity table for the database
     * to allocate a teacher to a planned_activity.
     * 
     * @param employeeId The id of the employee to allocate.
     * @param plannedActivityId The id of the planned activity to allocate to the employee
     * @param allocatedHours The amount of hours to allocate the employee to the planned activity.
     * @throws DBException
     */
    public void createAllocationForTeacher(int employeeId, int plannedActivityId, int allocatedHours)
            throws DBException {
        final String failureMessage = "Could not create teacher allocation";
        try {
            allocateTeacherStatement.setInt(1, employeeId);
            allocateTeacherStatement.setInt(2, plannedActivityId);
            allocateTeacherStatement.setInt(3, allocatedHours);
            allocateTeacherStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            handleException(failureMessage, e);
        }
    }

    /**
     * Deletes an allocation in the employee_planned_activity for the database to
     * deallocate a teacher from a planned_activity.
     * 
     * @param employeeId The employee to deallocate.
     * @param plannedActivityId The planned activity to deallocate the employee from.
     * @throws DBException
     */
    public void deleteAllocationFromTeacher(int employeeId, int plannedActivityId) throws DBException {
        final String failureMessage = "Could not create teacher allocation";
        try {
            deallocateTeacherStatement.setInt(1, employeeId);
            deallocateTeacherStatement.setInt(2, plannedActivityId);
            deallocateTeacherStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            handleException(failureMessage, e);
        }
    }

    /**
     * Takes the updated course object and writes the new student count to the
     * database.
     * 
     * @param course
     * @throws DBException
     */
    public void updateStudentsForCourse(CourseDTO course) throws DBException {
        final String failureMessage = "Could not update number of students in course";
        try {
            updateStudentsInCourseStatement.setInt(1, course.getStudentCount());
            updateStudentsInCourseStatement.setInt(2, course.getSurrogateId());
            updateStudentsInCourseStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            handleException(failureMessage, e);
        }
    }

    /**
     * Creates a teaching activity based on the specified TeachingActivity object
     * and it's properties.
     * 
     * @param teachingActivity
     * @throws DBException
     */
    public void createTeachingActivity(TeachingActivityDTO teachingActivity) throws DBException {
        final String failureMessage = "Could not create teaching activity";
        try {
            createTeachingActivityStatement.setString(1, teachingActivity.getActivityName());
            createTeachingActivityStatement.setDouble(2, teachingActivity.getMultiplicationFactor());
            createTeachingActivityStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            handleException(failureMessage, e);
        }
    }

    private Course findCourseInternal(int courseInstanceId, boolean lockExclusive)
            throws SQLException, BusinessConstraintException {
        ResultSet result = null;
        Course course = null;
        try {
            PreparedStatement targetStatement;
            if (lockExclusive) {
                targetStatement = findCourseLockingStatement;
            } else {
                targetStatement = findCourseStatement;
            }
            targetStatement.setInt(1, courseInstanceId);
            result = targetStatement.executeQuery();
            boolean exists = result.next();
            if (!exists) {
                return null;
            }

            // Get all attributes from course_instance and course_layout
            course = new Course(
                    courseInstanceId,
                    result.getInt("course_layout_id"),
                    result.getString("instance_id"),
                    result.getString("course_code"),
                    result.getString("course_name"),
                    result.getInt("num_students"),
                    result.getInt("study_year"),
                    StudyPeriod.valueOf(result.getString("study_period")),
                    result.getDouble("hp"),
                    result.getInt("min_students"),
                    result.getInt("max_students"));
        } finally {
            if (result != null) {
                result.close();
            }
        }

        return course;
    }

    private Teacher findTeacherInternal(int employeeId)
            throws SQLException, DBException, InvalidRangeException, BusinessConstraintException {
        ResultSet result = null;
        Teacher teacher = null;

        // Get the teacher attributes
        try {
            findTeacherStatement.setInt(1, employeeId);
            result = findTeacherStatement.executeQuery();
            boolean exists = result.next();
            if (!exists) {
                return null;
            }
            teacher = new Teacher(
                    employeeId,
                    result.getString("first_name"),
                    result.getString("last_name"),
                    result.getString("street"),
                    result.getString("zip"),
                    result.getString("city"),
                    result.getInt("salary"));
        } finally {
            if (result != null) {
                result.close();
            }
        }

        // Find all planned activities allocated to the teacher
        try {

            getPlannedActivitiesForTeacherStatement.setInt(1, employeeId);
            result = getPlannedActivitiesForTeacherStatement.executeQuery();
            while (result.next()) {
                int plannedActivityId = result.getInt("planned_activity_id");
                int allocatedHours = result.getInt("allocated_hours");
                PlannedActivity plannedActivity = findPlannedActivityInternal(plannedActivityId);
                if (plannedActivity != null) {
                    teacher.allocatePlannedActivity(plannedActivity, allocatedHours);
                }
            }
        } finally {
            if (result != null) {
                result.close();
            }
        }

        return teacher;
    }

    private PlannedActivity findPlannedActivityInternal(int plannedActivityId)
            throws SQLException, DBException, BusinessConstraintException {
        ResultSet result = null;
        PlannedActivity plannedActivity = null;
        try {
            // Find the planned activity
            findPlannedActivityStatement.setInt(1, plannedActivityId);
            result = findPlannedActivityStatement.executeQuery();
            boolean exists = result.next();
            if (!exists) {
                return null;
            }
            String activityName = result.getString("activity_name");
            double multiplicationFactor = result.getDouble("factor");
            int plannedHours = result.getInt("planned_hours");
            int courseInstanceId = result.getInt("course_instance_id");
            Course course = findCourseInternal(courseInstanceId, false); // Find the associated course
            if (course == null) {
                throw new DBException("Invalid State: planned activity is missing a course instance.");
            }
            plannedActivity = new PlannedActivity(plannedActivityId, course, activityName, multiplicationFactor);

            // Set the planned hours for derived and non derived teaching activity types
            if (activityName.equals("Examination")) {
                plannedActivity.setPlannedHours(
                        (int) Math.round(32 + 0.725 * course.getStudentCount()));
            } else if (activityName.equals("Admin")) {
                plannedActivity.setPlannedHours(
                        (int) Math.round(2 * course.getHp() + 28 + 0.2 * course.getStudentCount()));
            } else {
                plannedActivity.setPlannedHours(plannedHours);
            }
        } finally {
            if (result != null) {
                result.close();
            }
        }

        return plannedActivity;
    }

    // all SQL commands goes here
    private void prepareStatements() throws SQLException, IOException {
        String findCourseSql = Files.readString(
                Path.of("ApplicationQueries/FindCourse.sql"));
        findCourseStatement = connection.prepareStatement(findCourseSql);
        findCourseLockingStatement = connection.prepareStatement(findCourseSql + " FOR UPDATE");
        findTeacherStatement = connection.prepareStatement(
                Files.readString(Path.of("ApplicationQueries/FindTeacher.sql")));
        findPlannedActivityStatement = connection.prepareStatement(
                Files.readString(Path.of("ApplicationQueries/FindPlannedActivity.sql")));
        findAllCoursesStatement = connection.prepareStatement(
                Files.readString(Path.of("ApplicationQueries/FindAllCourses.sql")));
        findAllTeachersStatement = connection.prepareStatement(
                Files.readString(Path.of("ApplicationQueries/FindAllTeachers.sql")));
        findAllPlannedActivitiesStatement = connection.prepareStatement(
                Files.readString(Path.of("ApplicationQueries/FindAllPlannedActivities.sql")));
        updateStudentsInCourseStatement = connection.prepareStatement(
                Files.readString(Path.of("ApplicationQueries/UpdateStudentsInCourse.sql")));
        getPlannedActivitiesForTeacherStatement = connection.prepareStatement(
                Files.readString(Path.of("ApplicationQueries/GetPlannedActivitiesForTeacher.sql")));
        getTeachingCostStatement = connection.prepareStatement(
                Files.readString(Path.of("ApplicationQueries/GetTeachingCost.sql")));
        allocateTeacherStatement = connection.prepareStatement(
                Files.readString(Path.of("ApplicationQueries/AllocateTeacher.sql")));
        deallocateTeacherStatement = connection.prepareStatement(
                Files.readString(Path.of("ApplicationQueries/DeallocateTeacher.sql")));
        createTeachingActivityStatement = connection.prepareStatement(
                Files.readString(Path.of("ApplicationQueries/CreateTeachingActivity.sql")));
        findTeachersAllocatedToTeachingActivityStatement = connection.prepareStatement(
                Files.readString(Path.of("ApplicationQueries/FindTeachersAllocatedToTeachingActivity.sql")));
    }

    private void connectToDB(String user, String password) throws ClassNotFoundException, SQLException {
        connection = DriverManager.getConnection(url, user, password);
        connection.setAutoCommit(false); // disable auto-commit for transaction management
    }

    private void handleException(String message, Exception cause) throws DBException {
        String failureMsg = message;
        try {
            connection.rollback();
        } catch (SQLException rollback) {
            failureMsg += "also failed to rollback transaction because of: " + rollback.getMessage();
        }

        // if the problem is something else, we wrap it in a DBException and throw it
        // further
        if (cause != null) {
            throw new DBException(failureMsg, cause);
        } else {
            throw new DBException(failureMsg);
        }
    }

    private void closeResultSet(String failureMsg, ResultSet result) throws DBException {
        try {
            if (result != null) {
                result.close();
            }
        } catch (Exception e) {
            throw new DBException(failureMsg + " Could not close result set.", e);
        }
    }
}
