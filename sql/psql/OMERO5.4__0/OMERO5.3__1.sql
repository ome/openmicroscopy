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
--- OMERO5 release upgrade from OMERO5.3__1 to OMERO5.4__0.
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

SELECT omero_assert_db_version('OMERO5.3', 1);
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
             VALUES ('OMERO5.4',     0,            'OMERO5.3',      1);

-- ... up to patch 0:

INSERT INTO adminprivilege (id, permissions, value) SELECT ome_nextval('seq_adminprivilege'), -52, 'Chgrp';
INSERT INTO adminprivilege (id, permissions, value) SELECT ome_nextval('seq_adminprivilege'), -52, 'Chown';
INSERT INTO adminprivilege (id, permissions, value) SELECT ome_nextval('seq_adminprivilege'), -52, 'DeleteFile';
INSERT INTO adminprivilege (id, permissions, value) SELECT ome_nextval('seq_adminprivilege'), -52, 'DeleteManagedRepo';
INSERT INTO adminprivilege (id, permissions, value) SELECT ome_nextval('seq_adminprivilege'), -52, 'DeleteOwned';
INSERT INTO adminprivilege (id, permissions, value) SELECT ome_nextval('seq_adminprivilege'), -52, 'DeleteScriptRepo';
INSERT INTO adminprivilege (id, permissions, value) SELECT ome_nextval('seq_adminprivilege'), -52, 'ModifyGroup';
INSERT INTO adminprivilege (id, permissions, value) SELECT ome_nextval('seq_adminprivilege'), -52, 'ModifyGroupMembership';
INSERT INTO adminprivilege (id, permissions, value) SELECT ome_nextval('seq_adminprivilege'), -52, 'ModifyUser';
INSERT INTO adminprivilege (id, permissions, value) SELECT ome_nextval('seq_adminprivilege'), -52, 'ReadSession';
INSERT INTO adminprivilege (id, permissions, value) SELECT ome_nextval('seq_adminprivilege'), -52, 'Sudo';
INSERT INTO adminprivilege (id, permissions, value) SELECT ome_nextval('seq_adminprivilege'), -52, 'WriteFile';
INSERT INTO adminprivilege (id, permissions, value) SELECT ome_nextval('seq_adminprivilege'), -52, 'WriteManagedRepo';
INSERT INTO adminprivilege (id, permissions, value) SELECT ome_nextval('seq_adminprivilege'), -52, 'WriteOwned';
INSERT INTO adminprivilege (id, permissions, value) SELECT ome_nextval('seq_adminprivilege'), -52, 'WriteScriptRepo';

-- ... up to patch 1:

-- use a table to note the role IDs explicitly in the database

CREATE TABLE _roles (
    root_user_id BIGINT NOT NULL,
    guest_user_id BIGINT NOT NULL,
    system_group_id BIGINT NOT NULL,
    user_group_id BIGINT NOT NULL,
    guest_group_id BIGINT NOT NULL
);

INSERT INTO _roles (root_user_id, guest_user_id, system_group_id, user_group_id, guest_group_id)
    VALUES (0, 1, 0, 1, 2);

-- Prevent SQL DELETE from removing the root experimenter from the system or user group.

CREATE OR REPLACE FUNCTION prevent_root_deactivate_delete() RETURNS "trigger" AS $$

    DECLARE
        roles _roles%ROWTYPE;

    BEGIN
        SELECT * INTO STRICT roles FROM _roles;

        IF OLD.child = roles.root_user_id THEN
            IF OLD.parent = roles.system_group_id THEN
                RAISE EXCEPTION 'cannot remove system group membership for root';
            ELSIF OLD.parent = roles.user_group_id THEN
                RAISE EXCEPTION 'cannot remove user group membership for root';
            END IF;
        END IF;
        RETURN OLD;
    END;
$$ LANGUAGE plpgsql;

-- Prevent SQL UPDATE from removing the root experimenter from the system or user group.

