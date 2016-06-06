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
--- OMERO5 development release upgrade from OMERO5.2__0 to OMERO5.3DEV__5.
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

SELECT omero_assert_db_version('OMERO5.2', 0);
DROP FUNCTION omero_assert_db_version(varchar, int);


--
-- check PostgreSQL server version and database encoding
--

CREATE OR REPLACE FUNCTION db_pretty_version(version INTEGER) RETURNS TEXT AS $$

BEGIN
    RETURN (version/10000)::TEXT || '.' || ((version/100)%100)::TEXT || '.' || (version%100)::TEXT;

END;$$ LANGUAGE plpgsql;


CREATE FUNCTION assert_db_server_prerequisites(version_prereq INTEGER) RETURNS void AS $$

DECLARE
    version_num INTEGER;
    char_encoding TEXT;

BEGIN
    SELECT CAST(setting AS INTEGER) INTO STRICT version_num
        FROM pg_settings WHERE name = 'server_version_num';
    SELECT pg_encoding_to_char(encoding) INTO STRICT char_encoding
        FROM pg_database WHERE datname = current_database();

    IF version_num < version_prereq THEN
        RAISE EXCEPTION 'PostgreSQL database server version % is less than OMERO prerequisite %',
            db_pretty_version(version_num), db_pretty_version(version_prereq);
    END IF;

    IF char_encoding != 'UTF8' THEN
        RAISE EXCEPTION 'OMERO database character encoding must be UTF8, not %', char_encoding;
    ELSE
        SET client_encoding = 'UTF8';
    END IF;

END;$$ LANGUAGE plpgsql;

SELECT assert_db_server_prerequisites(90300);  -- TBD

DROP FUNCTION assert_db_server_prerequisites(INTEGER);
DROP FUNCTION db_pretty_version(INTEGER);


--
-- Actual upgrade
--

INSERT INTO dbpatch (currentVersion, currentPatch, previousVersion, previousPatch)
             VALUES ('OMERO5.3DEV',  5,            'OMERO5.2',      0);

-- ... up to patch 0:

CREATE FUNCTION assert_no_roi_keywords_namespaces() RETURNS void AS $$

DECLARE
  roi_row roi%ROWTYPE;
  element TEXT;

BEGIN
    FOR roi_row IN SELECT * FROM roi LOOP
        IF roi_row.keywords IS NOT NULL THEN
            FOREACH element IN ARRAY roi_row.keywords LOOP
                IF element <> '' THEN
                    RAISE EXCEPTION 'data in roi.keywords row id=%', roi_row.id;
                END IF;
            END LOOP;
        END IF;
        IF roi_row.namespaces IS NOT NULL THEN
            FOREACH element IN ARRAY roi_row.namespaces LOOP
                IF element <> '' THEN
                    RAISE EXCEPTION 'data in roi.namespaces row id=%', roi_row.id;
                END IF;
            END LOOP;
        END IF;
    END LOOP;

END;$$ LANGUAGE plpgsql;

SELECT assert_no_roi_keywords_namespaces();
DROP FUNCTION assert_no_roi_keywords_namespaces();

ALTER TABLE roi DROP COLUMN keywords;
ALTER TABLE roi DROP COLUMN namespaces;

ALTER TABLE shape DROP COLUMN anchor;
ALTER TABLE shape DROP COLUMN baselineshift;
ALTER TABLE shape DROP COLUMN decoration;
ALTER TABLE shape DROP COLUMN direction;
ALTER TABLE shape DROP COLUMN g;
ALTER TABLE shape DROP COLUMN glyphorientationvertical;
ALTER TABLE shape DROP COLUMN strokedashoffset;
ALTER TABLE shape DROP COLUMN strokelinejoin;
ALTER TABLE shape DROP COLUMN strokemiterlimit;
ALTER TABLE shape DROP COLUMN vectoreffect;
ALTER TABLE shape DROP COLUMN visibility;
ALTER TABLE shape DROP COLUMN writingmode;

-- ... up to patch 1:

CREATE TABLE folder (
    id BIGINT PRIMARY KEY,
    description TEXT,
    permissions BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    version INTEGER,
    creation_id BIGINT NOT NULL,
    external_id BIGINT UNIQUE,
    group_id BIGINT NOT NULL,
    owner_id BIGINT NOT NULL,
    update_id BIGINT NOT NULL,
    parentFolder BIGINT
);

CREATE TABLE folderannotationlink (
    id BIGINT PRIMARY KEY,
    permissions BIGINT NOT NULL,
    version INTEGER,
    child BIGINT NOT NULL,
    creation_id BIGINT NOT NULL,
    external_id BIGINT UNIQUE,
    group_id BIGINT NOT NULL,
    owner_id BIGINT NOT NULL,
    update_id BIGINT NOT NULL,
    parent BIGINT NOT NULL,
    UNIQUE (parent, child, owner_id)
);

CREATE TABLE folderimagelink (
    id BIGINT PRIMARY KEY,
    permissions BIGINT NOT NULL,
    version INTEGER,
    child BIGINT NOT NULL,
    creation_id BIGINT NOT NULL,
    external_id BIGINT UNIQUE,
    group_id BIGINT NOT NULL,
    owner_id BIGINT NOT NULL,
    update_id BIGINT NOT NULL,
    parent BIGINT NOT NULL,
    UNIQUE (parent, child, owner_id)
);

