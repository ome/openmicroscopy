-- Copyright (C) 2012-3 Glencoe Software, Inc. All rights reserved.
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
--- OMERO5 development release upgrade from OMERO4.4__0 to OMERO5.0__0.
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

SELECT omero_assert_db_version('OMERO4.4', 0);
DROP FUNCTION omero_assert_db_version(varchar, int);


INSERT INTO dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
             VALUES ('OMERO5.0',     0,              'OMERO4.4',          0);

--
-- Actual upgrade
--

CREATE TABLE checksumalgorithm (
    id INT8 PRIMARY KEY,
    external_id INT8 UNIQUE,
    permissions INT8 NOT NULL,
    "value" VARCHAR(255) NOT NULL UNIQUE,
    CONSTRAINT fkchecksumalgorithm_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo
);

CREATE TABLE fileset (
    id INT8 PRIMARY KEY,
    creation_id INT8 NOT NULL,
    external_id INT8 UNIQUE,
    group_id INT8 NOT NULL,
    owner_id INT8 NOT NULL,
    permissions INT8 NOT NULL,
    templatePrefix TEXT NOT NULL,
    update_id INT8 NOT NULL,
    version INT4,
    CONSTRAINT fkfileset_creation_id_event          FOREIGN KEY (creation_id) REFERENCES event,
    CONSTRAINT fkfileset_external_id_externalinfo   FOREIGN KEY (external_id) REFERENCES externalinfo,
    CONSTRAINT fkfileset_group_id_experimentergroup FOREIGN KEY (group_id)    REFERENCES experimentergroup,
    CONSTRAINT fkfileset_owner_id_experimenter      FOREIGN KEY (owner_id)    REFERENCES experimenter,
    CONSTRAINT fkfileset_update_id_event            FOREIGN KEY (update_id)   REFERENCES event
);

CREATE TABLE filesetannotationlink (
    id INT8 PRIMARY KEY,
    child INT8 NOT NULL,
    creation_id INT8 NOT NULL,
    external_id INT8 UNIQUE,
    group_id INT8 NOT NULL,
    owner_id INT8 NOT NULL,
    parent INT8 NOT NULL,
    permissions INT8 NOT NULL,
    update_id INT8 NOT NULL,
    version INT4,
    UNIQUE (parent, child, owner_id),
    CONSTRAINT fkfilesetannotationlink_child_annotation           FOREIGN KEY (child)       REFERENCES annotation,
    CONSTRAINT fkfilesetannotationlink_creation_id_event          FOREIGN KEY (creation_id) REFERENCES event,
    CONSTRAINT fkfilesetannotationlink_external_id_externalinfo   FOREIGN KEY (external_id) REFERENCES externalinfo,
    CONSTRAINT fkfilesetannotationlink_group_id_experimentergroup FOREIGN KEY (group_id)    REFERENCES experimentergroup,
    CONSTRAINT fkfilesetannotationlink_owner_id_experimenter      FOREIGN KEY (owner_id)    REFERENCES experimenter,
    CONSTRAINT fkfilesetannotationlink_parent_fileset             FOREIGN KEY (parent)      REFERENCES fileset,
    CONSTRAINT fkfilesetannotationlink_update_id_event            FOREIGN KEY (update_id)   REFERENCES event
);

CREATE TABLE filesetentry (
    id INT8 PRIMARY KEY,
    clientPath TEXT NOT NULL,
    creation_id INT8 NOT NULL,
    external_id INT8 UNIQUE,
    fileset INT8 NOT NULL,
    fileset_index INT4 NOT NULL,
    group_id INT8 NOT NULL,
    originalFile INT8 NOT NULL,
    owner_id INT8 NOT NULL,
    permissions INT8 NOT NULL,
    update_id INT8 NOT NULL,
    version INT4,
    UNIQUE (fileset, fileset_index),
    CONSTRAINT fkfilesetentry_creation_id_event          FOREIGN KEY (creation_id)  REFERENCES event,
    CONSTRAINT fkfilesetentry_external_id_externalinfo   FOREIGN KEY (external_id)  REFERENCES externalinfo,
    CONSTRAINT fkfilesetentry_fileset_fileset            FOREIGN KEY (fileset)      REFERENCES fileset,
    CONSTRAINT fkfilesetentry_group_id_experimentergroup FOREIGN KEY (group_id)     REFERENCES experimentergroup,
    CONSTRAINT fkfilesetentry_originalFile_originalfile  FOREIGN KEY (originalFile) REFERENCES originalfile,
    CONSTRAINT fkfilesetentry_owner_id_experimenter      FOREIGN KEY (owner_id)     REFERENCES experimenter,
    CONSTRAINT fkfilesetentry_update_id_event            FOREIGN KEY (update_id)    REFERENCES event
);

