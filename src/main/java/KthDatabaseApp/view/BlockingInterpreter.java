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

import java.util.Scanner;
import KthDatabaseApp.controller.Controller;
import KthDatabaseApp.model.DBCredentials;
import KthDatabaseApp.model.TeachingCostDTO;
import KthDatabaseApp.model.TeacherOverallocationException;

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
                switch (cmdLine.getCmd()) {
                    case HELP: {
                        for (Command command : Command.values()) {
                            if (command == Command.ILLEGAL_COMMAND) {
                                continue;
                            }
                            System.out.println(command);
                        }
                        break;
                    }
                    case QUIT: {
                        keepReceivingCmds = false;
                        break;
                    }
                    case COST: {
                        String param = cmdLine.getParameter(0);
                        if (param.equals("")) {
                            throw new InvalidParametersException("cost course_instance_id", Command.COST);
                        }

                        int courseId = Integer.parseInt(param);
                        TeachingCostDTO teachingCost = controller.getTeachingCost(courseId);
                        System.out.println("courseCode: " + teachingCost.courseCode);
                        System.out.println("instanceId: " + teachingCost.instanceId);
                        System.out.println("studyPeriod: " + teachingCost.studyPeriod);
                        System.out.println("plannedCost: " + teachingCost.plannedCost);
                        System.out.println("actualCost: " + teachingCost.actualCost);
                        break;
                    }
                    // GET_COURSES
                    case INCREASE: {
                        String param1 = cmdLine.getParameter(0);
                        String param2 = cmdLine.getParameter(1);
                        if (param1.equals("") || param2.equals("")) {
                            throw new InvalidParametersException("increase course_instance_id num_students", Command.INCREASE);
                        }

                        int courseId = Integer.parseInt(param1);
                        int numStudents = Integer.parseInt(param2);
                        controller.addStudentsToCourse(courseId, numStudents);
                        break;
                    }
                    case ALLOCATE: {
                        String param1 = cmdLine.getParameter(0);
                        String param2 = cmdLine.getParameter(1);
                        String param3 = cmdLine.getParameter(2);
                        if (param1.equals("") || param2.equals("") || param3.equals("")) {
                            throw new InvalidParametersException("allocate teacher_id planned_acitvity_id allocated_hours",
                                    Command.ALLOCATE);
                        }

                        int teacherId = Integer.parseInt(param1);
                        int plannedActivityId = Integer.parseInt(param2);
                        int allocatedHours = Integer.parseInt(param3);
                        controller.allocateTeacherToPlannedActivity(teacherId, plannedActivityId, allocatedHours);
                        System.out.println("Teacher allocated successfully");
                        break;
                    }
                    case DEALLOCATE: {
                        String param1 = cmdLine.getParameter(0);
                        String param2 = cmdLine.getParameter(1);
                        if (param1.equals("") || param2.equals("")) {
                            throw new InvalidParametersException("deallocate teacher_id planned_activity_id", Command.DEALLOCATE);
                        }

                        int teacherId = Integer.parseInt(param1);
                        int plannedActivityId = Integer.parseInt(param2);
                        controller.deallocateTeacherFromPlannedActivity(teacherId, plannedActivityId);
                        System.out.println("Teacher deallocated successfully");
                    }
                    default:
                        System.out.println("Illegal command");
                }
            } catch (Exception e) {
                System.out.println("Operation failed");
                System.out.println(e.getMessage());
                if (!(e instanceof InvalidParametersException || e instanceof TeacherOverallocationException)) {
                    e.printStackTrace();
                }
            }
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

    private String readNextCommand() {
        System.out.print(COMMAND_PROMPT);
        return console.nextLine();
    }
}
