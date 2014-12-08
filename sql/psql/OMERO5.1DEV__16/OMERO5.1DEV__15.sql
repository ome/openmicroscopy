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
--- OMERO5 development release upgrade from OMERO5.1DEV__15 to OMERO5.1DEV__16.
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

SELECT omero_assert_db_version('OMERO5.1DEV', 15);
DROP FUNCTION omero_assert_db_version(varchar, int);


INSERT INTO dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
             VALUES ('OMERO5.1DEV',  16,                'OMERO5.1DEV',    15);

--
-- Actual upgrade
--

-- drop all constraints for the map tables

ALTER TABLE annotation_mapvalue
	DROP CONSTRAINT annotation_mapvalue_pkey;

ALTER TABLE experimentergroup_config
	DROP CONSTRAINT experimentergroup_config_pkey;

ALTER TABLE genericexcitationsource_map
	DROP CONSTRAINT genericexcitationsource_map_pkey;

ALTER TABLE imagingenvironment_map
	DROP CONSTRAINT imagingenvironment_map_pkey;

ALTER TABLE metadataimportjob_versioninfo
	DROP CONSTRAINT metadataimportjob_versioninfo_pkey;

ALTER TABLE uploadjob_versioninfo
	DROP CONSTRAINT uploadjob_versioninfo_pkey;

-- annotation_mapvalue

ALTER TABLE annotation_mapvalue
	ADD COLUMN index integer;

UPDATE annotation_mapvalue
    SET index = x.rn
   FROM (
        SELECT -1 + row_number() over (partition by annotation_id order by mapvalue_key) as rn,
               annotation_id, mapvalue_key
          FROM annotation_mapvalue) as x
  WHERE annotation_mapvalue.annotation_id = x.annotation_id
    AND annotation_mapvalue.mapvalue_key = x.mapvalue_key;

ALTER TABLE annotation_mapvalue
    ALTER COLUMN index SET NOT NULL;

ALTER TABLE annotation_mapvalue
	RENAME COLUMN mapvalue_key TO name;

ALTER TABLE annotation_mapvalue
	RENAME COLUMN mapvalue TO "value";

-- experimentergroup_config

ALTER TABLE experimentergroup_config
	ADD COLUMN index integer;

UPDATE experimentergroup_config
    SET index = x.rn
   FROM (
        SELECT -1 + row_number() over (partition by experimentergroup_id order by config_key) as rn,
               experimentergroup_id, config_key
          FROM experimentergroup_config) as x
  WHERE experimentergroup_config.experimentergroup_id = x.experimentergroup_id
    AND experimentergroup_config.config_key = x.config_key;

ALTER TABLE experimentergroup_config
	RENAME COLUMN config_key to name;

ALTER TABLE experimentergroup_config
	RENAME COLUMN config TO "value";

ALTER TABLE experimentergroup_config
    ALTER COLUMN index SET NOT NULL;

-- genericexcitation_map

ALTER TABLE genericexcitationsource_map
	ADD COLUMN index integer;

UPDATE genericexcitationsource_map
    SET index = x.rn
   FROM (
        SELECT -1 + row_number() over (partition by genericexcitationsource_id order by map_key) as rn,
               genericexcitationsource_id, map_key
          FROM genericexcitationsource_map) as x
  WHERE genericexcitationsource_map.genericexcitationsource_id = x.genericexcitationsource_id
    AND genericexcitationsource_map.map_key = x.map_key;

ALTER TABLE genericexcitationsource_map
	RENAME COLUMN map_key TO name;

ALTER TABLE genericexcitationsource_map
	RENAME COLUMN "map" TO "value";

ALTER TABLE genericexcitationsource_map
    ALTER COLUMN index SET NOT NULL;

-- imagingenvironment_map

ALTER TABLE imagingenvironment_map
	ADD COLUMN index integer;

