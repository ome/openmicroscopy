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
--- OMERO5 development release upgrade from OMERO5.2__0 to OMERO5.3DEV__12.
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

SELECT assert_db_server_prerequisites(90300);

DROP FUNCTION assert_db_server_prerequisites(INTEGER);
DROP FUNCTION db_pretty_version(INTEGER);


--
-- Actual upgrade
--

INSERT INTO dbpatch (currentVersion, currentPatch, previousVersion, previousPatch)
             VALUES ('OMERO5.3DEV',  12,           'OMERO5.2',      0);

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
DROP TRIGGER IF EXISTS detector_annotation_link_event_trigger_insert ON detectorannotationlink;
DROP TRIGGER dichroic_annotation_link_event_trigger        ON dichroicannotationlink;
DROP TRIGGER IF EXISTS dichroic_annotation_link_event_trigger_insert ON dichroicannotationlink;
DROP TRIGGER experimenter_annotation_link_event_trigger        ON experimenterannotationlink;
DROP TRIGGER experimenter_annotation_link_event_trigger_insert ON experimenterannotationlink;
DROP TRIGGER experimentergroup_annotation_link_event_trigger        ON experimentergroupannotationlink;
DROP TRIGGER experimentergroup_annotation_link_event_trigger_insert ON experimentergroupannotationlink;
DROP TRIGGER fileset_annotation_link_event_trigger        ON filesetannotationlink;
DROP TRIGGER fileset_annotation_link_event_trigger_insert ON filesetannotationlink;
DROP TRIGGER filter_annotation_link_event_trigger        ON filterannotationlink;
DROP TRIGGER IF EXISTS filter_annotation_link_event_trigger_insert ON filterannotationlink;
DROP TRIGGER folder_annotation_link_event_trigger        ON folderannotationlink;
DROP TRIGGER folder_annotation_link_event_trigger_insert ON folderannotationlink;
DROP TRIGGER image_annotation_link_event_trigger        ON imageannotationlink;
DROP TRIGGER image_annotation_link_event_trigger_insert ON imageannotationlink;
DROP TRIGGER instrument_annotation_link_event_trigger        ON instrumentannotationlink;
DROP TRIGGER IF EXISTS instrument_annotation_link_event_trigger_insert ON instrumentannotationlink;
DROP TRIGGER lightpath_annotation_link_event_trigger        ON lightpathannotationlink;
DROP TRIGGER IF EXISTS lightpath_annotation_link_event_trigger_insert ON lightpathannotationlink;
DROP TRIGGER lightsource_annotation_link_event_trigger        ON lightsourceannotationlink;
DROP TRIGGER IF EXISTS lightsource_annotation_link_event_trigger_insert ON lightsourceannotationlink;
DROP TRIGGER namespace_annotation_link_event_trigger        ON namespaceannotationlink;
DROP TRIGGER namespace_annotation_link_event_trigger_insert ON namespaceannotationlink;
DROP TRIGGER node_annotation_link_event_trigger        ON nodeannotationlink;
DROP TRIGGER node_annotation_link_event_trigger_insert ON nodeannotationlink;
DROP TRIGGER objective_annotation_link_event_trigger        ON objectiveannotationlink;
DROP TRIGGER IF EXISTS objective_annotation_link_event_trigger_insert ON objectiveannotationlink;
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
DROP TRIGGER IF EXISTS shape_annotation_link_event_trigger_insert ON shapeannotationlink;
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

-- ... up to patch 6:

CREATE FUNCTION combine_ctm(new_transform FLOAT[], ctm FLOAT[]) RETURNS FLOAT[] AS $$

BEGIN
    RETURN ARRAY [ctm[1] * new_transform[1] + ctm[3] * new_transform[2],
                  ctm[2] * new_transform[1] + ctm[4] * new_transform[2],
                  ctm[1] * new_transform[3] + ctm[3] * new_transform[4],
                  ctm[2] * new_transform[3] + ctm[4] * new_transform[4],
                  ctm[1] * new_transform[5] + ctm[3] * new_transform[6] + ctm[5],
                  ctm[2] * new_transform[5] + ctm[4] * new_transform[6] + ctm[6]];

