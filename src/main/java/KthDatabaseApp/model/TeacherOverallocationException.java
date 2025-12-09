package KthDatabaseApp.model;

public class TeacherOverallocationException extends Exception {
    public TeacherOverallocationException(String message) {
        super(message); // Call the constructor of the superclass (Exception) with the message
    }

    public TeacherOverallocationException(String reason, Throwable rootCause) {
        super(reason, rootCause); // Call the constructor of the superclass (Exception) with the reason and root
                                  // cause
    }
}