CREATE TABLE folderroilink (
    id BIGINT PRIMARY KEY,
    permissions BIGINT NOT NULL,
    version INTEGER,
    child BIGINT NOT NULL,
    creation_id BIGINT NOT NULL,
    external_id BIGINT UNIQUE,
    group_id BIGINT NOT NULL,
    owner_id BIGINT NOT NULL,
    update_id BIGINT NOT NULL,
    parent BIGINT NOT NULL,
    UNIQUE (parent, child, owner_id)
);

ALTER TABLE folder
    ADD CONSTRAINT FKfolder_creation_id_event
    FOREIGN KEY (creation_id)
    REFERENCES event;

ALTER TABLE folder
    ADD CONSTRAINT FKfolder_update_id_event
    FOREIGN KEY (update_id)
    REFERENCES event;

ALTER TABLE folder
    ADD CONSTRAINT FKfolder_external_id_externalinfo
    FOREIGN KEY (external_id)
    REFERENCES externalinfo;

ALTER TABLE folder
    ADD CONSTRAINT FKfolder_parentFolder_folder
    FOREIGN KEY (parentFolder)
    REFERENCES folder;

ALTER TABLE folder
    ADD CONSTRAINT FKfolder_group_id_experimentergroup
    FOREIGN KEY (group_id)
    REFERENCES experimentergroup;

ALTER TABLE folder
    ADD CONSTRAINT FKfolder_owner_id_experimenter
    FOREIGN KEY (owner_id)
    REFERENCES experimenter;

ALTER TABLE folderannotationlink
    ADD CONSTRAINT FKfolderannotationlink_creation_id_event
    FOREIGN KEY (creation_id)
    REFERENCES event;

ALTER TABLE folderannotationlink
    ADD CONSTRAINT FKfolderannotationlink_child_annotation
    FOREIGN KEY (child)
    REFERENCES annotation;

ALTER TABLE folderannotationlink
    ADD CONSTRAINT FKfolderannotationlink_update_id_event
    FOREIGN KEY (update_id)
    REFERENCES event;

ALTER TABLE folderannotationlink
    ADD CONSTRAINT FKfolderannotationlink_external_id_externalinfo
    FOREIGN KEY (external_id)
    REFERENCES externalinfo;

ALTER TABLE folderannotationlink
    ADD CONSTRAINT FKfolderannotationlink_group_id_experimentergroup
    FOREIGN KEY (group_id)
    REFERENCES experimentergroup;

ALTER TABLE folderannotationlink
    ADD CONSTRAINT FKfolderannotationlink_owner_id_experimenter
    FOREIGN KEY (owner_id)
    REFERENCES experimenter;

ALTER TABLE folderannotationlink
    ADD CONSTRAINT FKfolderannotationlink_parent_folder
    FOREIGN KEY (parent)
    REFERENCES folder;

ALTER TABLE folderimagelink
    ADD CONSTRAINT FKfolderimagelink_creation_id_event
    FOREIGN KEY (creation_id)
    REFERENCES event;

ALTER TABLE folderimagelink
    ADD CONSTRAINT FKfolderimagelink_child_image
    FOREIGN KEY (child)
    REFERENCES image;

ALTER TABLE folderimagelink
    ADD CONSTRAINT FKfolderimagelink_update_id_event
    FOREIGN KEY (update_id)
    REFERENCES event;

ALTER TABLE folderimagelink
    ADD CONSTRAINT FKfolderimagelink_external_id_externalinfo
    FOREIGN KEY (external_id)
    REFERENCES externalinfo;

ALTER TABLE folderimagelink
    ADD CONSTRAINT FKfolderimagelink_group_id_experimentergroup
    FOREIGN KEY (group_id)
    REFERENCES experimentergroup;

ALTER TABLE folderimagelink
    ADD CONSTRAINT FKfolderimagelink_owner_id_experimenter
    FOREIGN KEY (owner_id)
    REFERENCES experimenter;

ALTER TABLE folderimagelink
    ADD CONSTRAINT FKfolderimagelink_parent_folder
    FOREIGN KEY (parent)
    REFERENCES folder;

ALTER TABLE folderroilink
    ADD CONSTRAINT FKfolderroilink_creation_id_event
    FOREIGN KEY (creation_id)
    REFERENCES event;

ALTER TABLE folderroilink
    ADD CONSTRAINT FKfolderroilink_child_roi
    FOREIGN KEY (child)
    REFERENCES roi;

ALTER TABLE folderroilink
    ADD CONSTRAINT FKfolderroilink_update_id_event
    FOREIGN KEY (update_id)
    REFERENCES event;

ALTER TABLE folderroilink
    ADD CONSTRAINT FKfolderroilink_external_id_externalinfo
    FOREIGN KEY (external_id)
    REFERENCES externalinfo;

ALTER TABLE folderroilink
    ADD CONSTRAINT FKfolderroilink_group_id_experimentergroup
    FOREIGN KEY (group_id)
    REFERENCES experimentergroup;

ALTER TABLE folderroilink
    ADD CONSTRAINT FKfolderroilink_owner_id_experimenter
    FOREIGN KEY (owner_id)
    REFERENCES experimenter;

ALTER TABLE folderroilink
    ADD CONSTRAINT FKfolderroilink_parent_folder
    FOREIGN KEY (parent)
    REFERENCES folder;

