package KthDatabaseApp.integration;

public class DBException extends Exception { // exception extending the built-in Exception class
    public DBException(String message) {
        super(message); // Call the constructor of the superclass (Exception) with the message
    }

    public DBException(String reason, Throwable rootCause) {
        super(reason, rootCause); // Call the constructor of the superclass (Exception) with the reason and root cause
    }
}
