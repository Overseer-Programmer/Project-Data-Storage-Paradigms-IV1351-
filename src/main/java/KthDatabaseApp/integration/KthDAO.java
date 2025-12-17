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

    // a PreparedStatement is a precompiled SQL statement
    private PreparedStatement findCourseStatement;
    private PreparedStatement findTeacherStatement;
    private PreparedStatement findPlannedActivityStatement;
    private PreparedStatement findAllTeachersStatement;
    private PreparedStatement findAllPlannedActivitiesStatement;
    private PreparedStatement getPlannedActivitiesForTeacherStatement;

    // Task 1 Compute teaching cost
    private PreparedStatement getPlannedTeachingCostStatement;
    private PreparedStatement getActualTeachingCostStatement;

    // Task 2 modify course instance
    private PreparedStatement UpdateStudentsInCourseStatement;

    // Task 3 teacher allocation
    private PreparedStatement getPlannedActivitiesForTeacherLockingStatement;
    private PreparedStatement allocateTeacherStatement;
    private PreparedStatement deallocateTeacherStatement;

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
     * @param courseId The id column of the course_instance relation which
     *                 you want to gather information from.
     * @return a Course object or null if it was not found.
     * @throws DBException
     */
    public Course findCourse(int courseInstanceId) throws DBException {
        final String failureMessage = "Could not find course";
        Course course = null;
        try {
            course = findCourseInternal(courseInstanceId);
            connection.commit();
        } catch (SQLException | InvalidRangeException e) {
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

    public PlannedActivity findPlannedActivity(int plannedActivityId) throws DBException {
        final String failureMessage = "Could not find planned activity";
        PlannedActivity plannedActivity = null;
        try {
            plannedActivity = findPlannedActivityInternal(plannedActivityId);
            connection.commit();
        } catch (SQLException | InvalidRangeException e) {
            handleException(failureMessage, e);
        }
        return plannedActivity;
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
                int plannedActivityId = result.getInt("planned_activity_id");
                PlannedActivity plannedActivity = findPlannedActivityInternal(plannedActivityId);
                if (plannedActivity != null) {
                    plannedActivities.add(plannedActivity);
                }
            }
            connection.commit();
        } catch (SQLException | InvalidRangeException e) {
            handleException(failureMessage, e);
        } finally {
            closeResultSet(failureMessage, result);
        }
        return plannedActivities;
    }

    /**
     * Executes teaching cost queries to obtain the teaching cost for the specified
     * course.
     * 
     * @param course
     * @return A TeachingCostDTO object unless an exception was thrown.
     * @throws DBException
     */
    public TeachingCostDTO findTeachingCost(Course course) throws DBException {
        final String failureMessage = "Could not find teaching cost";
        ResultSet plannedTeachingCostResult = null;
        ResultSet actualTeachingCostResult = null;
        TeachingCostDTO teachingCost = null;
        try {
            getPlannedTeachingCostStatement.setInt(1, course.getSurrogateId());
            plannedTeachingCostResult = getActualTeachingCostStatement.executeQuery();
            getActualTeachingCostStatement.setInt(1, course.getSurrogateId());
            actualTeachingCostResult = getActualTeachingCostStatement.executeQuery();
            teachingCost = new TeachingCostDTO(
                course.getCourseCode(),
                course.getInstanceId(),
                course.getStudyPeriod(),
                plannedTeachingCostResult.getInt("cost"),
                actualTeachingCostResult.getInt("cost")
            );
            connection.commit();
        } catch (SQLException e) {
            handleException(failureMessage, e);
        } finally {
            closeResultSet(failureMessage, plannedTeachingCostResult);
            closeResultSet(failureMessage, actualTeachingCostResult);
        }
        return teachingCost;
    }

    /**
     * Updates the allocation for a teacher on the database to match the allocation
     * of the specified teacher object. Will acquire an exclusive lock on the
     * current allocated rows for the teacher in the database. Note that this method
     * only supports inserting and deleting allocations for the teacher, it does not
     * support updating the allocated hours of existing allocations.
     * 
     * @param teacher the updated teacher object to read from.
     * @throws DBException
     */
    public void updateAllocationForTeacher(TeacherDTO teacher) throws DBException {
        final String failureMessage = "Could not update teacher allocation";
        ResultSet result = null;
        try {
            getPlannedActivitiesForTeacherLockingStatement.setInt(1, teacher.getEmployeeId());
            result = getPlannedActivitiesForTeacherLockingStatement.executeQuery();
            List<TeacherAllocation> updatedAllocations = teacher.getTeachingAllocations();
            List<TeacherAllocation> newAllocations = new ArrayList<>(updatedAllocations);
            while (result.next()) {
                int currentPlannedActivityId = result.getInt("plannedActivityId");

                // Deallocate from all removed allocations
                boolean plannedActivityFound = false;
                for (TeacherAllocation allocation : updatedAllocations) {
                    if (allocation.plannedActivity.getId() == currentPlannedActivityId) {
                        plannedActivityFound = true;
                        break;
                    }
                }
                if (!plannedActivityFound) {
                    deallocatePlannedActivityFromTeacher(teacher.getEmployeeId(), currentPlannedActivityId);
                }

                // Remove already existing allocations from the list of new allocations
                for (TeacherAllocation allocation : newAllocations) {
                    if (allocation.plannedActivity.getId() == currentPlannedActivityId) {
                        newAllocations.remove(allocation);
                        break;
                    }
                }
            }

            // Add the actual new allocations to the database
            for (TeacherAllocation allocation : newAllocations) {
                allocatePlannedActivityToTeacher(teacher.getEmployeeId(), allocation.plannedActivity.getId(),
                        allocation.allocatedHours);
            }

            connection.commit();
        } catch (SQLException ex) {
            handleException(failureMessage, ex);
        } finally {
            closeResultSet(failureMessage, result);
        }
    }

    public void addStudentsToCourse(int courseId, int newNumStudents) throws DBException {
        final String failureMessage = "Could not update number of students in course";
        try {
            UpdateStudentsInCourseStatement.setInt(1, newNumStudents);
            UpdateStudentsInCourseStatement.setInt(2, courseId);
            UpdateStudentsInCourseStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            handleException(failureMessage, e);
        }
    }

    private Course findCourseInternal(int courseInstanceId)
            throws InvalidRangeException, SQLException {
        ResultSet result = null;
        Course course = null;
        try {
            findCourseStatement.setInt(1, courseInstanceId);
            result = findCourseStatement.executeQuery();
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
            result.close();
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
            result.close();
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
            result.close();
        }

        return teacher;
    }

    private PlannedActivity findPlannedActivityInternal(int plannedActivityId)
            throws SQLException, DBException, InvalidRangeException {
        // Find the associated course
        Course course = findCourseInternal(plannedActivityId);
        if (course == null) {
            throw new DBException("Invalid State: planned activity is missing a course instance.");
        }

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
            int plannedHours = result.getInt("plannedHours");
            plannedActivity = new PlannedActivity(plannedActivityId, course, activityName, multiplicationFactor);

            // Set the planned hours for derived and non derived teaching activity types
            if (activityName.equals("Examination")) {
                plannedActivity.setPlannedHours((int) Math.round(32 + 0.725 * course.getStudentCount()));
            } else if (activityName.equals("Admin")) {
                plannedActivity
                        .setPlannedHours((int) Math.round(2 * course.getHp() + 28 + 0.2 * course.getStudentCount()));
            } else {
                plannedActivity.setPlannedHours(plannedHours);
            }
        } finally {
            result.close();
        }

        return plannedActivity;
    }

    private void deallocatePlannedActivityFromTeacher(int employeeId, int plannedActivityId) throws SQLException {
        deallocateTeacherStatement.setInt(1, employeeId);
        deallocateTeacherStatement.setInt(2, plannedActivityId);
        deallocateTeacherStatement.executeUpdate();
    }

    private void allocatePlannedActivityToTeacher(int employeeId, int plannedActivityId, int allocatedHours)
            throws SQLException {
        allocateTeacherStatement.setInt(1, employeeId);
        allocateTeacherStatement.setInt(2, plannedActivityId);
        allocateTeacherStatement.setInt(3, allocatedHours);
        allocateTeacherStatement.executeUpdate();
    }

    // all SQL commands goes here
    private void prepareStatements() throws SQLException, IOException {
        findCourseStatement = connection.prepareStatement(Files.readString(
                Path.of("ApplicationQueries/FindCourse.sql")));
        findTeacherStatement = connection.prepareStatement(
                Files.readString(Path.of("ApplicationQueries/FindTeacher.sql")));
        findPlannedActivityStatement = connection.prepareStatement(
                Files.readString(Path.of("ApplicationQueries/FindPlannedActivity.sql")));
        findAllTeachersStatement = connection.prepareStatement(
                Files.readString(Path.of("ApplicationQueries/FindAllTeachers.sql")));
        findAllPlannedActivitiesStatement = connection.prepareStatement(
                Files.readString(Path.of("ApplicationQueries/FindAllPlannedActivities.sql")));
        findAllPlannedActivitiesStatement = connection.prepareStatement(
                Files.readString(Path.of("ApplicationQueries/UpdateStudentsInCourse.sql")));
        getPlannedActivitiesForTeacherStatement = connection.prepareStatement(
                Files.readString(Path.of("ApplicationQueries/GetPlannedActivitiesForTeacher.sql")));
        getPlannedActivitiesForTeacherLockingStatement = connection.prepareStatement(
                Files.readString(Path.of("ApplicationQueries/GetPlannedActivitiesForTeacherLocking.sql")));
        getPlannedTeachingCostStatement = connection.prepareStatement(
                Files.readString(Path.of("ApplicationQueries/GetPlannedTeachingCost.sql")));
        getActualTeachingCostStatement = connection.prepareStatement(
                Files.readString(Path.of("ApplicationQueries/GetActualTeachingCost.sql")));
        allocateTeacherStatement = connection.prepareStatement(
                Files.readString(Path.of("ApplicationQueries/AllocateTeacher.sql")));
        deallocateTeacherStatement = connection.prepareStatement(
                Files.readString(Path.of("ApplicationQueries/DeallocateTeacher.sql")));
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
            result.close();
        } catch (Exception e) {
            throw new DBException(failureMsg + " Could not close result set.", e);
        }
    }
}