CREATE INDEX i_folder_owner ON folder(owner_id);
CREATE INDEX i_folder_group ON folder(group_id);
CREATE INDEX i_folder_parentfolder ON folder(parentfolder);
CREATE INDEX i_folderannotationlink_owner ON folderannotationlink(owner_id);
CREATE INDEX i_folderannotationlink_group ON folderannotationlink(group_id);
CREATE INDEX i_folderAnnotationLink_parent ON folderannotationlink(parent);
CREATE INDEX i_folderAnnotationLink_child ON folderannotationlink(child);
CREATE INDEX i_folderimagelink_owner ON folderimagelink(owner_id);
CREATE INDEX i_folderimagelink_group ON folderimagelink(group_id);
CREATE INDEX i_folderImageLink_parent ON folderimagelink(parent);
CREATE INDEX i_folderImageLink_child ON folderimagelink(child);
CREATE INDEX i_folderroilink_owner ON folderroilink(owner_id);
CREATE INDEX i_folderroilink_group ON folderroilink(group_id);
CREATE INDEX i_folderRoiLink_parent ON folderroilink(parent);
CREATE INDEX i_folderRoiLink_child ON folderroilink(child);

CREATE SEQUENCE seq_folder; INSERT INTO _lock_ids (name, id) SELECT 'seq_folder', nextval('_lock_seq');
CREATE SEQUENCE seq_folderannotationlink; INSERT INTO _lock_ids (name, id) SELECT 'seq_folderannotationlink', nextval('_lock_seq');
CREATE SEQUENCE seq_folderimagelink; INSERT INTO _lock_ids (name, id) SELECT 'seq_folderimagelink', nextval('_lock_seq');
CREATE SEQUENCE seq_folderroilink; INSERT INTO _lock_ids (name, id) SELECT 'seq_folderroilink', nextval('_lock_seq');

CREATE TRIGGER folder_annotation_link_event_trigger
    AFTER UPDATE ON folderannotationlink
    FOR EACH ROW
    EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.containers.Folder');

CREATE TRIGGER folder_annotation_link_event_trigger_insert
    AFTER INSERT ON folderannotationlink
    FOR EACH ROW
    EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.containers.Folder');

CREATE TRIGGER folder_annotation_link_delete_trigger
    BEFORE DELETE ON folderannotationlink
    FOR EACH ROW
    EXECUTE PROCEDURE annotation_link_delete_trigger('ome.model.containers.Folder');

CREATE VIEW count_folder_imageLinks_by_owner (folder_id, owner_id, count) AS SELECT parent, owner_id, COUNT(*)
    FROM folderimagelink GROUP BY parent, owner_id ORDER BY parent;

CREATE VIEW count_folder_roiLinks_by_owner (folder_id, owner_id, count) AS SELECT parent, owner_id, COUNT(*)
    FROM folderroilink GROUP BY parent, owner_id ORDER BY parent;

CREATE VIEW count_folder_annotationLinks_by_owner (folder_id, owner_id, count) AS SELECT parent, owner_id, COUNT(*)
    FROM folderannotationlink GROUP BY parent, owner_id ORDER BY parent;

CREATE VIEW count_Image_folderLinks_by_owner (Image_id, owner_id, count) AS SELECT child, owner_id, COUNT(*)
    FROM folderimagelink GROUP BY child, owner_id ORDER BY child;

CREATE VIEW count_Roi_folderLinks_by_owner (Roi_id, owner_id, count) AS SELECT child, owner_id, COUNT(*)
    FROM folderroilink GROUP BY child, owner_id ORDER BY child;