CREATE TABLE filesetjoblink (
    id INT8 PRIMARY KEY,
    child INT8 NOT NULL,
    creation_id INT8 NOT NULL,
    external_id INT8 UNIQUE,
    group_id INT8 NOT NULL,
    owner_id INT8 NOT NULL,
    parent INT8 NOT NULL,
    parent_index INT4 NOT NULL,
    permissions INT8 NOT NULL,
    update_id INT8 NOT NULL,
    version INT4,
    UNIQUE (parent, parent_index),
    UNIQUE (parent, child, owner_id),
    CONSTRAINT fkfilesetjoblink_child_job                  FOREIGN KEY (child)       REFERENCES job,
    CONSTRAINT fkfilesetjoblink_creation_id_event          FOREIGN KEY (creation_id) REFERENCES event,
    CONSTRAINT fkfilesetjoblink_external_id_externalinfo   FOREIGN KEY (external_id) REFERENCES externalinfo,
    CONSTRAINT fkfilesetjoblink_group_id_experimentergroup FOREIGN KEY (group_id)    REFERENCES experimentergroup,
    CONSTRAINT fkfilesetjoblink_owner_id_experimenter      FOREIGN KEY (owner_id)    REFERENCES experimenter,
    CONSTRAINT fkfilesetjoblink_parent_fileset             FOREIGN KEY (parent)      REFERENCES fileset,
    CONSTRAINT fkfilesetjoblink_update_id_event            FOREIGN KEY (update_id)   REFERENCES event
);

CREATE TABLE filesetversioninfo (
    id INT8 PRIMARY KEY,
    bioformatsReader VARCHAR(255) NOT NULL,
    bioformatsVersion VARCHAR(255) NOT NULL,
    creation_id INT8 NOT NULL,
    external_id INT8 UNIQUE,
    group_id INT8 NOT NULL,
    locale VARCHAR(255) NOT NULL,
    omeroVersion VARCHAR(255) NOT NULL,
    osArchitecture VARCHAR(255) NOT NULL,
    osName VARCHAR(255) NOT NULL,
    osVersion VARCHAR(255) NOT NULL,
    owner_id INT8 NOT NULL,
    permissions INT8 NOT NULL,
    update_id INT8 NOT NULL,
    version INT4,
    CONSTRAINT fkfilesetversioninfo_creation_id_event          FOREIGN KEY (creation_id) REFERENCES event,
    CONSTRAINT fkfilesetversioninfo_external_id_externalinfo   FOREIGN KEY (external_id) REFERENCES externalinfo,
    CONSTRAINT fkfilesetversioninfo_group_id_experimentergroup FOREIGN KEY (group_id)    REFERENCES experimentergroup,
    CONSTRAINT fkfilesetversioninfo_owner_id_experimenter      FOREIGN KEY (owner_id)    REFERENCES experimenter,
    CONSTRAINT fkfilesetversioninfo_update_id_event            FOREIGN KEY (update_id)   REFERENCES event
);

CREATE VIEW count_fileset_annotationlinks_by_owner (fileset_id, owner_id, "count") AS
    SELECT parent, owner_id, count(*) FROM filesetannotationlink
    GROUP BY parent, owner_id ORDER BY parent;

CREATE VIEW count_fileset_joblinks_by_owner (fileset_id, owner_id, "count") AS
    SELECT parent, owner_id, count(*) FROM filesetjoblink
    GROUP BY parent, owner_id ORDER BY parent;

