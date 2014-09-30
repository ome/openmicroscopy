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
--- OMERO5 development release upgrade from OMERO5.1DEV__9 to OMERO5.1DEV__10.
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

SELECT omero_assert_db_version('OMERO5.1DEV', 9);
DROP FUNCTION omero_assert_db_version(varchar, int);


INSERT INTO dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
             VALUES ('OMERO5.1DEV',    10,              'OMERO5.1DEV',       9);

--
-- Actual upgrade
--

-- #970 adjust constraint for dbpatch versions/patches

ALTER TABLE dbpatch DROP CONSTRAINT unique_dbpatch;

CREATE FUNCTION dbpatch_versions_trigger_function() RETURNS TRIGGER AS $$
BEGIN
    IF (NEW.currentversion <> NEW.previousversion OR NEW.currentpatch <> NEW.previouspatch) AND
       (SELECT COUNT(*) FROM dbpatch WHERE id <> NEW.id AND
        (currentversion <> previousversion OR currentpatch <> previouspatch) AND
        ((currentversion = NEW.currentversion AND currentpatch = NEW.currentpatch) OR
         (previousversion = NEW.previousversion AND previouspatch = NEW.previouspatch))) > 0 THEN
        RAISE 'upgrades cannot be repeated';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER dbpatch_versions_trigger
    BEFORE INSERT OR UPDATE ON dbpatch
    FOR EACH ROW
    EXECUTE PROCEDURE dbpatch_versions_trigger_function();

-- expand password hash and note password change dates

ALTER TABLE password ALTER COLUMN hash TYPE VARCHAR(255);
ALTER TABLE password ADD COLUMN changed TIMESTAMP WITHOUT TIME ZONE;

-- fill in password change dates from event log

CREATE FUNCTION update_changed_from_event_log() RETURNS void AS $$

DECLARE
    exp_id BIGINT;
    time_changed TIMESTAMP WITHOUT TIME ZONE;

BEGIN
    FOR exp_id IN
        SELECT DISTINCT ev.experimenter 
            FROM event ev, eventlog log, experimenter ex
            WHERE log.action = 'PASSWORD' AND ex.omename <> 'root'
            AND ev.id = log.event AND ev.experimenter = ex.id LOOP

        SELECT ev.time
            INTO STRICT time_changed
            FROM event ev, eventlog log
            WHERE log.action = 'PASSWORD' AND ev.experimenter = exp_id
            AND ev.id = log.event
            ORDER BY log.id DESC LIMIT 1;
       
        UPDATE password SET changed = time_changed
            WHERE experimenter_id = exp_id;
    END LOOP;

END;
$$ LANGUAGE plpgsql;

SELECT update_changed_from_event_log();

DROP FUNCTION update_changed_from_event_log();

--
-- FINISHED
--

UPDATE dbpatch SET message = 'Database updated.', finished = clock_timestamp()
    WHERE currentVersion  = 'OMERO5.1DEV' AND
          currentPatch    = 10            AND
          previousVersion = 'OMERO5.1DEV' AND
          previousPatch   = 9;

SELECT CHR(10)||CHR(10)||CHR(10)||'YOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION OMERO5.1DEV__10'||CHR(10)||CHR(10)||CHR(10) AS Status;

COMMIT;
