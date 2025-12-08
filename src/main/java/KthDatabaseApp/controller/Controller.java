
package KthDatabaseApp.controller;

import KthDatabaseApp.intergration.KthDAO;
import KthDatabaseApp.intergration.DBException;
import KthDatabaseApp.model.TeacherDTO;
import KthDatabaseApp.view.DBCredentials;
import java.util.List;

public class Controller {
    public void hello() {
        System.out.println("Hello world!");
    }

    private KthDAO database;

    public Controller() throws DBException {
        database = new KthDAO();
    }

    public void connectToDatabase(DBCredentials credentials) throws DBException {
        database.connectToDatabase(credentials.username, credentials.password);
    }

    public List<TeacherDTO> getTeachers() throws DBException {
        return database.findAllTeachers();
    }
}
