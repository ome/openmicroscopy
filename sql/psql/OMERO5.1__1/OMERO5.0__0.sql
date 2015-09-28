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
--- OMERO5 release upgrade from OMERO5.0__0 to OMERO5.1__1.
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


--
-- check PostgreSQL server version and database encoding
--

CREATE FUNCTION assert_db_server_prerequisites(version_prereq INTEGER) RETURNS void AS $$

DECLARE
    version_num INTEGER;
    char_encoding TEXT;

BEGIN
    SELECT CAST(setting AS INTEGER) INTO STRICT version_num FROM pg_settings WHERE name = 'server_version_num';
    SELECT pg_encoding_to_char(encoding) INTO STRICT char_encoding FROM pg_database WHERE datname = current_database();

    IF version_num < version_prereq THEN
        RAISE EXCEPTION 'database server version % is less than OMERO prerequisite %', version_num, version_prereq;
    END IF;

    IF char_encoding != 'UTF8' THEN
        RAISE EXCEPTION 'OMERO database character encoding must be UTF8, not %', char_encoding;
    ELSE
        SET client_encoding = 'UTF8';
    END IF;

END;$$ LANGUAGE plpgsql;

SELECT assert_db_server_prerequisites(90200);
DROP FUNCTION assert_db_server_prerequisites(INTEGER);


INSERT INTO dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
             VALUES ('OMERO5.1',     1,             'OMERO5.0',          0);

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

-- this patch's updates are superseded by 5.1DEV__13

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

CREATE TYPE UnitsPressure AS ENUM ('YPa','ZPa','EPa','PPa','TPa','GPa','MPa','kPa','hPa','daPa','Pa','dPa','cPa','mPa','µPa','nPa','pPa','fPa','aPa','zPa','yPa','bar','Mbar','kbar','dbar','cbar','mbar','atm','psi','Torr','mTorr','mm Hg');

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

update detector set voltageunit = 'V'::unitselectricpotential where voltage is not null;

update detectorsettings
  set readoutrateunit = case when readoutrate is null then null else 'MHz'::unitsfrequency end,
      voltageunit = case when voltage is null then null else 'V'::unitselectricpotential end
  where readoutrate is not null or voltage is not null;

update imagingenvironment
  set airpressureunit = case when airpressure is null then null else 'mbar'::unitspressure end,
      temperatureunit = case when temperature is null then null else '°C'::unitstemperature end
  where airpressure is not null or temperature is not null;

update laser
  set repetitionrateunit = case when repetitionrate is null then null else 'Hz'::unitsfrequency end,
      wavelengthunit = case when wavelength is null then null else 'nm'::unitslength end
  where repetitionrate is not null or wavelength is not null;

update lightsettings set wavelengthunit = 'nm'::unitslength where wavelength is not null;

update lightsource set powerunit = 'mW'::unitspower where power is not null;

update logicalchannel
  set emissionwaveunit = case when emissionwave is null then null else 'nm'::unitslength end,
      excitationwaveunit = case when excitationwave is null then null else 'nm'::unitslength end,
      pinholesizeunit = case when pinholesize is null then null else 'µm'::unitslength end
  where emissionwave is not null or excitationwave is not null or pinholesize is not null;

update objective set workingdistanceunit = 'µm'::unitslength where workingdistance is not null;

update pixels
  set physicalsizexunit = case when physicalsizex is null then null else 'µm'::unitslength end,
      physicalsizeyunit = case when physicalsizey is null then null else 'µm'::unitslength end,
      physicalsizezunit = case when physicalsizez is null then null else 'µm'::unitslength end,
      timeincrementunit = case when timeincrement is null then null else 's'::unitstime end
  where physicalsizex is not null or physicalsizey is not null or physicalsizez is not null or timeincrement is not null;

update planeinfo
  set deltatunit = case when deltat is null then null else 's'::unitstime end,
      exposuretimeunit = case when exposuretime is null then null else 's'::unitstime end,
      positionxunit = case when positionx is null then null else 'reference frame'::unitslength end,
      positionyunit = case when positiony is null then null else 'reference frame'::unitslength end,
      positionzunit = case when positionz is null then null else 'reference frame'::unitslength end
  where deltat is not null or exposuretime is not null or positionx is not null or positiony is not null or positionz is not null;

