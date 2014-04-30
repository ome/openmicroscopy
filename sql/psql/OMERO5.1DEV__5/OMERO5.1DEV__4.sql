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
--- OMERO5 development release upgrade from OMERO5.1DEV__4 to OMERO5.1DEV__5.
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

SELECT omero_assert_db_version('OMERO5.1DEV', 4);
DROP FUNCTION omero_assert_db_version(varchar, int);


INSERT INTO dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
             VALUES ('OMERO5.1DEV',     5,              'OMERO5.1DEV',       4);

--
-- Actual upgrade
--

-- it is not worth keeping these uninformative rows

DELETE FROM metadataimportjob_versioninfo WHERE versioninfo = 'Unknown';
DELETE FROM uploadjob_versioninfo WHERE versioninfo = 'Unknown';

-- #12242: Bug: broken upgrade of nightshade

DELETE FROM annotationannotationlink link USING annotation parent
      WHERE link.parent = parent.id
        AND (parent.discriminator IN ('/basic/text/uri/', '/basic/text/url/')
             OR parent.group_id != link.group_id);

DELETE FROM annotationannotationlink link USING annotation child
      WHERE link.child = child.id
        AND (child.discriminator IN ('/basic/text/uri/', '/basic/text/url/')
             OR child.group_id != link.group_id);

DELETE FROM channelannotationlink link USING channel parent
      WHERE link.parent = parent.id
        AND parent.group_id != link.group_id;

DELETE FROM channelannotationlink link USING annotation child
      WHERE link.child = child.id
        AND child.discriminator IN ('/basic/text/uri/', '/basic/text/url/')
             OR child.group_id != link.group_id;

DELETE FROM datasetannotationlink link USING dataset parent
      WHERE link.parent = parent.id
        AND parent.group_id != link.group_id;

DELETE FROM datasetannotationlink link USING annotation child
      WHERE link.child = child.id
        AND child.discriminator IN ('/basic/text/uri/', '/basic/text/url/')
             OR child.group_id != link.group_id;

DELETE FROM experimenterannotationlink link USING annotation child
      WHERE link.child = child.id
        AND child.discriminator IN ('/basic/text/uri/', '/basic/text/url/')
             OR child.group_id != link.group_id;

DELETE FROM experimentergroupannotationlink link USING annotation child
      WHERE link.child = child.id
        AND child.discriminator IN ('/basic/text/uri/', '/basic/text/url/')
             OR child.group_id != link.group_id;

DELETE FROM filesetannotationlink link USING fileset parent
      WHERE link.parent = parent.id
        AND parent.group_id != link.group_id;

DELETE FROM filesetannotationlink link USING annotation child
      WHERE link.child = child.id
        AND child.discriminator IN ('/basic/text/uri/', '/basic/text/url/')
             OR child.group_id != link.group_id;

DELETE FROM imageannotationlink link USING image parent
      WHERE link.parent = parent.id
        AND parent.group_id != link.group_id;

DELETE FROM imageannotationlink link USING annotation child
      WHERE link.child = child.id
        AND child.discriminator IN ('/basic/text/uri/', '/basic/text/url/')
             OR child.group_id != link.group_id;

DELETE FROM namespaceannotationlink link USING namespace parent
      WHERE link.parent = parent.id
        AND parent.group_id != link.group_id;

DELETE FROM namespaceannotationlink link USING annotation child
      WHERE link.child = child.id
        AND child.discriminator IN ('/basic/text/uri/', '/basic/text/url/')
             OR child.group_id != link.group_id;

DELETE FROM nodeannotationlink link USING annotation child
      WHERE link.child = child.id
        AND child.discriminator IN ('/basic/text/uri/', '/basic/text/url/')
             OR child.group_id != link.group_id;

DELETE FROM originalfileannotationlink link USING originalfile parent
      WHERE link.parent = parent.id
        AND parent.group_id != link.group_id;

DELETE FROM originalfileannotationlink link USING annotation child
      WHERE link.child = child.id
        AND child.discriminator IN ('/basic/text/uri/', '/basic/text/url/')
             OR child.group_id != link.group_id;

DELETE FROM pixelsannotationlink link USING pixels parent
      WHERE link.parent = parent.id
        AND parent.group_id != link.group_id;

