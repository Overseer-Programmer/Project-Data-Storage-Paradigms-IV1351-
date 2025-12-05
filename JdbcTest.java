import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseDemo {
    public static void main(String[] args) {
        // Database credentials
        String url = "jdbc:postgresql://localhost:5432/simplejdbc";
        String username = "your_username";
        String password = "your_password";
        
        try {
            // Establish connection
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connected to database!");
            
            // Create statement
            Statement stmt = conn.createStatement();
            
            // Execute query
            ResultSet rs = stmt.executeQuery("SELECT * FROM your_table");
            
            // Process results
            while (rs.next()) {
                System.out.println(rs.getString("column_name"));
            }
            
            // Close resources
            rs.close();
            stmt.close();
            conn.close();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}