update plate
  set welloriginxunit = case when welloriginx is null then null else 'reference frame'::unitslength end,
      welloriginyunit = case when welloriginy is null then null else 'reference frame'::unitslength end
  where welloriginx is not null or welloriginy is not null;

update shape
  set fontsizeunit = case when fontsize is null then null else 'pt'::unitslength end,
      strokewidthunit = case when strokewidth is null then null else 'pixel'::unitslength end
  where fontsize is not null or strokewidth is not null;

update stagelabel
  set positionxunit = case when positionx is null then null else 'reference frame'::unitslength end,
      positionyunit = case when positiony is null then null else 'reference frame'::unitslength end,
      positionzunit = case when positionz is null then null else 'reference frame'::unitslength end
  where positionx is not null or positiony is not null or positionz is not null;

update transmittancerange
  set cutinunit = case when cutin is null then null else 'nm'::unitslength end,
      cutintoleranceunit = case when cutintolerance is null then null else 'nm'::unitslength end,
      cutoutunit = case when cutout is null then null else 'nm'::unitslength end,
      cutouttoleranceunit = case when cutouttolerance is null then null else 'nm'::unitslength end
  where cutin is not null or cutintolerance is not null or cutout is not null or cutouttolerance is not null;

update wellsample
  set posxunit = case when posx is null then null else 'reference frame'::unitslength end,
      posyunit = case when posy is null then null else 'reference frame'::unitslength end
  where posx is not null or posy is not null;

-- reactivate not null constraints
alter table pixelstype alter column bitsize set not null;

-- fix column types that aren't enums
ALTER TABLE shape
        ALTER COLUMN fontsize TYPE double precision /* TYPE change - table: shape original: integer new: double precision */,
        ALTER COLUMN strokewidth TYPE double precision /* TYPE change - table: shape original: integer new: double precision */;

ALTER TABLE transmittancerange
        ALTER COLUMN cutin TYPE positive_float /* TYPE change - table: transmittancerange original: positive_int new: positive_float */,
        ALTER COLUMN cutout TYPE positive_float /* TYPE change - table: transmittancerange original: positive_int new: positive_float */;

-- #6719 LDAP: Add DN for groups

-- Add "ldap" column to "experimentergroup", default to false

ALTER TABLE experimentergroup ADD COLUMN ldap BOOL NOT NULL DEFAULT false;

-- 5.1DEV__15: fix missing nonnegative_float domain

CREATE DOMAIN nonnegative_float AS DOUBLE PRECISION CHECK (VALUE >= 0);

ALTER TABLE transmittancerange
    ALTER COLUMN cutintolerance TYPE nonnegative_float,
    ALTER COLUMN cutouttolerance TYPE nonnegative_float;

-- 5.1DEV__16: ordered maps

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


-- 5.1DEV__17: node, deletelogs, etc.

ALTER TABLE node
    ALTER COLUMN conn TYPE text;

create index _fs_deletelog_event on _fs_deletelog(event_id);
create index _fs_deletelog_file on _fs_deletelog(file_id);
create index _fs_deletelog_owner on _fs_deletelog(owner_id);
create index _fs_deletelog_group on _fs_deletelog(group_id);
create index _fs_deletelog_path on _fs_deletelog(path);
create index _fs_deletelog_name on _fs_deletelog(name);
create index _fs_deletelog_repo on _fs_deletelog(repo);


-- 5.1DEV__18: fix #11182: reduce duplication in REINDEX logs upon chgrp

CREATE TABLE _updated_annotations (
    event_id BIGINT NOT NULL,
    entity_type TEXT NOT NULL,
    entity_id BIGINT NOT NULL,
    CONSTRAINT FK_updated_annotations_event_id
        FOREIGN KEY (event_id)
        REFERENCES event);

CREATE INDEX _updated_annotations_event_index
    ON _updated_annotations (event_id);

CREATE INDEX _updated_annotations_row_index
    ON _updated_annotations (event_id, entity_type, entity_id);


