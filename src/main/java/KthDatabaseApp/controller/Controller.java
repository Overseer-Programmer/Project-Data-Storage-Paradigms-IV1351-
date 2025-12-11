package KthDatabaseApp.controller;

import KthDatabaseApp.integration.DBException;
import KthDatabaseApp.integration.KthDAO;
import KthDatabaseApp.model.*;

import java.util.List;
import javax.print.attribute.standard.MediaSize;

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

    public void addStudentsToCourse(int courseID, int addedStudents)  throws DBException {    
        Course course = database.getCourse(courseID);
        database.addStudentsToCourse(courseID, course.getStudentCount() + addedStudents);
    }

    public void allocateTeacherToPlannedActivity(int teacherID, int plannedActivityID, int allocatedHours) throws DBException, TeacherOverallocationException {
        database.allocateTeacherToPlannedActivity(teacherID, plannedActivityID, allocatedHours);    
    }

    public void deallocateTeacherFromPlannedActivity(int teacherID, int plannedActivityID) throws DBException
    {
        database.deallocateTeacherFromPlannedActivity(teacherID, plannedActivityID);
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
     */

    public TeachingCostDTO getTeachingCost(int courseInstanceId) throws DBException {
        Course course = database.getCourse(courseInstanceId);
        double totalPlannedCost = 0;
        double totalActualCost = 0;
        for (PlannedActivityDTO plannedActivity : course.getPlannedActivities()) {
            List<Teacher> teachers = database.getTeachersAllocatedToPlannedActivity(plannedActivity);
            double plannedHourDistribution = plannedActivity.getTotalHours(plannedActivity.getPlannedHours()) / teachers.size();
            for (Teacher teacher : teachers) {
                double hourlyWage = teacher.getHourlyWage();
                totalPlannedCost += hourlyWage * plannedHourDistribution;
                totalActualCost += hourlyWage * teacher.getAllocatedHoursForPlannedActivity(plannedActivity);
            }
        }


        return new TeachingCostDTO(
                course.getCourseCode(),
                course.getInstanceId(),
                course.getStudyPeriod(),
                Math.round(totalPlannedCost),
                Math.round(totalActualCost));
    }
}