CREATE FUNCTION preserve_folder_tree() RETURNS "trigger" AS $$

    DECLARE
        parent_id BIGINT;

    BEGIN
        parent_id := NEW.parentfolder;
        WHILE parent_id IS NOT NULL
        LOOP
            IF parent_id = NEW.id THEN
                RAISE EXCEPTION 'folder % would cause a cycle in the hierarchy', NEW.id;
            ELSE
                SELECT parentfolder INTO STRICT parent_id FROM folder WHERE id = parent_id;
            END IF;
        END LOOP;

        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER preserve_folder_tree
    AFTER INSERT OR UPDATE ON folder
    FOR EACH ROW EXECUTE PROCEDURE preserve_folder_tree();


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

        FOR pid IN SELECT DISTINCT parent FROM folderannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _updated_annotations (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.containers.Folder', pid
                WHERE NOT EXISTS (SELECT 1 FROM _updated_annotations AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.containers.Folder' AND ua.entity_id = pid);
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

-- ... up to patch 2:

UPDATE shape SET x = cx, y = cy WHERE cx IS NOT NULL OR cy IS NOT NULL;

ALTER TABLE shape DROP COLUMN cx;
ALTER TABLE shape DROP COLUMN cy;
ALTER TABLE shape RENAME COLUMN rx TO radiusx;
ALTER TABLE shape RENAME COLUMN ry TO radiusy;

-- ... up to patch 3:

UPDATE shape SET fontstyle = 'Bold'
    WHERE (fontstyle IS NULL OR fontstyle = '') AND fontweight ILIKE 'bold';

UPDATE shape SET fontstyle = 'BoldItalic'
    WHERE (fontstyle ILIKE 'italic' OR fontstyle ILIKE 'oblique') AND fontweight ILIKE 'bold';

ALTER TABLE shape DROP COLUMN fontstretch;
ALTER TABLE shape DROP COLUMN fontvariant;
ALTER TABLE shape DROP COLUMN fontweight;
ALTER TABLE shape DROP COLUMN strokelinecap;

ALTER TABLE shape ADD COLUMN markerstart VARCHAR(255);
ALTER TABLE shape ADD COLUMN markerend   VARCHAR(255);

-- ... up to patch 4:

UPDATE pixels SET sha1 = 'Pending...' WHERE sha1 = 'Foo';

-- ... up to patch 5:

ALTER TABLE _updated_annotations RENAME TO _reindexing_required;
ALTER TABLE _reindexing_required RENAME CONSTRAINT FK_updated_annotations_event_id TO FK_reindexing_required_event_id;
ALTER INDEX _updated_annotations_event_index RENAME TO _reindexing_required_event_index;
ALTER INDEX _updated_annotations_row_index   RENAME TO _reindexing_required_row_index;

DROP TRIGGER annotation_annotation_link_event_trigger        ON annotationannotationlink;
DROP TRIGGER annotation_annotation_link_event_trigger_insert ON annotationannotationlink;
DROP TRIGGER channel_annotation_link_event_trigger        ON channelannotationlink;
DROP TRIGGER channel_annotation_link_event_trigger_insert ON channelannotationlink;
DROP TRIGGER dataset_annotation_link_event_trigger        ON datasetannotationlink;
DROP TRIGGER dataset_annotation_link_event_trigger_insert ON datasetannotationlink;
DROP TRIGGER detector_annotation_link_event_trigger        ON detectorannotationlink;
DROP TRIGGER detector_annotation_link_event_trigger_insert ON detectorannotationlink;
DROP TRIGGER dichroic_annotation_link_event_trigger        ON dichroicannotationlink;
DROP TRIGGER dichroic_annotation_link_event_trigger_insert ON dichroicannotationlink;
DROP TRIGGER experimenter_annotation_link_event_trigger        ON experimenterannotationlink;
DROP TRIGGER experimenter_annotation_link_event_trigger_insert ON experimenterannotationlink;
DROP TRIGGER experimentergroup_annotation_link_event_trigger        ON experimentergroupannotationlink;
DROP TRIGGER experimentergroup_annotation_link_event_trigger_insert ON experimentergroupannotationlink;
DROP TRIGGER fileset_annotation_link_event_trigger        ON filesetannotationlink;
DROP TRIGGER fileset_annotation_link_event_trigger_insert ON filesetannotationlink;
DROP TRIGGER filter_annotation_link_event_trigger        ON filterannotationlink;
DROP TRIGGER filter_annotation_link_event_trigger_insert ON filterannotationlink;
DROP TRIGGER folder_annotation_link_event_trigger        ON folderannotationlink;
DROP TRIGGER folder_annotation_link_event_trigger_insert ON folderannotationlink;
DROP TRIGGER image_annotation_link_event_trigger        ON imageannotationlink;
DROP TRIGGER image_annotation_link_event_trigger_insert ON imageannotationlink;
DROP TRIGGER instrument_annotation_link_event_trigger        ON instrumentannotationlink;
DROP TRIGGER instrument_annotation_link_event_trigger_insert ON instrumentannotationlink;
DROP TRIGGER lightpath_annotation_link_event_trigger        ON lightpathannotationlink;
DROP TRIGGER lightpath_annotation_link_event_trigger_insert ON lightpathannotationlink;
DROP TRIGGER lightsource_annotation_link_event_trigger        ON lightsourceannotationlink;
DROP TRIGGER lightsource_annotation_link_event_trigger_insert ON lightsourceannotationlink;
DROP TRIGGER namespace_annotation_link_event_trigger        ON namespaceannotationlink;
DROP TRIGGER namespace_annotation_link_event_trigger_insert ON namespaceannotationlink;
DROP TRIGGER node_annotation_link_event_trigger        ON nodeannotationlink;
DROP TRIGGER node_annotation_link_event_trigger_insert ON nodeannotationlink;
DROP TRIGGER objective_annotation_link_event_trigger        ON objectiveannotationlink;
DROP TRIGGER objective_annotation_link_event_trigger_insert ON objectiveannotationlink;
DROP TRIGGER originalfile_annotation_link_event_trigger        ON originalfileannotationlink;
DROP TRIGGER originalfile_annotation_link_event_trigger_insert ON originalfileannotationlink;
DROP TRIGGER planeinfo_annotation_link_event_trigger        ON planeinfoannotationlink;
DROP TRIGGER planeinfo_annotation_link_event_trigger_insert ON planeinfoannotationlink;
DROP TRIGGER plate_annotation_link_event_trigger        ON plateannotationlink;
DROP TRIGGER plate_annotation_link_event_trigger_insert ON plateannotationlink;
DROP TRIGGER plateacquisition_annotation_link_event_trigger        ON plateacquisitionannotationlink;
DROP TRIGGER plateacquisition_annotation_link_event_trigger_insert ON plateacquisitionannotationlink;
DROP TRIGGER project_annotation_link_event_trigger        ON projectannotationlink;
DROP TRIGGER project_annotation_link_event_trigger_insert ON projectannotationlink;
DROP TRIGGER reagent_annotation_link_event_trigger        ON reagentannotationlink;
DROP TRIGGER reagent_annotation_link_event_trigger_insert ON reagentannotationlink;
DROP TRIGGER roi_annotation_link_event_trigger        ON roiannotationlink;
DROP TRIGGER roi_annotation_link_event_trigger_insert ON roiannotationlink;
DROP TRIGGER screen_annotation_link_event_trigger        ON screenannotationlink;
DROP TRIGGER screen_annotation_link_event_trigger_insert ON screenannotationlink;
DROP TRIGGER session_annotation_link_event_trigger        ON sessionannotationlink;
DROP TRIGGER session_annotation_link_event_trigger_insert ON sessionannotationlink;
DROP TRIGGER shape_annotation_link_event_trigger        ON shapeannotationlink;
DROP TRIGGER shape_annotation_link_event_trigger_insert ON shapeannotationlink;
DROP TRIGGER well_annotation_link_event_trigger        ON wellannotationlink;
DROP TRIGGER well_annotation_link_event_trigger_insert ON wellannotationlink;

DROP FUNCTION annotation_link_event_trigger();

CREATE OR REPLACE FUNCTION annotation_update_event_trigger() RETURNS TRIGGER AS $$

    DECLARE
        pid BIGINT;
        eid BIGINT;

    BEGIN
        SELECT INTO eid _current_or_new_event();

        FOR pid IN SELECT DISTINCT parent FROM annotationannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.annotations.Annotation', pid
                WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.annotations.Annotation' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM channelannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.core.Channel', pid
                WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.core.Channel' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM datasetannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.containers.Dataset', pid
                WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.containers.Dataset' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM detectorannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.acquisition.Detector', pid
                WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.acquisition.Detector' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM dichroicannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.acquisition.Dichroic', pid
                WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.acquisition.Dichroic' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM experimenterannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.meta.Experimenter', pid
                WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.meta.Experimenter' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM experimentergroupannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.meta.ExperimenterGroup', pid
                WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.meta.ExperimenterGroup' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM filesetannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.fs.Fileset', pid
                WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.fs.Fileset' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM filterannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.acquisition.Filter', pid
                WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.acquisition.Filter' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM folderannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.containers.Folder', pid
                WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.containers.Folder' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM imageannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.core.Image', pid
                WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.core.Image' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM instrumentannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.acquisition.Instrument', pid
                WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.acquisition.Instrument' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM lightpathannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.acquisition.LightPath', pid
                WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.acquisition.LightPath' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM lightsourceannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.acquisition.LightSource', pid
                WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.acquisition.LightSource' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM namespaceannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.meta.Namespace', pid
                WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.meta.Namespace' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM nodeannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.meta.Node', pid
                WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.meta.Node' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM objectiveannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.acquisition.Objective', pid
                WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.acquisition.Objective' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM originalfileannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.core.OriginalFile', pid
                WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.core.OriginalFile' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM planeinfoannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.core.PlaneInfo', pid
                WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.core.PlaneInfo' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM plateannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.screen.Plate', pid
                WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.screen.Plate' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM plateacquisitionannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.screen.PlateAcquisition', pid
                WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.screen.PlateAcquisition' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM projectannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.containers.Project', pid
                WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.containers.Project' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM reagentannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.screen.Reagent', pid
                WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.screen.Reagent' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM roiannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.roi.Roi', pid
                WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.roi.Roi' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM screenannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.screen.Screen', pid
                WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.screen.Screen' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM sessionannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.meta.Session', pid
                WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.meta.Session' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM shapeannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.roi.Shape', pid
                WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.roi.Shape' AND ua.entity_id = pid);
        END LOOP;

        FOR pid IN SELECT DISTINCT parent FROM wellannotationlink WHERE child = new.id
        LOOP
            INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.screen.Well', pid
                WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS ua
                    WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.screen.Well' AND ua.entity_id = pid);
        END LOOP;

        RETURN new;
    END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION annotation_link_update_trigger() RETURNS "trigger"
    AS '
    DECLARE
        eid int8;

    BEGIN
        SELECT INTO eid _current_or_new_event();

        INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
            SELECT eid, TG_ARGV[0], old.parent
            WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS ua
                WHERE ua.event_id = eid AND ua.entity_type = TG_ARGV[0] AND ua.entity_id = old.parent);

        INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
            SELECT eid, TG_ARGV[0], new.parent
            WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS ua
                WHERE ua.event_id = eid AND ua.entity_type = TG_ARGV[0] AND ua.entity_id = new.parent);

        RETURN new;

    END;'
