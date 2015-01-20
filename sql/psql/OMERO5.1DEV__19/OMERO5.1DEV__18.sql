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
--- OMERO5 development release upgrade from OMERO5.1DEV__18 to OMERO5.1DEV__19.
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

SELECT omero_assert_db_version('OMERO5.1DEV', 18);
DROP FUNCTION omero_assert_db_version(varchar, int);


--
-- Actual upgrade
--

-- The namespace table now reflects all the namespaces used in the annotation.ns column.

CREATE FUNCTION add_to_namespace() RETURNS "trigger" AS $$
    BEGIN
        IF NEW.ns IS NOT NULL THEN
            UPDATE namespace SET update_id = NEW.update_id WHERE name = NEW.ns;

            IF NOT FOUND THEN
                INSERT INTO namespace (id, name, permissions, creation_id, update_id, owner_id, group_id)
                    SELECT ome_nextval('seq_namespace'), NEW.ns, -52, NEW.update_id, NEW.update_id, NEW.owner_id, 1;
            END IF;
        END IF;

        RETURN NULL;
    END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION delete_from_namespace() RETURNS "trigger" AS $$
    BEGIN
        IF EXISTS (SELECT 1 FROM annotation WHERE ns = OLD.name LIMIT 1) THEN
            RAISE EXCEPTION 'cannot delete namespace that is still used by annotation';
        END IF;

        RETURN OLD;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER add_to_namespace
    AFTER INSERT OR UPDATE ON annotation
    FOR EACH ROW EXECUTE PROCEDURE add_to_namespace();

CREATE TRIGGER delete_from_namespace
    BEFORE DELETE ON namespace
    FOR EACH ROW EXECUTE PROCEDURE delete_from_namespace();

INSERT INTO namespace (id, name, permissions, creation_id, update_id, owner_id, group_id)
    SELECT ome_nextval('seq_namespace'), ns, -52, update_id, update_id, owner_id, 1
        FROM annotation WHERE id IN
             (SELECT id_row.id 
                  FROM (SELECT id, row_number() OVER (PARTITION BY ns) AS row_n FROM annotation
                            WHERE ns IS NOT NULL AND ns NOT IN (SELECT name FROM namespace)) AS id_row
                  WHERE id_row.row_n = 1);


--
-- FINISHED
--

UPDATE dbpatch SET message = 'Database updated.', finished = clock_timestamp()
    WHERE currentVersion  = 'OMERO5.1DEV' AND
          currentPatch    = 19            AND
          previousVersion = 'OMERO5.1DEV' AND
          previousPatch   = 18;

SELECT CHR(10)||CHR(10)||CHR(10)||'YOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION OMERO5.1DEV__19'||CHR(10)||CHR(10)||CHR(10) AS Status;

COMMIT;
