package KthDatabaseApp.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Teacher implements TeacherDTO {
    private final int employeeId;
    private String firstName;
    private String lastName;
    private Address address;
    private int salary;
    private List<TeacherAllocation> allocatedPlannedActivities; // A list of all planned activities allocated to this
                                                                // teacher along with the amount of allocated hours.

    /**
     * Contains information about an employee from the employee and person relations
     * of the conceptual model.
     * 
     * @param employeeId The employee_id attribute of the employee table
     * @param firstName  The first_name attribute of the person table
     * @param lastName   The last_name attribute of the person table
     * @param street     The street attribute of the person table
     * @param zip        The zip attribute of the person table
     * @param city       The city attribute of the person table
     * @param salary     The salary attribute of the employee table
     */
    public Teacher(
            int employeeId,
            String firstName,
            String lastName,
            String street,
            String zip,
            String city,
            int salary) {
        this.employeeId = employeeId;
        setFullName(firstName, lastName);
        setAddress(street, zip, city);
        setSalary(salary);
        allocatedPlannedActivities = new ArrayList<>();
    }

    /**
     * Allocates the teacher to a planned activity. If adding the activity would
     * result in having the teacher be allocated to more than 4 different course
     * instances in the same study period and study year, the allocation will be
     * rejected.
     * 
     * @param plannedActivity The planned activity to allocate
     * @param allocatedHours  The amount of hours the teacher should be allocated to
     *                        the planned activity
     * @throws TeacherOverallocationException
     */
    public void allocatePlannedActivity(PlannedActivityDTO plannedActivity, int allocatedHours)
            throws TeacherOverallocationException {
        TeacherAllocation newAllocation = new TeacherAllocation(plannedActivity, allocatedHours);
        allocatedPlannedActivities.add(newAllocation);
        HashMap<Integer, HashMap<StudyPeriod, List<Integer>>> allocatedCourseInstances = new HashMap<>();
        for (TeacherAllocation allocation : allocatedPlannedActivities) {
            CourseDTO allocatedCourse = allocation.plannedActivity.getCourse();
            int studyYear = allocatedCourse.getStudyYear();
            StudyPeriod studyPeriod = allocatedCourse.getStudyPeriod();
            if (!allocatedCourseInstances.containsKey(studyYear)) {
                allocatedCourseInstances.put(studyYear, new HashMap<>());
            }
            List<Integer> allocatedCourseInstancesCurrentPeriod = allocatedCourseInstances.get(studyYear)
                    .get(studyPeriod);
            if (allocatedCourseInstancesCurrentPeriod == null) {
                allocatedCourseInstancesCurrentPeriod = new ArrayList<>();
                allocatedCourseInstances.get(studyYear).put(studyPeriod, allocatedCourseInstancesCurrentPeriod);
            }
            int allocatedCourseInstanceId = allocatedCourse.getSurrogateId();
            if (!allocatedCourseInstancesCurrentPeriod.contains(allocatedCourseInstanceId)) {
                allocatedCourseInstancesCurrentPeriod.add(allocatedCourseInstanceId);
                if (allocatedCourseInstancesCurrentPeriod.size() > 4) {
                    allocatedPlannedActivities.remove(newAllocation);
                    throw new TeacherOverallocationException("Cannot allocate planned activity id="
                            + plannedActivity.getId()
                            + " to teacher employeeId=" + employeeId
                            + " because the teacher would be allocated to more than 4 different course instances during a particular period.");
                }
            }
        }
    }

    /**
     * Deallocates the teacher from a planned activity if it exists.
     * 
     * @param plannedActivity
     */
    public void deallocatePlannedActivity(PlannedActivityDTO plannedActivity) {
        for (TeacherAllocation allocation : allocatedPlannedActivities) {
            if (allocation.plannedActivity.getId() == plannedActivity.getId()) {
                allocatedPlannedActivities.remove(allocation);
                break;
            }
        }
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

    public double getHourlyWage() {
        return (double) salary / (30.0 * 24);
    }

    public double getAllocatedHoursForPlannedActivity(PlannedActivityDTO plannedActivity) {
        for (TeacherAllocation allocation : allocatedPlannedActivities) {
            if (allocation.plannedActivity.getId() == plannedActivity.getId()) {
                return plannedActivity.getTotalHours(allocation.allocatedHours);
            }
        }
        return 0;
    }
}