LANGUAGE plpgsql;

CREATE FUNCTION annotation_link_insert_trigger() RETURNS "trigger"
    AS '
    DECLARE
        eid int8;

    BEGIN
        SELECT INTO eid _current_or_new_event();

        INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
            SELECT eid, TG_ARGV[0], new.parent
            WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS ua
                WHERE ua.event_id = eid AND ua.entity_type = TG_ARGV[0] AND ua.entity_id = new.parent);

        RETURN new;

    END;'
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION annotation_link_delete_trigger() RETURNS "trigger"
    AS '
    DECLARE
        eid int8;

    BEGIN
        SELECT INTO eid _current_or_new_event();

        INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
            SELECT eid, TG_ARGV[0], old.parent
            WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS ua
                WHERE ua.event_id = eid AND ua.entity_type = TG_ARGV[0] AND ua.entity_id = old.parent);

        RETURN old;

    END;'
LANGUAGE plpgsql;

CREATE TRIGGER annotation_annotation_link_update_trigger
        AFTER UPDATE ON annotationannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_update_trigger('ome.model.annotations.Annotation');
CREATE TRIGGER annotation_annotation_link_insert_trigger
        AFTER INSERT ON annotationannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_insert_trigger('ome.model.annotations.Annotation');
CREATE TRIGGER channel_annotation_link_update_trigger
        AFTER UPDATE ON channelannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_update_trigger('ome.model.core.Channel');
