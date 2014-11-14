-- Copyright (C) 2012-2014 Glencoe Software, Inc. All rights reserved.
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
--- OMERO5 development release upgrade from OMERO5.0__0 to OMERO5.1DEV__13.
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

SELECT omero_assert_db_version('OMERO5.0', 0);
DROP FUNCTION omero_assert_db_version(varchar, int);


INSERT INTO dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
             VALUES ('OMERO5.1DEV',  13,             'OMERO5.0',          0);

--
-- Actual upgrade
--

ALTER TABLE session ADD COLUMN userIP varchar(15);
ALTER TABLE logicalchannel ALTER COLUMN emissionWave TYPE FLOAT8;
ALTER TABLE logicalchannel ALTER COLUMN excitationWave TYPE FLOAT8;
ALTER TABLE laser ALTER COLUMN wavelength TYPE FLOAT8;
ALTER TABLE lightsettings ALTER COLUMN wavelength TYPE FLOAT8;

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

UPDATE pixels SET physicalSizeX = NULL WHERE physicalSizeX <= 0;
UPDATE pixels SET physicalSizeY = NULL WHERE physicalSizeY <= 0;
UPDATE pixels SET physicalSizeZ = NULL WHERE physicalSizeZ <= 0;

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

-- #12126

UPDATE pixelstype SET bitsize = 16 WHERE value = 'uint16';

-- # map annotation

CREATE TABLE annotation_mapValue (
    annotation_id INT8 NOT NULL,
    mapValue VARCHAR(255) NOT NULL,
    mapValue_key VARCHAR(255),
    PRIMARY KEY (annotation_id, mapValue_key),
    CONSTRAINT FKannotation_mapvalue_map
        FOREIGN KEY (annotation_id) 
        REFERENCES annotation
);

CREATE TABLE experimentergroup_config (
    experimentergroup_id INT8 NOT NULL,
    config VARCHAR(255) NOT NULL,
    config_key VARCHAR(255),
    PRIMARY KEY (experimentergroup_id, config_key),
    CONSTRAINT FKexperimentergroup_config_map
        FOREIGN KEY (experimentergroup_id) 
        REFERENCES experimentergroup
);

CREATE TABLE genericexcitationsource (
    lightsource_id INT8 PRIMARY KEY,
    CONSTRAINT FKgenericexcitationsource_lightsource_id_lightsource 
        FOREIGN KEY (lightsource_id) 
        REFERENCES lightsource
);

CREATE TABLE genericexcitationsource_map (
    genericexcitationsource_id INT8 NOT NULL,
    "map" VARCHAR(255) NOT NULL,
    map_key VARCHAR(255),
    PRIMARY KEY (genericexcitationsource_id, map_key),
    CONSTRAINT FKgenericexcitationsource_map_map
        FOREIGN KEY (genericexcitationsource_id) 
        REFERENCES genericexcitationsource
);

CREATE TABLE imagingenvironment_map (
    imagingenvironment_id INT8 NOT NULL,
    "map" VARCHAR(255) NOT NULL,
    map_key VARCHAR(255),
    PRIMARY KEY (imagingenvironment_id, map_key),
    CONSTRAINT FKimagingenvironment_map_map
        FOREIGN KEY (imagingenvironment_id) 
        REFERENCES imagingenvironment
);

-- #12193: replace FilesetVersionInfo with map property on Fileset

CREATE TABLE metadataimportjob_versioninfo (
    metadataimportjob_id INT8 NOT NULL,
    versioninfo VARCHAR(255) NOT NULL,
    versioninfo_key VARCHAR(255),
    PRIMARY KEY (metadataimportjob_id, versioninfo_key),
    CONSTRAINT FKmetadataimportjob_versioninfo_map
        FOREIGN KEY (metadataimportjob_id) 
        REFERENCES metadataimportjob
);

CREATE TABLE uploadjob_versioninfo (
    uploadjob_id INT8 NOT NULL,
    versioninfo VARCHAR(255) NOT NULL,
    versioninfo_key VARCHAR(255),
    PRIMARY KEY (uploadjob_id, versioninfo_key),
    CONSTRAINT FKuploadjob_versioninfo_map
        FOREIGN KEY (uploadjob_id) 
        REFERENCES uploadjob
);

INSERT INTO metadataimportjob_versioninfo (metadataimportjob_id, versioninfo_key, versioninfo)
    SELECT metadataimportjob.job_id, 'bioformats.reader', filesetversioninfo.bioformatsreader
    FROM filesetversioninfo, metadataimportjob
    WHERE filesetversioninfo.id = metadataimportjob.versioninfo;

INSERT INTO metadataimportjob_versioninfo (metadataimportjob_id, versioninfo_key, versioninfo)
    SELECT metadataimportjob.job_id, 'bioformats.version', filesetversioninfo.bioformatsversion
    FROM filesetversioninfo, metadataimportjob
    WHERE filesetversioninfo.id = metadataimportjob.versioninfo;

INSERT INTO metadataimportjob_versioninfo (metadataimportjob_id, versioninfo_key, versioninfo)
    SELECT metadataimportjob.job_id, 'locale', filesetversioninfo.locale
    FROM filesetversioninfo, metadataimportjob
    WHERE filesetversioninfo.id = metadataimportjob.versioninfo;

INSERT INTO metadataimportjob_versioninfo (metadataimportjob_id, versioninfo_key, versioninfo)
    SELECT metadataimportjob.job_id, 'omero.version', filesetversioninfo.omeroversion
    FROM filesetversioninfo, metadataimportjob
    WHERE filesetversioninfo.id = metadataimportjob.versioninfo;

INSERT INTO metadataimportjob_versioninfo (metadataimportjob_id, versioninfo_key, versioninfo)
    SELECT metadataimportjob.job_id, 'os.name', filesetversioninfo.osname
    FROM filesetversioninfo, metadataimportjob
    WHERE filesetversioninfo.id = metadataimportjob.versioninfo;

INSERT INTO metadataimportjob_versioninfo (metadataimportjob_id, versioninfo_key, versioninfo)
    SELECT metadataimportjob.job_id, 'os.version', filesetversioninfo.osversion
    FROM filesetversioninfo, metadataimportjob
    WHERE filesetversioninfo.id = metadataimportjob.versioninfo;

INSERT INTO metadataimportjob_versioninfo (metadataimportjob_id, versioninfo_key, versioninfo)
    SELECT metadataimportjob.job_id, 'os.architecture', filesetversioninfo.osarchitecture
    FROM filesetversioninfo, metadataimportjob
    WHERE filesetversioninfo.id = metadataimportjob.versioninfo;

INSERT INTO uploadjob_versioninfo (uploadjob_id, versioninfo_key, versioninfo)
    SELECT uploadjob.job_id, 'bioformats.reader', filesetversioninfo.bioformatsreader
    FROM filesetversioninfo, uploadjob
    WHERE filesetversioninfo.id = uploadjob.versioninfo;

INSERT INTO uploadjob_versioninfo (uploadjob_id, versioninfo_key, versioninfo)
    SELECT uploadjob.job_id, 'bioformats.version', filesetversioninfo.bioformatsversion
    FROM filesetversioninfo, uploadjob
    WHERE filesetversioninfo.id = uploadjob.versioninfo;

INSERT INTO uploadjob_versioninfo (uploadjob_id, versioninfo_key, versioninfo)
    SELECT uploadjob.job_id, 'locale', filesetversioninfo.locale
    FROM filesetversioninfo, uploadjob
    WHERE filesetversioninfo.id = uploadjob.versioninfo;

INSERT INTO uploadjob_versioninfo (uploadjob_id, versioninfo_key, versioninfo)
    SELECT uploadjob.job_id, 'omero.version', filesetversioninfo.omeroversion
    FROM filesetversioninfo, uploadjob
    WHERE filesetversioninfo.id = uploadjob.versioninfo;

INSERT INTO uploadjob_versioninfo (uploadjob_id, versioninfo_key, versioninfo)
    SELECT uploadjob.job_id, 'os.name', filesetversioninfo.osname
    FROM filesetversioninfo, uploadjob
    WHERE filesetversioninfo.id = uploadjob.versioninfo;

