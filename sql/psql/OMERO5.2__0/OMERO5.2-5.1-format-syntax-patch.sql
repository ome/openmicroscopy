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
--- Fix format string syntax in PL/pgSQL functions for OMERO 5.1 and 5.2.
--- The upgrade to OMERO 5.3 also performs this fix.
---

BEGIN;


--
-- check OMERO database version
--

CREATE OR REPLACE FUNCTION omero_check_db_version() RETURNS void AS $$

DECLARE
    current_version VARCHAR;

BEGIN
    IF NOT EXISTS (SELECT id FROM dbpatch WHERE currentversion = 'OMERO4.4') THEN
        RAISE 'this database does not need to be patched';
    END IF;

    SELECT currentversion INTO STRICT current_version
        FROM dbpatch ORDER BY id DESC LIMIT 1;

    IF NOT current_version IN ('OMERO5.1', 'OMERO5.2') THEN
        RAISE 'wrong OMERO database version for this patch script';
    END IF;

END;$$ LANGUAGE plpgsql;

SELECT omero_check_db_version();
DROP FUNCTION omero_check_db_version();


--
-- Actual patch
--

INSERT INTO dbpatch (currentversion, currentpatch, previousversion, previouspatch, message)
    (SELECT currentversion, currentpatch, currentversion, currentpatch,
         'Patched function syntax.'
         FROM dbpatch ORDER BY id DESC LIMIT 1);

CREATE OR REPLACE FUNCTION filesetentry_fileset_index_move() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    DECLARE
      duplicate INT8;
    BEGIN

      -- Avoids a query if the new and old values of x are the same.
      IF new.fileset = old.fileset AND new.fileset_index = old.fileset_index THEN
          RETURN new;
      END IF;

      -- At most, there should be one duplicate
      SELECT id INTO duplicate
        FROM filesetentry
       WHERE fileset = new.fileset AND fileset_index = new.fileset_index
      OFFSET 0
       LIMIT 1;

      IF duplicate IS NOT NULL THEN
          RAISE NOTICE 'Remapping filesetentry % via (-1 - oldvalue )', duplicate;
          UPDATE filesetentry SET fileset_index = -1 - fileset_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;$$;

CREATE OR REPLACE FUNCTION filesetjoblink_parent_index_move() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    DECLARE
      duplicate INT8;
    BEGIN

      -- Avoids a query if the new and old values of x are the same.
      IF new.parent = old.parent AND new.parent_index = old.parent_index THEN
          RETURN new;
      END IF;

      -- At most, there should be one duplicate
      SELECT id INTO duplicate
        FROM filesetjoblink
       WHERE parent = new.parent AND parent_index = new.parent_index
       OFFSET 0
       LIMIT 1;

      IF duplicate IS NOT NULL THEN
          RAISE NOTICE 'Remapping filesetjoblink % via (-1 - oldvalue )', duplicate;
          UPDATE filesetjoblink SET parent_index = -1 - parent_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;$$;


--
-- FINISHED
--

UPDATE dbpatch SET finished = clock_timestamp()
    WHERE id IN (SELECT id FROM dbpatch ORDER BY id DESC LIMIT 1);

SELECT CHR(10)||CHR(10)||CHR(10)||'YOU HAVE SUCCESSFULLY PATCHED YOUR DATABASE'||CHR(10)||CHR(10)||CHR(10) AS Status;

COMMIT;