CREATE TABLE indexingjob (
    job_id INT8 PRIMARY KEY,
    CONSTRAINT fkindexingjob_job_id_job FOREIGN KEY (job_id) REFERENCES job
);

CREATE TABLE integritycheckjob (
    job_id INT8 PRIMARY KEY,
    CONSTRAINT fkintegritycheckjob_job_id_job FOREIGN KEY (job_id) REFERENCES job
);

CREATE TABLE metadataimportjob (
    job_id INT8 PRIMARY KEY,
    versionInfo INT8 NOT NULL,
    CONSTRAINT fkmetadataimportjob_job_id_job FOREIGN KEY (job_id) REFERENCES job,
    CONSTRAINT fkmetadataimportjob_versionInfo_filesetversioninfo FOREIGN KEY (versionInfo) REFERENCES filesetversioninfo
);

CREATE TABLE pixeldatajob (
    job_id INT8 PRIMARY KEY,
    CONSTRAINT fkpixeldatajob_job_id_job FOREIGN KEY (job_id) REFERENCES job
);

CREATE TABLE thumbnailgenerationjob (
    job_id INT8 PRIMARY KEY,
    CONSTRAINT fkthumbnailgenerationjob_job_id_job FOREIGN KEY (job_id) REFERENCES job
);

CREATE TABLE uploadjob (
    job_id INT8 PRIMARY KEY,
    versionInfo INT8,
    CONSTRAINT fkuploadjob_job_id_job FOREIGN KEY (job_id) REFERENCES job,
    CONSTRAINT fkuploadjob_versionInfo_filesetversioninfo FOREIGN KEY (versionInfo) REFERENCES filesetversioninfo
);

CREATE SEQUENCE seq_checksumalgorithm;
CREATE SEQUENCE seq_fileset;
CREATE SEQUENCE seq_filesetannotationlink;
CREATE SEQUENCE seq_filesetentry;
CREATE SEQUENCE seq_filesetjoblink;
CREATE SEQUENCE seq_filesetversioninfo;

INSERT INTO _lock_ids (name, id) SELECT 'seq_checksumalgorithm', nextval('_lock_seq');
INSERT INTO _lock_ids (name, id) SELECT 'seq_fileset', nextval('_lock_seq');
INSERT INTO _lock_ids (name, id) SELECT 'seq_filesetannotationlink', nextval('_lock_seq');
INSERT INTO _lock_ids (name, id) SELECT 'seq_filesetentry', nextval('_lock_seq');
INSERT INTO _lock_ids (name, id) SELECT 'seq_filesetjoblink', nextval('_lock_seq');
INSERT INTO _lock_ids (name, id) SELECT 'seq_filesetversioninfo', nextval('_lock_seq');

INSERT INTO checksumalgorithm (id, permissions, "value") SELECT ome_nextval('seq_checksumalgorithm'), -52, 'Adler-32';
INSERT INTO checksumalgorithm (id, permissions, "value") SELECT ome_nextval('seq_checksumalgorithm'), -52, 'CRC-32';
INSERT INTO checksumalgorithm (id, permissions, "value") SELECT ome_nextval('seq_checksumalgorithm'), -52, 'MD5-128';
INSERT INTO checksumalgorithm (id, permissions, "value") SELECT ome_nextval('seq_checksumalgorithm'), -52, 'Murmur3-32';
INSERT INTO checksumalgorithm (id, permissions, "value") SELECT ome_nextval('seq_checksumalgorithm'), -52, 'Murmur3-128';
INSERT INTO checksumalgorithm (id, permissions, "value") SELECT ome_nextval('seq_checksumalgorithm'), -52, 'SHA1-160';

INSERT INTO filtertype (id, permissions, "value") SELECT ome_nextval('seq_filtertype'), -52, 'Tuneable';

-- See trac #11549 regarding CHECK adjustments below.

-- Note that constraints DROPped below may have been differently named.

ALTER TABLE detectorsettings ADD COLUMN integration INT4 CONSTRAINT detectorsettings_integration_check
    CHECK (integration > 0);
