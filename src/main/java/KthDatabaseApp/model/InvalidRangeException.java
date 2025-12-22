package KthDatabaseApp.model;

public class InvalidRangeException extends Exception {
    public InvalidRangeException(String message) {
        super(message); // Call the constructor of the superclass (Exception) with the message
    }

    public InvalidRangeException(String reason, Throwable rootCause) {
        super(reason, rootCause); // Call the constructor of the superclass (Exception) with the reason and root
                                  // cause
    }
}
