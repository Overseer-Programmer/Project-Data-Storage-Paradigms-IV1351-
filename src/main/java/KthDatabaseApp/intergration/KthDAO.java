package KthDatabaseApp.intergration;

import java.sql.Connection; // help us connect to the database
import java.sql.DriverManager; // help us manage the connection
import java.sql.PreparedStatement; // help us create prepared statements
import java.sql.ResultSet;
import java.sql.SQLException; // help us handle SQL exceptions
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import KthDatabaseApp.model.*;

public class KthDAO {
    public final String database_name = "kth_group15";

    // connection DB
    private final String url = "jdbc:postgresql://localhost:5432/" + database_name;
    private Connection connection;

    // a PreparedStatement is a precompiled SQL statement

    // teacher
    private PreparedStatement findAllTeacherStmt; // PreparedStatement for finding all teacher
    private PreparedStatement getPlannedActivitiesWithEmployeeDataStmt; // PreparedStatement for getting planned
                                                                        // activities of a course with employee
                                                                        // information
    private PreparedStatement findTeacherByIdStmt;
    private PreparedStatement countTeacherAllocationStmt; // PreparedStatement for counting teacher allocations
    private PreparedStatement findTeacherAllocationStmt; // PreparedStatement for finding teacher allocations

    // course instance
    // private PreparedStatement findCourseInstanceByCodelstmt; //PreparedStatement
    // for finding course instance by code
    private PreparedStatement findCourseInstanceStmt; // PreparedStatement for finding course instances
    private PreparedStatement findCourseInstaceForUpdateStmt; // PreparedStatement for finding course instance for
                                                              // update
    private PreparedStatement updateCourseInstanceStudentsStmt; // PreparedStatement for updating course instance
                                                                // students

    // allocation
    private PreparedStatement insertAllocationStmt; // PreparedStatement for inserting an allocation
    private PreparedStatement deleteAllocationStmt; // PreparedStatement for deleting an allocation
    // private PreparedStatement findAllocationsForTeacherstmt; //PreparedStatement
    // for finding allocations for a teacher in a period

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
        List<TeacherDTO> teachers = new ArrayList<>();
        try {
            ResultSet result = findAllTeacherStmt.executeQuery();
            while (result.next()) {
                teachers.add(new Teacher(result.getInt("id")));
            }
            connection.commit();
        } catch (SQLException e) {
            handleException("Failed to fetch teachers", e);
        }

        return teachers;
    }

    // Get the total planned costs for the specified course instance
    public long getPlannedCourseTeachingCosts(Course course) throws DBException {
        double totalPlannedHourCost = 0;
        try {
            getPlannedActivitiesWithEmployeeDataStmt.setInt(1, course.courseInstanceId);
            ResultSet result = getPlannedActivitiesWithEmployeeDataStmt.executeQuery();
            HashMap<Integer, PlannedActivity> plannedActivities = new HashMap<>();
            while (result.next()) {
                // Get or create the planned activity
                int plannedActivityId = result.getInt("plannedActivityId");
                PlannedActivity currentPlannedActivity = plannedActivities.get(plannedActivityId);
                if (currentPlannedActivity == null) {
                    String activityName = result.getString("activity_name");
                    int plannedHours = 0;
                    if (activityName.equals("Examination")) {
                        plannedHours = (int) Math.round(32 + 0.725 * course.getStudentCount());
                    } else if (activityName.equals("Admin")) {
                        plannedHours = (int) Math.round(2 * course.getHp() + 28 + 0.2 * course.getStudentCount());
                    }
                    else {
                        plannedHours = result.getInt("planned_hours");
                    }
                    currentPlannedActivity = new PlannedActivity(plannedActivityId, plannedHours, activityName);
                    plannedActivities.put(plannedActivityId, currentPlannedActivity);
                }

                // Allocate the teacher to the planned activity
                int employeeId = result.getInt("employee_id");
                int salary = result.getInt("salary");
                Teacher newTeacher = new Teacher(employeeId);
                newTeacher.setSalary(salary);
                currentPlannedActivity.allocateTeacher(newTeacher);
            }

            // Calculate the total planned hour cost
            for (PlannedActivity currentPlannedActivity : plannedActivities.values()) {
                TeacherDTO allocatedTeachers[] = currentPlannedActivity.getAllocatedTeachers();
                double plannedHourDistribution = (double) currentPlannedActivity.planned_hours
                        / allocatedTeachers.length;
                for (int i = 0; i < allocatedTeachers.length; i++) {
                    TeacherDTO teacher = allocatedTeachers[i];
                    totalPlannedHourCost += teacher.getHourlyWage() * plannedHourDistribution;
                }
            }

            connection.commit();
        } catch (SQLException sqle) {
            handleException("Could not get planned course teaching costs", sqle);
        }
        return Math.round(totalPlannedHourCost);
    }

    private void prepareStatements() throws SQLException { // all SQL commands goes here
        findAllTeacherStmt = connection.prepareStatement(
                "SELECT e.id, p.first_name, p.last_name " +
                        "FROM employee AS e " +
                        "INNER JOIN person AS p ON e.person_id = p.id " +
                        "INNER JOIN employee_planned_activity AS epa ON epa.employee_id = e.id " +
                        "INNER JOIN planned_activity AS pa ON epa.plannedActivityId = pa.id " +
                        "INNER JOIN course_instance AS ci ON pa.course_instance_id = ci.id " +
                        "GROUP BY e.id, p.first_name, p.last_name " +
                        "ORDER BY p.first_name");

        getPlannedActivitiesWithEmployeeDataStmt = connection.prepareStatement(
                "SELECT epa.planned_activity_id, ta.activity_name, pa.planned_hours, epa.employee_id, e.salary " +
                        "FROM course_instance AS ci " +
                        "JOIN planned_activity AS pa ON pa.course_instance_id = ci.id " +
                        "JOIN teaching_activity AS ta ON pa.teaching_activity_id = ta.id " +
                        "JOIN employee_planned_activity AS epa ON epa.plannedActivityId = pa.id " +
                        "JOIN employee AS e ON epa.employee_id = e.id " +
                        "WHERE ci.id = ?");

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
}
