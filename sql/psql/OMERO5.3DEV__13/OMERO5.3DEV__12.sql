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
--- OMERO5 development release upgrade from OMERO5.3DEV__12 to OMERO5.3DEV__13.
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

SELECT omero_assert_db_version('OMERO5.3DEV', 12);
DROP FUNCTION omero_assert_db_version(varchar, int);


--
-- Actual upgrade
--

INSERT INTO dbpatch (currentVersion, currentPatch, previousVersion, previousPatch)
             VALUES ('OMERO5.3DEV',  13,           'OMERO5.3DEV',   12);

ALTER TABLE annotation ALTER COLUMN name TYPE TEXT;
ALTER TABLE annotation ALTER COLUMN ns TYPE TEXT;
ALTER TABLE channel ALTER COLUMN lookuptable TYPE TEXT;
ALTER TABLE dataset ALTER COLUMN name TYPE TEXT;
ALTER TABLE folder ALTER COLUMN name TYPE TEXT;
ALTER TABLE image ALTER COLUMN name TYPE TEXT;
ALTER TABLE importjob ALTER COLUMN imagename TYPE TEXT;
ALTER TABLE importjob ALTER COLUMN imagedescription TYPE TEXT;
ALTER TABLE logicalchannel ALTER COLUMN name TYPE TEXT;
ALTER TABLE namespace ALTER COLUMN name TYPE TEXT;
ALTER TABLE namespace ALTER COLUMN displayname TYPE TEXT;
ALTER TABLE originalfile ALTER COLUMN name TYPE TEXT;
ALTER TABLE originalfile ALTER COLUMN hash TYPE TEXT;
ALTER TABLE plate ALTER COLUMN name TYPE TEXT;
ALTER TABLE plate ALTER COLUMN status TYPE TEXT;
ALTER TABLE plateacquisition ALTER COLUMN name TYPE TEXT;
ALTER TABLE project ALTER COLUMN name TYPE TEXT;
ALTER TABLE reagent ALTER COLUMN name TYPE TEXT;
ALTER TABLE renderingdef ALTER COLUMN name TYPE TEXT;
ALTER TABLE roi ALTER COLUMN name TYPE TEXT;
ALTER TABLE screen ALTER COLUMN name TYPE TEXT;
ALTER TABLE screen ALTER COLUMN protocoldescription TYPE TEXT;
ALTER TABLE screen ALTER COLUMN reagentsetdescription TYPE TEXT;
ALTER TABLE stagelabel ALTER COLUMN name TYPE TEXT;
ALTER TABLE well ALTER COLUMN externaldescription TYPE TEXT;


--
-- FINISHED
--

UPDATE dbpatch SET message = 'Database updated.', finished = clock_timestamp()
    WHERE currentVersion  = 'OMERO5.3DEV' AND
          currentPatch    = 13            AND
          previousVersion = 'OMERO5.3DEV' AND
          previousPatch   = 12;

SELECT CHR(10)||CHR(10)||CHR(10)||'YOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION OMERO5.3DEV__13'||CHR(10)||CHR(10)||CHR(10) AS Status;

COMMIT;
