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
import KthDatabaseApp.intergration.DBException;

/**
 * Reads and interprets user commands. This command interpreter is blocking, the user
 * interface does not react to user input while a command is being executed.
 */
public class BlockingInterpreter {
    private static final String COMMAND_PROMPT = "> ";
    private final Scanner console = new Scanner(System.in);
    private Controller controller;
    private boolean keepReceivingCmds = false;

    /**
     * Creates a new instance that will use the specified controller for all operations.
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
                    case HELP:
                        for (Command command : Command.values()) {
                            if (command == Command.ILLEGAL_COMMAND) {
                                continue;
                            }
                            System.out.println("HEJ");
                        }
                        break;
                    case QUIT:
                        keepReceivingCmds = false;
                        break;
                    case TEST:
                        break;
                    case COST:

                    String param = cmdLine.getParameter(0);
                    if (param == null) {
                        System.out.println("Missing parameter: course_instance_id");
                        
                    }

                    int courseId = Integer.parseInt(param);

                    TeachingCostDTO teachingCost = controller.getTeachingCost(courseId);
                    System.out.println();
                    System.out.println("+-----------+-----------+------------+---------------+---------------+");
                    System.out.printf("| %9s| %9s | %10s | %13s | %13s |%n",
                            "courseCode", "instanceId", "studyPeriod", "plannedCost", "actualCost");
                    System.out.println("+-----------+-----------+------------+---------------+---------------+");
                    System.out.printf("| %9s | %9s | %10s | %13d | %13d |%n",
                            teachingCost.courseCode,
                            teachingCost.instanceId,
                            teachingCost.studyPeriod,
                            teachingCost.plannedCost,
                            teachingCost.actualCost);
                    System.out.println("+-----------+-----------+------------+---------------+---------------+");
                    
                    // COST course_instance_id
                    // GET_COURSES
                    
                    break;
                    case INCREASE:
                    
                    String param1 = cmdLine.getParameter(0);
                     String param2 = cmdLine.getParameter(1);

                     if (param1 == null || param2 == null) {
                        System.out.println("Missing parameter: course_instance_id or num_of_students");
                        break;
                     }

                    int courseID2 = Integer.parseInt(param1);

                    int num_of_students = Integer.parseInt(param2);

                    TeachingCostDTO updatedTeachingCost = controller.uppdateStudentsstmt( courseID2, num_of_students);

                  System.out.println();
                    System.out.println("+-----------+-----------+------------+---------------+---------------+");
                    System.out.printf("| %9s| %9s | %10s | %13s | %13s |%n",
                            "courseCode", "instanceId", "studyPeriod", "plannedCost", "actualCost");
                    System.out.println("+-----------+-----------+------------+---------------+---------------+");
                    System.out.printf("| %9s | %9s | %10s | %13d | %13d |%n",
                            updatedTeachingCost.courseCode,
                            updatedTeachingCost.instanceId,
                            updatedTeachingCost.studyPeriod,
                            updatedTeachingCost.plannedCost,
                            updatedTeachingCost.actualCost);
                    System.out.println("+-----------+-----------+------------+---------------+---------------+");

                    break;
                    case ALLOCATE:
                     String teacher = cmdLine.getParameter(0);
                    String planned_acitvity = cmdLine.getParameter(1);
                       String hours = cmdLine.getParameter(2);

                        if (teacher == null || planned_acitvity == null ||hours == null) {
                        System.out.println("Missing parameter: course_instance_id or num_of_students");
                        break; 

                    
                     }

                        int t = Integer.parseInt(teacher);
                        int pa = Integer.parseInt(planned_acitvity);
                        int h = Integer.parseInt(hours);
                        try {
                            controller.allocateTeacherToPlannedActivity(t, pa, h);
                            System.out.println("Teacher allocated successfully");

                        } catch (TeacherOverallocationException  e) {
                         System.out.println("Teacher allocation failed: " + e.getMessage());
                        }catch (DBException a) {
                            System.out.println("An error occurred during allocation: " + a.getMessage());
                        }
                    break;
                    case DEALLOCATE:
                     String teacher_id = cmdLine.getParameter(0);
                     String planned_activity_id= cmdLine.getParameter(1);

                        if(teacher_id == null || planned_activity_id == null){
                            System.out.println("Misssing paramters");
                        }

                        int te = Integer.parseInt(teacher_id);
                        int pa_id = Integer.parseInt(planned_activity_id);

                        try {
                            controller.dealallocate(te, pa_id);
                            System.out.println("deallocation successful");
                        } catch (DBException e) {
                            System.out.println("deallocation failed: " + e.getMessage());
                        }

                    default:
                        System.out.println("illegal command");
                }
            } catch (Exception e) {
                System.out.println("Operation failed");
                System.out.println(e.getMessage());
                e.printStackTrace();
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
