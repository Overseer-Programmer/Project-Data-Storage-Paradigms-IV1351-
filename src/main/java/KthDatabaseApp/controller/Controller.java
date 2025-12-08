
    package KthDatabaseApp.controller;
import KthDatabaseApp.intergration.DBConnection;
import KthDatabaseApp.intergration.DBException;
import KthDatabaseApp.model.TeacherDTO;
import java.util.List;

public class Controller {
    public void hello() {
        System.out.println("Hello world!");
    }


    private DBConnection db;

    public Controller() throws DBException {
        db = new DBConnection();
    }



    public List<TeacherDTO> getTeachers() throws DBException {
        return db.findAllTeachers();
    }


  
}

