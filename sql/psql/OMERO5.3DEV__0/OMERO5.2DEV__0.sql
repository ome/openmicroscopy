-- Copyright (C) 2012-4 Glencoe Software, Inc. All rights reserved.
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
--- OMERO5 development release upgrade from OMERO5.2DEV__0 to OMERO5.3DEV__0.
---

BEGIN;


--
-- check OMERO database version
--

CREATE OR REPLACE FUNCTION omero_assert_db_version(expected_version VARCHAR, expected_patch INTEGER) RETURNS void AS $$

DECLARE
    current_version VARCHAR;
    current_patch INTEGER;

BEGIN
    SELECT currentversion, currentpatch INTO STRICT current_version, current_patch
        FROM dbpatch ORDER BY id DESC LIMIT 1;

    IF current_version <> expected_version OR current_patch <> expected_patch THEN
        RAISE EXCEPTION 'wrong OMERO database version for this upgrade script';
    END IF;

END;$$ LANGUAGE plpgsql;

SELECT omero_assert_db_version('OMERO5.2DEV', 0);
DROP FUNCTION omero_assert_db_version(varchar, int);


--
-- check PostgreSQL server version and database encoding
--

CREATE OR REPLACE FUNCTION db_pretty_version(version INTEGER) RETURNS TEXT AS $$

BEGIN
    RETURN (version/10000)::TEXT || '.' || ((version/100)%100)::TEXT || '.' || (version%100)::TEXT;

END;$$ LANGUAGE plpgsql;


CREATE FUNCTION assert_db_server_prerequisites(version_prereq INTEGER) RETURNS void AS $$

DECLARE
    version_num INTEGER;
    char_encoding TEXT;

BEGIN
    SELECT CAST(setting AS INTEGER) INTO STRICT version_num
        FROM pg_settings WHERE name = 'server_version_num';
    SELECT pg_encoding_to_char(encoding) INTO STRICT char_encoding
        FROM pg_database WHERE datname = current_database();

    IF version_num < version_prereq THEN
        RAISE EXCEPTION 'PostgreSQL database server version % is less than OMERO prerequisite %',
            db_pretty_version(version_num), db_pretty_version(version_prereq);
    END IF;

    IF char_encoding != 'UTF8' THEN
        RAISE EXCEPTION 'OMERO database character encoding must be UTF8, not %', char_encoding;
    ELSE
        SET client_encoding = 'UTF8';
    END IF;

END;$$ LANGUAGE plpgsql;

SELECT assert_db_server_prerequisites(90300);  -- TBD

DROP FUNCTION assert_db_server_prerequisites(INTEGER);
DROP FUNCTION db_pretty_version(INTEGER);


--
-- Actual upgrade
--

INSERT INTO dbpatch (currentVersion, currentPatch, previousVersion, previousPatch)
             VALUES ('OMERO5.3DEV',  0,            'OMERO5.2DEV',   0);

-- ... up to patch 0:

CREATE FUNCTION assert_no_roi_keywords_namespaces() RETURNS void AS $$

DECLARE
  roi_row roi%ROWTYPE;
  element TEXT;

BEGIN
    FOR roi_row IN SELECT * FROM roi LOOP
        IF roi_row.keywords IS NOT NULL THEN
        FOREACH element IN ARRAY roi_row.keywords LOOP
            IF element <> '' THEN
                RAISE EXCEPTION 'data in roi.keywords row id=%', roi_row.id;
            END IF;
        END LOOP;
        END IF;
        IF roi_row.namespaces IS NOT NULL THEN
        FOREACH element IN ARRAY roi_row.namespaces LOOP
            IF element <> '' THEN
                RAISE EXCEPTION 'data in roi.namespaces row id=%', roi_row.id;
            END IF;
        END LOOP;
        END IF;
    END LOOP;

END;$$ LANGUAGE plpgsql;

SELECT assert_no_roi_keywords_namespaces();
DROP FUNCTION assert_no_roi_keywords_namespaces();

ALTER TABLE roi DROP COLUMN keywords;
ALTER TABLE roi DROP COLUMN namespaces;

ALTER TABLE shape DROP COLUMN anchor;
ALTER TABLE shape DROP COLUMN baselineshift;
ALTER TABLE shape DROP COLUMN decoration;
ALTER TABLE shape DROP COLUMN direction;
ALTER TABLE shape DROP COLUMN g;
ALTER TABLE shape DROP COLUMN glyphorientationvertical;
ALTER TABLE shape DROP COLUMN strokedashoffset;
ALTER TABLE shape DROP COLUMN strokelinejoin;
ALTER TABLE shape DROP COLUMN strokemiterlimit;
ALTER TABLE shape DROP COLUMN vectoreffect;
ALTER TABLE shape DROP COLUMN visibility;
ALTER TABLE shape DROP COLUMN writingmode;


--
-- FINISHED
--

UPDATE dbpatch SET message = 'Database updated.', finished = clock_timestamp()
    WHERE currentVersion  = 'OMERO5.3DEV' AND
          currentPatch    = 0             AND
          previousVersion = 'OMERO5.2DEV' AND
          previousPatch   = 0;

SELECT CHR(10)||CHR(10)||CHR(10)||'YOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION OMERO5.3DEV__0'||CHR(10)||CHR(10)||CHR(10) AS Status;

COMMIT;
