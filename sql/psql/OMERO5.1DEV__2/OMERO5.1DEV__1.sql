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
--- OMERO5 development release upgrade from OMERO5.1DEV__1 to OMERO5.1DEV__2.
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

SELECT omero_assert_db_version('OMERO5.1DEV', 1);
DROP FUNCTION omero_assert_db_version(varchar, int);


INSERT INTO dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
             VALUES ('OMERO5.1DEV',     2,              'OMERO5.1DEV',       1);

--
-- Actual upgrade
--

-- #11877 move import logs to upload jobs so they are no longer file annotations
-- may have been missed in 5.0 by users starting from 5.0RC1
CREATE FUNCTION upgrade_import_logs() RETURNS void AS $$

    DECLARE
        import      RECORD;
        time_now    TIMESTAMP WITHOUT TIME ZONE;
        event_type  BIGINT;
        event_id    BIGINT;
        new_link_id BIGINT;

    BEGIN
        SELECT id INTO STRICT event_type FROM eventtype WHERE value = 'Internal';

        FOR import IN
            SELECT fal.id AS old_link_id, a.id AS annotation_id, u.job_id AS job_id, a.file AS log_id
              FROM filesetannotationlink fal, annotation a, filesetjoblink fjl, uploadjob u
             WHERE fal.parent = fjl.parent AND fal.child = a.id AND fjl.child = u.job_id
               AND a.discriminator = '/type/OriginalFile/' AND a.ns = 'openmicroscopy.org/omero/import/logFile' LOOP

            SELECT clock_timestamp() INTO time_now;
            SELECT ome_nextval('seq_event') INTO event_id;
            SELECT ome_nextval('seq_joboriginalfilelink') INTO new_link_id;

            INSERT INTO event (id, permissions, "time", experimenter, experimentergroup, session, type)
                SELECT event_id, a.permissions, time_now, a.owner_id, a.group_id, 0, event_type
                  FROM annotation a WHERE a.id = import.annotation_id;

            INSERT INTO eventlog (id, action, permissions, entityid, entitytype, event)
                SELECT ome_nextval('seq_eventlog'), 'INSERT', e.permissions, new_link_id, 'ome.model.jobs.JobOriginalFileLink', event_id
                  FROM event e WHERE e.id = event_id;

            INSERT INTO joboriginalfilelink (id, permissions, creation_id, update_id, owner_id, group_id, parent, child)
                SELECT new_link_id, old_link.permissions, old_link.creation_id, old_link.update_id, old_link.owner_id, old_link.group_id, import.job_id, import.log_id
                  FROM filesetannotationlink old_link WHERE old_link.id = import.old_link_id;

            UPDATE originalfile SET mimetype = 'application/omero-log-file' WHERE id = import.log_id;

            DELETE FROM annotationannotationlink WHERE parent = import.annotation_id OR child = import.annotation_id;
            DELETE FROM channelannotationlink WHERE child = import.annotation_id;
            DELETE FROM datasetannotationlink WHERE child = import.annotation_id;
            DELETE FROM experimenterannotationlink WHERE child = import.annotation_id;
            DELETE FROM experimentergroupannotationlink WHERE child = import.annotation_id;
            DELETE FROM filesetannotationlink WHERE child = import.annotation_id;
            DELETE FROM imageannotationlink WHERE child = import.annotation_id;
            DELETE FROM namespaceannotationlink WHERE child = import.annotation_id;
            DELETE FROM nodeannotationlink WHERE child = import.annotation_id;
            DELETE FROM originalfileannotationlink WHERE child = import.annotation_id;
            DELETE FROM pixelsannotationlink WHERE child = import.annotation_id;
            DELETE FROM planeinfoannotationlink WHERE child = import.annotation_id;
            DELETE FROM plateacquisitionannotationlink WHERE child = import.annotation_id;
            DELETE FROM plateannotationlink WHERE child = import.annotation_id;
            DELETE FROM projectannotationlink WHERE child = import.annotation_id;
            DELETE FROM reagentannotationlink WHERE child = import.annotation_id;
            DELETE FROM roiannotationlink WHERE child = import.annotation_id;
            DELETE FROM screenannotationlink WHERE child = import.annotation_id;
            DELETE FROM sessionannotationlink WHERE child = import.annotation_id;
            DELETE FROM wellannotationlink WHERE child = import.annotation_id;
            DELETE FROM wellsampleannotationlink WHERE child = import.annotation_id;
            DELETE FROM annotation WHERE id = import.annotation_id;
        END LOOP;
    END;
$$ LANGUAGE plpgsql;

SELECT upgrade_import_logs();

DROP FUNCTION upgrade_import_logs();

-- #11664 fix brittleness of _fs_deletelog()
CREATE OR REPLACE FUNCTION _fs_log_delete() RETURNS TRIGGER AS $_fs_log_delete$
    BEGIN
        IF OLD.repo IS NOT NULL THEN
            INSERT INTO _fs_deletelog (event_id, file_id, owner_id, group_id, "path", "name", repo, params)
                SELECT _current_or_new_event(), OLD.id, OLD.owner_id, OLD.group_id, OLD."path", OLD."name", OLD.repo, OLD.params;
        END IF;
        RETURN OLD;
    END;