CREATE OR REPLACE FUNCTION prevent_root_deactivate_update() RETURNS "trigger" AS $$

    DECLARE
        roles _roles%ROWTYPE;

    BEGIN
        SELECT * INTO STRICT roles FROM _roles;

        IF OLD.child != NEW.child OR OLD.parent != NEW.parent THEN
            IF OLD.child = roles.root_user_id THEN
                IF OLD.parent = roles.system_group_id THEN
                    RAISE EXCEPTION 'cannot remove system group membership for root';
                ELSIF OLD.parent = roles.user_group_id THEN
                    RAISE EXCEPTION 'cannot remove user group membership for root';
                END IF;
            END IF;
        END IF;
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

-- Prevent the root and guest experimenters from being renamed.

CREATE OR REPLACE FUNCTION prevent_experimenter_rename() RETURNS "trigger" AS $$

    DECLARE
        roles _roles%ROWTYPE;

    BEGIN
        SELECT * INTO STRICT roles FROM _roles;

        IF OLD.omename != NEW.omename THEN
            IF OLD.id = roles.root_user_id THEN
                RAISE EXCEPTION 'cannot rename root experimenter';
            ELSIF OLD.id = roles.guest_user_id THEN
                RAISE EXCEPTION 'cannot rename guest experimenter';
            END IF;
        END IF;
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

-- Prevent the system, user and guest groups from being renamed.

CREATE OR REPLACE FUNCTION prevent_experimenter_group_rename() RETURNS "trigger" AS $$

    DECLARE
        roles _roles%ROWTYPE;

    BEGIN
        SELECT * INTO STRICT roles FROM _roles;

        IF OLD.name != NEW.name THEN
            IF OLD.id = roles.system_group_id THEN
                RAISE EXCEPTION 'cannot rename system experimenter group';
            ELSIF OLD.id = roles.user_group_id THEN
                RAISE EXCEPTION 'cannot rename user experimenter group';
            ELSIF OLD.id = roles.guest_group_id THEN
                RAISE EXCEPTION 'cannot rename guest experimenter group';
            END IF;
        END IF;
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

-- Prevent light administrator privileges from restricting the root experimenter.

CREATE FUNCTION prevent_root_privilege_restriction() RETURNS "trigger" AS $$

    DECLARE
        roles _roles%ROWTYPE;

    BEGIN
        SELECT * INTO STRICT roles FROM _roles;

        IF NEW.experimenter_id = roles.root_user_id AND NEW.name LIKE 'AdminPrivilege:%' AND NEW.value NOT ILIKE 'true' THEN
            RAISE EXCEPTION 'cannot restrict admin privileges of root experimenter';
        END IF;
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER prevent_root_privilege_restriction
    BEFORE INSERT OR UPDATE ON experimenter_config
    FOR EACH ROW EXECUTE PROCEDURE prevent_root_privilege_restriction();

-- Set up the current administrative privileges table and use it to prevent privilege elevation.

CREATE TABLE _current_admin_privileges (
    transaction BIGINT,
    privilege VARCHAR(255)
);

CREATE INDEX i_current_admin_privileges_transactions ON _current_admin_privileges(transaction);

