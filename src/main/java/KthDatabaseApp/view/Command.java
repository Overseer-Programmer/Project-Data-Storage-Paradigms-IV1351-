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

/**
 * Defines all commands that can be performed by a user of the chat application.
 */
public enum Command {
    HELP("Get information about how to use all commands."),
    QUIT("Leave the chat application."),
    GET_COURSES("Get all courses in the database."),
    GET_TEACHERS("Get all teachers in the database."),
    GET_PLANNED_ACTIVITIES("Get all planned activities in the database."),
    TEACHING_COST("Get the teaching cost for a course specified by course_instance_id."),
    CHANGE_STUDENT_COUNT("Change the student count of a course by a delta value, which is the amount to add or remove from the current student count."),
    ALLOCATE("Allocate a planned activity to a teacher along with the amount of allocated hours."),
    DEALLOCATE("Deallocate a planned activity from a teacher."),
    ILLEGAL_COMMAND("None of the valid commands above was specified.");

    public static int longestCommand = 22;
    private final String description;

    Command(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
