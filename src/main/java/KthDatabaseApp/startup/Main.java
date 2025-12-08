package KthDatabaseApp.startup;

import KthDatabaseApp.view.BlockingInterpreter;
import KthDatabaseApp.view.DBCredentials;
import KthDatabaseApp.controller.Controller;
import KthDatabaseApp.intergration.DBException;


public class Main {
    public static void main(String[] args) throws DBException {
        Controller controller = new Controller();
        BlockingInterpreter interpreter = new BlockingInterpreter(controller);
        DBCredentials credentials = interpreter.promptUsernameAndPassword();
        controller.connectToDatabase(credentials);
        interpreter.handleCmds();
    }
}


/*  try{

       // BlockingInterpreter interpreter = new BlockingInterpreter(new Controller());
       Controller contr = new Controller();

         // TEST: hämta alla lärare och skriv ut
            List<TeacherDTO> teachers = contr.getTeachers();
            System.out.println("=== Lärare från databasen ===");
            for (TeacherDTO t : teachers) {
                System.out.println(t.getId() + " " + t.getFirstName() + " " + t.getLastName());
            }


        //interpreter.handleCmds();

        } catch (DBException e){
            System.out.println("Could not connect" + e.getMessage());

        } */