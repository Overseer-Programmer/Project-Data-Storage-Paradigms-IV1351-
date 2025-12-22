package KthDatabaseApp.model;

import java.util.List;

public interface TeacherDTO {
    public int getEmployeeId();
    public String getFirstName();
    public String getLastName();
    public String getFullName();
    public Address getAddress();
    public int getSalary();
    /**
     * Gets the amount of hours the teacher is allocated to the planned activity,
     * accounting for multiplication factor.
     * 
     * @param plannedActivity
     * @return The allocated hours, is 0 if the teacher was not allocated to the
     *         planned activity.
     */
    public List<TeacherAllocationDTO> getTeachingAllocations();
    public int getMaxTeachingLoad();
}