ALTER TABLE detectorsettings ADD COLUMN zoom FLOAT8;

ALTER TABLE image ADD COLUMN fileset INT8;
ALTER TABLE image ADD CONSTRAINT fkimage_fileset_fileset FOREIGN KEY (fileset) REFERENCES fileset;

ALTER TABLE objective DROP CONSTRAINT objective_nominalMagnification_check;
ALTER TABLE objective ALTER COLUMN nominalMagnification TYPE FLOAT8;

ALTER TABLE originalfile RENAME COLUMN sha1 TO hash;
ALTER TABLE originalfile ALTER COLUMN hash DROP NOT NULL;
ALTER TABLE originalfile ALTER COLUMN "size" DROP NOT NULL;
ALTER TABLE originalfile ADD COLUMN hasher INT8;
ALTER TABLE originalfile ADD CONSTRAINT fkoriginalfile_hasher_checksumalgorithm FOREIGN KEY (hasher) REFERENCES checksumalgorithm;
UPDATE originalfile SET hasher = (SELECT id FROM checksumalgorithm WHERE "value" = 'SHA1-160') WHERE hash ~ '^[A-Fa-f0-9]{40}$';

ALTER TABLE pixels ADD COLUMN significantBits INT4;
ALTER TABLE pixels DROP CONSTRAINT pixels_check;
ALTER TABLE pixels ADD CONSTRAINT pixels_check
   CHECK (significantBits > 0 AND sizeX > 0 AND sizeY > 0 AND sizeZ > 0 AND sizeC > 0 AND sizeT > 0);

CREATE OR REPLACE FUNCTION annotation_update_event_trigger() RETURNS "trigger"
    AS '
    DECLARE
        rec RECORD;
        eid INT8;
        cnt INT8;
    BEGIN

        IF NOT EXISTS(SELECT table_name FROM information_schema.tables where table_name = ''_updated_annotations'') THEN
            CREATE TEMP TABLE _updated_annotations (entitytype varchar, entityid int8) ON COMMIT DELETE ROWS;
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

        FOR rec IN SELECT id, parent FROM experimenterannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.meta.Experimenter'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM experimentergroupannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.meta.ExperimenterGroup'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM filesetannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.fs.Fileset'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM imageannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.core.Image'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM namespaceannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.meta.Namespace'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM nodeannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.meta.Node'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM originalfileannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.core.OriginalFile'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM pixelsannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.core.Pixels'');
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

        FOR rec IN SELECT id, parent FROM wellannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.screen.Well'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM wellsampleannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.screen.WellSample'');
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

CREATE TRIGGER fileset_annotation_link_event_trigger
    AFTER UPDATE ON filesetannotationlink
    FOR EACH ROW EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.fs.Fileset');

CREATE TRIGGER fileset_annotation_link_delete_trigger
    BEFORE DELETE ON filesetannotationlink
    FOR EACH ROW EXECUTE PROCEDURE annotation_link_delete_trigger('ome.model.fs.Fileset');

CREATE INDEX i_FilesetAnnotationLink_child ON filesetannotationlink(child);
CREATE INDEX i_FilesetAnnotationLink_parent ON filesetannotationlink(parent);
CREATE INDEX i_FilesetEntry_fileset ON filesetentry(fileset);
CREATE INDEX i_FilesetEntry_originalFile ON filesetentry(originalFile);
CREATE INDEX i_FilesetJobLink_child ON filesetjoblink(child);
CREATE INDEX i_FilesetJobLink_parent ON filesetjoblink(parent);
CREATE INDEX i_Image_fileset ON image(fileset);
CREATE INDEX i_MetadataImportJob_versionInfo ON metadataimportjob(versionInfo);
CREATE INDEX i_OriginalFile_hasher ON originalfile(hasher);
CREATE INDEX i_UploadJob_versionInfo ON uploadjob(versionInfo);
CREATE INDEX i_fileset_group ON fileset(group_id);
CREATE INDEX i_fileset_owner ON fileset(owner_id);
CREATE INDEX i_filesetannotationlink_group ON filesetannotationlink(group_id);
CREATE INDEX i_filesetannotationlink_owner ON filesetannotationlink(owner_id);
CREATE INDEX i_filesetentry_group ON filesetentry(group_id);
CREATE INDEX i_filesetentry_owner ON filesetentry(owner_id);
CREATE INDEX i_filesetjoblink_group ON filesetjoblink(group_id);
CREATE INDEX i_filesetjoblink_owner ON filesetjoblink(owner_id);
CREATE INDEX i_filesetversioninfo_group ON filesetversioninfo(group_id);
CREATE INDEX i_filesetversioninfo_owner ON filesetversioninfo(owner_id);