END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION parse_transform(transform TEXT) RETURNS FLOAT[] AS $$

DECLARE
    number_text TEXT;
    number_texts TEXT[];
    transform_matrix FLOAT[];
    identity_matrix CONSTANT FLOAT[] := ARRAY [1, 0, 0, 1, 0, 0];
    angle FLOAT;

BEGIN
    IF transform IS NULL OR transform = '' OR transform = 'none' THEN
        RETURN NULL;
    END IF;

    IF left(transform, 2) = '[ ' AND right(transform, 2) = ' ]' THEN
        transform := 'matrix(' || left(right(transform, -2), -2) || ')';
    ELSIF left(transform, 1) = '[' AND right(transform, 1) = ']' THEN
        transform := 'matrix(' || left(right(transform, -1), -1) || ')';
    END IF;

    IF left(transform, 7) = 'matrix(' AND right(transform, 1) = ')' THEN

        number_texts := string_to_array(left(right(transform, -7), -1), ' ');

        IF array_length(number_texts, 1) != 6 THEN
            RAISE EXCEPTION 'must have six numbers in shape.transform value: %', transform;
        END IF;

        FOREACH number_text IN ARRAY number_texts LOOP
            transform_matrix := array_append(transform_matrix, CAST(number_text AS FLOAT));
        END LOOP;

    ELSIF left(transform, 10) = 'translate(' AND right(transform, 1) = ')' THEN

        number_texts := string_to_array(left(right(transform, -10), -1), ' ');

        IF array_length(number_texts, 1) = 1 THEN
            number_texts := array_append(number_texts, '0');
        ELSIF array_length(number_texts, 1) != 2 THEN
            RAISE EXCEPTION 'must have one or two numbers in shape.transform value: %', transform;
        END IF;

        transform_matrix := ARRAY [1, 0, 0, 1];

        FOREACH number_text IN ARRAY number_texts LOOP
            transform_matrix := array_append(transform_matrix, CAST(number_text AS FLOAT));
        END LOOP;

    ELSIF left(transform, 6) = 'scale(' AND right(transform, 1) = ')' THEN

        number_texts := string_to_array(left(right(transform, -6), -1), ' ');

        IF array_length(number_texts, 1) = 1 THEN
            number_texts[2] := number_texts[1];
        ELSIF array_length(number_texts, 1) != 2 THEN
            RAISE EXCEPTION 'must have one or two numbers in shape.transform value: %', transform;
        END IF;

        FOREACH number_text IN ARRAY number_texts LOOP
            transform_matrix := array_append(transform_matrix, CAST(number_text AS FLOAT)) || CAST(ARRAY [0, 0] AS FLOAT[]);
        END LOOP;

    ELSIF left(transform, 7) = 'rotate(' AND right(transform, 1) = ')' THEN

        number_texts := string_to_array(left(right(transform, -7), -1), ' ');

        IF array_length(number_texts, 1) = 1 THEN
            number_texts := number_texts || ARRAY ['0', '0'];
        ELSIF array_length(number_texts, 1) != 3 THEN
            RAISE EXCEPTION 'must have one or three numbers in shape.transform value: %', transform;
        END IF;

        FOREACH number_text IN ARRAY number_texts LOOP
            transform_matrix := array_append(transform_matrix, CAST(number_text AS FLOAT));
        END LOOP;

        angle := transform_matrix[1] * pi() / 180;

        IF transform_matrix[2] = 0 AND transform_matrix[3] = 0 THEN
            transform_matrix := ARRAY [cos(angle), sin(angle), -sin(angle), cos(angle), 0, 0];
        ELSE
            transform_matrix := combine_ctm(ARRAY [1, 0, 0, 1, -transform_matrix[2], -transform_matrix[3]],
                      combine_ctm(ARRAY [cos(angle), sin(angle), -sin(angle), cos(angle), 0, 0],
                      combine_ctm(ARRAY [1, 0, 0, 1, transform_matrix[2], transform_matrix[3]],
                                  identity_matrix)));
        END IF;

    ELSIF left(transform, 6) = 'skewX(' AND right(transform, 1) = ')' THEN

        number_texts := string_to_array(left(right(transform, -6), -1), ' ');

        IF array_length(number_texts, 1) != 1 THEN
            RAISE EXCEPTION 'must have one number in shape.transform value: %', transform;
        END IF;

        angle := CAST(number_texts[1] AS FLOAT) * pi() / 180;
        transform_matrix := ARRAY [1, 0, tan(angle), 1, 0, 0];

    ELSIF left(transform, 6) = 'skewY(' AND right(transform, 1) = ')' THEN

        number_texts := string_to_array(left(right(transform, -6), -1), ' ');

        IF array_length(number_texts, 1) != 1 THEN
            RAISE EXCEPTION 'must have one number in shape.transform value: %', transform;
        END IF;

        angle := CAST(number_texts[1] AS FLOAT) * pi() / 180;
        transform_matrix := ARRAY [1, tan(angle), 0, 1, 0, 0];

    ELSE
        RAISE EXCEPTION 'cannot parse shape.transform value: %', transform;

    END IF;

    IF transform_matrix = identity_matrix THEN
        RETURN NULL;
    ELSE
        RETURN transform_matrix;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE TABLE affinetransform (
    id BIGINT PRIMARY KEY,
    a00 DOUBLE PRECISION NOT NULL,
    a10 DOUBLE PRECISION NOT NULL,
    a01 DOUBLE PRECISION NOT NULL,
    a11 DOUBLE PRECISION NOT NULL,
    a02 DOUBLE PRECISION NOT NULL,
    a12 DOUBLE PRECISION NOT NULL,
    permissions BIGINT NOT NULL,
    version INTEGER,
    external_id BIGINT UNIQUE,
    group_id BIGINT NOT NULL,
    owner_id BIGINT NOT NULL,
    creation_id BIGINT NOT NULL,
    update_id BIGINT NOT NULL);