UPDATE imagingenvironment_map
    SET index = x.rn
   FROM (
        SELECT -1 + row_number() over (partition by imagingenvironment_id order by map_key) as rn,
               imagingenvironment_id, map_key
          FROM imagingenvironment_map) as x
  WHERE imagingenvironment_map.imagingenvironment_id = x.imagingenvironment_id
    AND imagingenvironment_map.map_key = x.map_key;

ALTER TABLE imagingenvironment_map
	RENAME COLUMN map_key TO name;

ALTER TABLE imagingenvironment_map
	RENAME COLUMN "map" TO "value";

ALTER TABLE imagingenvironment_map
    ALTER COLUMN index SET NOT NULL;

-- metadataimportjob_versioninfo

ALTER TABLE metadataimportjob_versioninfo
	ADD COLUMN index integer;

UPDATE metadataimportjob_versioninfo
    SET index = x.rn
   FROM (
        SELECT -1 + row_number() over (partition by metadataimportjob_id order by versioninfo_key) as rn,
               metadataimportjob_id, versioninfo_key
          FROM metadataimportjob_versioninfo) as x
  WHERE metadataimportjob_versioninfo.metadataimportjob_id = x.metadataimportjob_id
    AND metadataimportjob_versioninfo.versioninfo_key = x.versioninfo_key;

ALTER TABLE metadataimportjob_versioninfo
	RENAME COLUMN versioninfo_key TO name;

ALTER TABLE metadataimportjob_versioninfo
	RENAME COLUMN versioninfo TO "value";

ALTER TABLE metadataimportjob_versioninfo
    ALTER COLUMN index SET NOT NULL;

-- uploadjob_versioninfo

ALTER TABLE uploadjob_versioninfo
	ADD COLUMN index integer;

UPDATE uploadjob_versioninfo
    SET index = x.rn
   FROM (
        SELECT -1 + row_number() over (partition by uploadjob_id order by versioninfo_key) as rn,
               uploadjob_id, versioninfo_key
          FROM uploadjob_versioninfo) as x
  WHERE uploadjob_versioninfo.uploadjob_id = x.uploadjob_id
    AND uploadjob_versioninfo.versioninfo_key = x.versioninfo_key;

ALTER TABLE uploadjob_versioninfo
	RENAME COLUMN versioninfo_key TO name;

ALTER TABLE uploadjob_versioninfo
	RENAME COLUMN versioninfo TO "value";

ALTER TABLE uploadjob_versioninfo
    ALTER COLUMN index SET NOT NULL;

-- add new constraints

ALTER TABLE annotation_mapvalue
	ADD CONSTRAINT annotation_mapvalue_pkey PRIMARY KEY (annotation_id, index);

ALTER TABLE experimentergroup_config
	ADD CONSTRAINT experimentergroup_config_pkey PRIMARY KEY (experimentergroup_id, index);

ALTER TABLE genericexcitationsource_map
	ADD CONSTRAINT genericexcitationsource_map_pkey PRIMARY KEY (genericexcitationsource_id, index);

ALTER TABLE imagingenvironment_map
	ADD CONSTRAINT imagingenvironment_map_pkey PRIMARY KEY (imagingenvironment_id, index);

ALTER TABLE metadataimportjob_versioninfo
	ADD CONSTRAINT metadataimportjob_versioninfo_pkey PRIMARY KEY (metadataimportjob_id, index);

ALTER TABLE uploadjob_versioninfo
	ADD CONSTRAINT uploadjob_versioninfo_pkey PRIMARY KEY (uploadjob_id, index);

--
-- FINISHED
--

UPDATE dbpatch SET message = 'Database updated.', finished = clock_timestamp()
    WHERE currentVersion  = 'OMERO5.1DEV' AND
          currentPatch    = 16            AND
          previousVersion = 'OMERO5.1DEV' AND
          previousPatch   = 15;

SELECT CHR(10)||CHR(10)||CHR(10)||'YOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION OMERO5.1DEV__16'||CHR(10)||CHR(10)||CHR(10) AS Status;

COMMIT;