CREATE TRIGGER channel_annotation_link_insert_trigger
        AFTER INSERT ON channelannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_insert_trigger('ome.model.core.Channel');
CREATE TRIGGER dataset_annotation_link_update_trigger
        AFTER UPDATE ON datasetannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_update_trigger('ome.model.containers.Dataset');
CREATE TRIGGER dataset_annotation_link_insert_trigger
        AFTER INSERT ON datasetannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_insert_trigger('ome.model.containers.Dataset');
CREATE TRIGGER detector_annotation_link_update_trigger
        AFTER UPDATE ON detectorannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_update_trigger('ome.model.acquisition.Detector');
CREATE TRIGGER detector_annotation_link_insert_trigger
        AFTER INSERT ON detectorannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_insert_trigger('ome.model.acquisition.Detector');
CREATE TRIGGER dichroic_annotation_link_update_trigger
        AFTER UPDATE ON dichroicannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_update_trigger('ome.model.acquisition.Dichroic');
CREATE TRIGGER dichroic_annotation_link_insert_trigger
        AFTER INSERT ON dichroicannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_insert_trigger('ome.model.acquisition.Dichroic');
CREATE TRIGGER experimenter_annotation_link_update_trigger
        AFTER UPDATE ON experimenterannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_update_trigger('ome.model.meta.Experimenter');
CREATE TRIGGER experimenter_annotation_link_insert_trigger
        AFTER INSERT ON experimenterannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_insert_trigger('ome.model.meta.Experimenter');
CREATE TRIGGER experimentergroup_annotation_link_update_trigger
        AFTER UPDATE ON experimentergroupannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_update_trigger('ome.model.meta.ExperimenterGroup');
CREATE TRIGGER experimentergroup_annotation_link_insert_trigger
        AFTER INSERT ON experimentergroupannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_insert_trigger('ome.model.meta.ExperimenterGroup');
CREATE TRIGGER fileset_annotation_link_update_trigger
        AFTER UPDATE ON filesetannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_update_trigger('ome.model.fs.Fileset');
CREATE TRIGGER fileset_annotation_link_insert_trigger
        AFTER INSERT ON filesetannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_insert_trigger('ome.model.fs.Fileset');
CREATE TRIGGER filter_annotation_link_update_trigger
        AFTER UPDATE ON filterannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_update_trigger('ome.model.acquisition.Filter');
CREATE TRIGGER filter_annotation_link_insert_trigger
        AFTER INSERT ON filterannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_insert_trigger('ome.model.acquisition.Filter');
CREATE TRIGGER folder_annotation_link_update_trigger
        AFTER UPDATE ON folderannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_update_trigger('ome.model.containers.Folder');
CREATE TRIGGER folder_annotation_link_insert_trigger
        AFTER INSERT ON folderannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_insert_trigger('ome.model.containers.Folder');
CREATE TRIGGER image_annotation_link_update_trigger
        AFTER UPDATE ON imageannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_update_trigger('ome.model.core.Image');
CREATE TRIGGER image_annotation_link_insert_trigger
        AFTER INSERT ON imageannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_insert_trigger('ome.model.core.Image');
CREATE TRIGGER instrument_annotation_link_update_trigger
        AFTER UPDATE ON instrumentannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_update_trigger('ome.model.acquisition.Instrument');
CREATE TRIGGER instrument_annotation_link_insert_trigger
        AFTER INSERT ON instrumentannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_insert_trigger('ome.model.acquisition.Instrument');
CREATE TRIGGER lightpath_annotation_link_update_trigger
        AFTER UPDATE ON lightpathannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_update_trigger('ome.model.acquisition.LightPath');
CREATE TRIGGER lightpath_annotation_link_insert_trigger
        AFTER INSERT ON lightpathannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_insert_trigger('ome.model.acquisition.LightPath');
CREATE TRIGGER lightsource_annotation_link_update_trigger
        AFTER UPDATE ON lightsourceannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_update_trigger('ome.model.acquisition.LightSource');
CREATE TRIGGER lightsource_annotation_link_insert_trigger
        AFTER INSERT ON lightsourceannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_insert_trigger('ome.model.acquisition.LightSource');
CREATE TRIGGER namespace_annotation_link_update_trigger
        AFTER UPDATE ON namespaceannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_update_trigger('ome.model.meta.Namespace');
CREATE TRIGGER namespace_annotation_link_insert_trigger
        AFTER INSERT ON namespaceannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_insert_trigger('ome.model.meta.Namespace');
CREATE TRIGGER node_annotation_link_update_trigger
        AFTER UPDATE ON nodeannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_update_trigger('ome.model.meta.Node');
CREATE TRIGGER node_annotation_link_insert_trigger
        AFTER INSERT ON nodeannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_insert_trigger('ome.model.meta.Node');
CREATE TRIGGER objective_annotation_link_update_trigger
        AFTER UPDATE ON objectiveannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_update_trigger('ome.model.acquisition.Objective');
CREATE TRIGGER objective_annotation_link_insert_trigger
        AFTER INSERT ON objectiveannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_insert_trigger('ome.model.acquisition.Objective');