CREATE SEQUENCE seq_affinetransform; INSERT INTO _lock_ids (name, id)
    SELECT 'seq_affinetransform', nextval('_lock_seq');

ALTER TABLE affinetransform ADD CONSTRAINT FKaffinetransform_creation_id_event
    FOREIGN KEY (creation_id) REFERENCES event;
ALTER TABLE affinetransform ADD CONSTRAINT FKaffinetransform_update_id_event
    FOREIGN KEY (update_id) REFERENCES event;
ALTER TABLE affinetransform ADD CONSTRAINT FKaffinetransform_external_id_externalinfo
    FOREIGN KEY (external_id) REFERENCES externalinfo;
ALTER TABLE affinetransform ADD CONSTRAINT FKaffinetransform_group_id_experimentergroup
    FOREIGN KEY (group_id) REFERENCES experimentergroup;
ALTER TABLE affinetransform ADD CONSTRAINT FKaffinetransform_owner_id_experimenter
    FOREIGN KEY (owner_id) REFERENCES experimenter;

CREATE FUNCTION upgrade_transform(
    svg_transform VARCHAR(255), permissions BIGINT, owner_id BIGINT, group_id BIGINT)
    RETURNS BIGINT AS $$

DECLARE
    matrix FLOAT[];
    transform_id BIGINT;
    event_id BIGINT;

BEGIN
    matrix := parse_transform(svg_transform);

    IF matrix IS NULL THEN
        RETURN NULL;
    END IF;

    SELECT ome_nextval('seq_affinetransform') INTO STRICT transform_id;
    SELECT _current_or_new_event() INTO STRICT event_id;

    INSERT INTO affinetransform (id, a00, a10, a01, a11, a02, a12,
                                 permissions, owner_id, group_id, creation_id, update_id)
        VALUES (transform_id, matrix[1], matrix[2], matrix[3], matrix[4], matrix[5], matrix[6],
                permissions, owner_id, group_id,  event_id, event_id);

    RETURN transform_id;
END;
$$ LANGUAGE plpgsql;