CREATE OR REPLACE FUNCTION group_link_insert_check() RETURNS "trigger" AS $$

    DECLARE
        roles _roles%ROWTYPE;

    BEGIN
        SELECT * INTO STRICT roles FROM _roles;
        IF NEW.parent = roles.system_group_id AND EXISTS (SELECT 1 FROM adminprivilege p WHERE NOT
                (EXISTS (SELECT 1 FROM experimenter_config WHERE experimenter_id = NEW.child AND name = 'AdminPrivilege:' || p.value AND value NOT ILIKE 'true') OR
                 EXISTS (SELECT 1 FROM _current_admin_privileges WHERE transaction = txid_current() AND privilege = p.value))) THEN
            RAISE EXCEPTION 'cannot give administrator privileges that current user does not have';
        END IF;

        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION group_link_update_check() RETURNS "trigger" AS $$

    DECLARE
        roles _roles%ROWTYPE;

    BEGIN
        SELECT * INTO STRICT roles FROM _roles;

        IF (OLD.parent <> NEW.parent OR OLD.child <> NEW.child) AND NEW.parent = roles.system_group_id AND
        EXISTS (SELECT 1 FROM adminprivilege p WHERE NOT
                (EXISTS (SELECT 1 FROM experimenter_config WHERE experimenter_id = NEW.child AND name = 'AdminPrivilege:' || p.value AND value NOT ILIKE 'true') OR
                 EXISTS (SELECT 1 FROM _current_admin_privileges WHERE transaction = txid_current() AND privilege = p.value))) THEN
            RAISE EXCEPTION 'cannot give administrator privileges that current user does not have';
        END IF;

        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION user_config_delete_check() RETURNS "trigger" AS $$

    DECLARE
        roles _roles%ROWTYPE;

    BEGIN
        SELECT * INTO STRICT roles FROM _roles;

        IF OLD.name LIKE 'AdminPrivilege:%' AND OLD.value NOT ILIKE 'true' AND
       EXISTS (SELECT 1 FROM groupexperimentermap WHERE parent = roles.system_group_id AND child = OLD.experimenter_id) AND
       NOT EXISTS (SELECT 1 FROM _current_admin_privileges p WHERE p.transaction = txid_current() AND 'AdminPrivilege:' || p.privilege = OLD.name) THEN
            RAISE EXCEPTION 'cannot give administrator privileges that current user does not have';
        END IF;

        RETURN OLD;
    END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION user_config_update_check() RETURNS "trigger" AS $$

    DECLARE
        roles _roles%ROWTYPE;

    BEGIN
        SELECT * INTO STRICT roles FROM _roles;

        IF (OLD.experimenter_id <> NEW.experimenter_id OR OLD.name <> NEW.name OR OLD.value <> NEW.value) AND
       OLD.name LIKE 'AdminPrivilege:%' AND OLD.value NOT ILIKE 'true' AND
       EXISTS (SELECT 1 FROM groupexperimentermap WHERE parent = roles.system_group_id AND child = OLD.experimenter_id) AND
       NOT EXISTS (SELECT 1 FROM _current_admin_privileges p WHERE p.transaction = txid_current() AND 'AdminPrivilege:' || p.privilege = OLD.name) THEN
            RAISE EXCEPTION 'cannot give administrator privileges that current user does not have';
        END IF;

        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE CONSTRAINT TRIGGER group_link_insert_trigger
    AFTER INSERT ON groupexperimentermap DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW EXECUTE PROCEDURE group_link_insert_check();

CREATE CONSTRAINT TRIGGER group_link_update_trigger
    AFTER UPDATE ON groupexperimentermap DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW EXECUTE PROCEDURE group_link_update_check();

CREATE CONSTRAINT TRIGGER user_config_delete_trigger
    AFTER DELETE ON experimenter_config DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW EXECUTE PROCEDURE user_config_delete_check();

CREATE CONSTRAINT TRIGGER user_config_update_trigger
    AFTER UPDATE ON experimenter_config DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW EXECUTE PROCEDURE user_config_update_check();

-- ... up to patch 2:

-- Use secret key in setting originalfile.repo.

