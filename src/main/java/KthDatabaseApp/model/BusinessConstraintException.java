package KthDatabaseApp.model;

public class BusinessConstraintException extends Exception {
    public BusinessConstraintException(String message) {
        super("Business constraint violation: " + message); // Call the constructor of the superclass (Exception) with the message
    }
}