ALTER TABLE shape RENAME COLUMN transform TO transform_old;
ALTER TABLE shape ADD COLUMN transform BIGINT;

ALTER TABLE shape ADD CONSTRAINT FKshape_transform_affinetransform
    FOREIGN KEY (transform) REFERENCES affinetransform;

UPDATE shape SET transform = upgrade_transform(transform_old, permissions, owner_id, group_id)
    WHERE transform_old IS NOT NULL;

ALTER TABLE shape DROP COLUMN transform_old;

DROP FUNCTION upgrade_transform(VARCHAR(255), BIGINT, BIGINT, BIGINT);
DROP FUNCTION parse_transform(TEXT);
DROP FUNCTION combine_ctm(FLOAT[], FLOAT[]);

CREATE INDEX i_affinetransform_owner ON affinetransform(owner_id);
CREATE INDEX i_affinetransform_group ON affinetransform(group_id);
CREATE INDEX i_shape_transform ON shape(transform);

-- ... up to patch 7:

CREATE TABLE projectionaxis (
    id BIGINT PRIMARY KEY,
    permissions BIGINT NOT NULL,
    value VARCHAR(255) NOT NULL UNIQUE,
    external_id BIGINT UNIQUE,
    CONSTRAINT FKprojectionaxis_external_id_externalinfo
      FOREIGN KEY (external_id) REFERENCES externalinfo);

CREATE TABLE projectiontype (
    id BIGINT PRIMARY KEY,
    permissions BIGINT NOT NULL,
    value VARCHAR(255) NOT NULL UNIQUE,
    external_id BIGINT UNIQUE,
    CONSTRAINT FKprojectiontype_external_id_externalinfo
      FOREIGN KEY (external_id) REFERENCES externalinfo);

CREATE TABLE projectiondef (
    id BIGINT PRIMARY KEY,
    active BOOLEAN NOT NULL,
    axis BIGINT NOT NULL,
    permissions BIGINT NOT NULL,
    startplane nonnegative_int,
    endplane nonnegative_int,
    version INTEGER,
    creation_id BIGINT NOT NULL,
    external_id BIGINT UNIQUE,
    group_id BIGINT NOT NULL,
    owner_id BIGINT NOT NULL,
    update_id BIGINT NOT NULL,
    renderingdef BIGINT NOT NULL,
    type BIGINT NOT NULL,
    renderingdef_index INTEGER NOT NULL,
    UNIQUE (renderingdef, renderingdef_index),
    CONSTRAINT FKprojectiondef_creation_id_event
      FOREIGN KEY (creation_id) REFERENCES event,
    CONSTRAINT FKprojectiondef_update_id_event
      FOREIGN KEY (update_id) REFERENCES event,
    CONSTRAINT FKprojectiondef_external_id_externalinfo
      FOREIGN KEY (external_id) REFERENCES externalinfo,
    CONSTRAINT FKprojectiondef_axis_projectionaxis
      FOREIGN KEY (axis) REFERENCES projectionaxis,
    CONSTRAINT FKprojectiondef_type_projectiontype
      FOREIGN KEY (type) REFERENCES projectiontype,
    CONSTRAINT FKprojectiondef_group_id_experimentergroup
      FOREIGN KEY (group_id) REFERENCES experimentergroup,
    CONSTRAINT FKprojectiondef_renderingdef_renderingdef
      FOREIGN KEY (renderingdef) REFERENCES renderingdef,
    CONSTRAINT FKprojectiondef_owner_id_experimenter
      FOREIGN KEY (owner_id) REFERENCES experimenter);

CREATE SEQUENCE seq_projectiondef;
INSERT INTO _lock_ids (name, id)
    SELECT 'seq_projectiondef', nextval('_lock_seq');

CREATE SEQUENCE seq_projectionaxis;
INSERT INTO _lock_ids (name, id)
    SELECT 'seq_projectionaxis', nextval('_lock_seq');

CREATE SEQUENCE seq_projectiontype;
INSERT INTO _lock_ids (name, id)
    SELECT 'seq_projectiontype', nextval('_lock_seq');

