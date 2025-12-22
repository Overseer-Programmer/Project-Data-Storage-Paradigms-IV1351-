/*
 * The MIT License
 *
 * Copyright 2017 Leif Lindbäck <leifl@kth.se>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package KthDatabaseApp.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import KthDatabaseApp.controller.Controller;
import KthDatabaseApp.controller.EntityNotFoundException;
import KthDatabaseApp.integration.DBException;
import KthDatabaseApp.model.*;

/**
 * Reads and interprets user commands. This command interpreter is blocking, the
 * user
 * interface does not react to user input while a command is being executed.
 */
public class BlockingInterpreter {
    private static final String COMMAND_PROMPT = "> ";
    private final Scanner console = new Scanner(System.in);
    private Controller controller;
    private boolean keepReceivingCmds = false;

    /**
     * Creates a new instance that will use the specified controller for all
     * operations.
     * 
     * @param controller The controller used by this instance.
     */
    public BlockingInterpreter(Controller controller) {
        this.controller = controller;
    }

    /**
     * Stops the commend interpreter.
     */
    public void stop() {
        keepReceivingCmds = false;
    }

    /**
     * Interprets and performs user commands. This method will not return until the
     * UI has been stopped. The UI is stopped either when the user gives the
     * "quit" command, or when the method <code>stop()</code> is called.
     */
    public void handleCmds() {
        keepReceivingCmds = true;
        while (keepReceivingCmds) {
            try {
                CmdLine cmdLine = new CmdLine(readNextCommand());
                List<String> parameters = new ArrayList<>();
                String nextParameter;
                do {
                    nextParameter = cmdLine.getParameter(parameters.size());
                    if (nextParameter != null) {
                        parameters.add(nextParameter);
                    }
                } while (nextParameter != null);
                executeCommand(cmdLine.getCmd(), parameters);
            } catch (Exception e) {
                System.out.println("Operation failed");
                System.out.println(e.getMessage());
                if (!(e instanceof InvalidParametersException || e instanceof BusinessConstraintException
                        || e instanceof EntityNotFoundException)) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void executeCommand(Command command, List<String> parameters)
            throws InvalidParametersException, DBException, BusinessConstraintException, InvalidRowLengthException,
            EntityNotFoundException {
        switch (command) {
            case HELP: {
                for (Command currentCommand : Command.values()) {
                    if (currentCommand == Command.ILLEGAL_COMMAND) {
                        continue;
                    }
                    System.out.println(String.format("%-" + Command.longestCommand + "s:\t%s",
                            currentCommand.toString(), currentCommand.getDescription()));
                }
                break;
            }
            case QUIT: {
                keepReceivingCmds = false;
                break;
            }
            case GET_COURSES: {
                List<? extends CourseDTO> courses = controller.findAllCourses();
                Table table = new Table(
                        "Course instance id",
                        "Course code",
                        "Course name",
                        "HP score",
                        "Student count",
                        "Min students",
                        "Max Students");
                for (CourseDTO course : courses) {
                    table.addRow(
                            course.getSurrogateId(),
                            course.getCourseCode(),
                            course.getCourseName(),
                            course.getHp(),
                            course.getStudentCount(),
                            course.getMinStudents(),
                            course.getMaxStudents());
                }
                table.printOut();
                break;
            }
            case GET_TEACHERS: {
                List<? extends TeacherDTO> teachers = controller.findAllTeachers();
                Table table = new Table("Teacher id", "Name", "Max teaching load");
                for (TeacherDTO teacher : teachers) {
                    table.addRow(teacher.getEmployeeId(), teacher.getFullName(), teacher.getMaxTeachingLoad());
                }
                table.printOut();
                break;
            }
            case GET_PLANNED_ACTIVITIES: {
                List<? extends PlannedActivityDTO> plannedActivities = controller.findAllPlannedActivities();
                Table table = new Table("Planned activity id", "Planned hours", "Activity name", "Course instance id",
                        "Study year", "Study period");
                for (PlannedActivityDTO plannedActivity : plannedActivities) {
                    table.addRow(
                            plannedActivity.getId(),
                            plannedActivity.getPlannedHours(),
                            plannedActivity.getActivityName(),
                            plannedActivity.getCourseSurrogateId(),
                            plannedActivity.getCourseStudyYear(),
                            plannedActivity.getCourseStudyPeriod());
                }
                table.printOut();
                break;
            }
            case GET_TEACHERS_FOR_ACTIVITY: {
                final InvalidParametersException exception = new InvalidParametersException(
                        "get_teachers_for_activity [activity_name]", Command.GET_TEACHERS_FOR_ACTIVITY);
                String activityName = getStringParameter(parameters, 0, exception);
                List<TeacherAllocationDTO> allocations = controller.findTeacherAllocationsForTeachingActivity(activityName);
                Table table = new Table("Course instance id", "Course name", "Teacher id", "Teacher Name", "Activity name");
                for (TeacherAllocationDTO allocation : allocations) {
                    table.addRow(
                        allocation.getAllocatedCourseSurrogateId(),
                        allocation.getAllocatedCourseName(),
                        allocation.getTeacherId(),
                        allocation.getTeacherFullName(),
                        allocation.getActivityName()
                    );
                }
                table.printOut();
                break;
            }
            case TEACHING_COST: {
                final InvalidParametersException exception = new InvalidParametersException(
                        "teaching_cost [course_instance_id]", Command.TEACHING_COST);
                int courseId = getIntParameter(parameters, 0, exception);

                TeachingCostDTO teachingCost = controller.getTeachingCost(courseId);
                Table table = new Table("Course code", "Instance id", "Study Period", "Planned cost", "Actual cost");
                table.addRow(
                        teachingCost.courseCode,
                        teachingCost.instanceId,
                        teachingCost.studyPeriod,
                        teachingCost.plannedCost,
                        teachingCost.actualCost
                );
                table.printOut();
                break;
            }
            case CHANGE_STUDENT_COUNT: {
                final InvalidParametersException exception = new InvalidParametersException(
                        "change_student_count [course_instance_id] [delta]",
                        Command.CHANGE_STUDENT_COUNT);
                int courseId = getIntParameter(parameters, 0, exception);
                int delta = getIntParameter(parameters, 1, exception);

                controller.changeStudentsForCourse(courseId, delta);
                System.out.println("Successfully updated student count");
                break;
            }
            case GET_TEACHER_ALLOCATIONS: {
                final InvalidParametersException exception = new InvalidParametersException(
                        "get_teacher_allocations [teacher_id]", Command.GET_TEACHER_ALLOCATIONS);
                int teacherId = getIntParameter(parameters, 0, exception);
                TeacherDTO teacher = controller.getTeacher(teacherId);
                System.out.println("Max course assignments at a particular period: " + teacher.getMaxTeachingLoad());
                Table table = new Table("Planned activity id", "Planned hours", "Allocated hours", "Course instance id",
                        "Study year", "Study period");
                for (TeacherAllocationDTO allocation : teacher.getTeachingAllocations()) {
                    table.addRow(
                            allocation.getPlannedActivityId(),
                            allocation.getPlannedHours(),
                            allocation.getAllocatedHours(),
                            allocation.getAllocatedCourseSurrogateId(),
                            allocation.getAllocatedCourseStudyYear(),
                            allocation.getAllocatedCourseStudyPeriod());
                }
                table.printOut();
                break;
            }
            case ALLOCATE: {
                final InvalidParametersException exception = new InvalidParametersException(
                        "allocate [teacher_id] [planned_activity_id] [allocated_hours]",
                        Command.ALLOCATE);
                int teacherId = getIntParameter(parameters, 0, exception);
                int plannedActivityId = getIntParameter(parameters, 1, exception);
                int allocatedHours = getIntParameter(parameters, 2, exception);

                controller.allocateTeacherToPlannedActivity(teacherId, plannedActivityId, allocatedHours);
                System.out.println("Teacher allocated successfully");
                break;
            }
            case DEALLOCATE: {
                final InvalidParametersException exception = new InvalidParametersException(
                        "deallocate [teacher_id] [planned_activity_id]",
                        Command.DEALLOCATE);
                int teacherId = getIntParameter(parameters, 0, exception);
                int plannedActivityId = getIntParameter(parameters, 1, exception);

                controller.deallocateTeacherFromPlannedActivity(teacherId, plannedActivityId);
                System.out.println("Teacher deallocated successfully");
                break;
            }
            case CREATE_ACTIVITY: {
                final InvalidParametersException exception = new InvalidParametersException(
                        "create_activity [activity_name] [multiplication_factor]", Command.CREATE_ACTIVITY);
                String activityName = getStringParameter(parameters, 0, exception);
                double multiplicationFactor = getDoubleParameter(parameters, 1, exception);
                controller.createTeachingActivity(activityName, multiplicationFactor);
                System.out.println("Successfully added teaching activity " + activityName + ".");
                break;
            }
            default:
                System.out.println("Illegal command");
        }
    }

    // Asks for the username and password required to connect to a database
    public static DBCredentials promptUsernameAndPassword() {
        Scanner console = new Scanner(System.in);
        System.out.print("Enter your database username:");
        String dbUsername = console.nextLine();
        System.out.print("Enter your database password:");
        String dbUserPassword = console.nextLine();
        console.close();
        return new DBCredentials(dbUsername, dbUserPassword);
    }

    private int getIntParameter(List<String> parameters, int parameterIndex, InvalidParametersException exception)
            throws InvalidParametersException {
        assertParameters(parameters, parameterIndex, exception);
        int value;
        try {
            value = Integer.parseInt(parameters.get(parameterIndex));
        } catch (Exception e) {
            throw exception;
        }
        return value;
    }

    private String getStringParameter(List<String> parameters, int parameterIndex, InvalidParametersException exception) throws InvalidParametersException {
        assertParameters(parameters, parameterIndex, exception);
        return parameters.get(parameterIndex);
    }

    private double getDoubleParameter(List<String> parameters, int parameterIndex, InvalidParametersException exception) throws InvalidParametersException {
        assertParameters(parameters, parameterIndex, exception);
        double value;
        try {
            value = Double.parseDouble(parameters.get(parameterIndex));
        } catch (Exception e) {
            throw exception;
        }
        return value;

    }

    private void assertParameters(List<String> parameters, int parameterIndex, InvalidParametersException exception) throws InvalidParametersException {
        if (parameters.size() - 1 < parameterIndex) {
            throw exception;
        }
    }

    private String readNextCommand() {
        System.out.print(COMMAND_PROMPT);
        return console.nextLine();
    }
}
