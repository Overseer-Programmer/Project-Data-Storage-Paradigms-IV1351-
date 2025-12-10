package KthDatabaseApp.intergration;

import java.sql.Connection; // help us connect to the database
import java.sql.DriverManager; // help us manage the connection
import java.sql.PreparedStatement; // help us create prepared statements
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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
                        result.getInt("planned_activity_id"),
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
     * @param employeeId The employee_id from the employee table
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
                int employeeId = result.getInt("employee_id");
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

    private Teacher getTeacherInternal(int employeeId, ResultSet result) throws SQLException {
        getTeacherStatement.setInt(1, employeeId);
        result = getTeacherStatement.executeQuery();
        result.next();
        return new Teacher(
                result.getInt("employee_id"),
                result.getString("first_name"),
                result.getString("last_name"),
                result.getString("street"),
                result.getString("zip"),
                result.getString("city"),
                result.getInt("salary"));
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
                "SELECT e.id AS employee_id, " +
                        "    p.first_name, " +
                        "    p.last_name, " +
                        "    p.street, " +
                        "    p.zip, " +
                        "    p.city, " +
                        "    e.salary " +
                        "FROM employee AS e " +
                        "JOIN person AS p ON e.person_id = p.id " +
                        "WHERE e.id = ?");

        getPlannedActivitiesForCourseStatement = connection.prepareStatement(
                "SELECT pa.id AS planned_activity_id, pa.planned_hours, ta.activity_name, ta.factor " +
                        "FROM planned_activity AS pa " +
                        "JOIN teaching_activity AS ta ON pa.teaching_activity_id = ta.id " +
                        "WHERE pa.course_instance_id = ?");

        getTeachersAllocatedToPlannedActivityStatement = connection.prepareStatement(
            "SELECT employee_id, allocated_hours " +
            "FROM employee_planned_activity " +
            "WHERE planned_activity_id = ?"
        );
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