INSERT INTO projectionaxis (id, permissions, value)
    SELECT ome_nextval('seq_projectionaxis'), -52, 'T';
INSERT INTO projectionaxis (id, permissions, value)
    SELECT ome_nextval('seq_projectionaxis'), -52, 'ModuloT';
INSERT INTO projectionaxis (id, permissions, value)
    SELECT ome_nextval('seq_projectionaxis'), -52, 'Z';
INSERT INTO projectionaxis (id, permissions, value)
    SELECT ome_nextval('seq_projectionaxis'), -52, 'ModuloZ';

INSERT INTO projectiontype (id, permissions, value)
    SELECT ome_nextval('seq_projectiontype'), -52, 'maximum';
INSERT INTO projectiontype (id, permissions, value)
    SELECT ome_nextval('seq_projectiontype'), -52, 'mean';
INSERT INTO projectiontype (id, permissions, value)
    SELECT ome_nextval('seq_projectiontype'), -52, 'sum';

CREATE OR REPLACE FUNCTION projectiondef_renderingdef_index_move() RETURNS "trigger" AS '
    DECLARE
      duplicate BIGINT;
    BEGIN

      -- Avoids a query if the new and old values of x are the same.
      IF new.renderingdef = old.renderingdef AND new.renderingdef_index = old.renderingdef_index THEN
          RETURN new;
      END IF;

      -- At most, there should be one duplicate
      SELECT id INTO duplicate
        FROM projectiondef
       WHERE renderingdef = new.renderingdef AND renderingdef_index = new.renderingdef_index
      OFFSET 0
       LIMIT 1;

      IF duplicate IS NOT NULL THEN
          RAISE NOTICE ''Remapping projectiondef % via (-1 - oldvalue )'', duplicate;
          UPDATE projectiondef SET renderingdef_index = -1 - renderingdef_index WHERE id = duplicate;
      END IF;

      RETURN new;
END;' LANGUAGE plpgsql;

CREATE TRIGGER projectiondef_renderingdef_index_trigger
    BEFORE UPDATE ON projectiondef
    FOR EACH ROW
    EXECUTE PROCEDURE projectiondef_renderingdef_index_move();

CREATE INDEX i_projectiondef_owner ON projectiondef(owner_id);
CREATE INDEX i_projectiondef_group ON projectiondef(group_id);
CREATE INDEX i_projectiondef_renderingdef ON projectiondef(renderingdef);
CREATE INDEX i_projectiondef_axis ON projectiondef(axis);
CREATE INDEX i_projectiondef_type ON projectiondef(type);

-- ... up to patch 8:

ALTER TABLE session ALTER COLUMN userip TYPE VARCHAR(45);

CREATE TABLE experimenter_config (
    experimenter_id BIGINT NOT NULL,
    name TEXT NOT NULL,
    value TEXT NOT NULL,
    index INTEGER NOT NULL,
    PRIMARY KEY (experimenter_id, index),
    CONSTRAINT FKexperimenter_config_map
        FOREIGN KEY (experimenter_id) REFERENCES experimenter);

CREATE FUNCTION experimenter_config_map_entry_delete_trigger_function() RETURNS "trigger" AS '
BEGIN
    DELETE FROM experimenter_config
        WHERE experimenter_id = OLD.id;
    RETURN OLD;
END;'
LANGUAGE plpgsql;

CREATE TRIGGER experimenter_config_map_entry_delete_trigger
    BEFORE DELETE ON experimenter
    FOR EACH ROW
    EXECUTE PROCEDURE experimenter_config_map_entry_delete_trigger_function();

CREATE INDEX experimenter_config_name ON experimenter_config(name);
CREATE INDEX experimenter_config_value ON experimenter_config(value);

-- ... up to patch 9:

ALTER TABLE projectiondef ADD stepping positive_int;

-- ... up to patch 10:

ALTER TABLE codomainmapcontext DROP CONSTRAINT FKcodomainmapcontext_renderingDef_renderingdef;
ALTER TABLE codomainmapcontext DROP renderingdef;
ALTER TABLE codomainmapcontext DROP renderingdef_index;

