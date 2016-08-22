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
--- OMERO5 development release upgrade from OMERO5.3DEV__9 to OMERO5.3DEV__10.
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

SELECT omero_assert_db_version('OMERO5.3DEV', 9);
DROP FUNCTION omero_assert_db_version(varchar, int);


--
-- Actual upgrade
--

INSERT INTO dbpatch (currentVersion, currentPatch, previousVersion, previousPatch)
             VALUES ('OMERO5.3DEV',  10,            'OMERO5.3DEV',   9);


ALTER TABLE codomainmapcontext DROP CONSTRAINT FKcodomainmapcontext_renderingDef_renderingdef;
ALTER TABLE codomainmapcontext DROP renderingdef;
ALTER TABLE codomainmapcontext DROP renderingdef_index;

DROP TRIGGER codomainmapcontext_renderingDef_index_trigger ON codomainmapcontext;
DROP FUNCTION codomainmapcontext_renderingDef_index_move();

ALTER TABLE codomainmapcontext ADD channelBinding int8;
ALTER TABLE codomainmapcontext ADD channelBinding_index int4;

ALTER TABLE codomainmapcontext
ADD CONSTRAINT FKcodomainmapcontext_channelBinding_channelbinding
FOREIGN KEY (channelBinding)
REFERENCES channelbinding;

CREATE OR REPLACE FUNCTION codomainmapcontext_channelBinding_index_move() RETURNS "trigger" AS '
    DECLARE
      duplicate INT8;
    BEGIN

      -- Avoids a query if the new and old values of x are the same.
      IF new.channelBinding = old.channelBinding AND new.channelBinding_index = old.channelBinding_index THEN
          RETURN new;
      END IF;

      -- At most, there should be one duplicate
      SELECT id INTO duplicate
        FROM codomainmapcontext
       WHERE channelBinding = new.channelBinding AND channelBinding_index = new.channelBinding_index
      OFFSET 0
       LIMIT 1;

      IF duplicate IS NOT NULL THEN
          RAISE NOTICE ''Remapping codomainmapcontext %% via (-1 - oldvalue )'', duplicate;
          UPDATE codomainmapcontext SET channelBinding_index = -1 - channelBinding_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;' LANGUAGE plpgsql;

CREATE TRIGGER codomainmapcontext_channelBinding_index_trigger
    BEFORE UPDATE ON codomainmapcontext
    FOR EACH ROW EXECUTE PROCEDURE codomainmapcontext_channelBinding_index_move ();

CREATE INDEX i_CodomainMapContext_channelBinding ON codomainmapcontext(channelBinding);

--
-- FINISHED
--

UPDATE dbpatch SET message = 'Database updated.', finished = clock_timestamp()
    WHERE currentVersion  = 'OMERO5.3DEV' AND
          currentPatch    = 10             AND
          previousVersion = 'OMERO5.3DEV' AND
          previousPatch   = 9;

SELECT CHR(10)||CHR(10)||CHR(10)||'YOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION OMERO5.3DEV__10'||CHR(10)||CHR(10)||CHR(10) AS Status;

COMMIT;
