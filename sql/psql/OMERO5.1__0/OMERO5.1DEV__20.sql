-- Copyright (C) 2014 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--
-- This program is free software; you can redistribute it and/or modify
-- it under the terms of the GNU General Public License as published by
-- the Free Software Foundation; either version 2 of the License, or
-- (at your option) any later version.
--
-- This program is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU General Public License for more details.
--
-- You should have received a copy of the GNU General Public License along
-- with this program; if not, write to the Free Software Foundation, Inc.,
-- 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
--

---
--- OMERO5 development release upgrade from OMERO5.1DEV__20 to OMERO5.1__0.
---

BEGIN;

CREATE OR REPLACE FUNCTION omero_assert_db_version(version varchar, patch int) RETURNS void AS '
DECLARE
    rec RECORD;
BEGIN

    SELECT INTO rec *
           FROM dbpatch
          WHERE id = ( SELECT id FROM dbpatch ORDER BY id DESC LIMIT 1 )
            AND currentversion = version
            AND currentpatch = patch;

    IF NOT FOUND THEN
        RAISE EXCEPTION ''ASSERTION ERROR: Wrong database version'';
    END IF;

END;' LANGUAGE plpgsql;

SELECT omero_assert_db_version('OMERO5.1DEV', 20);
DROP FUNCTION omero_assert_db_version(varchar, int);

INSERT INTO dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
             VALUES ('OMERO5.1',     0,              'OMERO5.1DEV',       20);

--
-- Actual upgrade
--

--
-- check PostgreSQL server version and database encoding
--

CREATE FUNCTION assert_db_server_prerequisites(version_prereq INTEGER) RETURNS void AS $$

DECLARE
    version_num INTEGER;
    char_encoding TEXT;

BEGIN
    SELECT CAST(setting AS INTEGER) INTO STRICT version_num FROM pg_settings WHERE name = 'server_version_num';
    SELECT pg_encoding_to_char(encoding) INTO STRICT char_encoding FROM pg_database WHERE datname = current_database();

    IF version_num < version_prereq THEN
        RAISE EXCEPTION 'database server version % is less than OMERO prerequisite %', version_num, version_prereq;
    END IF;

    IF char_encoding != 'UTF8' THEN
       RAISE EXCEPTION 'OMERO database character encoding must be UTF8, not %', char_encoding;
    END IF;

END;$$ LANGUAGE plpgsql;

SELECT assert_db_server_prerequisites(90200);
DROP FUNCTION assert_db_server_prerequisites(INTEGER);

-- Temporary workaround for the width of map types
alter table annotation_mapvalue alter column name type text;
alter table annotation_mapvalue alter column value type text;

-- Add lookup table to channel and channel binding to
-- contain a server-specific lookup for a LUT.
alter table channel add column lookupTable varchar(255);
alter table channelbinding add column lookupTable varchar(255);

--
-- FINISHED
--

UPDATE dbpatch SET message = 'Database updated.', finished = clock_timestamp()
    WHERE currentVersion  = 'OMERO5.1'    AND
          currentPatch    = 0             AND
          previousVersion = 'OMERO5.1DEV' AND
          previousPatch   = 20;

SELECT CHR(10)||CHR(10)||CHR(10)||'YOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION OMERO5.1__0'||CHR(10)||CHR(10)||CHR(10) AS Status;

COMMIT;

-- A previous dev upgrade added the unit 'kBar' with an
-- unintentional capital 'B'. Modifying pg_enum directly
-- would allow correcting the typo but is not possible
-- without superuser credentials. Instead, we simply add
-- the extra enum. That can't be done in a transaction,
-- however, and is therefore done here at the end.
ALTER TYPE unitspressure ADD VALUE 'kbar' BEFORE 'kBar';
