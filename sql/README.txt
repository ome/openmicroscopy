DATABASE SCRIPTS:
=================

The directories which are named after the supported database profiles
(currently "psql") contain scripts for creating fresh databases as well
as for upgrading existing databases.

All scripts are of the form:

  TARGETVERSION__TARGETPATCH/CURRENTVERSION__CURRENTPATCH.sql

with the exception of scripts concatenated by "omero db script":

  CURRENTVERSION__0/psql-header.sql      Prefixes schema.sql
  CURRENTVERSION__0/schema.sql           Creates a fresh database
  CURRENTVERSION__0/psql-footer.sql      Suffixes schema.sql, adjusts the "dbpatch" table

To create the update scripts, first cleaned versions of each schema are
compared (where cleaned means simple formatting differences, random
foreign key names, etc. are removed). Then it is necessary to compare
the values in all enumerations.