INSERT INTO uploadjob_versioninfo (uploadjob_id, versioninfo_key, versioninfo)
    SELECT uploadjob.job_id, 'os.version', filesetversioninfo.osversion
    FROM filesetversioninfo, uploadjob
    WHERE filesetversioninfo.id = uploadjob.versioninfo;

INSERT INTO uploadjob_versioninfo (uploadjob_id, versioninfo_key, versioninfo)
    SELECT uploadjob.job_id, 'os.architecture', filesetversioninfo.osarchitecture
    FROM filesetversioninfo, uploadjob
    WHERE filesetversioninfo.id = uploadjob.versioninfo;

ALTER TABLE metadataimportjob DROP COLUMN versioninfo;
ALTER TABLE uploadjob DROP COLUMN versioninfo;

DROP SEQUENCE seq_filesetversioninfo;
DROP TABLE filesetversioninfo;

-- it is not worth keeping these uninformative rows

DELETE FROM metadataimportjob_versioninfo WHERE versioninfo = 'Unknown';
DELETE FROM uploadjob_versioninfo WHERE versioninfo = 'Unknown';

-- #12242: Bug: broken upgrade of nightshade
-- #11479: https://github.com/openmicroscopy/openmicroscopy/pull/2369#issuecomment-41701620
-- So, remove annotations with bad discriminators or inter-group links.

-- return if the group IDs include multiple non-user groups
CREATE FUNCTION is_too_many_group_ids(VARIADIC group_ids BIGINT[]) RETURNS BOOLEAN AS $$

    DECLARE
        user_group  BIGINT;
        other_group BIGINT;
        curr_group  BIGINT;
        index       BIGINT;

    BEGIN
        SELECT id INTO user_group FROM experimentergroup WHERE name = 'user';

        FOR index IN 1 .. array_upper(group_ids, 1) LOOP
            curr_group := group_ids[index];
            CONTINUE WHEN user_group = curr_group;
            IF other_group IS NULL THEN
                other_group := curr_group;
            ELSIF other_group != curr_group THEN
                RETURN TRUE;
            END IF;
        END LOOP;

        RETURN FALSE;
    END;

$$ LANGUAGE plpgsql;

DELETE FROM annotationannotationlink link
      USING annotation parent, annotation child
      WHERE link.parent = parent.id AND link.child = child.id
        AND (parent.discriminator IN ('/basic/text/uri/', '/basic/text/url/') OR
             child.discriminator  IN ('/basic/text/uri/', '/basic/text/url/') OR
             is_too_many_group_ids(parent.group_id, link.group_id, child.group_id));

DELETE FROM channelannotationlink link
      USING channel parent, annotation child
      WHERE link.parent = parent.id AND link.child = child.id
        AND (child.discriminator IN ('/basic/text/uri/', '/basic/text/url/') OR
             is_too_many_group_ids(parent.group_id, link.group_id, child.group_id));

DELETE FROM datasetannotationlink link
      USING dataset parent, annotation child
      WHERE link.parent = parent.id AND link.child = child.id
        AND (child.discriminator IN ('/basic/text/uri/', '/basic/text/url/') OR
             is_too_many_group_ids(parent.group_id, link.group_id, child.group_id));

DELETE FROM experimenterannotationlink link
      USING annotation child
      WHERE link.child = child.id
        AND (child.discriminator IN ('/basic/text/uri/', '/basic/text/url/') OR
             is_too_many_group_ids(link.group_id, child.group_id));

DELETE FROM experimentergroupannotationlink link
      USING annotation child
      WHERE link.child = child.id
        AND (child.discriminator IN ('/basic/text/uri/', '/basic/text/url/') OR
             is_too_many_group_ids(link.group_id, child.group_id));

DELETE FROM filesetannotationlink link
      USING fileset parent, annotation child
      WHERE link.parent = parent.id AND link.child = child.id
        AND (child.discriminator IN ('/basic/text/uri/', '/basic/text/url/') OR
             is_too_many_group_ids(parent.group_id, link.group_id, child.group_id));

DELETE FROM imageannotationlink link
      USING image parent, annotation child
      WHERE link.parent = parent.id AND link.child = child.id
        AND (child.discriminator IN ('/basic/text/uri/', '/basic/text/url/') OR
             is_too_many_group_ids(parent.group_id, link.group_id, child.group_id));

DELETE FROM namespaceannotationlink link
      USING namespace parent, annotation child
      WHERE link.parent = parent.id AND link.child = child.id
        AND (child.discriminator IN ('/basic/text/uri/', '/basic/text/url/') OR
             is_too_many_group_ids(parent.group_id, link.group_id, child.group_id));

DELETE FROM nodeannotationlink link
      USING annotation child
      WHERE link.child = child.id
        AND (child.discriminator IN ('/basic/text/uri/', '/basic/text/url/') OR
             is_too_many_group_ids(link.group_id, child.group_id));

DELETE FROM originalfileannotationlink link
      USING originalfile parent, annotation child
      WHERE link.parent = parent.id AND link.child = child.id
        AND (child.discriminator IN ('/basic/text/uri/', '/basic/text/url/') OR
             is_too_many_group_ids(parent.group_id, link.group_id, child.group_id));

DELETE FROM pixelsannotationlink link
      USING pixels parent, annotation child
      WHERE link.parent = parent.id AND link.child = child.id
        AND (child.discriminator IN ('/basic/text/uri/', '/basic/text/url/') OR
             is_too_many_group_ids(parent.group_id, link.group_id, child.group_id));

DELETE FROM planeinfoannotationlink link
      USING planeinfo parent, annotation child
      WHERE link.parent = parent.id AND link.child = child.id
        AND (child.discriminator IN ('/basic/text/uri/', '/basic/text/url/') OR
             is_too_many_group_ids(parent.group_id, link.group_id, child.group_id));

DELETE FROM plateacquisitionannotationlink link
      USING plateacquisition parent, annotation child
      WHERE link.parent = parent.id AND link.child = child.id
        AND (child.discriminator IN ('/basic/text/uri/', '/basic/text/url/') OR
             is_too_many_group_ids(parent.group_id, link.group_id, child.group_id));

DELETE FROM plateannotationlink link
      USING plate parent, annotation child
      WHERE link.parent = parent.id AND link.child = child.id
        AND (child.discriminator IN ('/basic/text/uri/', '/basic/text/url/') OR
             is_too_many_group_ids(parent.group_id, link.group_id, child.group_id));

DELETE FROM projectannotationlink link
      USING project parent, annotation child
      WHERE link.parent = parent.id AND link.child = child.id
        AND (child.discriminator IN ('/basic/text/uri/', '/basic/text/url/') OR
             is_too_many_group_ids(parent.group_id, link.group_id, child.group_id));

DELETE FROM reagentannotationlink link
      USING reagent parent, annotation child
      WHERE link.parent = parent.id AND link.child = child.id
        AND (child.discriminator IN ('/basic/text/uri/', '/basic/text/url/') OR
             is_too_many_group_ids(parent.group_id, link.group_id, child.group_id));

DELETE FROM roiannotationlink link
      USING roi parent, annotation child
      WHERE link.parent = parent.id AND link.child = child.id
        AND (child.discriminator IN ('/basic/text/uri/', '/basic/text/url/') OR
             is_too_many_group_ids(parent.group_id, link.group_id, child.group_id));

DELETE FROM screenannotationlink link
      USING screen parent, annotation child
      WHERE link.parent = parent.id AND link.child = child.id
        AND (child.discriminator IN ('/basic/text/uri/', '/basic/text/url/') OR
             is_too_many_group_ids(parent.group_id, link.group_id, child.group_id));

DELETE FROM sessionannotationlink link
      USING annotation child
      WHERE link.child = child.id
        AND (child.discriminator IN ('/basic/text/uri/', '/basic/text/url/') OR
             is_too_many_group_ids(link.group_id, child.group_id));

