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
--- OMERO5 development release upgrade from OMERO5.3DEV__10 to OMERO5.3DEV__11.
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

SELECT omero_assert_db_version('OMERO5.3DEV', 10);
DROP FUNCTION omero_assert_db_version(varchar, int);


--
-- Actual upgrade
--

INSERT INTO dbpatch (currentVersion, currentPatch, previousVersion, previousPatch)
             VALUES ('OMERO5.3DEV',  11,            'OMERO5.3DEV',  10);

CREATE VIEW count_annotation_channellinks_by_owner (annotation_id, owner_id, count) AS
    SELECT child, owner_id, count(*) FROM channelannotationlink GROUP BY child, owner_id ORDER BY child;

CREATE VIEW count_annotation_datasetlinks_by_owner (annotation_id, owner_id, count) AS
    SELECT child, owner_id, count(*) FROM datasetannotationlink GROUP BY child, owner_id ORDER BY child;

CREATE VIEW count_annotation_detectorlinks_by_owner (annotation_id, owner_id, count) AS
    SELECT child, owner_id, count(*) FROM detectorannotationlink GROUP BY child, owner_id ORDER BY child;

CREATE VIEW count_annotation_dichroiclinks_by_owner (annotation_id, owner_id, count) AS
    SELECT child, owner_id, count(*) FROM dichroicannotationlink GROUP BY child, owner_id ORDER BY child;

CREATE VIEW count_annotation_experimenterlinks_by_owner (annotation_id, owner_id, count) AS
    SELECT child, owner_id, count(*) FROM experimenterannotationlink GROUP BY child, owner_id ORDER BY child;

CREATE VIEW count_annotation_experimentergrouplinks_by_owner (annotation_id, owner_id, count) AS
    SELECT child, owner_id, count(*) FROM experimentergroupannotationlink GROUP BY child, owner_id ORDER BY child;

CREATE VIEW count_annotation_filesetlinks_by_owner (annotation_id, owner_id, count) AS
    SELECT child, owner_id, count(*) FROM filesetannotationlink GROUP BY child, owner_id ORDER BY child;

CREATE VIEW count_annotation_filterlinks_by_owner (annotation_id, owner_id, count) AS
    SELECT child, owner_id, count(*) FROM filterannotationlink GROUP BY child, owner_id ORDER BY child;

CREATE VIEW count_annotation_folderlinks_by_owner (annotation_id, owner_id, count) AS
    SELECT child, owner_id, count(*) FROM folderannotationlink GROUP BY child, owner_id ORDER BY child;

CREATE VIEW count_annotation_imagelinks_by_owner (annotation_id, owner_id, count) AS
    SELECT child, owner_id, count(*) FROM imageannotationlink GROUP BY child, owner_id ORDER BY child;

CREATE VIEW count_annotation_instrumentlinks_by_owner (annotation_id, owner_id, count) AS
    SELECT child, owner_id, count(*) FROM instrumentannotationlink GROUP BY child, owner_id ORDER BY child;

CREATE VIEW count_annotation_lightpathlinks_by_owner (annotation_id, owner_id, count) AS
    SELECT child, owner_id, count(*) FROM lightpathannotationlink GROUP BY child, owner_id ORDER BY child;

CREATE VIEW count_annotation_lightsourcelinks_by_owner (annotation_id, owner_id, count) AS
    SELECT child, owner_id, count(*) FROM lightsourceannotationlink GROUP BY child, owner_id ORDER BY child;

CREATE VIEW count_annotation_namespacelinks_by_owner (annotation_id, owner_id, count) AS
    SELECT child, owner_id, count(*) FROM namespaceannotationlink GROUP BY child, owner_id ORDER BY child;

CREATE VIEW count_annotation_nodelinks_by_owner (annotation_id, owner_id, count) AS
    SELECT child, owner_id, count(*) FROM nodeannotationlink GROUP BY child, owner_id ORDER BY child;

CREATE VIEW count_annotation_objectivelinks_by_owner (annotation_id, owner_id, count) AS
    SELECT child, owner_id, count(*) FROM objectiveannotationlink GROUP BY child, owner_id ORDER BY child;

CREATE VIEW count_annotation_originalfilelinks_by_owner (annotation_id, owner_id, count) AS
    SELECT child, owner_id, count(*) FROM originalfileannotationlink GROUP BY child, owner_id ORDER BY child;

CREATE VIEW count_annotation_planeinfolinks_by_owner (annotation_id, owner_id, count) AS
    SELECT child, owner_id, count(*) FROM planeinfoannotationlink GROUP BY child, owner_id ORDER BY child;

CREATE VIEW count_annotation_plateacquisitionlinks_by_owner (annotation_id, owner_id, count) AS
    SELECT child, owner_id, count(*) FROM plateacquisitionannotationlink GROUP BY child, owner_id ORDER BY child;

CREATE VIEW count_annotation_platelinks_by_owner (annotation_id, owner_id, count) AS
    SELECT child, owner_id, count(*) FROM plateannotationlink GROUP BY child, owner_id ORDER BY child;

CREATE VIEW count_annotation_projectlinks_by_owner (annotation_id, owner_id, count) AS
    SELECT child, owner_id, count(*) FROM projectannotationlink GROUP BY child, owner_id ORDER BY child;

CREATE VIEW count_annotation_reagentlinks_by_owner (annotation_id, owner_id, count) AS
    SELECT child, owner_id, count(*) FROM reagentannotationlink GROUP BY child, owner_id ORDER BY child;

CREATE VIEW count_annotation_roilinks_by_owner (annotation_id, owner_id, count) AS
    SELECT child, owner_id, count(*) FROM roiannotationlink GROUP BY child, owner_id ORDER BY child;

CREATE VIEW count_annotation_screenlinks_by_owner (annotation_id, owner_id, count) AS
    SELECT child, owner_id, count(*) FROM screenannotationlink GROUP BY child, owner_id ORDER BY child;

CREATE VIEW count_annotation_sessionlinks_by_owner (annotation_id, owner_id, count) AS
    SELECT child, owner_id, count(*) FROM sessionannotationlink GROUP BY child, owner_id ORDER BY child;

CREATE VIEW count_annotation_shapelinks_by_owner (annotation_id, owner_id, count) AS
    SELECT child, owner_id, count(*) FROM shapeannotationlink GROUP BY child, owner_id ORDER BY child;

CREATE VIEW count_annotation_welllinks_by_owner (annotation_id, owner_id, count) AS
    SELECT child, owner_id, count(*) FROM wellannotationlink GROUP BY child, owner_id ORDER BY child;


--
-- FINISHED
--

UPDATE dbpatch SET message = 'Database updated.', finished = clock_timestamp()
    WHERE currentVersion  = 'OMERO5.3DEV' AND
          currentPatch    = 11             AND
          previousVersion = 'OMERO5.3DEV' AND
          previousPatch   = 10;

SELECT CHR(10)||CHR(10)||CHR(10)||'YOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION OMERO5.3DEV__11'||CHR(10)||CHR(10)||CHR(10) AS Status;

COMMIT;
