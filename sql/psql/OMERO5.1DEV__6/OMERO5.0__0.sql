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
--- OMERO5 development release upgrade from OMERO5.0__0 to OMERO5.1DEV__6.
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
             VALUES ('OMERO5.1DEV',     6,              'OMERO5.0',       0);

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
        time        TIMESTAMP WITHOUT TIME ZONE;
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

            SELECT clock_timestamp() INTO time;
            SELECT ome_nextval('seq_event') INTO event_id;
            SELECT ome_nextval('seq_joboriginalfilelink') INTO new_link_id;

            INSERT INTO event (id, permissions, time, experimenter, experimentergroup, session, type)
                SELECT event_id, a.permissions, time, a.owner_id, a.group_id, 0, event_type
                  FROM annotation a WHERE a.id = import.annotation_id;

            INSERT INTO eventlog (id, action, permissions, entityid, entitytype, event)
                SELECT ome_nextval('seq_eventlog'), 'INSERT', e.permissions, new_link_id, 'ome.model.jobs.JobOriginalFileLink', event_id
                  FROM event e WHERE e.id = event_id;

            INSERT INTO joboriginalfilelink (id, permissions, creation_id, update_id, owner_id, group_id, parent, child)
                SELECT new_link_id, old.permissions, old.creation_id, old.update_id, old.owner_id, old.group_id, import.job_id, import.log_id
                  FROM filesetannotationlink old WHERE old.id = import.old_link_id;

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
    CONSTRAINT FKF96E60858062A40 
        FOREIGN KEY (annotation_id) 
        REFERENCES annotation
);

CREATE TABLE experimentergroup_config (
    experimentergroup_id INT8 NOT NULL,
    config VARCHAR(255) NOT NULL,
    config_key VARCHAR(255),
    PRIMARY KEY (experimentergroup_id, config_key),
    CONSTRAINT FKDC631B6CF5F0705D 
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
    CONSTRAINT FK7B28ABA9C1805FCD 
        FOREIGN KEY (genericexcitationsource_id) 
        REFERENCES genericexcitationsource
);

CREATE TABLE imagingenvironment_map (
    imagingenvironment_id INT8 NOT NULL,
    "map" VARCHAR(255) NOT NULL,
    map_key VARCHAR(255),
    PRIMARY KEY (imagingenvironment_id, map_key),
    CONSTRAINT FK7C8DCED8CDF68A87 
        FOREIGN KEY (imagingenvironment_id) 
        REFERENCES imagingenvironment
);

-- #12193: replace FilesetVersionInfo with map property on Fileset

CREATE TABLE metadataimportjob_versioninfo (
    metadataimportjob_id INT8 NOT NULL,
    versioninfo VARCHAR(255) NOT NULL,
    versioninfo_key VARCHAR(255),
    PRIMARY KEY (metadataimportjob_id, versioninfo_key),
    CONSTRAINT FK947FE61023506BCE 
        FOREIGN KEY (metadataimportjob_id) 
        REFERENCES metadataimportjob
);

CREATE TABLE uploadjob_versioninfo (
    uploadjob_id INT8 NOT NULL,
    versioninfo VARCHAR(255) NOT NULL,
    versioninfo_key VARCHAR(255),
    PRIMARY KEY (uploadjob_id, versioninfo_key),
    CONSTRAINT FK3B5720031800070E 
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

    BEGIN
        SELECT id INTO user_group FROM experimentergroup WHERE name = 'user';

        FOREACH curr_group IN ARRAY group_ids LOOP
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

DELETE FROM configuration
      WHERE name = 'DB check DBBadAnnotationCheck';

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

--
-- FINISHED
--

UPDATE dbpatch SET message = 'Database updated.', finished = clock_timestamp()
    WHERE currentVersion  = 'OMERO5.1DEV' AND
          currentPatch    = 6             AND
          previousVersion = 'OMERO5.0'    AND
          previousPatch   = 0;

SELECT CHR(10)||CHR(10)||CHR(10)||'YOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION OMERO5.1DEV__6'||CHR(10)||CHR(10)||CHR(10) AS Status;

COMMIT;