CREATE FUNCTION _protect_originalfile_repo_insert() RETURNS "trigger" AS $$

    DECLARE
        secret_key VARCHAR;
        secret_key_length INTEGER;
        is_good_change BOOLEAN := TRUE;

    BEGIN
        FOR secret_key IN SELECT uuid FROM node WHERE down IS NULL LOOP
            secret_key_length := LENGTH(secret_key);

            IF NEW.repo IS NULL THEN
                IF LEFT(NEW.name, secret_key_length) = secret_key THEN
                    NEW.name := RIGHT(NEW.name, -secret_key_length);
                END IF;
            ELSE
                IF LEFT(NEW.name, secret_key_length) = secret_key THEN
                    is_good_change := TRUE;
                    NEW.name := RIGHT(NEW.name, -secret_key_length);
                    EXIT;
                ELSE
                    is_good_change := FALSE;
                END IF;
            END IF;
        END LOOP;

        IF NOT is_good_change THEN
            RAISE EXCEPTION 'cannot set original repo property without secret key';
        END IF;

        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION _protect_originalfile_repo_update() RETURNS "trigger" AS $$

    DECLARE
        secret_key VARCHAR;
        secret_key_length INTEGER;
        is_good_change BOOLEAN := TRUE;

    BEGIN
        FOR secret_key IN SELECT uuid FROM node WHERE down IS NULL LOOP
            secret_key_length := LENGTH(secret_key);

            IF NEW.repo IS NULL OR OLD.repo = NEW.repo THEN
                IF LEFT(NEW.name, secret_key_length) = secret_key THEN
                    NEW.name := RIGHT(NEW.name, -secret_key_length);
                END IF;
            ELSE
                IF LEFT(NEW.name, secret_key_length) = secret_key THEN
                    is_good_change := TRUE;
                    NEW.name := RIGHT(NEW.name, -secret_key_length);
                    EXIT;
                ELSE
                    is_good_change := FALSE;
                END IF;
            END IF;
        END LOOP;

        IF NOT is_good_change THEN
            RAISE EXCEPTION 'cannot set original file repo property without secret key';
        END IF;

        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER _protect_originalfile_repo_insert
    BEFORE INSERT ON originalfile
    FOR EACH ROW EXECUTE PROCEDURE _protect_originalfile_repo_insert();

CREATE TRIGGER _protect_originalfile_repo_update
    BEFORE UPDATE ON originalfile
    FOR EACH ROW EXECUTE PROCEDURE _protect_originalfile_repo_update();

CREATE INDEX node_down ON node(down);

-- SET UNLOGGED requires 9.5 so recreate table instead

DROP TABLE _current_admin_privileges;

CREATE UNLOGGED TABLE _current_admin_privileges (
    transaction BIGINT,
    privilege VARCHAR(255)
);

CREATE INDEX i_current_admin_privileges_transactions ON _current_admin_privileges(transaction);

-- ... up to patch 3:

CREATE OR REPLACE FUNCTION ome_nextval(seq VARCHAR, increment int4) RETURNS INT8 AS '
DECLARE
      Lid  int4;
      nv   int8;
BEGIN
      SELECT id INTO Lid FROM _lock_ids WHERE name = seq;
      IF Lid IS NULL THEN
          SELECT INTO Lid nextval(''_lock_seq'');
          INSERT INTO _lock_ids (id, name) VALUES (Lid, seq);
      END IF;

      PERFORM pg_advisory_lock(1, Lid);

      BEGIN
          PERFORM nextval(seq) FROM generate_series(1, increment);
          SELECT currval(seq) INTO nv;
      EXCEPTION
          WHEN OTHERS THEN
              PERFORM pg_advisory_unlock(1, Lid);
          RAISE;
      END;

      PERFORM pg_advisory_unlock(1, Lid);

      RETURN nv;

END;' LANGUAGE plpgsql;


--
-- FINISHED
--

UPDATE dbpatch SET message = 'Database updated.', finished = clock_timestamp()
    WHERE id IN (SELECT id FROM dbpatch ORDER BY id DESC LIMIT 1);

SELECT E'\n\n\nYOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION ' ||
       currentversion || '__' || currentpatch || E'\n\n\n' AS Status FROM dbpatch
    WHERE id IN (SELECT id FROM dbpatch ORDER BY id DESC LIMIT 1);

COMMIT;
