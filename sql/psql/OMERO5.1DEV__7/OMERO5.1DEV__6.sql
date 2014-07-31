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
--- OMERO5 development release upgrade from OMERO5.1DEV__6 to OMERO5.1DEV__7.
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

SELECT omero_assert_db_version('OMERO5.1DEV', 6);
DROP FUNCTION omero_assert_db_version(varchar, int);


INSERT INTO dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
             VALUES ('OMERO5.1DEV',     7,              'OMERO5.1DEV',       6);

--
-- Actual upgrade
--

-- Add new checksum algorithm to enumeration.

INSERT INTO checksumalgorithm (id, permissions, value) 
    SELECT ome_nextval('seq_checksumalgorithm'), -52, 'File-Size-64'
    WHERE NOT EXISTS (SELECT id FROM checksumalgorithm WHERE value = 'File-Size-64');

-- Reverse endianness of hashes calculated with adjusted algorithms.

CREATE FUNCTION reverse_endian(forward TEXT) RETURNS TEXT AS $$

DECLARE
    index INTEGER := length(forward) - 1;
    backward TEXT := '';

BEGIN
    WHILE index > 0 LOOP
        backward := backward || substring(forward FROM index FOR 2);
        index := index - 2;
    END LOOP;
    IF index = 0 THEN
        RAISE 'cannot reverse strings of odd length';
    END IF;
    RETURN backward;
END;
$$ LANGUAGE plpgsql;

UPDATE originalfile SET hash = reverse_endian(hash)
    WHERE hash IS NOT NULL AND hasher IN
    (SELECT id FROM checksumalgorithm WHERE value IN ('Adler-32', 'CRC-32'));

DROP FUNCTION reverse_endian(TEXT);

-- Acquisition date is already optional in XML schema.

ALTER TABLE image ALTER COLUMN acquisitiondate DROP NOT NULL;

--
-- FINISHED
--

UPDATE dbpatch SET message = 'Database updated.', finished = clock_timestamp()
    WHERE currentVersion  = 'OMERO5.1DEV' AND
          currentPatch    = 7             AND
          previousVersion = 'OMERO5.1DEV' AND
          previousPatch   = 6;

SELECT CHR(10)||CHR(10)||CHR(10)||'YOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION OMERO5.1DEV__7'||CHR(10)||CHR(10)||CHR(10) AS Status;

COMMIT;
