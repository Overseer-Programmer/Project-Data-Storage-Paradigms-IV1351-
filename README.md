# Project-Data-Storage-Paradigms-IV1351-
This is the repository used to store and share all files used in the project for Data Storage Paradigms (IV1351).

# Requirements

PostgreSQL was used and could possibly be required for the sql scripts to run properly.

# How to use

Create a database and enter the integrated command line using psql. When you are inside a database do the following operations can be performed.

To import the database schema, run:
\i CreateDatabase.sql

To populate with the data listed in "DatabaseData", run:
\i PopulateDatabase.sql

To clear the database schema and all the data stored in it, run:
\i ClearDatabase.sql