DELETE FROM wellannotationlink link
      USING well parent, annotation child
      WHERE link.parent = parent.id AND link.child = child.id
        AND (child.discriminator IN ('/basic/text/uri/', '/basic/text/url/') OR
             is_too_many_group_ids(parent.group_id, link.group_id, child.group_id));

DELETE FROM wellsampleannotationlink link
      USING wellsample parent, annotation child
      WHERE link.parent = parent.id AND link.child = child.id
        AND (child.discriminator IN ('/basic/text/uri/', '/basic/text/url/') OR
             is_too_many_group_ids(parent.group_id, link.group_id, child.group_id));

DROP FUNCTION is_too_many_group_ids(VARIADIC group_ids BIGINT[]);

DELETE FROM annotation
      WHERE discriminator IN ('/basic/text/uri/', '/basic/text/url/');


-- Remove all DB checks

DELETE FROM configuration
      WHERE name LIKE ('DB check %');


-- Annotation link triggers for search
-- Note: no annotation insert trigger

DROP TRIGGER IF EXISTS annotation_annotation_link_event_trigger_insert ON annotationannotationlink;

CREATE TRIGGER annotation_annotation_link_event_trigger_insert
        AFTER INSERT ON annotationannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.annotations.Annotation');

DROP TRIGGER IF EXISTS channel_annotation_link_event_trigger_insert ON channelannotationlink;

CREATE TRIGGER channel_annotation_link_event_trigger_insert
        AFTER INSERT ON channelannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.core.Channel');

DROP TRIGGER IF EXISTS dataset_annotation_link_event_trigger_insert ON datasetannotationlink;

CREATE TRIGGER dataset_annotation_link_event_trigger_insert
        AFTER INSERT ON datasetannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.containers.Dataset');

DROP TRIGGER IF EXISTS experimenter_annotation_link_event_trigger_insert ON experimenterannotationlink;

CREATE TRIGGER experimenter_annotation_link_event_trigger_insert
        AFTER INSERT ON experimenterannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.meta.Experimenter');

DROP TRIGGER IF EXISTS experimentergroup_annotation_link_event_trigger_insert ON experimentergroupannotationlink;

CREATE TRIGGER experimentergroup_annotation_link_event_trigger_insert
        AFTER INSERT ON experimentergroupannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.meta.ExperimenterGroup');

DROP TRIGGER IF EXISTS fileset_annotation_link_event_trigger_insert ON filesetannotationlink;

CREATE TRIGGER fileset_annotation_link_event_trigger_insert
        AFTER INSERT ON filesetannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.fs.Fileset');

DROP TRIGGER IF EXISTS image_annotation_link_event_trigger_insert ON imageannotationlink;

CREATE TRIGGER image_annotation_link_event_trigger_insert
        AFTER INSERT ON imageannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.core.Image');

DROP TRIGGER IF EXISTS namespace_annotation_link_event_trigger_insert ON namespaceannotationlink;

CREATE TRIGGER namespace_annotation_link_event_trigger_insert
        AFTER INSERT ON namespaceannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.meta.Namespace');

DROP TRIGGER IF EXISTS node_annotation_link_event_trigger_insert ON nodeannotationlink;

CREATE TRIGGER node_annotation_link_event_trigger_insert
        AFTER INSERT ON nodeannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.meta.Node');

DROP TRIGGER IF EXISTS originalfile_annotation_link_event_trigger_insert ON originalfileannotationlink;

CREATE TRIGGER originalfile_annotation_link_event_trigger_insert
        AFTER INSERT ON originalfileannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.core.OriginalFile');

DROP TRIGGER IF EXISTS pixels_annotation_link_event_trigger_insert ON pixelsannotationlink;

CREATE TRIGGER pixels_annotation_link_event_trigger_insert
        AFTER INSERT ON pixelsannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.core.Pixels');

DROP TRIGGER IF EXISTS planeinfo_annotation_link_event_trigger_insert ON planeinfoannotationlink;

CREATE TRIGGER planeinfo_annotation_link_event_trigger_insert
        AFTER INSERT ON planeinfoannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.core.PlaneInfo');

DROP TRIGGER IF EXISTS plate_annotation_link_event_trigger_insert ON plateannotationlink;

CREATE TRIGGER plate_annotation_link_event_trigger_insert
        AFTER INSERT ON plateannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.screen.Plate');

DROP TRIGGER IF EXISTS plateacquisition_annotation_link_event_trigger_insert ON plateacquisitionannotationlink;

CREATE TRIGGER plateacquisition_annotation_link_event_trigger_insert
        AFTER INSERT ON plateacquisitionannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.screen.PlateAcquisition');

DROP TRIGGER IF EXISTS project_annotation_link_event_trigger_insert ON projectannotationlink;

CREATE TRIGGER project_annotation_link_event_trigger_insert
        AFTER INSERT ON projectannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.containers.Project');

DROP TRIGGER IF EXISTS reagent_annotation_link_event_trigger_insert ON reagentannotationlink;

CREATE TRIGGER reagent_annotation_link_event_trigger_insert
        AFTER INSERT ON reagentannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.screen.Reagent');

DROP TRIGGER IF EXISTS roi_annotation_link_event_trigger_insert ON roiannotationlink;

CREATE TRIGGER roi_annotation_link_event_trigger_insert
        AFTER INSERT ON roiannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.roi.Roi');

DROP TRIGGER IF EXISTS screen_annotation_link_event_trigger_insert ON screenannotationlink;

CREATE TRIGGER screen_annotation_link_event_trigger_insert
        AFTER INSERT ON screenannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.screen.Screen');

DROP TRIGGER IF EXISTS session_annotation_link_event_trigger_insert ON sessionannotationlink;

CREATE TRIGGER session_annotation_link_event_trigger_insert
        AFTER INSERT ON sessionannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.meta.Session');

DROP TRIGGER IF EXISTS well_annotation_link_event_trigger_insert ON wellannotationlink;

CREATE TRIGGER well_annotation_link_event_trigger_insert
        AFTER INSERT ON wellannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.screen.Well');

DROP TRIGGER IF EXISTS wellsample_annotation_link_event_trigger_insert ON wellsampleannotationlink;

CREATE TRIGGER wellsample_annotation_link_event_trigger_insert
        AFTER INSERT ON wellsampleannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.screen.WellSample');

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

-- Trac ticket #970

ALTER TABLE dbpatch DROP CONSTRAINT unique_dbpatch;
ALTER TABLE dbpatch ADD CONSTRAINT unique_dbpatch
  UNIQUE (currentversion, currentpatch, previousversion, previouspatch, message);

-- Trac ticket #12317 -- delete map property values along with their holders

CREATE FUNCTION experimentergroup_config_map_entry_delete_trigger_function() RETURNS "trigger" AS '
BEGIN
    DELETE FROM experimentergroup_config
        WHERE experimentergroup_id = OLD.id;
    RETURN OLD;
END;'
LANGUAGE plpgsql;

CREATE TRIGGER experimentergroup_config_map_entry_delete_trigger
    BEFORE DELETE ON experimentergroup
    FOR EACH ROW
    EXECUTE PROCEDURE experimentergroup_config_map_entry_delete_trigger_function();

CREATE FUNCTION genericexcitationsource_map_map_entry_delete_trigger_function() RETURNS "trigger" AS '
BEGIN
    DELETE FROM genericexcitationsource_map
        WHERE genericexcitationsource_id = OLD.lightsource_id;
    RETURN OLD;
END;'
LANGUAGE plpgsql;
 
CREATE TRIGGER genericexcitationsource_map_map_entry_delete_trigger
    BEFORE DELETE ON genericexcitationsource
    FOR EACH ROW
    EXECUTE PROCEDURE genericexcitationsource_map_map_entry_delete_trigger_function();

CREATE FUNCTION imagingenvironment_map_map_entry_delete_trigger_function() RETURNS "trigger" AS '
BEGIN
    DELETE FROM imagingenvironment_map
        WHERE imagingenvironment_id = OLD.id;
    RETURN OLD;
END;'
LANGUAGE plpgsql;

CREATE TRIGGER imagingenvironment_map_map_entry_delete_trigger
    BEFORE DELETE ON imagingenvironment
    FOR EACH ROW
    EXECUTE PROCEDURE imagingenvironment_map_map_entry_delete_trigger_function();