CREATE TRIGGER originalfile_annotation_link_update_trigger
        AFTER UPDATE ON originalfileannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_update_trigger('ome.model.core.OriginalFile');
CREATE TRIGGER originalfile_annotation_link_insert_trigger
        AFTER INSERT ON originalfileannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_insert_trigger('ome.model.core.OriginalFile');
CREATE TRIGGER planeinfo_annotation_link_update_trigger
        AFTER UPDATE ON planeinfoannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_update_trigger('ome.model.core.PlaneInfo');
CREATE TRIGGER planeinfo_annotation_link_insert_trigger
        AFTER INSERT ON planeinfoannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_insert_trigger('ome.model.core.PlaneInfo');
CREATE TRIGGER plate_annotation_link_update_trigger
        AFTER UPDATE ON plateannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_update_trigger('ome.model.screen.Plate');
CREATE TRIGGER plate_annotation_link_insert_trigger
        AFTER INSERT ON plateannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_insert_trigger('ome.model.screen.Plate');
CREATE TRIGGER plateacquisition_annotation_link_update_trigger
        AFTER UPDATE ON plateacquisitionannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_update_trigger('ome.model.screen.PlateAcquisition');
CREATE TRIGGER plateacquisition_annotation_link_insert_trigger
        AFTER INSERT ON plateacquisitionannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_insert_trigger('ome.model.screen.PlateAcquisition');
CREATE TRIGGER project_annotation_link_update_trigger
        AFTER UPDATE ON projectannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_update_trigger('ome.model.containers.Project');
CREATE TRIGGER project_annotation_link_insert_trigger
        AFTER INSERT ON projectannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_insert_trigger('ome.model.containers.Project');
CREATE TRIGGER reagent_annotation_link_update_trigger
        AFTER UPDATE ON reagentannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_update_trigger('ome.model.screen.Reagent');
CREATE TRIGGER reagent_annotation_link_insert_trigger
        AFTER INSERT ON reagentannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_insert_trigger('ome.model.screen.Reagent');
CREATE TRIGGER roi_annotation_link_update_trigger
        AFTER UPDATE ON roiannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_update_trigger('ome.model.roi.Roi');
CREATE TRIGGER roi_annotation_link_insert_trigger
        AFTER INSERT ON roiannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_insert_trigger('ome.model.roi.Roi');
CREATE TRIGGER screen_annotation_link_update_trigger
        AFTER UPDATE ON screenannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_update_trigger('ome.model.screen.Screen');
CREATE TRIGGER screen_annotation_link_insert_trigger
        AFTER INSERT ON screenannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_insert_trigger('ome.model.screen.Screen');
CREATE TRIGGER session_annotation_link_update_trigger
        AFTER UPDATE ON sessionannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_update_trigger('ome.model.meta.Session');
CREATE TRIGGER session_annotation_link_insert_trigger
        AFTER INSERT ON sessionannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_insert_trigger('ome.model.meta.Session');
CREATE TRIGGER shape_annotation_link_update_trigger
        AFTER UPDATE ON shapeannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_update_trigger('ome.model.roi.Shape');
CREATE TRIGGER shape_annotation_link_insert_trigger
        AFTER INSERT ON shapeannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_insert_trigger('ome.model.roi.Shape');
CREATE TRIGGER well_annotation_link_update_trigger
        AFTER UPDATE ON wellannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_update_trigger('ome.model.screen.Well');
CREATE TRIGGER well_annotation_link_insert_trigger
        AFTER INSERT ON wellannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_insert_trigger('ome.model.screen.Well');

DROP FUNCTION annotation_updates_note_reindex();

