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
--- OMERO5 development release upgrade from OMERO5.2DEV__0 to OMERO5.2__0.
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

-- https://trello.com/c/EwmJfe5u/89-critical-indexing-large-annotation-changes

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
    WHERE currentVersion  = 'OMERO5.2'    AND
          currentPatch    = 0             AND
          previousVersion = 'OMERO5.2DEV' AND
          previousPatch   = 0;

SELECT CHR(10)||CHR(10)||CHR(10)||'YOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION OMERO5.2__0'||CHR(10)||CHR(10)||CHR(10) AS Status;

COMMIT;
