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
--- OMERO5 development release upgrade from OMERO5.1DEV__11 to OMERO5.1DEV__12.
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

SELECT omero_assert_db_version('OMERO5.1DEV', 11);
DROP FUNCTION omero_assert_db_version(varchar, int);


INSERT INTO dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
             VALUES ('OMERO5.1DEV',    12,              'OMERO5.1DEV',       11);

--
-- Actual upgrade
--

-- #2587 LDAP: remove DN from OMERO DB.

-- Add "ldap" column to "experimenter", default to false

ALTER TABLE experimenter ADD COLUMN ldap BOOL NOT NULL DEFAULT false;

-- Set "ldap" value based on "dn" from "password"

UPDATE experimenter e SET ldap = true
    FROM password p
    WHERE e.id = p.experimenter_id AND
          p.dn IS NOT NULL;

-- Drop "dn" from "password" and delete entries that have a DN set
-- and no password

DELETE FROM password WHERE dn IS NOT NULL AND hash IS NULL;
ALTER TABLE password DROP COLUMN dn;

--
-- FINISHED
--

UPDATE dbpatch SET message = 'Database updated.', finished = clock_timestamp()
    WHERE currentVersion  = 'OMERO5.1DEV' AND
          currentPatch    = 12            AND
          previousVersion = 'OMERO5.1DEV' AND
          previousPatch   = 11;

SELECT CHR(10)||CHR(10)||CHR(10)||'YOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION OMERO5.1DEV__12'||CHR(10)||CHR(10)||CHR(10) AS Status;

COMMIT;
