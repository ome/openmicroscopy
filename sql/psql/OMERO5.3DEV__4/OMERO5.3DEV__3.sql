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
--- OMERO5 development release upgrade from OMERO5.3DEV__3 to OMERO5.3DEV__4.
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

SELECT omero_assert_db_version('OMERO5.3DEV', 3);
DROP FUNCTION omero_assert_db_version(varchar, int);


--
-- Actual upgrade
--

INSERT INTO dbpatch (currentVersion, currentPatch, previousVersion, previousPatch)
             VALUES ('OMERO5.3DEV',  4,            'OMERO5.3DEV',   3);

UPDATE pixels SET sha1 = 'Pending...' WHERE sha1 = 'Foo';

DELETE FROM annotation WHERE
    discriminator IN
        ('/basic/bool/', '/basic/num/double/', '/basic/num/long/',
         '/basic/term/', '/basic/time/', '/basic/text/comment/')
    AND id NOT IN (SELECT parent FROM annotationannotationlink)
    AND id NOT IN (SELECT child FROM annotationannotationlink)
    AND id NOT IN (SELECT child FROM channelannotationlink)
    AND id NOT IN (SELECT child FROM datasetannotationlink)
    AND id NOT IN (SELECT child FROM detectorannotationlink)
    AND id NOT IN (SELECT child FROM dichroicannotationlink)
    AND id NOT IN (SELECT child FROM experimenterannotationlink)
    AND id NOT IN (SELECT child FROM experimentergroupannotationlink)
    AND id NOT IN (SELECT child FROM filesetannotationlink)
    AND id NOT IN (SELECT child FROM filterannotationlink)
    AND id NOT IN (SELECT child FROM folderannotationlink)
    AND id NOT IN (SELECT child FROM imageannotationlink)
    AND id NOT IN (SELECT child FROM instrumentannotationlink)
    AND id NOT IN (SELECT child FROM lightpathannotationlink)
    AND id NOT IN (SELECT child FROM lightsourceannotationlink)
    AND id NOT IN (SELECT child FROM namespaceannotationlink)
    AND id NOT IN (SELECT child FROM nodeannotationlink)
    AND id NOT IN (SELECT child FROM objectiveannotationlink)
    AND id NOT IN (SELECT child FROM originalfileannotationlink)
    AND id NOT IN (SELECT child FROM planeinfoannotationlink)
    AND id NOT IN (SELECT child FROM plateacquisitionannotationlink)
    AND id NOT IN (SELECT child FROM plateannotationlink)
    AND id NOT IN (SELECT child FROM projectannotationlink)
    AND id NOT IN (SELECT child FROM reagentannotationlink)
    AND id NOT IN (SELECT child FROM roiannotationlink)
    AND id NOT IN (SELECT child FROM screenannotationlink)
    AND id NOT IN (SELECT child FROM sessionannotationlink)
    AND id NOT IN (SELECT child FROM shapeannotationlink)
    AND id NOT IN (SELECT child FROM wellannotationlink);


--
-- FINISHED
--

UPDATE dbpatch SET message = 'Database updated.', finished = clock_timestamp()
    WHERE currentVersion  = 'OMERO5.3DEV' AND
          currentPatch    = 4             AND
          previousVersion = 'OMERO5.3DEV' AND
          previousPatch   = 3;

SELECT CHR(10)||CHR(10)||CHR(10)||'YOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION OMERO5.3DEV__4'||CHR(10)||CHR(10)||CHR(10) AS Status;

COMMIT;