CREATE FUNCTION annotation_mapValue_map_entry_delete_trigger_function() RETURNS "trigger" AS '
BEGIN
    DELETE FROM annotation_mapValue
        WHERE annotation_id = OLD.id;
    RETURN OLD;
END;'
LANGUAGE plpgsql;

CREATE TRIGGER annotation_mapValue_map_entry_delete_trigger
    BEFORE DELETE ON annotation
    FOR EACH ROW
    EXECUTE PROCEDURE annotation_mapValue_map_entry_delete_trigger_function();

CREATE FUNCTION metadataimportjob_versionInfo_map_entry_delete_trigger_function() RETURNS "trigger" AS '
BEGIN
    DELETE FROM metadataimportjob_versionInfo
        WHERE metadataimportjob_id = OLD.job_id;
    RETURN OLD;
END;'
LANGUAGE plpgsql;

CREATE TRIGGER metadataimportjob_versionInfo_map_entry_delete_trigger
    BEFORE DELETE ON metadataimportjob
    FOR EACH ROW
    EXECUTE PROCEDURE metadataimportjob_versionInfo_map_entry_delete_trigger_function();

CREATE FUNCTION uploadjob_versionInfo_map_entry_delete_trigger_function() RETURNS "trigger" AS '
BEGIN
    DELETE FROM uploadjob_versionInfo
        WHERE uploadjob_id = OLD.job_id;
    RETURN OLD;
END;'
LANGUAGE plpgsql;

CREATE TRIGGER uploadjob_versionInfo_map_entry_delete_trigger
    BEFORE DELETE ON uploadjob
    FOR EACH ROW
    EXECUTE PROCEDURE uploadjob_versionInfo_map_entry_delete_trigger_function();


-- Adding extra annotation points to the model

CREATE TABLE detectorannotationlink (
    id INT8 PRIMARY KEY,
    permissions INT8 NOT NULL,
    version INT4,
    child INT8 NOT NULL,
    creation_id INT8 NOT NULL,
    external_id INT8 UNIQUE,
    group_id INT8 NOT NULL,
    owner_id INT8 NOT NULL,
    update_id INT8 NOT NULL,
    parent INT8 NOT NULL,
    UNIQUE (parent, child, owner_id),
    CONSTRAINT FKdetectorannotationlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event,
    CONSTRAINT FKdetectorannotationlink_child_annotation FOREIGN KEY (child) REFERENCES annotation,
    CONSTRAINT FKdetectorannotationlink_update_id_event FOREIGN KEY (update_id) REFERENCES event,
    CONSTRAINT FKdetectorannotationlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo,
    CONSTRAINT FKdetectorannotationlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup,
    CONSTRAINT FKdetectorannotationlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter,
    CONSTRAINT FKdetectorannotationlink_parent_detector FOREIGN KEY (parent) REFERENCES detector
);

CREATE INDEX i_detectorannotationlink_owner ON detectorannotationlink(owner_id);
CREATE INDEX i_detectorannotationlink_group ON detectorannotationlink(group_id);
CREATE INDEX i_DetectorAnnotationLink_parent ON detectorannotationlink(parent);
CREATE INDEX i_DetectorAnnotationLink_child ON detectorannotationlink(child);

CREATE TRIGGER detector_annotation_link_event_trigger
    AFTER UPDATE ON detectorannotationlink
    FOR EACH ROW
    EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.acquisition.Detector');

CREATE TRIGGER detector_annotation_link_delete_trigger
    BEFORE DELETE ON detectorannotationlink
    FOR EACH ROW
    EXECUTE PROCEDURE annotation_link_delete_trigger('ome.model.acquisition.Detector');

CREATE SEQUENCE seq_detectorannotationlink;
INSERT INTO _lock_ids (name, id) SELECT 'seq_detectorannotationlink', nextval('_lock_seq');

CREATE VIEW count_detector_annotationlinks_by_owner (detector_id, owner_id, count) 
    AS SELECT parent, owner_id, count(*)
    FROM detectorannotationlink GROUP BY parent, owner_id ORDER BY parent;

CREATE TABLE dichroicannotationlink (
    id INT8 PRIMARY KEY,
    permissions INT8 NOT NULL,
    version INT4,
    child INT8 NOT NULL,
    creation_id INT8 NOT NULL,
    external_id INT8 UNIQUE,
    group_id INT8 NOT NULL,
    owner_id INT8 NOT NULL,
    update_id INT8 NOT NULL,
    parent INT8 NOT NULL,
    UNIQUE (parent, child, owner_id),
    CONSTRAINT FKdichroicannotationlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event,
    CONSTRAINT FKdichroicannotationlink_child_annotation FOREIGN KEY (child) REFERENCES annotation,
    CONSTRAINT FKdichroicannotationlink_update_id_event FOREIGN KEY (update_id) REFERENCES event,
    CONSTRAINT FKdichroicannotationlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo,
    CONSTRAINT FKdichroicannotationlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup,
    CONSTRAINT FKdichroicannotationlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter,
    CONSTRAINT FKdichroicannotationlink_parent_dichroic FOREIGN KEY (parent) REFERENCES dichroic
);

CREATE INDEX i_dichroicannotationlink_owner ON dichroicannotationlink(owner_id);
CREATE INDEX i_dichroicannotationlink_group ON dichroicannotationlink(group_id);
CREATE INDEX i_DichroicAnnotationLink_parent ON dichroicannotationlink(parent);
CREATE INDEX i_DichroicAnnotationLink_child ON dichroicannotationlink(child);

CREATE TRIGGER dichroic_annotation_link_event_trigger
    AFTER UPDATE ON dichroicannotationlink
    FOR EACH ROW
    EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.acquisition.Dichroic');

CREATE TRIGGER dichroic_annotation_link_delete_trigger
    BEFORE DELETE ON dichroicannotationlink
    FOR EACH ROW
    EXECUTE PROCEDURE annotation_link_delete_trigger('ome.model.acquisition.Dichroic');

CREATE SEQUENCE seq_dichroicannotationlink;
INSERT INTO _lock_ids (name, id) SELECT 'seq_dichroicannotationlink', nextval('_lock_seq');

CREATE VIEW count_dichroic_annotationlinks_by_owner (dichroic_id, owner_id, count)
    AS SELECT parent, owner_id, count(*)
    FROM dichroicannotationlink GROUP BY parent, owner_id ORDER BY parent;

CREATE TABLE filterannotationlink (
    id INT8 PRIMARY KEY,
    permissions INT8 NOT NULL,
    version INT4,
    child INT8 NOT NULL,
    creation_id INT8 NOT NULL,
    external_id INT8 UNIQUE,
    group_id INT8 NOT NULL,
    owner_id INT8 NOT NULL,
    update_id INT8 NOT NULL,
    parent INT8 NOT NULL,
    UNIQUE (parent, child, owner_id),
    CONSTRAINT FKfilterannotationlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event,
    CONSTRAINT FKfilterannotationlink_child_annotation FOREIGN KEY (child) REFERENCES annotation,
    CONSTRAINT FKfilterannotationlink_update_id_event FOREIGN KEY (update_id) REFERENCES event,
    CONSTRAINT FKfilterannotationlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo,
    CONSTRAINT FKfilterannotationlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup,
    CONSTRAINT FKfilterannotationlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter,
    CONSTRAINT FKfilterannotationlink_parent_filter FOREIGN KEY (parent) REFERENCES filter
);

CREATE INDEX i_filterannotationlink_owner ON filterannotationlink(owner_id);
CREATE INDEX i_filterannotationlink_group ON filterannotationlink(group_id);
CREATE INDEX i_FilterAnnotationLink_parent ON filterannotationlink(parent);
CREATE INDEX i_FilterAnnotationLink_child ON filterannotationlink(child);

CREATE TRIGGER filter_annotation_link_event_trigger
    AFTER UPDATE ON filterannotationlink
    FOR EACH ROW
    EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.acquisition.Filter');

