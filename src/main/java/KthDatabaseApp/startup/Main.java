package KthDatabaseApp.startup;

import KthDatabaseApp.view.BlockingInterpreter;

import java.io.IOException;

import KthDatabaseApp.controller.Controller;
import KthDatabaseApp.integration.DBException;
import KthDatabaseApp.model.*;


public class Main {
    public static void main(String[] args) throws DBException, IOException, InvalidCredentialsException {
        Controller controller = new Controller();
        if (args.length > 0 && args[0].equals("EnterCredentials")) {
            DBCredentials credentials = BlockingInterpreter.promptUsernameAndPassword();
            DBCredentials.storeCredentials(credentials);
        }
        else {
            DBCredentials credentials = DBCredentials.readCredentials();
            controller.connectToDatabase(credentials);
            if (args.length > 0 && args[0].equals("Debug")) {
                TeachingCostDTO teachingCost = controller.getTeachingCost(1);
                System.out.println("courseCode: " + teachingCost.courseCode);
                System.out.println("instanceId: " + teachingCost.instanceId);
                System.out.println("studyPeriod: " + teachingCost.studyPeriod);
                System.out.println("plannedCost: " + teachingCost.plannedCost);
                System.out.println("actualCost: " + teachingCost.actualCost);
            }
            else {
                BlockingInterpreter interpreter = new BlockingInterpreter(controller);
                interpreter.handleCmds();
            }
        }        
    }
}