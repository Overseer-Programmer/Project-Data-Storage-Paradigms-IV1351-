# Project Data Storage Paradigms (IV1351)
This is the repository used to store and share all files used in the project for Data Storage Paradigms (IV1351).

## Requirements
You must create the database and perform queries on it in a linux environment or use Windows Subsystem for Linux (WSL).
PostgreSQL 18.1 was used and the sql scripts might not function for earlier versions. For task 3 you will also need to have Maven installed.

## How to use
Firstly, make sure to enter a terminal in this project folder and open up a database in ```psql```.

### Create database schema and populate it
To create the database schema, run:
```bash
\i CreateDatabase.sql
```

To populate with the data listed in ```DatabaseData```, run:
```bash
\i PopulateDatabase.sql
```

To clear the database schema and all the data stored in it, run:
```bash
\i ClearDatabase.sql
```

### Run task 2 (mandatory part) queries
The queries related to the mandatory part in task 2 for the project are stored in ```TaskQueries``` ordered in ascending order from the [project description](https://canvas.kth.se/courses/57087/pages/project-2).

#### Query 1
To run query 1, run:
```bash
\i TaskQueries/Query1.sql
```

#### Query 2
This query randomly selects a course instance from the current year to filter on.

To run query 2, run:
```bash
\i TaskQueries/Query2.sql
```

Please note that this query could return 0 rows if the course has no assigned employees. This is possible because the ```PopulateDatabase.sql``` does not guarantee that every course instance is assigned at least one employee.

#### Query 3
To run query 3, you must first choose an employee (teacher) to filter for. You can find employees assigned to course instances of the current year by running:
```bash
\i HelperQueries/FindEmployeesForCurrentYearCourseInstances.sql
```

Extract the ```id``` value for your desired employee and enter:
```bash
\set chosen_employee_id YOUR_EMPLOYEE_ID
```

Then you can run the query:
```bash
\i TaskQueries/Query3.sql
```

#### Query 4
To run query 4, you must first specify the minimum amount of course instances you want employees to be assigned to. You do this by entering:
```bash
\set min_course_instances INSTANCE_COUNT
```

Then you can run the query:
```bash
\i TaskQueries/Query4.sql
```

### Run task 3 application queries

To run the application, you must first have a role in PostgreSQL with a username and password (You will later have to enter these credentials). Then you must have a database in your role called ```kth_group15```. You can create this database, it's schema and populate it by running:
```sql
\i SetupDatabase.sql
```

Once this is set up, you can enter your credentials by running:

```bash
mvn exec:java -Dexec.args="EnterCredentials"
```

Then you can run the program by running:

```bash
mvn exec:java
```
### Terminal

To list all available commands and usage instruction, enter

```bash
'HELP
```

### Task 3.1 - Computing the teaching cost for a specific course

To compute the teaching cost, first select a course instance by its ID.

A list of all available course instances can be displayed by entering the following command:

```bash
GET_COURSES
```

Select a course instance ID from the list and note it for later use.

To compute and display the teaching cost for the selected course, run the following command:

```bash
TEACHING_COST <course_instance_id>
```

### Task 3.2 - Increase student count for a specific course

To increase the number of registered students, specify the course instance by its ID. The course instance can be the same as the one used in the previous step.

The student count is updated using the following command: 

```bash
CHANGE_STUDENT_COUNT <course_instance_id> <number_of_students>
```

The system enforces constraints on the student count for a course instance. If the updated number of students exceeds the permitted range, the command will result in an error. 

### Task 3.3 -  Allocate and deallocate teaching loads

#### How to allocate 

First select a teacher by ID.

```bash
GET_TEACHERS
```

Next,select a planned activity by its ID.

```bash
GET_PLANNED_ACTIVITIES
```
To allocate a planned activity you must also specify the number if allocated hours.

Run the following command to perform the allocation

```bash
ALLOCATE <teacher_id> <planned_activity_id> <allocated hours>
```

#### How to deallocate

To deallocate a teaching load, the same parameters are used:

```bash
DEALLOCATE <teacher_id> <planned_activity_id> <allocated hours>
```

### Task 3.4 - Add a new teaching activity called "Exercise"

A new teaching acitvity named 'Exercise' is added to the system using the following command:

```bash
CREATE ACTIVITY <activity_name> <multiplicationfactor> 
```

When the command is executed, the database automatically handles the remaining logic.
A teacher who is assigned to fewer than four course instances is selected and allocated to the newly created planned activity.