CREATE TRIGGER filter_annotation_link_delete_trigger
    BEFORE DELETE ON filterannotationlink
    FOR EACH ROW
    EXECUTE PROCEDURE annotation_link_delete_trigger('ome.model.acquisition.Filter');

CREATE SEQUENCE seq_filterannotationlink;
INSERT INTO _lock_ids (name, id) SELECT 'seq_filterannotationlink', nextval('_lock_seq');

CREATE VIEW count_filter_annotationlinks_by_owner (filter_id, owner_id, count)
    AS SELECT parent, owner_id, count(*)
    FROM filterannotationlink GROUP BY parent, owner_id ORDER BY parent;

CREATE TABLE instrumentannotationlink (
    id INT8 PRIMARY KEY,
    permissions INT8 NOT NULL,
    version INT4,
    child INT8 NOT NULL,
    creation_id INT8 NOT NULL,
    external_id INT8 UNIQUE,
    group_id INT8 NOT NULL,
    owner_id INT8 NOT NULL,
    update_id INT8 NOT NULL,
    parent INT8 NOT NULL,
    UNIQUE (parent, child, owner_id),
    CONSTRAINT FKinstrumentannotationlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event,
    CONSTRAINT FKinstrumentannotationlink_child_annotation FOREIGN KEY (child) REFERENCES annotation,
    CONSTRAINT FKinstrumentannotationlink_update_id_event FOREIGN KEY (update_id) REFERENCES event,
    CONSTRAINT FKinstrumentannotationlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo,
    CONSTRAINT FKinstrumentannotationlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup,
    CONSTRAINT FKinstrumentannotationlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter,
    CONSTRAINT FKinstrumentannotationlink_parent_instrument FOREIGN KEY (parent) REFERENCES instrument
);

CREATE INDEX i_instrumentannotationlink_owner ON instrumentannotationlink(owner_id);
CREATE INDEX i_instrumentannotationlink_group ON instrumentannotationlink(group_id);
CREATE INDEX i_InstrumentAnnotationLink_parent ON instrumentannotationlink(parent);
CREATE INDEX i_InstrumentAnnotationLink_child ON instrumentannotationlink(child);

CREATE TRIGGER instrument_annotation_link_event_trigger
    AFTER UPDATE ON instrumentannotationlink
    FOR EACH ROW
    EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.acquisition.Instrument');

CREATE TRIGGER instrument_annotation_link_delete_trigger
    BEFORE DELETE ON instrumentannotationlink
    FOR EACH ROW
    EXECUTE PROCEDURE annotation_link_delete_trigger('ome.model.acquisition.Instrument');

CREATE SEQUENCE seq_instrumentannotationlink;
INSERT INTO _lock_ids (name, id) SELECT 'seq_instrumentannotationlink', nextval('_lock_seq');

CREATE VIEW count_instrument_annotationlinks_by_owner (instrument_id, owner_id, count)
    AS SELECT parent, owner_id, count(*)
    FROM instrumentannotationlink GROUP BY parent, owner_id ORDER BY parent;

CREATE TABLE lightpathannotationlink (
    id INT8 PRIMARY KEY,
    permissions INT8 NOT NULL,
    version INT4,
    child INT8 NOT NULL,
    creation_id INT8 NOT NULL,
    external_id INT8 UNIQUE,
    group_id INT8 NOT NULL,
    owner_id INT8 NOT NULL,
    update_id INT8 NOT NULL,
    parent INT8 NOT NULL,
    UNIQUE (parent, child, owner_id),
    CONSTRAINT FKlightpathannotationlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event,
    CONSTRAINT FKlightpathannotationlink_child_annotation FOREIGN KEY (child) REFERENCES annotation,
    CONSTRAINT FKlightpathannotationlink_update_id_event FOREIGN KEY (update_id) REFERENCES event,
    CONSTRAINT FKlightpathannotationlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo,
    CONSTRAINT FKlightpathannotationlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup,
    CONSTRAINT FKlightpathannotationlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter,
    CONSTRAINT FKlightpathannotationlink_parent_lightpath FOREIGN KEY (parent) REFERENCES lightpath
);

CREATE INDEX i_lightpathannotationlink_owner ON lightpathannotationlink(owner_id);
CREATE INDEX i_lightpathannotationlink_group ON lightpathannotationlink(group_id);
CREATE INDEX i_LightPathAnnotationLink_parent ON lightpathannotationlink(parent);
CREATE INDEX i_LightPathAnnotationLink_child ON lightpathannotationlink(child);

CREATE TRIGGER lightpath_annotation_link_event_trigger
    AFTER UPDATE ON lightpathannotationlink
    FOR EACH ROW
    EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.acquisition.LightPath');

CREATE TRIGGER lightpath_annotation_link_delete_trigger
    BEFORE DELETE ON lightpathannotationlink
    FOR EACH ROW
    EXECUTE PROCEDURE annotation_link_delete_trigger('ome.model.acquisition.LightPath');

CREATE SEQUENCE seq_lightpathannotationlink;
INSERT INTO _lock_ids (name, id) SELECT 'seq_lightpathannotationlink', nextval('_lock_seq');

CREATE VIEW count_lightpath_annotationlinks_by_owner (lightpath_id, owner_id, count)
    AS SELECT parent, owner_id, count(*)
    FROM lightpathannotationlink GROUP BY parent, owner_id ORDER BY parent;

CREATE TABLE lightsourceannotationlink (
    id INT8 PRIMARY KEY,
    permissions INT8 NOT NULL,
    version INT4,
    child INT8 NOT NULL,
    creation_id INT8 NOT NULL,
    external_id INT8 UNIQUE,
    group_id INT8 NOT NULL,
    owner_id INT8 NOT NULL,
    update_id INT8 NOT NULL,
    parent INT8 NOT NULL,
    UNIQUE (parent, child, owner_id),
    CONSTRAINT FKlightsourceannotationlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event,
    CONSTRAINT FKlightsourceannotationlink_child_annotation FOREIGN KEY (child) REFERENCES annotation,
    CONSTRAINT FKlightsourceannotationlink_update_id_event FOREIGN KEY (update_id) REFERENCES event,
    CONSTRAINT FKlightsourceannotationlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo,
    CONSTRAINT FKlightsourceannotationlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup,
    CONSTRAINT FKlightsourceannotationlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter,
    CONSTRAINT FKlightsourceannotationlink_parent_lightsource FOREIGN KEY (parent) REFERENCES lightsource
);

CREATE INDEX i_lightsourceannotationlink_owner ON lightsourceannotationlink(owner_id);
CREATE INDEX i_lightsourceannotationlink_group ON lightsourceannotationlink(group_id);
CREATE INDEX i_LightSourceAnnotationLink_parent ON lightsourceannotationlink(parent);
CREATE INDEX i_LightSourceAnnotationLink_child ON lightsourceannotationlink(child);

CREATE TRIGGER lightsource_annotation_link_event_trigger
    AFTER UPDATE ON lightsourceannotationlink
    FOR EACH ROW
    EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.acquisition.LightSource');

CREATE TRIGGER lightsource_annotation_link_delete_trigger
    BEFORE DELETE ON lightsourceannotationlink
    FOR EACH ROW
    EXECUTE PROCEDURE annotation_link_delete_trigger('ome.model.acquisition.LightSource');

CREATE SEQUENCE seq_lightsourceannotationlink;
INSERT INTO _lock_ids (name, id) SELECT 'seq_lightsourceannotationlink', nextval('_lock_seq');

CREATE VIEW count_lightsource_annotationlinks_by_owner (lightsource_id, owner_id, count)
    AS SELECT parent, owner_id, count(*)
    FROM lightsourceannotationlink GROUP BY parent, owner_id ORDER BY parent;