CREATE FUNCTION filesetentry_fileset_index_move() RETURNS "trigger" AS '
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
          RAISE NOTICE ''Remapping filesetentry % via (-1 - oldvalue )'', duplicate;
          UPDATE filesetentry SET fileset_index = -1 - fileset_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;' LANGUAGE plpgsql;

CREATE TRIGGER filesetentry_fileset_index_trigger
    BEFORE UPDATE ON filesetentry
    FOR EACH ROW EXECUTE PROCEDURE filesetentry_fileset_index_move();

CREATE FUNCTION filesetjoblink_parent_index_move() RETURNS "trigger" AS '
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
          RAISE NOTICE ''Remapping filesetjoblink % via (-1 - oldvalue )'', duplicate;
          UPDATE filesetjoblink SET parent_index = -1 - parent_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;' LANGUAGE plpgsql;

CREATE TRIGGER filesetjoblink_parent_index_trigger
    BEFORE UPDATE ON filesetjoblink
    FOR EACH ROW EXECUTE PROCEDURE filesetjoblink_parent_index_move();

-- Prevent the deletion of mimetype = "Directory" objects
CREATE FUNCTION _fs_dir_delete() RETURNS "trigger" AS $_fs_dir_delete$
    BEGIN
        IF OLD.repo IS NOT NULL THEN
            IF OLD.mimetype = 'Directory' THEN
                --
                -- If any children are found, prevent deletion
                --
                IF EXISTS(SELECT ID FROM originalfile
                           WHERE path = OLD.path || OLD.name || '/'
                           LIMIT 1) THEN

                    -- CANCEL DELETE
                    RAISE EXCEPTION '%', 'Directory('||OLD.id||')='||OLD.path||OLD.name||'/ is not empty!';

                END IF;
            END IF;
        END IF;
        RETURN OLD; -- proceed
    END;
$_fs_dir_delete$ LANGUAGE plpgsql;

CREATE TRIGGER _fs_dir_delete
    BEFORE DELETE ON originalfile
    FOR EACH ROW EXECUTE PROCEDURE _fs_dir_delete();

CREATE TABLE _fs_deletelog (
    event_id BIGINT NOT NULL,
    file_id BIGINT NOT NULL,
    owner_id BIGINT NOT NULL,
    group_id BIGINT NOT NULL,
    "path" TEXT NOT NULL,
    "name" VARCHAR(255) NOT NULL,
    repo VARCHAR(36) NOT NULL,
    params TEXT[2][]
);

CREATE FUNCTION _fs_log_delete() RETURNS TRIGGER AS $_fs_log_delete$
    BEGIN
        IF OLD.repo IS NOT NULL THEN
            INSERT INTO _fs_deletelog SELECT
                _current_or_new_event(),
                OLD.id, OLD.owner_id, OLD.group_id,
                OLD.path, OLD.name, OLD.repo, OLD.params;
        END IF;
        RETURN OLD;
    END;
$_fs_log_delete$ LANGUAGE plpgsql;

CREATE TRIGGER _fs_log_delete
    AFTER DELETE ON originalfile
    FOR EACH ROW EXECUTE PROCEDURE _fs_log_delete();

--
-- At this point the database is at OMERO5.0DEV__6. Now continue on to OMERO5.0__0.
--

