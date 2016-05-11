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

-- TODO


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