CREATE TABLE objectiveannotationlink (
    id INT8 PRIMARY KEY,
    permissions INT8 NOT NULL,
    version INT4,
    child INT8 NOT NULL,
    creation_id INT8 NOT NULL,
    external_id INT8 UNIQUE,
    group_id INT8 NOT NULL,
    owner_id INT8 NOT NULL,
    update_id INT8 NOT NULL,
    parent INT8 NOT NULL,
    UNIQUE (parent, child, owner_id),
    CONSTRAINT FKobjectiveannotationlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event,
    CONSTRAINT FKobjectiveannotationlink_child_annotation FOREIGN KEY (child) REFERENCES annotation,
    CONSTRAINT FKobjectiveannotationlink_update_id_event FOREIGN KEY (update_id) REFERENCES event,
    CONSTRAINT FKobjectiveannotationlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo,
    CONSTRAINT FKobjectiveannotationlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup,
    CONSTRAINT FKobjectiveannotationlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter,
    CONSTRAINT FKobjectiveannotationlink_parent_objective FOREIGN KEY (parent) REFERENCES objective
);

CREATE INDEX i_objectiveannotationlink_owner ON objectiveannotationlink(owner_id);
CREATE INDEX i_objectiveannotationlink_group ON objectiveannotationlink(group_id);
CREATE INDEX i_ObjectiveAnnotationLink_parent ON objectiveannotationlink(parent);
CREATE INDEX i_ObjectiveAnnotationLink_child ON objectiveannotationlink(child);

CREATE TRIGGER objective_annotation_link_event_trigger
    AFTER UPDATE ON objectiveannotationlink
    FOR EACH ROW
    EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.acquisition.Objective');

CREATE TRIGGER objective_annotation_link_delete_trigger
    BEFORE DELETE ON objectiveannotationlink
    FOR EACH ROW
    EXECUTE PROCEDURE annotation_link_delete_trigger('ome.model.acquisition.Objective');

CREATE SEQUENCE seq_objectiveannotationlink;
INSERT INTO _lock_ids (name, id) SELECT 'seq_objectiveannotationlink', nextval('_lock_seq');

CREATE VIEW count_objective_annotationlinks_by_owner (objective_id, owner_id, count)
    AS SELECT parent, owner_id, count(*)
    FROM objectiveannotationlink GROUP BY parent, owner_id ORDER BY parent;

CREATE TABLE shapeannotationlink (
    id INT8 PRIMARY KEY,
    permissions INT8 NOT NULL,
    version INT4,
    child INT8 NOT NULL,
    creation_id INT8 NOT NULL,
    external_id INT8 UNIQUE,
    group_id INT8 NOT NULL,
    owner_id INT8 NOT NULL,
    update_id INT8 NOT NULL,
    parent INT8 NOT NULL,
    UNIQUE (parent, child, owner_id),
    CONSTRAINT FKshapeannotationlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event,
    CONSTRAINT FKshapeannotationlink_child_annotation FOREIGN KEY (child) REFERENCES annotation,
    CONSTRAINT FKshapeannotationlink_update_id_event FOREIGN KEY (update_id) REFERENCES event,
    CONSTRAINT FKshapeannotationlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo,
    CONSTRAINT FKshapeannotationlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup,
    CONSTRAINT FKshapeannotationlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter,
    CONSTRAINT FKshapeannotationlink_parent_shape FOREIGN KEY (parent) REFERENCES shape
);

CREATE INDEX i_shapeannotationlink_owner ON shapeannotationlink(owner_id);
CREATE INDEX i_shapeannotationlink_group ON shapeannotationlink(group_id);
CREATE INDEX i_ShapeAnnotationLink_parent ON shapeannotationlink(parent);
CREATE INDEX i_ShapeAnnotationLink_child ON shapeannotationlink(child);

CREATE TRIGGER shape_annotation_link_event_trigger
    AFTER UPDATE ON shapeannotationlink
    FOR EACH ROW
    EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.roi.Shape');

CREATE TRIGGER shape_annotation_link_delete_trigger
    BEFORE DELETE ON shapeannotationlink
    FOR EACH ROW
    EXECUTE PROCEDURE annotation_link_delete_trigger('ome.model.roi.Shape');

CREATE SEQUENCE seq_shapeannotationlink;
INSERT INTO _lock_ids (name, id) SELECT 'seq_shapeannotationlink', nextval('_lock_seq');

CREATE VIEW count_shape_annotationlinks_by_owner (shape_id, owner_id, count)
    AS SELECT parent, owner_id, count(*)
    FROM shapeannotationlink GROUP BY parent, owner_id ORDER BY parent;

INSERT INTO imageannotationlink (id, permissions, version, child, creation_id, external_id, group_id, owner_id, update_id, parent)
    SELECT ome_nextval('seq_imageannotationlink'), pal.permissions, pal.version, pal.child, pal.creation_id, pal.external_id, pal.group_id, pal.owner_id, pal.update_id, pixels.image
    FROM pixelsannotationlink pal, pixels
    WHERE pal.parent = pixels.id;

DROP VIEW count_Pixels_annotationLinks_by_owner;
DROP SEQUENCE seq_pixelsannotationlink;
DROP TABLE pixelsannotationlink;

INSERT INTO imageannotationlink (id, permissions, version, child, creation_id, external_id, group_id, owner_id, update_id, parent)
    SELECT ome_nextval('seq_imageannotationlink'), wsl.permissions, wsl.version, wsl.child, wsl.creation_id, wsl.external_id, wsl.group_id, wsl.owner_id, wsl.update_id, wellsample.image
    FROM wellsampleannotationlink wsl, wellsample
    WHERE wsl.parent = wellsample.id;

DROP VIEW count_WellSample_annotationLinks_by_owner;
DROP SEQUENCE seq_wellsampleannotationlink;
DROP TABLE wellsampleannotationlink;

DELETE FROM _lock_ids WHERE 'name' IN ('seq_pixelsannotationlink',
                                       'seq_wellsampleannotationlink');

CREATE OR REPLACE FUNCTION annotation_update_event_trigger() RETURNS "trigger"
    AS '
    DECLARE
        rec RECORD;
        eid INT8;
        cnt INT8;
    BEGIN

        IF NOT EXISTS(SELECT table_name FROM information_schema.tables where table_name = ''_updated_annotations'') THEN
            CREATE TEMP TABLE _updated_annotations (entitytype varchar, entityid INT8) ON COMMIT DELETE ROWS;
        END IF;


        FOR rec IN SELECT id, parent FROM annotationannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.annotations.Annotation'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM channelannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.core.Channel'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM datasetannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.containers.Dataset'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM detectorannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.acquisition.Detector'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM dichroicannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.acquisition.Dichroic'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM experimenterannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.meta.Experimenter'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM experimentergroupannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.meta.ExperimenterGroup'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM filesetannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.fs.Fileset'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM filterannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.acquisition.Filter'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM imageannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.core.Image'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM instrumentannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.acquisition.Instrument'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM lightpathannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.acquisition.LightPath'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM lightsourceannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.acquisition.LightSource'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM namespaceannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.meta.Namespace'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM nodeannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.meta.Node'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM objectiveannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.acquisition.Objective'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM originalfileannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.core.OriginalFile'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM planeinfoannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.core.PlaneInfo'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM plateannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.screen.Plate'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM plateacquisitionannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.screen.PlateAcquisition'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM projectannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.containers.Project'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM reagentannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.screen.Reagent'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM roiannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.roi.Roi'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM screenannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.screen.Screen'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM sessionannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.meta.Session'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM shapeannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.roi.Shape'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM wellannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.screen.Well'');
        END LOOP;

        SELECT INTO cnt count(*) FROM _updated_annotations;
        IF cnt <> 0 THEN
            SELECT INTO eid _current_or_new_event();
            INSERT INTO eventlog (id, action, permissions, entityid, entitytype, event)
                 SELECT ome_nextval(''seq_eventlog''), ''REINDEX'', -52, entityid, entitytype, eid
                   FROM _updated_annotations;
        END IF;

        RETURN new;

    END;'
LANGUAGE plpgsql;

-- #970 adjust constraint for dbpatch versions/patches

ALTER TABLE dbpatch DROP CONSTRAINT unique_dbpatch;

