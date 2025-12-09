package KthDatabaseApp.intergration;

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
    private PreparedStatement findAllTeachers;
    private PreparedStatement getPlannedActivitiesForCourse;
    private PreparedStatement getTeacherSalariesAllocatedToPlannedActivity;

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

    public List<TeacherDTO> findAllTeachers() throws DBException {
        final String failureMessage = "Failed to fetch teachers";
        List<TeacherDTO> teachers = new ArrayList<>();
        ResultSet result = null;
        try {
            result = findAllTeachers.executeQuery();
            while (result.next()) {
                teachers.add(new Teacher(result.getInt("id")));
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
     * Gets all planned activities for a course.
     * 
     * @param course the course you want to find the teaching activities for.
     * @return A list of all planned activities
     * @throws DBException
     */
    public List<PlannedActivity> getPlannedActivitiesForCourse(CourseDTO course) throws DBException {
        final String failureMessage = "Could not get planned course teaching costs";
        List<PlannedActivity> plannedActivities = new ArrayList<>();
        ResultSet result = null;
        try {
            getPlannedActivitiesForCourse.setInt(1, course.getSurrogateId());
            result = getPlannedActivitiesForCourse.executeQuery();
            while (result.next()) {
                int plannedActivityId = result.getInt("plannedActivityId");
                String activityName = result.getString("activity_name");
                int plannedHours = result.getInt("planned_hours");
                PlannedActivity newPlannedActivity = new PlannedActivity(plannedActivityId, activityName);
                if (plannedHours != 0) {
                    newPlannedActivity.setPlannedHours(plannedHours);
                }
                plannedActivities.add(newPlannedActivity);

            }
            connection.commit();
        } catch (SQLException sqle) {
            handleException(failureMessage, sqle);
        } finally {
            closeResultSet(failureMessage, result);
        }
        return plannedActivities;
    }

    /**
     * Gets the salaries of all teachers that are allocated to a planned activity.
     * @param plannedActivity The planned activity
     * @return A list of teacher salaries
     * @throws DBException
     */
    public List<Integer> getTeacherSalariesAllocatedToPlannedActivity(PlannedActivityDTO plannedActivity) throws DBException {
        final String failureMessage = "Could not get get teachers allocated to course";
        List<Integer> salaries = new ArrayList<>();
        ResultSet result = null;
        try {
            getTeacherSalariesAllocatedToPlannedActivity.setInt(1, plannedActivity.getId());
            result = getTeacherSalariesAllocatedToPlannedActivity.executeQuery();
            while (result.next()) {
                salaries.add(result.getInt("salary"));
            }
            connection.commit();
        } catch (SQLException sqle) {
            handleException(failureMessage, sqle);
        } finally {
            closeResultSet(failureMessage, result);
        }
        return salaries;
    }

    private void prepareStatements() throws SQLException { // all SQL commands goes here
        findAllTeachers = connection.prepareStatement(
                "SELECT e.id, p.first_name, p.last_name " +
                        "FROM employee AS e " +
                        "INNER JOIN person AS p ON e.person_id = p.id " +
                        "INNER JOIN employee_planned_activity AS epa ON epa.employee_id = e.id " +
                        "INNER JOIN planned_activity AS pa ON epa.plannedActivityId = pa.id " +
                        "INNER JOIN course_instance AS ci ON pa.course_instance_id = ci.id " +
                        "GROUP BY e.id, p.first_name, p.last_name " +
                        "ORDER BY p.first_name");

        getPlannedActivitiesForCourse = connection.prepareStatement(
                "SELECT pa.id AS planned_activity_id, ta.activity_name, pa.planned_hours " +
                        "FROM course_instance AS ci " +
                        "JOIN planned_activity AS pa ON pa.course_instance_id = ci.id " +
                        "JOIN teaching_activity AS ta ON pa.teaching_activity_id = ta.id " +
                        "WHERE ci.id = ?");

        getTeacherSalariesAllocatedToPlannedActivity = connection.prepareStatement(
                "SELECT e.salary " +
                        "FROM employee_planned_activity AS epa " +
                        "JOIN employee AS e ON epa.employee_id = e.id " +
                        "WHERE epa.planned_activity_id = ?");
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
