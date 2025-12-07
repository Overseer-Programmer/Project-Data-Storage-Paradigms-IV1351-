package KthDatabaseApp.intergration;

import java.sql.Connection; // help us connect to the database
import java.sql.DriverManager; // help us manage the connection
import java.sql.PreparedStatement; // help us create prepared statements
import java.sql.SQLException; // help us handle SQL exceptions



public class DBConnection {

    // connection DB
    private final String url = "jdbc:postgresql://localhost:5432/test";
    private final String user = "postgres";
    private final String password = "postgres";

    // Table and column names
    private static final String PLANNED_ACTIVITY_TABLE_NAME = "planned_activities";
    private static final String EMPLOYEE_PLANNED_ACITIVTY_PK_COLUMN_NAME1 = "employee_id";
    private static final String EMPLOYEE_PLANNED_ACITIVTY_PK_COLUMN_NAME2 = "planned_activity_id";
    private static final String COURSE_INSTANCE_TABLE_NAME = "course_instances";
      private static final String COURSE_INSTANCE_PK_COLUMN_NAME = "course";


    
    private Connection connection;

    //teacher
    private PreparedStatement findTeacherstmt; //PreparedStatement for finding a teacher
    private PreparedStatement countTeacherAllocationstm; //PreparedStatement for counting teacher allocations
    private PreparedStatement findTeacherAllocationstmt; //PreparedStatement for finding teacher allocations

    //course instance
    //private PreparedStatement findCourseInstanceByCodelstmt; //PreparedStatement for finding course instance by code
    private PreparedStatement findCourseInstancestm; //PreparedStatement for finding course instances
    private PreparedStatement updateCourseInstanceStudentsStmt; //PreparedStatement for updating course instance students
    private PreparedStatement findCourseInstaceForUpdatestmt; //PreparedStatement for finding course instance for update

    //allocation
    private PreparedStatement insertAllocationStmt; //PreparedStatement for inserting an allocation
    private PreparedStatement deleteAllocationstmt; //PreparedStatement for deleting an allocation
    private PreparedStatement findAllocationsForTeacherstmt; //PreparedStatement for finding allocations for a teacher in a period




    public DBConnection() throws SQLException {

        connectToBankDB();
    }



    private void handleException(String message, Exception cause) throws DBException {
        
        String FailureMsg = message;
        try {
            connection.rollback();

        }  catch (SQLException rollback) {

           FailureMsg += "also failed to rollback transaction because of:" + rollback.getMessage();
           
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

    private void connectToBankDB() throws SQLException {

        connection = DriverManager.getConnection(url, user, password);

        connection.setAutoCommit(false); // disable auto-commit for transaction management
    }
 
    private void prepareStatements() throws SQLException {

    }
  
  
    
  
   

     
       
      

    

}
