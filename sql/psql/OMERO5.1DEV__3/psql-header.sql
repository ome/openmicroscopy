
--
-- GENERATED %(TIME)s from %(DIR)s
--
-- This file was created by the bin/omero db script command
-- and contains an MD5 version of your OMERO root users's password.
-- You should think about deleting it as soon as possible.
--
-- To create your database:
--
--     createdb omero
--     createlang plpgsql omero
--     psql omero < %(SCRIPT)s
--
--
BEGIN;

CREATE DOMAIN nonnegative_int AS INTEGER CHECK (VALUE >= 0);
CREATE DOMAIN positive_int AS INTEGER CHECK (VALUE > 0);
CREATE DOMAIN positive_float AS DOUBLE PRECISION CHECK (VALUE > 0);
CREATE DOMAIN percent_fraction AS DOUBLE PRECISION CHECK (VALUE >= 0 AND VALUE <= 1);