DELETE FROM pixelsannotationlink link USING annotation child
      WHERE link.child = child.id
        AND child.discriminator IN ('/basic/text/uri/', '/basic/text/url/')
             OR child.group_id != link.group_id;

DELETE FROM planeinfoannotationlink link USING planeinfo parent
      WHERE link.parent = parent.id
        AND parent.group_id != link.group_id;

DELETE FROM planeinfoannotationlink link USING annotation child
      WHERE link.child = child.id
        AND child.discriminator IN ('/basic/text/uri/', '/basic/text/url/')
             OR child.group_id != link.group_id;

DELETE FROM plateacquisitionannotationlink link USING plateacquisition parent
      WHERE link.parent = parent.id
        AND parent.group_id != link.group_id;

DELETE FROM plateacquisitionannotationlink link USING annotation child
      WHERE link.child = child.id
        AND child.discriminator IN ('/basic/text/uri/', '/basic/text/url/')
             OR child.group_id != link.group_id;

DELETE FROM plateannotationlink link USING plate parent
      WHERE link.parent = parent.id
        AND parent.group_id != link.group_id;

DELETE FROM plateannotationlink link USING annotation child
      WHERE link.child = child.id
        AND child.discriminator IN ('/basic/text/uri/', '/basic/text/url/')
             OR child.group_id != link.group_id;

DELETE FROM projectannotationlink link USING project parent
      WHERE link.parent = parent.id
        AND parent.group_id != link.group_id;

DELETE FROM projectannotationlink link USING annotation child
      WHERE link.child = child.id
        AND child.discriminator IN ('/basic/text/uri/', '/basic/text/url/')
             OR child.group_id != link.group_id;

DELETE FROM reagentannotationlink link USING reagent parent
      WHERE link.parent = parent.id
        AND parent.group_id != link.group_id;

DELETE FROM reagentannotationlink link USING annotation child
      WHERE link.child = child.id
        AND child.discriminator IN ('/basic/text/uri/', '/basic/text/url/')
             OR child.group_id != link.group_id;

DELETE FROM roiannotationlink link USING roi parent
      WHERE link.parent = parent.id
        AND parent.group_id != link.group_id;

DELETE FROM roiannotationlink link USING annotation child
      WHERE link.child = child.id
        AND child.discriminator IN ('/basic/text/uri/', '/basic/text/url/')
             OR child.group_id != link.group_id;

DELETE FROM screenannotationlink link USING screen parent
      WHERE link.parent = parent.id
        AND parent.group_id != link.group_id;

DELETE FROM screenannotationlink link USING annotation child
      WHERE link.child = child.id
        AND child.discriminator IN ('/basic/text/uri/', '/basic/text/url/')
             OR child.group_id != link.group_id;

DELETE FROM sessionannotationlink link USING annotation child
      WHERE link.child = child.id
        AND child.discriminator IN ('/basic/text/uri/', '/basic/text/url/')
             OR child.group_id != link.group_id;

DELETE FROM wellannotationlink link USING well parent
      WHERE link.parent = parent.id
        AND parent.group_id != link.group_id;

DELETE FROM wellannotationlink link USING annotation child
      WHERE link.child = child.id
        AND child.discriminator IN ('/basic/text/uri/', '/basic/text/url/')
             OR child.group_id != link.group_id;

DELETE FROM wellsampleannotationlink link USING wellsample parent
      WHERE link.parent = parent.id
        AND parent.group_id != link.group_id;

DELETE FROM wellsampleannotationlink link USING annotation child
      WHERE link.child = child.id
        AND child.discriminator IN ('/basic/text/uri/', '/basic/text/url/')
             OR child.group_id != link.group_id;

DELETE FROM annotation 
      WHERE discriminator IN ('/basic/text/uri/', '/basic/text/url/');

--
-- FINISHED
--

UPDATE dbpatch SET message = 'Database updated.', finished = clock_timestamp()
    WHERE currentVersion  = 'OMERO5.1DEV' AND
          currentPatch    = 5             AND
          previousVersion = 'OMERO5.1DEV' AND
          previousPatch   = 4;

SELECT CHR(10)||CHR(10)||CHR(10)||'YOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION OMERO5.1DEV__5'||CHR(10)||CHR(10)||CHR(10) AS Status;

COMMIT;
