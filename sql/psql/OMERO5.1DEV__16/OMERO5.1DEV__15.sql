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

ALTER TABLE annotation_mapvalue
	RENAME COLUMN mapvalue_key TO name;

ALTER TABLE annotation_mapvalue
	RENAME COLUMN mapvalue TO "value";

ALTER TABLE annotation_mapvalue
	ADD COLUMN index integer NOT NULL;

ALTER TABLE experimentergroup_config
	RENAME COLUMN config_key to name;

ALTER TABLE experimentergroup_config
	RENAME COLUMN config TO "value";

ALTER TABLE experimentergroup_config
	ADD COLUMN index integer NOT NULL;

ALTER TABLE genericexcitationsource_map
	RENAME COLUMN map_key TO name;

ALTER TABLE genericexcitationsource_map
	RENAME COLUMN "map" TO "value";

ALTER TABLE genericexcitationsource_map
	ADD COLUMN index integer NOT NULL;

ALTER TABLE imagingenvironment_map
	RENAME COLUMN map_key TO name;

ALTER TABLE imagingenvironment_map
	RENAME COLUMN "map" TO "value";

ALTER TABLE imagingenvironment_map
	ADD COLUMN index integer NOT NULL;

ALTER TABLE metadataimportjob_versioninfo
	RENAME COLUMN versioninfo_key TO name;

ALTER TABLE metadataimportjob_versioninfo
	RENAME COLUMN versioninfo TO "value";

ALTER TABLE metadataimportjob_versioninfo
	ADD COLUMN index integer NOT NULL;

ALTER TABLE uploadjob_versioninfo
	RENAME COLUMN versioninfo_key TO name;

ALTER TABLE uploadjob_versioninfo
	RENAME COLUMN versioninfo TO "value";

ALTER TABLE uploadjob_versioninfo
	ADD COLUMN index integer NOT NULL;

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
