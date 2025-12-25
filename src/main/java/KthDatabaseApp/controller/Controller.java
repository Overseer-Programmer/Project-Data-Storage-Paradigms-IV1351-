package KthDatabaseApp.controller;

import KthDatabaseApp.integration.DBException;
import KthDatabaseApp.integration.KthDAO;
import KthDatabaseApp.model.*;

import java.util.ArrayList;
import java.util.List;

public class Controller {
    private KthDAO database;

    public Controller() throws DBException {
        database = new KthDAO();
    }

    public void connectToDatabase(DBCredentials credentials) throws DBException {
        database.connectToDatabase(credentials.username, credentials.password);
    }

    public List<? extends CourseDTO> findAllCourses() throws DBException {
        return database.findAllCourses();
    }

    public List<? extends TeacherDTO> findAllTeachers() throws DBException {
        return database.findAllTeachers();
    }

    public List<? extends PlannedActivityDTO> findAllPlannedActivities() throws DBException {
        return database.findAllPlannedActivities();
    }

    /**
     * Finds all teachers who are allocated to the planned activities specified by
     * the teaching activity.
     * 
     * @param activityName The activity name for the teaching activity.
     * @return A list of TeacherAllocationDTO objects which contains the teachers
     *         and the planned activity they are allocated to.
     * @throws DBException
     */
    public List<TeacherAllocationDTO> findTeacherAllocationsForTeachingActivity(String activityName)
            throws DBException {
        List<TeacherAllocationDTO> allocations = new ArrayList<>();
        List<Teacher> teachers = database.findTeachersAllocatedToTeachingActivity(activityName);
        for (Teacher teacher : teachers) {
            for (TeacherAllocationDTO allocation : teacher.getTeachingAllocations()) {
                if (allocation.getActivityName().equals(activityName)) {
                    allocations.add(allocation);
                }
            }
        }
        return allocations;
    }

    /**
     * Gets the teacher specified by id or throws an exception if the teacher does
     * not exist.
     * 
     * @param teacherId
     * @return
     * @throws EntityNotFoundException
     * @throws DBException
     */
    public TeacherDTO getTeacher(int teacherId) throws EntityNotFoundException, DBException {
        Teacher teacher = database.findTeacher(teacherId);
        if (teacher == null) {
            throw new EntityNotFoundException(String.format("Teacher of id=%d does not exist.", teacherId));
        }
        return teacher;
    }

    /**
     * Changes the number of students allocated to the course by the delta. An
     * exclusive lock is acquired in this transaction to avoid the lost update
     * anomaly.
     * 
     * @param courseId
     * @param delta
     * @throws DBException
     * @throws BusinessConstraintException
     * @throws EntityNotFoundException
     */
    public void changeStudentsForCourse(int courseId, int delta)
            throws DBException, BusinessConstraintException, EntityNotFoundException {
        Course course = database.findCourse(courseId, true);
        if (course == null) {
            throw new EntityNotFoundException(String.format("Course of id=%d does not exist.", courseId));
        }
        course.changeStudentCount(delta);
        database.updateStudentsForCourse(course);
    }

    /**
     * Allocates a planned activity to the teacher.
     * 
     * @param teacherId
     * @param plannedActivityId
     * @param allocatedHours
     * @throws DBException
     * @throws BusinessConstraintException
     * @throws EntityNotFoundException
     */
    public void allocateTeacherToPlannedActivity(int teacherId, int plannedActivityId, int allocatedHours)
            throws DBException {
        database.createAllocationForTeacher(teacherId, plannedActivityId, allocatedHours);
    }

    /**
     * Deallocates a planned activity from a teacher.
     * 
     * @param teacherId
     * @param plannedActivityId
     * @throws DBException
     * @throws EntityNotFoundException
     * @throws BusinessConstraintException
     */
    public void deallocateTeacherFromPlannedActivity(int teacherId, int plannedActivityId)
            throws DBException, EntityNotFoundException, BusinessConstraintException {
        database.deleteAllocationFromTeacher(teacherId, plannedActivityId);
    }

    /**
     * Gets the planned cost and actual cost for a course along with some course
     * information. The cost is calculated based on the salary of teachers and the
     * total hours they are planned to be allocated vs how much they are actually
     * allocated to the course.
     * 
     * @param courseInstanceId The id of the course instance to calculated the cost
     *                         from
     * @return A TeachingCostDTO object with all relevant data.
     * @throws DBException
     * @throws EntityNotFoundException
     */
    public TeachingCostDTO getTeachingCost(int courseInstanceId) throws DBException, EntityNotFoundException {
        TeachingCostDTO teachingCost = database.findTeachingCost(courseInstanceId);
        if (teachingCost == null) {
            throw new EntityNotFoundException(String.format("Course with courseInstanceId=%d not found", courseInstanceId));
        }
        return teachingCost;
    }

    public void createTeachingActivity(String activityName, double multiplicationFactor) throws DBException {
        TeachingActivity teachingActivity = new TeachingActivity(activityName, multiplicationFactor);
        database.createTeachingActivity(teachingActivity);
    }
}
