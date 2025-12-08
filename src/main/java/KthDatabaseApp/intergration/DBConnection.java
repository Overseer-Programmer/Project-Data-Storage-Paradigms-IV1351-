package KthDatabaseApp.intergration;


import java.sql.Connection; // help us connect to the database
import java.sql.DriverManager; // help us manage the connection
import java.sql.PreparedStatement; // help us create prepared statements
import java.sql.ResultSet;
import java.sql.SQLException; // help us handle SQL exceptions
import java.util.ArrayList;
import java.util.List;
import KthDatabaseApp.model.TeacherDTO;

public class DBConnection {
    public final String database_name = "KthGroup15";

    // connection DB
    private final String url = "jdbc:postgresql://localhost:5432/" + database_name;
    private final String user = "postgres";
    private final String password = "postgres";

    /*// Table and column names
    private static final String PLANNED_ACTIVITY_TABLE_NAME = "planned_activities";
    private static final String employee_planned_activity_TABLE_NAME = "employee_planned_activity";
    private static final String EMPLOYEE_PLANNED_ACITIVTY_PK_EMLOYEE_ID = "employee_id";
    private static final String EMPLOYEE_PLANNED_ACITIVTY_PK_PLANNED_ACITIVITY_ID = "planned_activity_id";
    private static final String COURSE_INSTANCE_TABLE_NAME = "course_instances";
      private static final String COURSE_INSTANCE_PK_COLUMN_NAME = "course";*/


    
    private Connection connection;

    // a preparedstatement is a precompiled SQL statement

    //teacher
    private PreparedStatement findAllTeacherstmt; //PreparedStatement for finding all teacher
    private PreparedStatement findTeacherByIdstmt;
    private PreparedStatement countTeacherAllocationstm; //PreparedStatement for counting teacher allocations
    private PreparedStatement findTeacherAllocationstmt; //PreparedStatement for finding teacher allocations

    //course instance
    //private PreparedStatement findCourseInstanceByCodelstmt; //PreparedStatement for finding course instance by code
    private PreparedStatement findCourseInstancestm; //PreparedStatement for finding course instances
    private PreparedStatement findCourseInstaceForUpdatestmt; //PreparedStatement for finding course instance for update
     private PreparedStatement updateCourseInstanceStudentsStmt; //PreparedStatement for updating course instance students

    //allocation
    private PreparedStatement insertAllocationStmt; //PreparedStatement for inserting an allocation
    private PreparedStatement deleteAllocationstmt; //PreparedStatement for deleting an allocation
    //private PreparedStatement findAllocationsForTeacherstmt; //PreparedStatement for finding allocations for a teacher in a period




    public DBConnection() throws DBException {

        try {
        connectToDB();
        prepareStatements();
        } catch (ClassNotFoundException | SQLException e) {
            throw new DBException("failed to connect to database", e);
        }
    }



    private void handleException(String message, Exception cause) throws DBException {
        
        String FailureMsg = message;
        try {
            connection.rollback();

        }  catch (SQLException rollback) {

           FailureMsg += "also failed to rollback transaction because of: " + rollback.getMessage();
           
        }

        // if the problem is something else, we wrap it in a DBException and throw it further
        if (cause !=null){ 
            throw new DBException(FailureMsg, cause);  
        } else {
            throw new DBException(FailureMsg);
        }
    }




    public void commit() throws DBException {

        try {
            connection.commit();

        } catch (SQLException e) {
            handleException("commit failed",e); 
        }
        

    }

    private void connectToDB() throws ClassNotFoundException, SQLException {

        
        connection = DriverManager.getConnection(url, user, password);

        connection.setAutoCommit(false); // disable auto-commit for transaction management
    }

    public List<TeacherDTO> findAllTeachers() throws DBException 
    {
        
        List<TeacherDTO> teachers = new ArrayList<>();
          
        try{
        
        ResultSet rs = findAllTeacherstmt.executeQuery();

             while (rs.next()) {
        teachers.add(new TeacherDTO(
            rs.getInt("id"),
            rs.getString("first_name"),
            rs.getString("last_name")
        ));
    }
        } catch (SQLException e){
            handleException("Failed to fetch teachers", e);
    }

    return teachers;

    }
    
    private void prepareStatements() throws SQLException { //  all SQL commands goes here


        findAllTeacherstmt = connection.prepareStatement(
        "SELECT e.id, e.employee_id,"+ "p.first_name, p.last_name "  + //CONCAT(p.first_name, ' ', p.last_name) AS employee_name,"
        "FROM employee AS e "+ 
        "INNER JOIN person AS p ON e.person_id = p.id " + 
        "INNER JOIN employee_planned_activity AS epa ON epa.employee_id = e.id " +
        "INNER JOIN planned_activity AS pa ON epa.planned_activity_id = pa.id " +
        "INNER JOIN course_instance AS ci ON pa.course_instance_id = ci.id " +
        "GROUP BY e.id, p.first_name, p.last_name "+
        "ORDER BY p.first_name"
        );


        //findTeacherByIdstmt = connection.prepareStatement(


    }

        
    }
 
    
    

