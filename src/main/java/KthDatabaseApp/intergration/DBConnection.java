package KthDatabaseApp.intergration;

import java.sql.Connection; // help us connect to the database
import java.sql.DriverManager; // help us manage the connection
import java.sql.PreparedStatement; // help us create prepared statements
import java.sql.ResultSet;
import java.sql.SQLException; // help us handle SQL exceptions
import java.util.ArrayList;
import java.util.List;
import KthDatabaseApp.model.Teacher;
import KthDatabaseApp.model.TeacherDTO;

public class DBConnection {
    public final String database_name = "kth_group15";

    // connection DB
    private final String url = "jdbc:postgresql://localhost:5432/" + database_name;
    private Connection connection;

    // a PreparedStatement is a precompiled SQL statement

    // teacher
    private PreparedStatement findAllTeacherStmt; // PreparedStatement for finding all teacher
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

    public void commit() throws DBException {

        try {
            connection.commit();

        } catch (SQLException e) {
            handleException("commit failed", e);
        }

    }

    private void connectToDB(String user, String password) throws ClassNotFoundException, SQLException {

        connection = DriverManager.getConnection(url, user, password);

        connection.setAutoCommit(false); // disable auto-commit for transaction management
    }

    public List<TeacherDTO> findAllTeachers() throws DBException {

        List<TeacherDTO> teachers = new ArrayList<>();

        try {

            ResultSet result = findAllTeacherStmt.executeQuery();

            while (result.next()) {
                teachers.add(new Teacher(result.getInt("id")));
            }
        } catch (SQLException e) {
            handleException("Failed to fetch teachers", e);
        }

        return teachers;

    }

    private void prepareStatements() throws SQLException { // all SQL commands goes here

        findAllTeacherStmt = connection.prepareStatement(
                "SELECT e.id, p.first_name, p.last_name " +                                                  
                "FROM employee AS e " +
                "INNER JOIN person AS p ON e.person_id = p.id " +
                "INNER JOIN employee_planned_activity AS epa ON epa.employee_id = e.id " +
                "INNER JOIN planned_activity AS pa ON epa.planned_activity_id = pa.id " +
                "INNER JOIN course_instance AS ci ON pa.course_instance_id = ci.id " +
                "GROUP BY e.id, p.first_name, p.last_name " +
                "ORDER BY p.first_name");

        // findTeacherByIdstmt = connection.prepareStatement(

    }

}