CREATE FUNCTION updated_entities_note_reindex() RETURNS void AS $$

    DECLARE
        curs CURSOR FOR SELECT * FROM _reindexing_required ORDER BY event_id LIMIT 100000 FOR UPDATE;
        row _reindexing_required%rowtype;

    BEGIN
        FOR row IN curs
        LOOP
            DELETE FROM _reindexing_required WHERE CURRENT OF curs;

            INSERT INTO eventlog (id, action, permissions, entityid, entitytype, event)
                SELECT ome_nextval('seq_eventlog'), 'REINDEX', -52, row.entity_id, row.entity_type, row.event_id
                WHERE NOT EXISTS (SELECT 1 FROM eventlog AS el
                    WHERE el.entityid = row.entity_id AND el.entitytype = row.entity_type AND el.event = row.event_id);

        END LOOP;
    END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION folder_update_trigger() RETURNS TRIGGER AS $$

    DECLARE
        eid BIGINT;
        iid BIGINT;

    BEGIN
        FOR iid IN SELECT DISTINCT r.image FROM roi AS r, folderroilink AS l
            WHERE l.parent = NEW.id AND l.child = r.id AND r.image IS NOT NULL LOOP

            IF eid IS NULL THEN
                eid := _current_or_new_event();
            END IF;

            INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.core.Image', iid
                WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS rr
                    WHERE rr.event_id = eid AND rr.entity_type = 'ome.model.core.Image' AND rr.entity_id = iid);
        END LOOP;

        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION folder_roi_link_insert_trigger() RETURNS TRIGGER AS $$

    DECLARE
        eid BIGINT;
        iid BIGINT;

    BEGIN
        FOR iid IN SELECT DISTINCT r.image FROM roi AS r
            WHERE r.id = NEW.child AND r.image IS NOT NULL LOOP

            IF eid IS NULL THEN
                eid := _current_or_new_event();
            END IF;

            INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.core.Image', iid
                WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS rr
                    WHERE rr.event_id = eid AND rr.entity_type = 'ome.model.core.Image' AND rr.entity_id = iid);
        END LOOP;

        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION folder_roi_link_update_trigger() RETURNS TRIGGER AS $$

    DECLARE
        eid BIGINT;
        iid BIGINT;

    BEGIN
        FOR iid IN SELECT DISTINCT r.image FROM roi AS r
            WHERE r.id IN (OLD.child, NEW.child) AND r.image IS NOT NULL LOOP

            IF eid IS NULL THEN
                eid := _current_or_new_event();
            END IF;

            INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.core.Image', iid
                WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS rr
                    WHERE rr.event_id = eid AND rr.entity_type = 'ome.model.core.Image' AND rr.entity_id = iid);
        END LOOP;

        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION folder_roi_link_delete_trigger() RETURNS TRIGGER AS $$

    DECLARE
        eid BIGINT;
        iid BIGINT;

    BEGIN
        FOR iid IN SELECT DISTINCT r.image FROM roi AS r
            WHERE r.id = OLD.child AND r.image IS NOT NULL LOOP

            IF eid IS NULL THEN
                eid := _current_or_new_event();
            END IF;

            INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.core.Image', iid
                WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS rr
                    WHERE rr.event_id = eid AND rr.entity_type = 'ome.model.core.Image' AND rr.entity_id = iid);
        END LOOP;

        RETURN OLD;
    END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION roi_insert_trigger() RETURNS TRIGGER AS $$

    DECLARE
        eid BIGINT;
        iid BIGINT;

    BEGIN
        iid := NEW.image;

        IF iid IS NOT NULL THEN
            eid := _current_or_new_event();

            INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.core.Image', iid
                WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS rr
                    WHERE rr.event_id = eid AND rr.entity_type = 'ome.model.core.Image' AND rr.entity_id = iid);
        END IF;

        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION roi_update_trigger() RETURNS TRIGGER AS $$

    DECLARE
        eid BIGINT;
        iid BIGINT;

    BEGIN
        IF OLD.image = NEW.image THEN
            RETURN NEW;
        END IF;

        iid := OLD.image;

        IF iid IS NOT NULL THEN
            eid := _current_or_new_event();

            INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.core.Image', iid
                WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS rr
                    WHERE rr.event_id = eid AND rr.entity_type = 'ome.model.core.Image' AND rr.entity_id = iid);
        END IF;

        iid := NEW.image;

        IF iid IS NOT NULL THEN
            IF eid IS NULL THEN
                eid := _current_or_new_event();
            END IF;

            INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.core.Image', iid
                WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS rr
                    WHERE rr.event_id = eid AND rr.entity_type = 'ome.model.core.Image' AND rr.entity_id = iid);
        END IF;

        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION roi_delete_trigger() RETURNS TRIGGER AS $$

    DECLARE
        eid BIGINT;
        iid BIGINT;

    BEGIN
        iid := OLD.image;

        IF iid IS NOT NULL THEN
            eid := _current_or_new_event();

            INSERT INTO _reindexing_required (event_id, entity_type, entity_id)
                SELECT eid, 'ome.model.core.Image', iid
                WHERE NOT EXISTS (SELECT 1 FROM _reindexing_required AS rr
                    WHERE rr.event_id = eid AND rr.entity_type = 'ome.model.core.Image' AND rr.entity_id = iid);
        END IF;

        RETURN OLD;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER folder_update_trigger
    AFTER UPDATE ON folder
    FOR EACH ROW
    EXECUTE PROCEDURE folder_update_trigger();

CREATE TRIGGER folder_roi_link_insert_trigger
    AFTER INSERT ON folderroilink
    FOR EACH ROW
    EXECUTE PROCEDURE folder_roi_link_insert_trigger();

CREATE TRIGGER folder_roi_link_update_trigger
    AFTER UPDATE ON folderroilink
    FOR EACH ROW
    EXECUTE PROCEDURE folder_roi_link_update_trigger();

CREATE TRIGGER folder_roi_link_delete_trigger
    AFTER DELETE ON folderroilink
    FOR EACH ROW
    EXECUTE PROCEDURE folder_roi_link_delete_trigger();

CREATE TRIGGER roi_insert_trigger
    AFTER INSERT ON roi
    FOR EACH ROW
    EXECUTE PROCEDURE roi_insert_trigger();

CREATE TRIGGER roi_update_trigger
    AFTER UPDATE ON roi
    FOR EACH ROW
    EXECUTE PROCEDURE roi_update_trigger();

CREATE TRIGGER roi_delete_trigger
    AFTER DELETE ON roi
    FOR EACH ROW
    EXECUTE PROCEDURE roi_delete_trigger();


--
-- FINISHED
--

UPDATE dbpatch SET message = 'Database updated.', finished = clock_timestamp()
    WHERE currentVersion  = 'OMERO5.3DEV' AND
          currentPatch    = 5             AND
          previousVersion = 'OMERO5.2'    AND
          previousPatch   = 0;

SELECT CHR(10)||CHR(10)||CHR(10)||'YOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION OMERO5.3DEV__5'||CHR(10)||CHR(10)||CHR(10) AS Status;

COMMIT;