CREATE FUNCTION dbpatch_versions_trigger_function() RETURNS TRIGGER AS $$
BEGIN
    IF (NEW.currentversion <> NEW.previousversion OR NEW.currentpatch <> NEW.previouspatch) AND
       (SELECT COUNT(*) FROM dbpatch WHERE id <> NEW.id AND
        (currentversion <> previousversion OR currentpatch <> previouspatch) AND
        ((currentversion = NEW.currentversion AND currentpatch = NEW.currentpatch) OR
         (previousversion = NEW.previousversion AND previouspatch = NEW.previouspatch))) > 0 THEN
        RAISE 'upgrades cannot be repeated';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER dbpatch_versions_trigger
    BEFORE INSERT OR UPDATE ON dbpatch
    FOR EACH ROW
    EXECUTE PROCEDURE dbpatch_versions_trigger_function();

-- expand password hash and note password change dates

ALTER TABLE password ALTER COLUMN hash TYPE VARCHAR(255);
ALTER TABLE password ADD COLUMN changed TIMESTAMP WITHOUT TIME ZONE;

-- fill in password change dates from event log

CREATE FUNCTION update_changed_from_event_log() RETURNS void AS $$

DECLARE
    exp_id BIGINT;
    time_changed TIMESTAMP WITHOUT TIME ZONE;

BEGIN
    FOR exp_id IN
        SELECT DISTINCT ev.experimenter 
            FROM event ev, eventlog log, experimenter ex
            WHERE log.action = 'PASSWORD' AND ex.omename <> 'root'
            AND ev.id = log.event AND ev.experimenter = ex.id LOOP

        SELECT ev.time
            INTO STRICT time_changed
            FROM event ev, eventlog log
            WHERE log.action = 'PASSWORD' AND ev.experimenter = exp_id
            AND ev.id = log.event
            ORDER BY log.id DESC LIMIT 1;

        UPDATE password SET changed = time_changed
            WHERE experimenter_id = exp_id;
    END LOOP;

END;
$$ LANGUAGE plpgsql;

SELECT update_changed_from_event_log();

DROP FUNCTION update_changed_from_event_log();

-- 5.1DEV__11: time units

CREATE TYPE UnitsTime AS ENUM ('Ys','Zs','Es','Ps','Ts','Gs','Ms','ks','hs','das','s','ds','cs','ms','µs','ns','ps','fs','as','zs','ys','min','h','d');

ALTER TABLE pixels
	ADD COLUMN timeincrementunit unitstime;

ALTER TABLE planeinfo
	ADD COLUMN deltatunit unitstime,
	ADD COLUMN exposuretimeunit unitstime;

CREATE INDEX i_pixels_timeincrement ON pixels USING btree (timeincrement);

CREATE INDEX i_planeinfo_deltat ON planeinfo USING btree (deltat);

CREATE INDEX i_planeinfo_exposuretime ON planeinfo USING btree (exposuretime);

-- 5.1DEV__11: Manual adjustments, mostly from psql-footer.sql

update pixels set timeincrementunit = 's'::unitstime where timeincrement is not null;
update planeinfo set deltatunit = 's'::unitstime where deltat is not null;
update planeinfo set exposuretimeunit = 's'::unitstime where exposuretime is not null;


-- OMERO5.1DEV__12: #2587 LDAP: remove DN from OMERO DB.

-- Add "ldap" column to "experimenter", default to false

ALTER TABLE experimenter ADD COLUMN ldap BOOL NOT NULL DEFAULT false;

-- Set "ldap" value based on "dn" from "password"

UPDATE experimenter e SET ldap = true
    FROM password p
    WHERE e.id = p.experimenter_id AND
          p.dn IS NOT NULL;

-- Drop "dn" from "password" and delete entries that have a DN set
-- and no password

DELETE FROM password WHERE dn IS NOT NULL AND hash IS NULL;
ALTER TABLE password DROP COLUMN dn;


-- 5.1DEV__13: other units

CREATE TYPE UnitsElectricPotential AS ENUM ('YV','ZV','EV','PV','TV','GV','MV','kV','hV','daV','V','dV','cV','mV','µV','nV','pV','fV','aV','zV','yV');

CREATE TYPE UnitsFrequency AS ENUM ('YHz','ZHz','EHz','PHz','THz','GHz','MHz','kHz','hHz','daHz','Hz','dHz','cHz','mHz','µHz','nHz','pHz','fHz','aHz','zHz','yHz');

CREATE TYPE UnitsLength AS ENUM ('Ym','Zm','Em','Pm','Tm','Gm','Mm','km','hm','dam','m','dm','cm','mm','µm','nm','pm','fm','am','zm','ym','Å','ua','ly','pc','thou','li','in','ft','yd','mi','pt','pixel','reference frame');

CREATE TYPE UnitsPower AS ENUM ('YW','ZW','EW','PW','TW','GW','MW','kW','hW','daW','W','dW','cW','mW','µW','nW','pW','fW','aW','zW','yW');

CREATE TYPE UnitsPressure AS ENUM ('YPa','ZPa','EPa','PPa','TPa','GPa','MPa','kPa','hPa','daPa','Pa','dPa','cPa','mPa','µPa','nPa','pPa','fPa','aPa','zPa','yPa','bar','Mbar','kBar','dbar','cbar','mbar','atm','psi','Torr','mTorr','mm Hg');

CREATE TYPE UnitsTemperature AS ENUM ('K','°C','°F','°R');

ALTER TABLE detector
	ADD COLUMN voltageunit unitselectricpotential;

ALTER TABLE detectorsettings
	ADD COLUMN readoutrateunit unitsfrequency,
	ADD COLUMN voltageunit unitselectricpotential;

ALTER TABLE imagingenvironment
	ADD COLUMN airpressureunit unitspressure,
	ADD COLUMN temperatureunit unitstemperature;

ALTER TABLE laser
	ADD COLUMN repetitionrateunit unitsfrequency,
	ADD COLUMN wavelengthunit unitslength;

ALTER TABLE lightsettings
	ADD COLUMN wavelengthunit unitslength;

ALTER TABLE lightsource
	ADD COLUMN powerunit unitspower;

ALTER TABLE logicalchannel
	ADD COLUMN emissionwaveunit unitslength,
	ADD COLUMN excitationwaveunit unitslength,
	ADD COLUMN pinholesizeunit unitslength;

ALTER TABLE objective
	ADD COLUMN workingdistanceunit unitslength;

ALTER TABLE pixels
	ADD COLUMN physicalsizexunit unitslength,
	ADD COLUMN physicalsizeyunit unitslength,
	ADD COLUMN physicalsizezunit unitslength;

ALTER TABLE planeinfo
	ADD COLUMN positionxunit unitslength,
	ADD COLUMN positionyunit unitslength,
	ADD COLUMN positionzunit unitslength;

ALTER TABLE plate
	ADD COLUMN welloriginxunit unitslength,
	ADD COLUMN welloriginyunit unitslength;

ALTER TABLE shape
	ADD COLUMN fontsizeunit unitslength,
	ADD COLUMN strokewidthunit unitslength;

ALTER TABLE stagelabel
	ADD COLUMN positionxunit unitslength,
	ADD COLUMN positionyunit unitslength,
	ADD COLUMN positionzunit unitslength;

ALTER TABLE transmittancerange
	ADD COLUMN cutinunit unitslength,
	ADD COLUMN cutintoleranceunit unitslength,
	ADD COLUMN cutoutunit unitslength,
	ADD COLUMN cutouttoleranceunit unitslength;

ALTER TABLE wellsample
	ADD COLUMN posxunit unitslength,
	ADD COLUMN posyunit unitslength;

CREATE INDEX i_detector_voltage ON detector USING btree (voltage);

CREATE INDEX i_detectorsettings_readoutrate ON detectorsettings USING btree (readoutrate);

CREATE INDEX i_detectorsettings_voltage ON detectorsettings USING btree (voltage);

CREATE INDEX i_imagingenvironment_airpressure ON imagingenvironment USING btree (airpressure);

CREATE INDEX i_imagingenvironment_temperature ON imagingenvironment USING btree (temperature);

CREATE INDEX i_laser_repetitionrate ON laser USING btree (repetitionrate);

CREATE INDEX i_laser_wavelength ON laser USING btree (wavelength);

