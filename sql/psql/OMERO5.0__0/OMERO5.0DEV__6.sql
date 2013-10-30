-- Copyright (C) 2012-3 Glencoe Software, Inc. All rights reserved.
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
--- OMERO5 development release upgrade from OMERO5.0DEV__6 to OMERO5.0__0.
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

SELECT omero_assert_db_version('OMERO5.0DEV', 6);
DROP FUNCTION omero_assert_db_version(varchar, int);


INSERT INTO dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
             VALUES ('OMERO5.0',     0,              'OMERO5.0DEV',       6);

--
-- Actual upgrade
--

-- Prevent Directory entries in the originalfile table from having their mimetype changed.
CREATE FUNCTION _fs_directory_mimetype() RETURNS "trigger" AS $$
    BEGIN
        IF OLD.mimetype = 'Directory' AND NEW.mimetype != 'Directory' THEN
            RAISE EXCEPTION '%%', 'Directory('||OLD.id||')='||OLD.path||OLD.name||'/ must remain a Directory';
        END IF;
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER _fs_directory_mimetype
    BEFORE UPDATE ON originalfile
    FOR EACH ROW EXECUTE PROCEDURE _fs_directory_mimetype();

--
-- FINISHED
--

UPDATE dbpatch SET message = 'Database updated.', finished = clock_timestamp()
    WHERE currentVersion  = 'OMERO5.0'    AND
          currentPatch    = 0             AND
          previousVersion = 'OMERO5.0DEV' AND
          previousPatch   = 6;

SELECT CHR(10)||CHR(10)||CHR(10)||'YOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION OMERO5.0__0'||CHR(10)||CHR(10)||CHR(10) AS Status;

COMMIT;
