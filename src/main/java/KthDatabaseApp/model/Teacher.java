package KthDatabaseApp.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Teacher implements TeacherDTO {
    private final int employeeId;
    private String firstName;
    private String lastName;
    private Address address;
    private int salary;
    private List<TeacherAllocation> teacherAllocations; // A list of all planned activities allocated to this
                                                           // teacher along with the amount of allocated hours.

    /**
     * Contains information about an employee from the employee and person relations
     * of the conceptual model.
     * 
     * @param employeeId         The employee_id attribute of the employee table
     * @param firstName          The first_name attribute of the person table
     * @param lastName           The last_name attribute of the person table
     * @param street             The street attribute of the person table
     * @param zip                The zip attribute of the person table
     * @param city               The city attribute of the person table
     * @param salary             The salary attribute of the employee table
     * @param teacherAllocations A list of all planned activities allocated to this
     *                           teacher along with the amount of allocated hours.
     */
    public Teacher(
            int employeeId,
            String firstName,
            String lastName,
            String street,
            String zip,
            String city,
            int salary,
            List<TeacherAllocation> teacherAllocations) {
        this.employeeId = Objects.requireNonNull(employeeId);
        setFullName(firstName, lastName);
        setAddress(street, zip, city);
        setSalary(salary);
        this.teacherAllocations = new ArrayList<>();
        if (teacherAllocations != null) {
            for (TeacherAllocation allocation : teacherAllocations) {
                this.teacherAllocations.add(new TeacherAllocation(this, allocation.plannedActivity, allocation.allocatedHours));
            }
        }
    }

    // Setters
    public void setFullName(String firstName, String lastName) {
        this.firstName = Objects.requireNonNull(firstName);
        this.lastName = Objects.requireNonNull(lastName);
    }

    public void setAddress(String street, String zip, String city) {
        this.address = new Address(street, zip, city);
    }

    public void setSalary(int salary) {
        this.salary = Objects.requireNonNull(salary);
    }

    // Getters
    public int getEmployeeId() {
        return employeeId;
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

    public List<? extends TeacherAllocationDTO> getTeachingAllocations() {
        List<TeacherAllocation> clone = new ArrayList<>(teacherAllocations);
        return clone;
    }

    /**
     * Returns the maximum amount of courses the teacher is allocated to for a
     * particular study year and study period. That means if a teacher is allocated
     * to the most courses at 2025 P3, that would be their max teaching load. A
     * teacher is allocated to a course indirectly via planned activities.
     */
    public int getMaxTeachingLoad() {
        int maxTeachingLoad = 0;
        HashMap<Integer, HashMap<StudyPeriod, List<Integer>>> allocatedCourseInstances = new HashMap<>();
        for (TeacherAllocation allocation : teacherAllocations) {
            int studyYear = allocation.getAllocatedCourseStudyYear();
            StudyPeriod studyPeriod = allocation.getAllocatedCourseStudyPeriod();
            if (!allocatedCourseInstances.containsKey(studyYear)) {
                allocatedCourseInstances.put(studyYear, new HashMap<>());
            }
            List<Integer> allocatedCourseInstancesCurrentPeriod = allocatedCourseInstances.get(studyYear)
                    .get(studyPeriod);
            if (allocatedCourseInstancesCurrentPeriod == null) {
                allocatedCourseInstancesCurrentPeriod = new ArrayList<>();
                allocatedCourseInstances.get(studyYear).put(studyPeriod, allocatedCourseInstancesCurrentPeriod);
            }
            int allocatedCourseInstanceId = allocation.getAllocatedCourseSurrogateId();
            if (!allocatedCourseInstancesCurrentPeriod.contains(allocatedCourseInstanceId)) {
                allocatedCourseInstancesCurrentPeriod.add(allocatedCourseInstanceId);
                if (allocatedCourseInstancesCurrentPeriod.size() > maxTeachingLoad) {
                    maxTeachingLoad = allocatedCourseInstancesCurrentPeriod.size();
                }
            }
        }
        return maxTeachingLoad;
    }
}
