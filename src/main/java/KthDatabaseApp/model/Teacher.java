package KthDatabaseApp.model;

public class Teacher implements TeacherDTO {
    private final int employee_id;
    private String firstName;
    private String lastName;
    private Address address;
    private int salary;

    public Teacher(int employee_id) {
        this.salary = 0;
        this.employee_id = employee_id;
    }

    // Setters
    public void setFullName(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public void setAddress(String street, String zip, String city) {
        this.address = new Address(street, zip, city);
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }

    // Getters
    public int getEmployeeId() {
        return employee_id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public Address getAddress() {
        return address;
    }

    public int getSalary() {
        return salary;
    }

    public double getHourlyWage() {
        return (double)salary / 30.0;
    }
}
