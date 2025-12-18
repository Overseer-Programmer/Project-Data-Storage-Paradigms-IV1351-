package KthDatabaseApp.view;

public class InvalidParametersException extends Exception {
    public InvalidParametersException(String correctSyntax, Command command) {
        super("Invalid parameters for command " + command + ".\nCorrect syntax: " + correctSyntax);
    }
}
