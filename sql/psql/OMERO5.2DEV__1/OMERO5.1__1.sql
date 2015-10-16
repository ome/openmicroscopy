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
--- OMERO5 development release upgrade from OMERO5.1__1 to OMERO5.2DEV__1.
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

SELECT omero_assert_db_version('OMERO5.1', 1);
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

SELECT assert_db_server_prerequisites(90300);

DROP FUNCTION assert_db_server_prerequisites(INTEGER);
DROP FUNCTION db_pretty_version(INTEGER);


--
-- Actual upgrade
--

INSERT INTO dbpatch (currentVersion, currentPatch, previousVersion, previousPatch)
             VALUES ('OMERO5.2DEV',  1,            'OMERO5.1',      1);

-- ... up to patch 0:

-- Prevent the deletion of mimetype = "Directory" objects
CREATE OR REPLACE FUNCTION _fs_dir_delete() RETURNS TRIGGER AS $$
    BEGIN
        --
        -- If any children are found, prevent deletion
        --
        IF OLD.mimetype = 'Directory' AND EXISTS(
            SELECT id FROM originalfile
            WHERE repo = OLD.repo AND path = OLD.path || OLD.name || '/'
            LIMIT 1) THEN

                -- CANCEL DELETE
                RAISE EXCEPTION '%', 'Directory('||OLD.id||')='||OLD.path||OLD.name||'/ is not empty!';

        END IF;
        RETURN OLD; -- proceed
    END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION annotation_updates_note_reindex() RETURNS void AS $$

    DECLARE
        curs CURSOR FOR SELECT * FROM _updated_annotations ORDER BY event_id LIMIT 100000 FOR UPDATE;
        row _updated_annotations%rowtype;

    BEGIN
        FOR row IN curs
        LOOP
            DELETE FROM _updated_annotations WHERE CURRENT OF curs;

            INSERT INTO eventlog (id, action, permissions, entityid, entitytype, event)
                SELECT ome_nextval('seq_eventlog'), 'REINDEX', -52, row.entity_id, row.entity_type, row.event_id
                WHERE NOT EXISTS (SELECT 1 FROM eventlog AS el
                    WHERE el.entityid = row.entity_id AND el.entitytype = row.entity_type AND el.event = row.event_id);

        END LOOP;
    END;
$$ LANGUAGE plpgsql;


--
-- FINISHED
--

UPDATE dbpatch SET message = 'Database updated.', finished = clock_timestamp()
    WHERE currentVersion  = 'OMERO5.2DEV' AND
          currentPatch    = 0             AND
          previousVersion = 'OMERO5.1'    AND
          previousPatch   = 1;

SELECT CHR(10)||CHR(10)||CHR(10)||'YOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION OMERO5.2DEV__0'||CHR(10)||CHR(10)||CHR(10) AS Status;

COMMIT;