CREATE OR REPLACE FUNCTION annotation_update_event_trigger() RETURNS TRIGGER AS $$

    DECLARE
        pid BIGINT;
        eid BIGINT;

    BEGIN
        SELECT INTO eid _current_or_new_event();
 
        FOR pid IN SELECT DISTINCT parent FROM annotationannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _updated_annotations (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.annotations.Annotation', pid
                WHERE NOT EXISTS (SELECT 1 FROM _updated_annotations AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.annotations.Annotation' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM channelannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _updated_annotations (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.core.Channel', pid
                WHERE NOT EXISTS (SELECT 1 FROM _updated_annotations AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.core.Channel' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM datasetannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _updated_annotations (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.containers.Dataset', pid
                WHERE NOT EXISTS (SELECT 1 FROM _updated_annotations AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.containers.Dataset' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM detectorannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _updated_annotations (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.acquisition.Detector', pid
                WHERE NOT EXISTS (SELECT 1 FROM _updated_annotations AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.acquisition.Detector' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM dichroicannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _updated_annotations (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.acquisition.Dichroic', pid
                WHERE NOT EXISTS (SELECT 1 FROM _updated_annotations AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.acquisition.Dichroic' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM experimenterannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _updated_annotations (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.meta.Experimenter', pid
                WHERE NOT EXISTS (SELECT 1 FROM _updated_annotations AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.meta.Experimenter' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM experimentergroupannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _updated_annotations (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.meta.ExperimenterGroup', pid
                WHERE NOT EXISTS (SELECT 1 FROM _updated_annotations AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.meta.ExperimenterGroup' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM filesetannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _updated_annotations (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.fs.Fileset', pid
                WHERE NOT EXISTS (SELECT 1 FROM _updated_annotations AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.fs.Fileset' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM filterannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _updated_annotations (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.acquisition.Filter', pid
                WHERE NOT EXISTS (SELECT 1 FROM _updated_annotations AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.acquisition.Filter' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM imageannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _updated_annotations (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.core.Image', pid
                WHERE NOT EXISTS (SELECT 1 FROM _updated_annotations AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.core.Image' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM instrumentannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _updated_annotations (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.acquisition.Instrument', pid
                WHERE NOT EXISTS (SELECT 1 FROM _updated_annotations AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.acquisition.Instrument' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM lightpathannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _updated_annotations (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.acquisition.LightPath', pid
                WHERE NOT EXISTS (SELECT 1 FROM _updated_annotations AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.acquisition.LightPath' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM lightsourceannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _updated_annotations (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.acquisition.LightSource', pid
                WHERE NOT EXISTS (SELECT 1 FROM _updated_annotations AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.acquisition.LightSource' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM namespaceannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _updated_annotations (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.meta.Namespace', pid
                WHERE NOT EXISTS (SELECT 1 FROM _updated_annotations AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.meta.Namespace' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM nodeannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _updated_annotations (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.meta.Node', pid
                WHERE NOT EXISTS (SELECT 1 FROM _updated_annotations AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.meta.Node' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM objectiveannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _updated_annotations (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.acquisition.Objective', pid
                WHERE NOT EXISTS (SELECT 1 FROM _updated_annotations AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.acquisition.Objective' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM originalfileannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _updated_annotations (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.core.OriginalFile', pid
                WHERE NOT EXISTS (SELECT 1 FROM _updated_annotations AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.core.OriginalFile' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM planeinfoannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _updated_annotations (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.core.PlaneInfo', pid
                WHERE NOT EXISTS (SELECT 1 FROM _updated_annotations AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.core.PlaneInfo' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM plateannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _updated_annotations (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.screen.Plate', pid
                WHERE NOT EXISTS (SELECT 1 FROM _updated_annotations AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.screen.Plate' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM plateacquisitionannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _updated_annotations (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.screen.PlateAcquisition', pid
                WHERE NOT EXISTS (SELECT 1 FROM _updated_annotations AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.screen.PlateAcquisition' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM projectannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _updated_annotations (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.containers.Project', pid
                WHERE NOT EXISTS (SELECT 1 FROM _updated_annotations AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.containers.Project' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM reagentannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _updated_annotations (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.screen.Reagent', pid
                WHERE NOT EXISTS (SELECT 1 FROM _updated_annotations AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.screen.Reagent' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM roiannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _updated_annotations (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.roi.Roi', pid
                WHERE NOT EXISTS (SELECT 1 FROM _updated_annotations AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.roi.Roi' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM screenannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _updated_annotations (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.screen.Screen', pid
                WHERE NOT EXISTS (SELECT 1 FROM _updated_annotations AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.screen.Screen' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM sessionannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _updated_annotations (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.meta.Session', pid
                WHERE NOT EXISTS (SELECT 1 FROM _updated_annotations AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.meta.Session' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM shapeannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _updated_annotations (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.roi.Shape', pid
                WHERE NOT EXISTS (SELECT 1 FROM _updated_annotations AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.roi.Shape' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM wellannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _updated_annotations (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.screen.Well', pid
                WHERE NOT EXISTS (SELECT 1 FROM _updated_annotations AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.screen.Well' AND ua.entity_id = pid);
        END LOOP;

        RETURN new;
    END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION annotation_link_event_trigger() RETURNS TRIGGER AS $$

    DECLARE
        eid int8;

    BEGIN
        SELECT INTO eid _current_or_new_event();

        INSERT INTO _updated_annotations (event_id, entity_type, entity_id)
            SELECT eid, TG_ARGV[0], new.parent
            WHERE NOT EXISTS (SELECT 1 FROM _updated_annotations AS ua
                WHERE ua.event_id = eid AND ua.entity_type = TG_ARGV[0] AND ua.entity_id = new.parent);

        RETURN new;

    END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION annotation_link_delete_trigger() RETURNS TRIGGER AS $$

    DECLARE
        eid int8;

    BEGIN
        SELECT INTO eid _current_or_new_event();

        INSERT INTO _updated_annotations (event_id, entity_type, entity_id)
            SELECT eid, TG_ARGV[0], old.parent
            WHERE NOT EXISTS (SELECT 1 FROM _updated_annotations AS ua
                WHERE ua.event_id = eid AND ua.entity_type = TG_ARGV[0] AND ua.entity_id = old.parent);

        RETURN old;

    END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION annotation_updates_note_reindex() RETURNS void AS $$

    DECLARE
        row RECORD;

    BEGIN
        FOR row IN SELECT * FROM _updated_annotations ORDER BY event_id FOR UPDATE
        LOOP
            DELETE FROM _updated_annotations WHERE _updated_annotations = row;

            INSERT INTO eventlog (id, action, permissions, entityid, entitytype, event)
                SELECT ome_nextval('seq_eventlog'), 'REINDEX', -52, row.entity_id, row.entity_type, row.event_id
                WHERE NOT EXISTS (SELECT 1 FROM eventlog AS el
                    WHERE el.entityid = row.entity_id AND el.entitytype = row.entity_type AND el.event = row.event_id);

        END LOOP;
    END;
$$ LANGUAGE plpgsql;

-- now delete duplicates from eventlog

DELETE FROM eventlog WHERE id IN
    (SELECT id_row.id
         FROM (SELECT id, row_number() OVER (PARTITION BY entityid, entitytype, event, permissions ORDER BY id) AS row_n
                   FROM eventlog WHERE action = 'REINDEX' AND external_id IS NULL) AS id_row
         WHERE id_row.row_n > 1);

--
-- have _fs_dir_delete trigger check only within repo
--

create or replace function _fs_dir_delete() returns trigger AS $_fs_dir_delete$
    begin
        --
        -- If any children are found, prevent deletion
        --
        if OLD.mimetype = 'Directory' and exists(
            select id from originalfile
            where repo = OLD.repo and path = OLD.path || OLD.name || '/'
            limit 1) then

                -- CANCEL DELETE
                RAISE EXCEPTION '%', 'Directory('||OLD.id||')='||OLD.path||OLD.name||'/ is not empty!';

        end if;
        return OLD; -- proceed
    end;
$_fs_dir_delete$ LANGUAGE plpgsql;

create index originalfile_path_index on originalfile (path);


-- 5.1DEV__19: Various model changes, especially to namespaces.

-- Replace globals' annotation count tables with views.

DROP TABLE count_experimenter_annotationlinks_by_owner;
DROP TABLE count_experimentergroup_annotationlinks_by_owner;
DROP TABLE count_node_annotationlinks_by_owner;
DROP TABLE count_session_annotationlinks_by_owner;

CREATE VIEW count_experimenter_annotationlinks_by_owner (experimenter_id, owner_id, count) AS
    SELECT parent, owner_id, count(*)
        FROM experimenterannotationlink
        GROUP BY parent, owner_id
        ORDER BY parent;

CREATE VIEW count_experimentergroup_annotationlinks_by_owner (experimentergroup_id, owner_id, count) AS
    SELECT parent, owner_id, count(*)
        FROM experimentergroupannotationlink
        GROUP BY parent, owner_id
        ORDER BY parent;

CREATE VIEW count_node_annotationlinks_by_owner (node_id, owner_id, count) AS
    SELECT parent, owner_id, count(*)
        FROM nodeannotationlink
        GROUP BY parent, owner_id
        ORDER BY parent;

CREATE VIEW count_session_annotationlinks_by_owner (session_id, owner_id, count) AS
    SELECT parent, owner_id, count(*)
        FROM sessionannotationlink
        GROUP BY parent, owner_id
        ORDER BY parent;

-- Namespace is now a global.

ALTER TABLE namespace DROP COLUMN creation_id;
ALTER TABLE namespace DROP COLUMN group_id;
ALTER TABLE namespace DROP COLUMN owner_id;
ALTER TABLE namespace DROP COLUMN update_id;

-- More objects are named.

ALTER TABLE annotation ADD COLUMN name VARCHAR(255);
ALTER TABLE namespace ADD COLUMN displayname VARCHAR(255);
ALTER TABLE roi ADD COLUMN name VARCHAR(255);

CREATE INDEX annotation_name ON annotation(name);
CREATE INDEX namespace_displayname ON namespace(displayname);
CREATE INDEX roi_name ON roi(name);

-- The namespace table now reflects all the namespaces used in the annotation.ns column.

CREATE FUNCTION add_to_namespace() RETURNS "trigger" AS $$
    BEGIN
        IF NOT (NEW.ns IS NULL OR EXISTS (SELECT 1 FROM namespace WHERE name = NEW.ns LIMIT 1)) THEN
            INSERT INTO namespace (id, name, permissions)
                SELECT ome_nextval('seq_namespace'), NEW.ns, -52;
        END IF;

        RETURN NULL;
    END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION update_namespace() RETURNS "trigger" AS $$
    BEGIN
        IF OLD.name <> NEW.name AND EXISTS (SELECT 1 FROM annotation WHERE ns = OLD.name LIMIT 1) THEN
            RAISE EXCEPTION 'cannot rename namespace that is still used by annotation';
        END IF;

        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION delete_from_namespace() RETURNS "trigger" AS $$
    BEGIN
        IF EXISTS (SELECT 1 FROM annotation WHERE ns = OLD.name LIMIT 1) THEN
            RAISE EXCEPTION 'cannot delete namespace that is still used by annotation';
        END IF;

        RETURN OLD;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER add_to_namespace
    AFTER INSERT OR UPDATE ON annotation
    FOR EACH ROW EXECUTE PROCEDURE add_to_namespace();

CREATE TRIGGER update_namespace
    BEFORE UPDATE ON namespace
    FOR EACH ROW EXECUTE PROCEDURE update_namespace();

CREATE TRIGGER delete_from_namespace
    BEFORE DELETE ON namespace
    FOR EACH ROW EXECUTE PROCEDURE delete_from_namespace();

INSERT INTO namespace (id, name, permissions)
    SELECT ome_nextval('seq_namespace'), ns, -52
        FROM annotation WHERE id IN
             (SELECT id_row.id 
                  FROM (SELECT id, row_number() OVER (PARTITION BY ns) AS row_n FROM annotation
                            WHERE ns IS NOT NULL AND ns NOT IN (SELECT name FROM namespace)) AS id_row
                  WHERE id_row.row_n = 1);

-- A property value is null if and only if the corresponding unit is null.

ALTER TABLE detector ADD CONSTRAINT voltage_unitpair
    CHECK (voltage IS NULL AND voltageunit IS NULL
        OR voltage IS NOT NULL AND voltageunit IS NOT NULL);

ALTER TABLE detectorsettings ADD CONSTRAINT readoutrate_unitpair
    CHECK (readoutrate IS NULL AND readoutrateunit IS NULL
        OR readoutrate IS NOT NULL AND readoutrateunit IS NOT NULL);

ALTER TABLE detectorsettings ADD CONSTRAINT voltage_unitpair
    CHECK (voltage IS NULL AND voltageunit IS NULL
        OR voltage IS NOT NULL AND voltageunit IS NOT NULL);

ALTER TABLE imagingenvironment ADD CONSTRAINT airpressure_unitpair
    CHECK (airpressure IS NULL AND airpressureunit IS NULL
        OR airpressure IS NOT NULL AND airpressureunit IS NOT NULL);

ALTER TABLE imagingenvironment ADD CONSTRAINT temperature_unitpair
    CHECK (temperature IS NULL AND temperatureunit IS NULL
        OR temperature IS NOT NULL AND temperatureunit IS NOT NULL);

ALTER TABLE laser ADD CONSTRAINT repetitionrate_unitpair
    CHECK (repetitionrate IS NULL AND repetitionrateunit IS NULL
        OR repetitionrate IS NOT NULL AND repetitionrateunit IS NOT NULL);

ALTER TABLE laser ADD CONSTRAINT wavelength_unitpair
    CHECK (wavelength IS NULL AND wavelengthunit IS NULL
        OR wavelength IS NOT NULL AND wavelengthunit IS NOT NULL);

ALTER TABLE lightsettings ADD CONSTRAINT wavelength_unitpair
    CHECK (wavelength IS NULL AND wavelengthunit IS NULL
        OR wavelength IS NOT NULL AND wavelengthunit IS NOT NULL);

ALTER TABLE lightsource ADD CONSTRAINT power_unitpair
    CHECK (power IS NULL AND powerunit IS NULL
        OR power IS NOT NULL AND powerunit IS NOT NULL);

ALTER TABLE logicalchannel ADD CONSTRAINT emissionwave_unitpair
    CHECK (emissionwave IS NULL AND emissionwaveunit IS NULL
        OR emissionwave IS NOT NULL AND emissionwaveunit IS NOT NULL);

ALTER TABLE logicalchannel ADD CONSTRAINT excitationwave_unitpair
    CHECK (excitationwave IS NULL AND excitationwaveunit IS NULL
        OR excitationwave IS NOT NULL AND excitationwaveunit IS NOT NULL);

ALTER TABLE logicalchannel ADD CONSTRAINT pinholesize_unitpair
    CHECK (pinholesize IS NULL AND pinholesizeunit IS NULL
        OR pinholesize IS NOT NULL AND pinholesizeunit IS NOT NULL);

ALTER TABLE objective ADD CONSTRAINT workingdistance_unitpair
    CHECK (workingdistance IS NULL AND workingdistanceunit IS NULL
        OR workingdistance IS NOT NULL AND workingdistanceunit IS NOT NULL);

ALTER TABLE pixels ADD CONSTRAINT physicalsizex_unitpair
    CHECK (physicalsizex IS NULL AND physicalsizexunit IS NULL
        OR physicalsizex IS NOT NULL AND physicalsizexunit IS NOT NULL);

ALTER TABLE pixels ADD CONSTRAINT physicalsizey_unitpair
    CHECK (physicalsizey IS NULL AND physicalsizeyunit IS NULL
        OR physicalsizey IS NOT NULL AND physicalsizeyunit IS NOT NULL);

ALTER TABLE pixels ADD CONSTRAINT physicalsizez_unitpair
    CHECK (physicalsizez IS NULL AND physicalsizezunit IS NULL
        OR physicalsizez IS NOT NULL AND physicalsizezunit IS NOT NULL);

ALTER TABLE pixels ADD CONSTRAINT timeincrement_unitpair
    CHECK (timeincrement IS NULL AND timeincrementunit IS NULL
        OR timeincrement IS NOT NULL AND timeincrementunit IS NOT NULL);

ALTER TABLE planeinfo ADD CONSTRAINT deltat_unitpair
    CHECK (deltat IS NULL AND deltatunit IS NULL
        OR deltat IS NOT NULL AND deltatunit IS NOT NULL);

ALTER TABLE planeinfo ADD CONSTRAINT exposuretime_unitpair
    CHECK (exposuretime IS NULL AND exposuretimeunit IS NULL
        OR exposuretime IS NOT NULL AND exposuretimeunit IS NOT NULL);

ALTER TABLE planeinfo ADD CONSTRAINT positionx_unitpair
    CHECK (positionx IS NULL AND positionxunit IS NULL
        OR positionx IS NOT NULL AND positionxunit IS NOT NULL);

ALTER TABLE planeinfo ADD CONSTRAINT positiony_unitpair
    CHECK (positiony IS NULL AND positionyunit IS NULL
        OR positiony IS NOT NULL AND positionyunit IS NOT NULL);

ALTER TABLE planeinfo ADD CONSTRAINT positionz_unitpair
    CHECK (positionz IS NULL AND positionzunit IS NULL
        OR positionz IS NOT NULL AND positionzunit IS NOT NULL);

ALTER TABLE plate ADD CONSTRAINT welloriginx_unitpair
    CHECK (welloriginx IS NULL AND welloriginxunit IS NULL
        OR welloriginx IS NOT NULL AND welloriginxunit IS NOT NULL);

ALTER TABLE plate ADD CONSTRAINT welloriginy_unitpair
    CHECK (welloriginy IS NULL AND welloriginyunit IS NULL
        OR welloriginy IS NOT NULL AND welloriginyunit IS NOT NULL);

ALTER TABLE shape ADD CONSTRAINT fontsize_unitpair
    CHECK (fontsize IS NULL AND fontsizeunit IS NULL
        OR fontsize IS NOT NULL AND fontsizeunit IS NOT NULL);

ALTER TABLE shape ADD CONSTRAINT strokewidth_unitpair
    CHECK (strokewidth IS NULL AND strokewidthunit IS NULL
        OR strokewidth IS NOT NULL AND strokewidthunit IS NOT NULL);

ALTER TABLE stagelabel ADD CONSTRAINT positionx_unitpair
    CHECK (positionx IS NULL AND positionxunit IS NULL
        OR positionx IS NOT NULL AND positionxunit IS NOT NULL);

ALTER TABLE stagelabel ADD CONSTRAINT positiony_unitpair
    CHECK (positiony IS NULL AND positionyunit IS NULL
        OR positiony IS NOT NULL AND positionyunit IS NOT NULL);

ALTER TABLE stagelabel ADD CONSTRAINT positionz_unitpair
    CHECK (positionz IS NULL AND positionzunit IS NULL
        OR positionz IS NOT NULL AND positionzunit IS NOT NULL);

ALTER TABLE transmittancerange ADD CONSTRAINT cutintolerance_unitpair
    CHECK (cutintolerance IS NULL AND cutintoleranceunit IS NULL
        OR cutintolerance IS NOT NULL AND cutintoleranceunit IS NOT NULL);

ALTER TABLE transmittancerange ADD CONSTRAINT cutin_unitpair
    CHECK (cutin IS NULL AND cutinunit IS NULL
        OR cutin IS NOT NULL AND cutinunit IS NOT NULL);

ALTER TABLE transmittancerange ADD CONSTRAINT cutouttolerance_unitpair
    CHECK (cutouttolerance IS NULL AND cutouttoleranceunit IS NULL
        OR cutouttolerance IS NOT NULL AND cutouttoleranceunit IS NOT NULL);

ALTER TABLE transmittancerange ADD CONSTRAINT cutout_unitpair
    CHECK (cutout IS NULL AND cutoutunit IS NULL
        OR cutout IS NOT NULL AND cutoutunit IS NOT NULL);

ALTER TABLE wellsample ADD CONSTRAINT posx_unitpair
    CHECK (posx IS NULL AND posxunit IS NULL
        OR posx IS NOT NULL AND posxunit IS NOT NULL);

ALTER TABLE wellsample ADD CONSTRAINT posy_unitpair
    CHECK (posy IS NULL AND posyunit IS NULL
        OR posy IS NOT NULL AND posyunit IS NOT NULL);

-- 5.1DEV__20: Move pixels.params to image.series.

-- _fs_protected_mimetype replaces _fs_directory_mimetype

CREATE FUNCTION _fs_protected_mimetype() RETURNS "trigger" AS $$
    BEGIN
        IF OLD.mimetype IN ('Directory', 'Repository') AND (NEW.mimetype IS NULL OR NEW.mimetype != OLD.mimetype) THEN
            RAISE EXCEPTION 'cannot change media type % of file id=%', OLD.mimetype, OLD.id;
        END IF;
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER _fs_protected_mimetype
    BEFORE UPDATE ON originalfile
    FOR EACH ROW EXECUTE PROCEDURE _fs_protected_mimetype();

DROP TRIGGER _fs_directory_mimetype ON originalfile;
DROP FUNCTION _fs_directory_mimetype();

-- _fs_log_delete() no longer copies params column

create or replace function _fs_log_delete() returns trigger AS $_fs_log_delete$
    begin
        if OLD.repo is not null then
            INSERT INTO _fs_deletelog (event_id, file_id, owner_id, group_id, "path", "name", repo)
                SELECT _current_or_new_event(), OLD.id, OLD.owner_id, OLD.group_id, OLD."path", OLD."name", OLD.repo;
        end if;
        return OLD;
    END;
$_fs_log_delete$ LANGUAGE plpgsql;

-- add new image.series column and populate from pixels.params

ALTER TABLE image ADD COLUMN series nonnegative_int NOT NULL DEFAULT 0;

CREATE FUNCTION check_params_columns() RETURNS void AS $$

DECLARE
  pixels_id BIGINT;
  image_id  BIGINT;
  path      TEXT;
  name      TEXT;
  params    TEXT[];
  image_no  INTEGER;
  target    TEXT;
  index     INTEGER;

BEGIN
  IF EXISTS (SELECT 1 FROM originalfile AS o WHERE o.params IS NOT NULL) THEN
    RAISE EXCEPTION 'data in originalfile.params which is to be dropped';
  END IF;

  FOR image_id, pixels_id, path, name, params IN
    SELECT p.image, p.id, p.path, p.name, p.params FROM pixels AS p
      WHERE p.params IS NOT NULL LOOP

    image_no := NULL;
    target   := NULL;

    FOR index IN array_lower(params, 1) .. array_upper(params, 1) LOOP
      CASE params[index][1]
        WHEN 'image_no' THEN
            SELECT CAST(params[index][2] AS INTEGER) INTO STRICT image_no;
        WHEN 'target' THEN
            SELECT params[index][2] INTO STRICT target;
        ELSE
            RAISE EXCEPTION 'for pixels id=%, unexpected params key: %', pixels_id, params[index][1];
      END CASE;
    END LOOP;

    -- The pixels.path column may have Windows-style separators.
    IF position('/' IN path) = 0 THEN
        path := translate(path, '\', '/');
    END IF;

    IF target IS NOT NULL AND target <> (path || '/' || name) THEN
      RAISE EXCEPTION 'for pixels id=%, params target does not match path and name', pixels_id;
    END IF;

    IF image_no > 0 THEN
      UPDATE image SET series = image_no WHERE id = image_id;
    END IF;
  END LOOP;

END;

$$ LANGUAGE plpgsql;

SELECT check_params_columns();
DROP FUNCTION check_params_columns();

-- drop params columns

ALTER TABLE originalfile DROP COLUMN params;
ALTER TABLE pixels DROP COLUMN params;

-- allow ORM to attempt to leave series as null

CREATE FUNCTION image_series_default_zero() RETURNS "trigger" AS $$
    BEGIN
        IF NEW.series IS NULL THEN
            NEW.series := 0;
        END IF;

        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER image_series_default_zero
    BEFORE INSERT OR UPDATE ON image
    FOR EACH ROW EXECUTE PROCEDURE image_series_default_zero();

-- Temporary workaround for the width of map types

alter table annotation_mapvalue alter column name type text;
alter table annotation_mapvalue alter column value type text;

-- Add lookup table to channel and channel binding to
-- contain a server-specific lookup for a LUT.
alter table channel add column lookupTable varchar(255);
alter table channelbinding add column lookupTable varchar(255);


-- 5.1__1: last-minute changes for 5.1

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
          previousVersion = 'OMERO5.0'    AND
          previousPatch   = 0;

SELECT CHR(10)||CHR(10)||CHR(10)||'YOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION OMERO5.1__1'||CHR(10)||CHR(10)||CHR(10) AS Status;

COMMIT;
