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
--- OMERO5 development release upgrade from OMERO5.4DEV__1 to OMERO5.4DEV__2.
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

SELECT omero_assert_db_version('OMERO5.4DEV', 1);
DROP FUNCTION omero_assert_db_version(varchar, int);


--
-- Actual upgrade
--

INSERT INTO dbpatch (currentVersion, currentPatch, previousVersion, previousPatch)
             VALUES ('OMERO5.4DEV',  2,            'OMERO5.4DEV',   1);

-- Use secret key in setting originalfile.repo.

CREATE FUNCTION _protect_originalfile_repo_insert() RETURNS "trigger" AS $$

    DECLARE
        secret_key VARCHAR;
        secret_key_length INTEGER;
        is_good_change BOOLEAN := TRUE;

    BEGIN
        FOR secret_key IN SELECT uuid FROM node WHERE down IS NULL LOOP
            secret_key_length := LENGTH(secret_key);

            IF NEW.repo IS NULL THEN
                IF LEFT(NEW.name, secret_key_length) = secret_key THEN
                    NEW.name := RIGHT(NEW.name, -secret_key_length);
                END IF;
            ELSE
                IF LEFT(NEW.name, secret_key_length) = secret_key THEN
                    is_good_change := TRUE;
                    NEW.name := RIGHT(NEW.name, -secret_key_length);
                    EXIT;
                ELSE
                    is_good_change := FALSE;
                END IF;
            END IF;
        END LOOP;

        IF NOT is_good_change THEN
            RAISE EXCEPTION 'cannot set original repo property without secret key';
        END IF;

        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION _protect_originalfile_repo_update() RETURNS "trigger" AS $$

    DECLARE
        secret_key VARCHAR;
        secret_key_length INTEGER;
        is_good_change BOOLEAN := TRUE;

    BEGIN
        FOR secret_key IN SELECT uuid FROM node WHERE down IS NULL LOOP
            secret_key_length := LENGTH(secret_key);

            IF NEW.repo IS NULL OR OLD.repo = NEW.repo THEN
                IF LEFT(NEW.name, secret_key_length) = secret_key THEN
                    NEW.name := RIGHT(NEW.name, -secret_key_length);
                END IF;
            ELSE
                IF LEFT(NEW.name, secret_key_length) = secret_key THEN
                    is_good_change := TRUE;
                    NEW.name := RIGHT(NEW.name, -secret_key_length);
                    EXIT;
                ELSE
                    is_good_change := FALSE;
                END IF;
            END IF;
        END LOOP;

        IF NOT is_good_change THEN
            RAISE EXCEPTION 'cannot set original file repo property without secret key';
        END IF;

        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER _protect_originalfile_repo_insert
    BEFORE INSERT ON originalfile
    FOR EACH ROW EXECUTE PROCEDURE _protect_originalfile_repo_insert();

CREATE TRIGGER _protect_originalfile_repo_update
    BEFORE UPDATE ON originalfile
    FOR EACH ROW EXECUTE PROCEDURE _protect_originalfile_repo_update();

CREATE INDEX node_down ON node(down);

-- SET UNLOGGED requires 9.5 so recreate table instead

DROP TABLE _current_admin_privileges;

CREATE UNLOGGED TABLE _current_admin_privileges (
    transaction BIGINT,
    privilege VARCHAR(255)
);

CREATE INDEX i_current_admin_privileges_transactions ON _current_admin_privileges(transaction);


--
-- FINISHED
--

UPDATE dbpatch SET message = 'Database updated.', finished = clock_timestamp()
    WHERE id IN (SELECT id FROM dbpatch ORDER BY id DESC LIMIT 1);

SELECT E'\n\n\nYOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION ' ||
       currentversion || '__' || currentpatch || E'\n\n\n' AS Status FROM dbpatch
    WHERE id IN (SELECT id FROM dbpatch ORDER BY id DESC LIMIT 1);

COMMIT;
