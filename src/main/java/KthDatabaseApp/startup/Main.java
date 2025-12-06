package KthDatabaseApp.startup;

import KthDatabaseApp.view.BlockingInterpreter;
import KthDatabaseApp.controller.Controller;

public class Main {
    public static void main(String[] args) {
        BlockingInterpreter interpreter = new BlockingInterpreter(new Controller());
        interpreter.handleCmds();
    }
}
