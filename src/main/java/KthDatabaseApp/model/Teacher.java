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
    private List<TeacherAllocation> teacherAllocations; // A list of all planned activities allocated to this
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
        teacherAllocations = new ArrayList<>();
    }

    /**
     * Allocates the teacher to a planned activity. If adding the activity would
     * result in having the teacher be allocated to more than 4 different course
     * instances in the same study period and study year, the allocation will be
     * rejected. The teacher allocation reference is also added to the planned
     * activity.
     * 
     * @param plannedActivity The planned activity to allocate
     * @param allocatedHours  The amount of hours the teacher should be allocated to
     *                        the planned activity
     * @throws BusinessConstraintException
     */
    public void allocatePlannedActivity(PlannedActivity plannedActivity, int allocatedHours)
            throws BusinessConstraintException {
        TeacherAllocation allocation = findTeacherAllocation(plannedActivity);
        if (allocation != null) {
            throw new BusinessConstraintException(String.format(
                    "Cannot allocate teacher (employee_id=%d) to planned activity (id=%d) because they are already allocated.",
                    getEmployeeId(), plannedActivity.getId()));
        }
        allocation = new TeacherAllocation(this, plannedActivity, allocatedHours);
        teacherAllocations.add(allocation);
        plannedActivity.addTeacherAllocation(allocation);
        if (getMaxTeachingLoad() > 4) {
            teacherAllocations.remove(allocation);
            plannedActivity.removeTeacherAllocation(allocation);
            throw new BusinessConstraintException("Cannot allocate planned activity id="
                    + plannedActivity.getId()
                    + " to teacher employeeId=" + employeeId
                    + " because the teacher would be allocated to more than 4 different course instances during a particular period.");
        }
    }

    /**
     * Deallocates the teacher from a planned activity if it exists. The teacher
     * allocation reference is also removed from the planned activity.
     * 
     * @param plannedActivity
     */
    public void deallocatePlannedActivity(PlannedActivity plannedActivity) {
        TeacherAllocation allocation = findTeacherAllocation(plannedActivity);
        if (allocation != null) {
            teacherAllocations.remove(allocation);
            plannedActivity.removeTeacherAllocation(allocation);
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
        for (TeacherAllocation allocation : teacherAllocations) {
            if (allocation.plannedActivity.getId() == plannedActivity.getId()) {
                return plannedActivity.getTotalHours(allocation.allocatedHours);
            }
        }
        return 0;
    }

    public List<TeacherAllocation> getTeachingAllocations() {
        List<TeacherAllocation> clone = new ArrayList<>(teacherAllocations);
        return clone;
    }

    public int getMaxTeachingLoad() {
        int maxTeachingLoad = 0;
        HashMap<Integer, HashMap<StudyPeriod, List<Integer>>> allocatedCourseInstances = new HashMap<>();
        System.out.println("Allocation count: " + teacherAllocations.size());
        for (TeacherAllocation allocation : teacherAllocations) {
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
                if (allocatedCourseInstancesCurrentPeriod.size() > maxTeachingLoad) {
                    maxTeachingLoad = allocatedCourseInstancesCurrentPeriod.size();
                }
            }
        }
        return maxTeachingLoad;
    }

    private TeacherAllocation findTeacherAllocation(PlannedActivity plannedActivity) {
        for (TeacherAllocation allocation : teacherAllocations) {
            if (allocation.plannedActivity.getId() == plannedActivity.getId()) {
                return allocation;
            }
        }
        return null;
    }
}
