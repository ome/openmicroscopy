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
--- OMERO5 release upgrade from OMERO5.4DEV__3 to OMERO5.4__0.
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

SELECT omero_assert_db_version('OMERO5.4DEV', 3);
DROP FUNCTION omero_assert_db_version(varchar, int);


--
-- Actual upgrade
--

INSERT INTO dbpatch (currentVersion, currentPatch, previousVersion, previousPatch)
             VALUES ('OMERO5.4',     0,            'OMERO5.4DEV',   3);

-- patch against 2017-SV5 regardless of if already patched

DROP INDEX originalfile_repo_path_index;

CREATE UNIQUE INDEX originalfile_repo_path_index ON originalfile
    (repo, regexp_split_to_array('/' || path || name || '/', '/+'))
    WHERE repo IS NOT NULL;


--
-- FINISHED
--

UPDATE dbpatch SET message = 'Database updated.', finished = clock_timestamp()
    WHERE id IN (SELECT id FROM dbpatch ORDER BY id DESC LIMIT 1);

SELECT E'\n\n\nYOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION ' ||
       currentversion || '__' || currentpatch || E'\n\n\n' AS Status FROM dbpatch
    WHERE id IN (SELECT id FROM dbpatch ORDER BY id DESC LIMIT 1);

COMMIT;
