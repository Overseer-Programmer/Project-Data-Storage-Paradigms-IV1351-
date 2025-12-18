package KthDatabaseApp.integration;

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

    // teacher
    private PreparedStatement getCourseStatement;
    private PreparedStatement getTeacherStatement;
    private PreparedStatement getPlannedActivitiesForCourseStatement;
    private PreparedStatement getTeachersAllocatedToPlannedActivityStatement;
    private PreparedStatement getAllTeachersStatement;

    // Task 2 modify course instance
    private PreparedStatement UpdateStudentsInCourseStatement;

    // Task 3 teacher allocation
    private PreparedStatement getPlannedActivitiesForTeacher;
    private PreparedStatement allocateTeacherStatement;
    private PreparedStatement deallocateTeacherStatement;
    
    //task4
    private PreparedStatement getAllTeachingActivitiesStatement;
    private PreparedStatement updateAllTeachingActivitiesStatement;

    public void connectToDatabase(String dbUsername, String dbUserPassword) throws DBException {
        try {
            connectToDB(dbUsername, dbUserPassword);
            prepareStatements();
        } catch (ClassNotFoundException | SQLException e) {
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
     * Gets a course and all its related information.
     * 
     * @param courseId The id column of the course_instance relation which
     *                 you want to gather information from.
     * @return a Course object.
     * @throws DBException
     */
    public Course getCourse(int courseInstanceId) throws DBException {
        final String failureMessage = "Could not get course";
        Course course = null;
        ResultSet result = null;
        try {
            getCourseStatement.setInt(1, courseInstanceId);
            result = getCourseStatement.executeQuery();
            result.next();

            // Get all attributes from course_instance and course_layout
            course = new Course(
                    result.getInt("course_instance_id"),
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

            // Get all planned activities for the course
            getPlannedActivitiesForCourseStatement.setInt(1, course.getSurrogateId());
            result = getPlannedActivitiesForCourseStatement.executeQuery();
            while (result.next()) {
                PlannedActivity newPlannedActivity = new PlannedActivity(
                        result.getInt("plannedActivityId"),
                        course,
                        result.getString("activity_name"),
                        result.getDouble("factor"));
                int plannedHours = result.getInt("planned_hours");
                if (plannedHours != 0) {
                    newPlannedActivity.setPlannedHours(plannedHours);
                }
                course.addPlannedActivity(newPlannedActivity);
            }

            connection.commit();
        } catch (SQLException | InvalidRangeException e) {
            handleException(failureMessage, e);
        } finally {
            closeResultSet(failureMessage, result);
        }

        return course;
    }

    /**
     * Gets a teacher using their employee id
     * 
     * @param employeeId The employeeId from the employee table
     * @return A Teacher object
     * @throws DBException
     */
    public Teacher getTeacher(int employeeId) throws DBException {
        final String failureMessage = "Could not get teacher";
        Teacher teacher = null;
        ResultSet result = null;
        try {
            teacher = getTeacherInternal(employeeId, result);
            connection.commit();
        } catch (SQLException e) {
            handleException(failureMessage, e);
        } finally {
            closeResultSet(failureMessage, result);
        }

        return teacher;
    }

    /**
     * Returns all teachers allocated to a planned activity.
     * 
     * @param plannedActivity
     * @return A list of Teachers
     * @throws DBException
     */
    public List<Teacher> getTeachersAllocatedToPlannedActivity(PlannedActivityDTO plannedActivity)
            throws DBException {
        final String failureMessage = "Could not get get teachers allocated to the planned activity";
        List<Teacher> teachers = new ArrayList<>();
        ResultSet result = null;
        try {
            getTeachersAllocatedToPlannedActivityStatement.setInt(1, plannedActivity.getId());
            result = getTeachersAllocatedToPlannedActivityStatement.executeQuery();
            while (result.next()) {
                int employeeId = result.getInt("employeeId");
                int allocatedHours = result.getInt("allocated_hours");
                Teacher teacher = getTeacherInternal(employeeId, result);
                teacher.allocatePlannedActivity(plannedActivity, allocatedHours);
                teachers.add(teacher);
            }
            connection.commit();
        } catch (SQLException | TeacherOverallocationException e) {
            handleException(failureMessage, e);
        } finally {
            closeResultSet(failureMessage, result);
        }
        return teachers;
    }

    public List<Teacher> getAllTeachers() throws DBException {
        final String failureMessage = "Could not get get teachers";
        List<Teacher> teachers = new ArrayList<>();
        ResultSet result = null;
        try {
            result = getAllTeachersStatement.executeQuery();
            while (result.next()) {
                Teacher teacher = getTeacherInternal(result.getInt("employee_id"), result);
                teachers.add(teacher);
            }
            connection.commit();
        } catch (SQLException e) {
            handleException(failureMessage, e);
        } finally {
            closeResultSet(failureMessage, result);
        }
        return teachers;
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
            getPlannedActivitiesForTeacher.setInt(1, teacher.getEmployeeId());
            result = getPlannedActivitiesForTeacher.executeQuery();
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

    public void addPlannedActivity(CourseDTO course, String activityName, int plannedHours) throws DBException {
        final String failureMessage = "Could not add teaching activity";
        ResultSet result;
        try {
            
            result = getAllTeachingActivitiesStatement.executeQuery();
            boolean teachingActivityFound = false;
            int matchingTeachingActivityId = -1; 
            double multiplicationFactor = 1; // Default value

            while(result.next()) 
            {
                if (result.getString("activity_name").equals(activityName)) {
                    teachingActivityFound = true;
                    matchingTeachingActivityId = result.getInt("id");
                    multiplicationFactor = result.getDouble(matchingTeachingActivityId);
                    break;
                }
            }
            if (!teachingActivityFound) {
                updateAllTeachingActivitiesStatement.setString(0, activityName);
                updateAllTeachingActivitiesStatement.setDouble(1, multiplicationFactor);
                updateAllTeachingActivitiesStatement.executeUpdate();

            }


            
            /**
             * Find all teaching activities and check if the the given teaching activity
             * with activityName exists. If it does not exist, add it. Finally, make sure
             * you have the id of the chose teaching activity.
             */
            // Add a planned activity to the planned_activity table with the activityName
            // and get the id of the new planned activity
            PlannedActivity newPlannedActivity = findPlannedActivityInternal(newPlannedActivityId);
            if (newPlannedActivity == null) {
                throw new DBException("Failed to add planned activity for unknown cause");
            }
            connection.commit();
        } catch (SQLException e) {
            handleException(failureMessage, e);
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

    private Teacher getTeacherInternal(int employeeId, ResultSet result) throws SQLException {
        getTeacherStatement.setInt(1, employeeId);
        result = getTeacherStatement.executeQuery();
        result.next();
        return new Teacher(
                result.getInt("employeeId"),
                result.getString("first_name"),
                result.getString("last_name"),
                result.getString("street"),
                result.getString("zip"),
                result.getString("city"),
                result.getInt("salary"));
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
    private void prepareStatements() throws SQLException {
        getCourseStatement = connection.prepareStatement(
                "SELECT ci.id AS course_instance_id, " +
                        "    ci.course_layout_id, " +
                        "    ci.instance_id, " +
                        "    cl.course_code, " +
                        "    cl.course_name, " +
                        "    ci.num_students, " +
                        "    ci.study_year, " +
                        "    ci.Study_Period, " +
                        "    cl.hp, " +
                        "    cl.min_students, " +
                        "    cl.max_students " +
                        "FROM course_instance AS ci " +
                        "JOIN course_layout AS cl ON ci.course_layout_id = cl.id " +
                        "WHERE ci.id = ?");

        getTeacherStatement = connection.prepareStatement(
                "SELECT e.id AS employeeId, " +
                        "    p.first_name, " +
                        "    p.last_name, " +
                        "    p.street, " +
                        "    p.zip, " +
                        "    p.city, " +
                        "    e.salary " +
                        "FROM employee AS e " +
                        "JOIN person AS p ON e.person_id = p.id " +
                        "WHERE e.id = ?");

        getAllTeachersStatement = connection.prepareStatement(
                "SELECT id " +
                        "FROM employee");

        getPlannedActivitiesForCourseStatement = connection.prepareStatement(
                "SELECT pa.id AS plannedActivityId, pa.planned_hours, ta.activity_name, ta.factor " +
                        "FROM planned_activity AS pa " +
                        "JOIN teaching_activity AS ta ON pa.teaching_activity_id = ta.id " +
                        "WHERE pa.course_instance_id = ?");

        getTeachersAllocatedToPlannedActivityStatement = connection.prepareStatement(
                "SELECT employeeId, allocated_hours " +
                        "FROM employee_planned_activity " +
                        "WHERE plannedActivityId = ?");

        UpdateStudentsInCourseStatement = connection.prepareStatement(
                "UPDATE course_instance " +
                        "SET num_students = ? " +
                        "WHERE id = ?"

        );

        // Requires exclusive lock to prevent lost update anomaly
        getPlannedActivitiesForTeacher = connection.prepareStatement(
                "SELECT plannedActivityId " +
                        "FROM employee_planned_activity " +
                        "WHERE employeeId = ? " +
                        "FOR UPDATE");

        deallocateTeacherStatement = connection.prepareStatement(
                "DELETE FROM employee_planned_activity " +
                        "WHERE employeeId = ? AND plannedActivityId = ?");

        allocateTeacherStatement = connection.prepareStatement(
                "INSERT INTO employee_planned_activity (employeeId, plannedActivityId, allocated_hours) " +
                        "VALUES (?, ? ,?)");

        getAllTeachingActivitiesStatement = connection.prepareStatement( 
            "SELECT id, activity_name, factor " + 
            "FROM teaching_activities;");

        updateAllTeachingActivitiesStatement = connection.prepareStatement(
        "INSERT INTO teaching_activities (activity_name, factor) " +
        "VALUES(?,?)");
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
