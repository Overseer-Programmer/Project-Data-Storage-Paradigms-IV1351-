package KthDatabaseApp.controller;

import KthDatabaseApp.integration.DBException;
import KthDatabaseApp.integration.KthDAO;
import KthDatabaseApp.model.*;

import java.util.List;

public class Controller {
    public void hello() {
        System.out.println("Hello world!");
    }

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
     * Gets the teacher specified by id or throws an exception if the teacher does not exist.
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
     * Changes the number of students allocated to the course by the delta.
     * @param courseId
     * @param delta
     * @throws DBException
     * @throws BusinessConstraintException
     * @throws EntityNotFoundException
     */
    public void changeStudentsForCourse(int courseId, int delta) throws DBException, BusinessConstraintException, EntityNotFoundException {
        Course course = database.findCourse(courseId);
        if (course == null) {
            throw new EntityNotFoundException(String.format("Course of id=%d does not exist.", courseId));
        }
        course.setStudentCount(course.getStudentCount() + delta);
        database.updateStudentsForCourse(course);
    }

    public void allocateTeacherToPlannedActivity(int teacherId, int plannedActivityId, int allocatedHours)
            throws DBException, BusinessConstraintException, EntityNotFoundException {
        // Assert the entities exist
        Teacher teacher = database.findTeacher(teacherId);
        if (teacher == null) {
            throw new EntityNotFoundException(String.format("Teacher of id=%d does not exist.", teacherId));
        }
        PlannedActivity plannedActivity = database.findPlannedActivity(plannedActivityId);
        if (plannedActivity == null) {
            throw new EntityNotFoundException(String.format("Planned activity of id=%d does not exist.", plannedActivityId));
        }

        // Update the database
        teacher.allocatePlannedActivity(plannedActivity, allocatedHours);
        database.updateAllocationForTeacher(teacher);
    }

    public void deallocateTeacherFromPlannedActivity(int teacherId, int plannedActivityId) throws DBException, EntityNotFoundException, BusinessConstraintException {
        // Assert the entities exist
        Teacher teacher = database.findTeacher(teacherId);
        if (teacher == null) {
            throw new EntityNotFoundException(String.format("Teacher of id=%d does not exist.", teacherId));
        }
        PlannedActivity plannedActivity = database.findPlannedActivity(plannedActivityId);
        if (plannedActivity == null) {
            throw new EntityNotFoundException(String.format("Planned activity of id=%d does not exist.", plannedActivityId));
        }

        // Update the database
        teacher.deallocatePlannedActivity(plannedActivity);
        database.updateAllocationForTeacher(teacher);
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
        Course course = database.findCourse(courseInstanceId);
        if (course == null) {
            throw new EntityNotFoundException(String.format("Course of id=%d does not exist.", courseInstanceId));
        }

        TeachingCostDTO teachingCost = database.findTeachingCost(course);
        return teachingCost;
    }
}
