package KthDatabaseApp.model;

public class InvalidCredentialsException extends Exception {
    public InvalidCredentialsException(String message) {
        super(message); // Call the constructor of the superclass (Exception) with the message
    }

    public InvalidCredentialsException(String reason, Throwable rootCause) {
        super(reason, rootCause); // Call the constructor of the superclass (Exception) with the reason and root
                                  // cause
    }
}
