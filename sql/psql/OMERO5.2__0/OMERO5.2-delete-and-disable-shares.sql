-- Copyright (C) 2016 Glencoe Software, Inc. All rights reserved.
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
--- OMERO5 in-place upgrade for the OMERO5.2__0 database to disable shares.
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

SELECT omero_assert_db_version('OMERO5.2', 0);
DROP FUNCTION omero_assert_db_version(varchar, int);


--
-- Actual upgrade
--


DELETE FROM sharemember;
DELETE FROM share;
CREATE OR REPLACE FUNCTION disable_shares() RETURNS trigger LANGUAGE plpgsql as $$
BEGIN
      RAISE EXCEPTION 'Shares are disabled';
END $$;
DROP TRIGGER IF EXISTS disable_shares ON share;
CREATE TRIGGER disable_shares BEFORE INSERT ON share FOR EACH ROW EXECUTE PROCEDURE disable_shares();


--
-- FINISHED
--

UPDATE dbpatch SET message = 'Shares disabled.', finished = clock_timestamp()
    WHERE currentVersion  = 'OMERO5.2'    AND
          currentPatch    = 0             AND
          previousVersion = 'OMERO5.2'    AND
          previousPatch   = 0;

SELECT CHR(10)||CHR(10)||CHR(10)||'SHARES HAVE BEEN DISABLED FOR OMERO5.2__0'||CHR(10)||CHR(10)||CHR(10) AS Status;

COMMIT;
