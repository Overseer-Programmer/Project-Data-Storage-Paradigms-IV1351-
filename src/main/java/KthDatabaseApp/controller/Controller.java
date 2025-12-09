
package KthDatabaseApp.controller;

import KthDatabaseApp.intergration.KthDAO;
import KthDatabaseApp.intergration.DBException;
import KthDatabaseApp.model.Course;
import KthDatabaseApp.model.PlannedActivity;
import KthDatabaseApp.model.Teacher;
import KthDatabaseApp.model.TeacherDTO;
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

    public List<TeacherDTO> getTeachers() throws DBException {
        return database.findAllTeachers();
    }

    public long getPlannedCourseTeachingCosts(Course course) throws DBException {
        double totalPlannedHourCost = 0;
        List<PlannedActivity> plannedActivities = database.getPlannedActivitiesForCourse(course);

        for (PlannedActivity plannedActivity : plannedActivities) {
            // Can you calculate the derived attributes here?
            String activityName = plannedActivity.getActivityName();
            if (activityName.equals("Examination")) {
                plannedActivity.setPlannedHours((int) Math.round(32 + 0.725 * course.getStudentCount()));
            } else if (activityName.equals("Admin")) {
                plannedActivity.setPlannedHours((int) Math.round(2 * course.getHp() + 28 + 0.2 * course.getStudentCount()));
            }

            // Calculate the total planned hour cost
            // Can you do this here?
            List<Integer> salaries = database.getTeacherSalariesAllocatedToPlannedActivity(plannedActivity);
            double plannedHourDistribution = (double) plannedActivity.getPlannedHours() / salaries.size();
            for (int salary : salaries) {
                totalPlannedHourCost += (salary / 30) * plannedHourDistribution;
            }
        }

        return Math.round(totalPlannedHourCost);
    }
}