$_fs_log_delete$ LANGUAGE plpgsql;

-- #11663 SQL DOMAIN types
CREATE DOMAIN nonnegative_int AS INTEGER CHECK (VALUE >= 0);
CREATE DOMAIN positive_int AS INTEGER CHECK (VALUE > 0);
CREATE DOMAIN positive_float AS DOUBLE PRECISION CHECK (VALUE > 0);
CREATE DOMAIN percent_fraction AS DOUBLE PRECISION CHECK (VALUE >= 0 AND VALUE <= 1);

ALTER TABLE detectorsettings ALTER COLUMN integration TYPE positive_int;
ALTER TABLE detectorsettings DROP CONSTRAINT detectorsettings_integration_check;

ALTER TABLE imagingenvironment ALTER COLUMN co2percent TYPE percent_fraction;
ALTER TABLE imagingenvironment ALTER COLUMN humidity TYPE percent_fraction;
ALTER TABLE imagingenvironment DROP CONSTRAINT imagingenvironment_check;

ALTER TABLE laser ALTER COLUMN frequencyMultiplication TYPE positive_int;
ALTER TABLE laser ALTER COLUMN wavelength TYPE positive_float;
ALTER TABLE laser DROP CONSTRAINT laser_check;

ALTER TABLE lightsettings ALTER COLUMN attenuation TYPE percent_fraction;
ALTER TABLE lightsettings ALTER COLUMN wavelength TYPE positive_float;
ALTER TABLE lightsettings DROP CONSTRAINT lightsettings_check;

ALTER TABLE logicalchannel ALTER COLUMN emissionWave TYPE positive_float;
ALTER TABLE logicalchannel ALTER COLUMN excitationWave TYPE positive_float;
ALTER TABLE logicalchannel ALTER COLUMN samplesPerPixel TYPE positive_int;
ALTER TABLE logicalchannel DROP CONSTRAINT logicalchannel_check;

ALTER TABLE otf ALTER COLUMN sizeX TYPE positive_int;
ALTER TABLE otf ALTER COLUMN sizeY TYPE positive_int;
ALTER TABLE otf DROP CONSTRAINT otf_check;

UPDATE pixels
  SET physicalSizeX = CASE WHEN physicalSizeX <= 0 THEN NULL ELSE physicalSizeX END,
      physicalSizeY = CASE WHEN physicalSizeY <= 0 THEN NULL ELSE physicalSizeY END,
      physicalSizeZ = CASE WHEN physicalSizeZ <= 0 THEN NULL ELSE physicalSizeZ END
  WHERE physicalSizeX <= 0 OR physicalSizeY <= 0 OR physicalSizeZ <= 0;

ALTER TABLE pixels ALTER COLUMN physicalSizeX TYPE positive_float;
ALTER TABLE pixels ALTER COLUMN physicalSizeY TYPE positive_float;
ALTER TABLE pixels ALTER COLUMN physicalSizeZ TYPE positive_float;
ALTER TABLE pixels ALTER COLUMN significantBits TYPE positive_int;
ALTER TABLE pixels ALTER COLUMN sizeC TYPE positive_int;
ALTER TABLE pixels ALTER COLUMN sizeT TYPE positive_int;
ALTER TABLE pixels ALTER COLUMN sizeX TYPE positive_int;
ALTER TABLE pixels ALTER COLUMN sizeY TYPE positive_int;
ALTER TABLE pixels ALTER COLUMN sizeZ TYPE positive_int;
ALTER TABLE pixels DROP CONSTRAINT pixels_check;

ALTER TABLE planeinfo ALTER COLUMN theC TYPE nonnegative_int;
ALTER TABLE planeinfo ALTER COLUMN theT TYPE nonnegative_int;
ALTER TABLE planeinfo ALTER COLUMN theZ TYPE nonnegative_int;
ALTER TABLE planeinfo DROP CONSTRAINT planeinfo_check;

ALTER TABLE transmittancerange ALTER COLUMN cutIn TYPE positive_int;
ALTER TABLE transmittancerange ALTER COLUMN cutInTolerance TYPE nonnegative_int;
ALTER TABLE transmittancerange ALTER COLUMN cutOut TYPE positive_int;
ALTER TABLE transmittancerange ALTER COLUMN cutOutTolerance TYPE nonnegative_int;
ALTER TABLE transmittancerange ALTER COLUMN transmittance TYPE percent_fraction;
ALTER TABLE transmittancerange DROP CONSTRAINT transmittancerange_check;

--
-- FINISHED
--

UPDATE dbpatch SET message = 'Database updated.', finished = clock_timestamp()
    WHERE currentVersion  = 'OMERO5.1DEV' AND
          currentPatch    = 2             AND
          previousVersion = 'OMERO5.1DEV' AND
          previousPatch   = 1;

SELECT CHR(10)||CHR(10)||CHR(10)||'YOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION OMERO5.1DEV__2'||CHR(10)||CHR(10)||CHR(10) AS Status;

COMMIT;
