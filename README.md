# Project Data Storage Paradigms (IV1351)
This is the repository used to store and share all files used in the project for Data Storage Paradigms (IV1351).

## Requirements
You must create the database and perform queries on it in a linux environment or use Windows Subsystem for Linux (WSL).
PostgreSQL 18.1 was used and the sql scripts might not function for earlier versions.

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
