--
-- Copyright 2011 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

---
--- OMERO-Beta4.3 release upgrade from OMERO4.2__0 to OMERO4.3__0
---

BEGIN;

\timing

-- Requirements:
--  * Applies only to OMERO4.2__0
--
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

SELECT omero_assert_db_version('OMERO4.2',0);
DROP FUNCTION omero_assert_db_version(varchar, int);


INSERT into dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
             values ('OMERO4.3',     0,              'OMERO4.2',          0);


--
-- #2694 Remove pg_geom
--

ALTER TABLE shape DROP COLUMN pg_geom;

--
-- FINISHED
--

UPDATE dbpatch set message = 'Database updated.', finished = now()
 WHERE currentVersion  = 'OMERO4.3'    and
          currentPatch    = 0          and
          previousVersion = 'OMERO4.2' and
          previousPatch   = 0;

COMMIT;