-- Prevent Directory entries in the originalfile table from having their mimetype changed.
CREATE FUNCTION _fs_directory_mimetype() RETURNS "trigger" AS $$
    BEGIN
        IF OLD.mimetype = 'Directory' AND NEW.mimetype != 'Directory' THEN
            RAISE EXCEPTION '%', 'Directory('||OLD.id||')='||OLD.path||OLD.name||'/ must remain a Directory';
        END IF;
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER _fs_directory_mimetype
    BEFORE UPDATE ON originalfile
    FOR EACH ROW EXECUTE PROCEDURE _fs_directory_mimetype();

-- Prevent SQL DELETE from removing the root experimenter from the system or user group.
CREATE FUNCTION prevent_root_deactivate_delete() RETURNS "trigger" AS $$
    BEGIN
        IF OLD.child = 0 THEN
            IF OLD.parent = 0 THEN
                RAISE EXCEPTION 'cannot remove system group membership for root';
            ELSIF OLD.parent = 1 THEN
                RAISE EXCEPTION 'cannot remove user group membership for root';
            END IF;
        END IF;
        RETURN OLD;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER prevent_root_deactivate_delete
    BEFORE DELETE ON groupexperimentermap
    FOR EACH ROW EXECUTE PROCEDURE prevent_root_deactivate_delete();

-- Prevent SQL UPDATE from removing the root experimenter from the system or user group.
CREATE FUNCTION prevent_root_deactivate_update() RETURNS "trigger" AS $$
    BEGIN
        IF OLD.child != NEW.child OR OLD.parent != NEW.parent THEN
            IF OLD.child = 0 THEN
                IF OLD.parent = 0 THEN
                    RAISE EXCEPTION 'cannot remove system group membership for root';
                ELSIF OLD.parent = 1 THEN
                    RAISE EXCEPTION 'cannot remove user group membership for root';
                END IF;
            END IF;
        END IF;
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER prevent_root_deactivate_update
    BEFORE UPDATE ON groupexperimentermap
    FOR EACH ROW EXECUTE PROCEDURE prevent_root_deactivate_update();

-- Prevent the root and guest experimenters from being renamed.
CREATE FUNCTION prevent_experimenter_rename() RETURNS "trigger" AS $$
    BEGIN
        IF OLD.omename != NEW.omename THEN
            IF OLD.id = 0 THEN
                RAISE EXCEPTION 'cannot rename root experimenter';
            ELSIF OLD.id = 1 THEN
                RAISE EXCEPTION 'cannot rename guest experimenter';
            END IF;
        END IF;
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER prevent_experimenter_rename
    BEFORE UPDATE ON experimenter
    FOR EACH ROW EXECUTE PROCEDURE prevent_experimenter_rename();

-- Prevent the system, user and guest groups from being renamed.
CREATE FUNCTION prevent_experimenter_group_rename() RETURNS "trigger" AS $$
    BEGIN
        IF OLD.name != NEW.name THEN
            IF OLD.id = 0 THEN
                RAISE EXCEPTION 'cannot rename system experimenter group';
            ELSIF OLD.id = 1 THEN
                RAISE EXCEPTION 'cannot rename user experimenter group';
            ELSIF OLD.id = 2 THEN
                RAISE EXCEPTION 'cannot rename guest experimenter group';
            END IF;
        END IF;
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER prevent_experimenter_group_rename
    BEFORE UPDATE ON experimentergroup
    FOR EACH ROW EXECUTE PROCEDURE prevent_experimenter_group_rename();

-- #11810 Fix Image.archived flag
UPDATE image set archived = false where id in (
    SELECT i.id FROM pixels p, image i
     WHERE p.image = i.id
       AND i.archived
       AND NOT EXISTS ( SELECT 1 FROM pixelsoriginalfilemap m WHERE m.child = p.id));

--
-- FINISHED
--

UPDATE dbpatch SET message = 'Database updated.', finished = clock_timestamp()
    WHERE currentVersion  = 'OMERO5.0' AND
          currentPatch    = 0          AND
          previousVersion = 'OMERO4.4' AND
          previousPatch   = 0;

SELECT CHR(10)||CHR(10)||CHR(10)||'YOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION OMERO5.0__0'||CHR(10)||CHR(10)||CHR(10) AS Status;

COMMIT;
