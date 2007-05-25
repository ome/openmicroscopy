
DATABASE SCRIPTS:
================

The directories which are named after the supported database profiles (currently "psql")
contains scripts for creating fresh databases as well as for upgrading existing database.

All scripts are of the form:

  CURRENTVERSION__CURRENTPATCH__TARGETVERSION__TARGETPATCH.sql

with the exception of:

  CURRENTVERSION__0__bootstrap.sql        Adds the "dbpatch" table to earlier databases
  CURRENTVERSION__0__schema.sql           Creates a fresh database
  CURRENTVERSION__0__data.sql             Adds OMERO-specific data to a fresh database


To create the update scripts, first cleaned version of each schema are compared 
(where cleaned means simple formatting differences, random foreign key names, etc. 
are removed). Then it is necessary to compare the values in all enumerations.
