
package KthDatabaseApp.controller;

import KthDatabaseApp.intergration.KthDAO;
import KthDatabaseApp.intergration.DBException;
import KthDatabaseApp.model.*;
import KthDatabaseApp.view.DBCredentials;
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
        for (PlannedActivity plannedActivity : course.getPlannedActivities()) {
            List<Teacher> teachers = database.getTeachersAllocatedToPlannedActivity(plannedActivity);
            double plannedHourDistribution = (double) plannedActivity.getTotalPlannedHours() / teachers.size();
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