DROP TRIGGER codomainmapcontext_renderingDef_index_trigger ON codomainmapcontext;
DROP FUNCTION codomainmapcontext_renderingDef_index_move();

ALTER TABLE codomainmapcontext ADD channelBinding int8 NOT NULL;
ALTER TABLE codomainmapcontext ADD channelBinding_index int4 NOT NULL;

ALTER TABLE codomainmapcontext ADD UNIQUE (channelBinding, channelBinding_index);


ALTER TABLE codomainmapcontext
ADD CONSTRAINT FKcodomainmapcontext_channelBinding_channelbinding
FOREIGN KEY (channelBinding)
REFERENCES channelbinding;

CREATE OR REPLACE FUNCTION codomainmapcontext_channelBinding_index_move() RETURNS "trigger" AS '
    DECLARE
      duplicate INT8;
    BEGIN

      -- Avoids a query if the new and old values of x are the same.
      IF new.channelBinding = old.channelBinding AND new.channelBinding_index = old.channelBinding_index THEN
          RETURN new;
      END IF;

      -- At most, there should be one duplicate
      SELECT id INTO duplicate
        FROM codomainmapcontext
       WHERE channelBinding = new.channelBinding AND channelBinding_index = new.channelBinding_index
      OFFSET 0
       LIMIT 1;

      IF duplicate IS NOT NULL THEN
          RAISE NOTICE ''Remapping codomainmapcontext % via (-1 - oldvalue )'', duplicate;
          UPDATE codomainmapcontext SET channelBinding_index = -1 - channelBinding_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;' LANGUAGE plpgsql;

CREATE TRIGGER codomainmapcontext_channelBinding_index_trigger
    BEFORE UPDATE ON codomainmapcontext
    FOR EACH ROW EXECUTE PROCEDURE codomainmapcontext_channelBinding_index_move ();

CREATE INDEX i_CodomainMapContext_channelBinding ON codomainmapcontext(channelBinding);

-- ... up to patch 11:

CREATE OR REPLACE FUNCTION filesetentry_fileset_index_move() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    DECLARE
      duplicate INT8;
    BEGIN

      -- Avoids a query if the new and old values of x are the same.
      IF new.fileset = old.fileset AND new.fileset_index = old.fileset_index THEN
          RETURN new;
      END IF;

      -- At most, there should be one duplicate
      SELECT id INTO duplicate
        FROM filesetentry
       WHERE fileset = new.fileset AND fileset_index = new.fileset_index
      OFFSET 0
       LIMIT 1;

      IF duplicate IS NOT NULL THEN
          RAISE NOTICE 'Remapping filesetentry % via (-1 - oldvalue )', duplicate;
          UPDATE filesetentry SET fileset_index = -1 - fileset_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;$$;

CREATE OR REPLACE FUNCTION filesetjoblink_parent_index_move() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    DECLARE
      duplicate INT8;
    BEGIN

      -- Avoids a query if the new and old values of x are the same.
      IF new.parent = old.parent AND new.parent_index = old.parent_index THEN
          RETURN new;
      END IF;

      -- At most, there should be one duplicate
      SELECT id INTO duplicate
        FROM filesetjoblink
       WHERE parent = new.parent AND parent_index = new.parent_index
       OFFSET 0
       LIMIT 1;

      IF duplicate IS NOT NULL THEN
          RAISE NOTICE 'Remapping filesetjoblink % via (-1 - oldvalue )', duplicate;
          UPDATE filesetjoblink SET parent_index = -1 - parent_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;$$;

-- ... up to patch 12:

-- TODO


--
-- FINISHED
--

UPDATE dbpatch SET message = 'Database updated.', finished = clock_timestamp()
    WHERE currentVersion  = 'OMERO5.3DEV' AND
          currentPatch    = 12            AND
          previousVersion = 'OMERO5.2'    AND
          previousPatch   = 0;

SELECT CHR(10)||CHR(10)||CHR(10)||'YOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION OMERO5.3DEV__12'||CHR(10)||CHR(10)||CHR(10) AS Status;

COMMIT;
