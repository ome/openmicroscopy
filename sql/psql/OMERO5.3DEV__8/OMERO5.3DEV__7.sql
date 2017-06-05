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
--- OMERO5 development release upgrade from OMERO5.3DEV__7 to OMERO5.3DEV__8.
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

SELECT omero_assert_db_version('OMERO5.3DEV', 7);
DROP FUNCTION omero_assert_db_version(varchar, int);


--
-- Actual upgrade
--

INSERT INTO dbpatch (currentVersion, currentPatch, previousVersion, previousPatch)
             VALUES ('OMERO5.3DEV',  8,            'OMERO5.3DEV',   7);

ALTER TABLE session ALTER COLUMN userip TYPE VARCHAR(45);

CREATE TABLE experimenter_config (
    experimenter_id BIGINT NOT NULL,
    name TEXT NOT NULL,
    value TEXT NOT NULL,
    index INTEGER NOT NULL,
    PRIMARY KEY (experimenter_id, index),
    CONSTRAINT FKexperimenter_config_map
        FOREIGN KEY (experimenter_id) REFERENCES experimenter);

CREATE FUNCTION experimenter_config_map_entry_delete_trigger_function() RETURNS "trigger" AS '
BEGIN
    DELETE FROM experimenter_config
        WHERE experimenter_id = OLD.id;
    RETURN OLD;
END;'
LANGUAGE plpgsql;

CREATE TRIGGER experimenter_config_map_entry_delete_trigger
    BEFORE DELETE ON experimenter
    FOR EACH ROW
    EXECUTE PROCEDURE experimenter_config_map_entry_delete_trigger_function();

CREATE INDEX experimenter_config_name ON experimenter_config(name);
CREATE INDEX experimenter_config_value ON experimenter_config(value);


--
-- FINISHED
--

UPDATE dbpatch SET message = 'Database updated.', finished = clock_timestamp()
    WHERE currentVersion  = 'OMERO5.3DEV' AND
          currentPatch    = 8             AND
          previousVersion = 'OMERO5.3DEV' AND
          previousPatch   = 7;

SELECT CHR(10)||CHR(10)||CHR(10)||'YOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION OMERO5.3DEV__8'||CHR(10)||CHR(10)||CHR(10) AS Status;

COMMIT;
