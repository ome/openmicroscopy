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
--- OMERO5 development release upgrade from OMERO5.0DEV__6 to OMERO5.0__0.
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

SELECT omero_assert_db_version('OMERO5.0DEV', 6);
DROP FUNCTION omero_assert_db_version(varchar, int);


INSERT INTO dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
             VALUES ('OMERO5.0',     0,              'OMERO5.0DEV',       6);

--
-- Actual upgrade
--

-- Prevent Directory entries in the originalfile table from having their mimetype changed.
CREATE FUNCTION _fs_directory_mimetype() RETURNS "trigger" AS $$
    BEGIN
        IF OLD.mimetype = 'Directory' AND NEW.mimetype != 'Directory' THEN
            RAISE EXCEPTION '%%', 'Directory('||OLD.id||')='||OLD.path||OLD.name||'/ must remain a Directory';
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


-- #11877 move import logs to upload jobs so they are no longer file annotations
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


--
-- FINISHED
--

UPDATE dbpatch SET message = 'Database updated.', finished = clock_timestamp()
    WHERE currentVersion  = 'OMERO5.0'    AND
          currentPatch    = 0             AND
          previousVersion = 'OMERO5.0DEV' AND
          previousPatch   = 6;

SELECT CHR(10)||CHR(10)||CHR(10)||'YOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION OMERO5.0__0'||CHR(10)||CHR(10)||CHR(10) AS Status;

COMMIT;