CREATE INDEX i_lightsettings_wavelength ON lightsettings USING btree (wavelength);

CREATE INDEX i_lightsource_power ON lightsource USING btree (power);

CREATE INDEX i_logicalchannel_emissionwave ON logicalchannel USING btree (emissionwave);

CREATE INDEX i_logicalchannel_excitationwave ON logicalchannel USING btree (excitationwave);

CREATE INDEX i_logicalchannel_pinholesize ON logicalchannel USING btree (pinholesize);

CREATE INDEX i_objective_workingdistance ON objective USING btree (workingdistance);

CREATE INDEX i_pixels_physicalsizex ON pixels USING btree (physicalsizex);

CREATE INDEX i_pixels_physicalsizey ON pixels USING btree (physicalsizey);

CREATE INDEX i_pixels_physicalsizez ON pixels USING btree (physicalsizez);

CREATE INDEX i_planeinfo_positionx ON planeinfo USING btree (positionx);

CREATE INDEX i_planeinfo_positiony ON planeinfo USING btree (positiony);

CREATE INDEX i_planeinfo_positionz ON planeinfo USING btree (positionz);

CREATE INDEX i_plate_welloriginx ON plate USING btree (welloriginx);

CREATE INDEX i_plate_welloriginy ON plate USING btree (welloriginy);

CREATE INDEX i_shape_fontsize ON shape USING btree (fontsize);

CREATE INDEX i_shape_strokewidth ON shape USING btree (strokewidth);

CREATE INDEX i_stagelabel_positionx ON stagelabel USING btree (positionx);

CREATE INDEX i_stagelabel_positiony ON stagelabel USING btree (positiony);

CREATE INDEX i_stagelabel_positionz ON stagelabel USING btree (positionz);

CREATE INDEX i_transmittancerange_cutin ON transmittancerange USING btree (cutin);

CREATE INDEX i_transmittancerange_cutintolerance ON transmittancerange USING btree (cutintolerance);

CREATE INDEX i_transmittancerange_cutout ON transmittancerange USING btree (cutout);

CREATE INDEX i_transmittancerange_cutouttolerance ON transmittancerange USING btree (cutouttolerance);

CREATE INDEX i_wellsample_posx ON wellsample USING btree (posx);

CREATE INDEX i_wellsample_posy ON wellsample USING btree (posy);

CREATE TRIGGER detector_annotation_link_event_trigger_insert
	AFTER INSERT ON detectorannotationlink
	FOR EACH ROW
	EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.acquisition.Detector');

CREATE TRIGGER dichroic_annotation_link_event_trigger_insert
	AFTER INSERT ON dichroicannotationlink
	FOR EACH ROW
	EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.acquisition.Dichroic');

CREATE TRIGGER filter_annotation_link_event_trigger_insert
	AFTER INSERT ON filterannotationlink
	FOR EACH ROW
	EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.acquisition.Filter');

CREATE TRIGGER instrument_annotation_link_event_trigger_insert
	AFTER INSERT ON instrumentannotationlink
	FOR EACH ROW
	EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.acquisition.Instrument');

CREATE TRIGGER lightpath_annotation_link_event_trigger_insert
	AFTER INSERT ON lightpathannotationlink
	FOR EACH ROW
	EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.acquisition.LightPath');

CREATE TRIGGER lightsource_annotation_link_event_trigger_insert
	AFTER INSERT ON lightsourceannotationlink
	FOR EACH ROW
	EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.acquisition.LightSource');

CREATE TRIGGER objective_annotation_link_event_trigger_insert
	AFTER INSERT ON objectiveannotationlink
	FOR EACH ROW
	EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.acquisition.Objective');

CREATE TRIGGER shape_annotation_link_event_trigger_insert
	AFTER INSERT ON shapeannotationlink
	FOR EACH ROW
	EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.roi.Shape');

-- 5.1DEV__13: Manual adjustments, mostly from psql-footer.sql

update pixels set timeincrementunit = 's'::unitstime where timeincrement is not null;

update planeinfo set deltatunit = 's'::unitstime where deltat is not null;
update planeinfo set exposuretimeunit = 's'::unitstime where exposuretime is not null;

update detector set voltageunit = 'V'::unitselectricpotential where  voltage is not null;

update detectorsettings set readoutrateunit = 'MHz'::unitsfrequency where readoutrate is not null;
update detectorsettings set voltageunit = 'V'::unitselectricpotential where voltage is not null;

update imagingenvironment set airpressureunit = 'mbar'::unitspressure where airpressure is not null;
update imagingenvironment set temperatureunit = '°C'::unitstemperature where temperature is not null;

update laser set repetitionrateunit = 'Hz'::unitsfrequency where repetitionrate is not null;
update laser set wavelengthunit = 'nm'::unitslength where wavelength is not null;

update lightsettings set wavelengthunit = 'nm'::unitslength where wavelength is not null;

update lightsource set powerunit = 'mW'::unitspower where power is not null;

update logicalchannel set emissionwaveunit = 'nm'::unitslength where emissionwave is not null;
update logicalchannel set excitationwaveunit = 'nm'::unitslength where excitationwave is not null;
update logicalchannel set pinholesizeunit = 'µm'::unitslength where pinholesize is not null;

update objective set workingdistanceunit = 'µm'::unitslength where workingdistance is not null;

update pixels set physicalsizexunit = 'µm'::unitslength where physicalsizex is not null;
update pixels set physicalsizeyunit = 'µm'::unitslength where physicalsizey is not null;
update pixels set physicalsizezunit = 'µm'::unitslength where physicalsizez is not null;

update planeinfo set positionxunit = 'reference frame'::unitslength where positionx is not null;
update planeinfo set positionyunit = 'reference frame'::unitslength where positiony is not null;
update planeinfo set positionzunit = 'reference frame'::unitslength where positionz is not null;

update plate set welloriginxunit = 'reference frame'::unitslength where welloriginx is not null;
update plate set welloriginyunit = 'reference frame'::unitslength where welloriginy is not null;

update shape set fontsizeunit = 'pt'::unitslength  where fontsize is not null;
update shape set strokewidthunit = 'pixel'::unitslength  where strokewidth is not null;

update stagelabel set positionxunit = 'reference frame'::unitslength where positionx is not null;
update stagelabel set positionyunit = 'reference frame'::unitslength where positiony is not null;
update stagelabel set positionzunit = 'reference frame'::unitslength where positionz is not null;

update transmittancerange set cutinunit = 'nm'::unitslength where cutin is not null;
update transmittancerange set cutintoleranceunit = 'nm'::unitslength where cutintolerance is not null;
update transmittancerange set cutoutunit = 'nm'::unitslength where cutout is not null;
update transmittancerange set cutouttoleranceunit = 'nm'::unitslength where cutouttolerance is not null;

update wellsample set posxunit = 'reference frame'::unitslength where posx is not null;
update wellsample set posyunit = 'reference frame'::unitslength where posy is not null;

-- reactivate not null constraints
alter table pixelstype alter column bitsize set not null;

-- fix column types that aren't enums
ALTER TABLE shape
        ALTER COLUMN fontsize TYPE double precision /* TYPE change - table: shape original: integer new: double precision */,
        ALTER COLUMN strokewidth TYPE double precision /* TYPE change - table: shape original: integer new: double precision */;

ALTER TABLE transmittancerange
        ALTER COLUMN cutin TYPE positive_float /* TYPE change - table: transmittancerange original: positive_int new: positive_float */,
        ALTER COLUMN cutout TYPE positive_float /* TYPE change - table: transmittancerange original: positive_int new: positive_float */;


--
-- FINISHED
--

UPDATE dbpatch SET message = 'Database updated.', finished = clock_timestamp()
    WHERE currentVersion  = 'OMERO5.1DEV' AND
          currentPatch    = 13            AND
          previousVersion = 'OMERO5.0'    AND
          previousPatch   = 0;

SELECT CHR(10)||CHR(10)||CHR(10)||'YOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION OMERO5.1DEV__13'||CHR(10)||CHR(10)||CHR(10) AS Status;

COMMIT;
