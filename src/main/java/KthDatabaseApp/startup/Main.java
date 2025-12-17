package KthDatabaseApp.startup;

import KthDatabaseApp.view.BlockingInterpreter;
import KthDatabaseApp.view.Command;
import KthDatabaseApp.view.InvalidParametersException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import KthDatabaseApp.controller.Controller;
import KthDatabaseApp.integration.DBException;
import KthDatabaseApp.model.*;

public class Main {
    public static void main(String[] args) throws DBException, IOException, InvalidCredentialsException,
            InvalidParametersException, BusinessConstraintException {
        Controller controller = new Controller();
        if (args.length > 0 && args[0].equals("EnterCredentials")) {
            DBCredentials credentials = BlockingInterpreter.promptUsernameAndPassword();
            DBCredentials.storeCredentials(credentials);
        } else {
            DBCredentials credentials = DBCredentials.readCredentials();
            controller.connectToDatabase(credentials);
            BlockingInterpreter interpreter = new BlockingInterpreter(controller);
            if (args.length > 0 && args[0].equals("Debug")) {
                List<String> parameters = new ArrayList<>();
                interpreter.executeCommand(Command.GET_PLANNED_ACTIVITIES, parameters);
            } else {
                interpreter.handleCmds();
            }
        }
    }
}