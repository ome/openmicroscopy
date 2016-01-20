-- Copyright (C) 2014 Glencoe Software, Inc. All rights reserved.
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
--- OMERO5 development release upgrade from OMERO5.1__0 to OMERO5.1__1.
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

SELECT omero_assert_db_version('OMERO5.1', 0);
DROP FUNCTION omero_assert_db_version(varchar, int);

INSERT INTO dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
             VALUES ('OMERO5.1',     1,              'OMERO5.1',          0);

--
-- Actual upgrade
--

CREATE INDEX originalfile_hash_index ON originalfile (hash);

CREATE INDEX annotation_discriminator ON annotation(discriminator);
CREATE INDEX annotation_ns ON annotation(ns);

CREATE INDEX experimentergroup_config_name ON experimentergroup_config(name);
CREATE INDEX experimentergroup_config_value ON experimentergroup_config(value);
CREATE INDEX genericexcitationsource_map_name ON genericexcitationsource_map(name);
CREATE INDEX genericexcitationsource_map_value ON genericexcitationsource_map(value);
CREATE INDEX imagingenvironment_map_name ON imagingenvironment_map(name);
CREATE INDEX imagingenvironment_map_value ON imagingenvironment_map(value);
CREATE INDEX annotation_mapValue_name ON annotation_mapValue(name);
CREATE INDEX annotation_mapValue_value ON annotation_mapValue(value);
CREATE INDEX metadataimportjob_versionInfo_name ON metadataimportjob_versionInfo(name);
CREATE INDEX metadataimportjob_versionInfo_value ON metadataimportjob_versionInfo(value);
CREATE INDEX uploadjob_versionInfo_name ON uploadjob_versionInfo(name);
CREATE INDEX uploadjob_versionInfo_value ON uploadjob_versionInfo(value);

ALTER TABLE experimentergroup_config ALTER COLUMN name TYPE TEXT;
ALTER TABLE experimentergroup_config ALTER COLUMN value TYPE TEXT;
ALTER TABLE genericexcitationsource_map ALTER COLUMN name TYPE TEXT;
ALTER TABLE genericexcitationsource_map ALTER COLUMN value TYPE TEXT;
ALTER TABLE imagingenvironment_map ALTER COLUMN name TYPE TEXT;
ALTER TABLE imagingenvironment_map ALTER COLUMN value TYPE TEXT;
ALTER TABLE metadataimportjob_versionInfo ALTER COLUMN name TYPE TEXT;
ALTER TABLE metadataimportjob_versionInfo ALTER COLUMN value TYPE TEXT;
ALTER TABLE uploadjob_versionInfo ALTER COLUMN name TYPE TEXT;
ALTER TABLE uploadjob_versionInfo ALTER COLUMN value TYPE TEXT;

INSERT INTO eventlog (id, action, permissions, entityid, entitytype, event)
    SELECT ome_nextval('seq_eventlog'), 'REINDEX', -52, run.id, 'ome.model.screen.PlateAcquisition', 0
        FROM plateacquisition AS run;

--
-- FINISHED
--

UPDATE dbpatch SET message = 'Database updated.', finished = clock_timestamp()
    WHERE currentVersion  = 'OMERO5.1'    AND
          currentPatch    = 1             AND
          previousVersion = 'OMERO5.1'    AND
          previousPatch   = 0;

SELECT CHR(10)||CHR(10)||CHR(10)||'YOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION OMERO5.1__1'||CHR(10)||CHR(10)||CHR(10) AS Status;

COMMIT;
