package startup;

import view.BlockingInterpreter;
import controller.Controller;

public class Main {
    public static void main(String[] args) {
        BlockingInterpreter interpreter = new BlockingInterpreter(new Controller());
        interpreter.handleCmds();
    }
}
