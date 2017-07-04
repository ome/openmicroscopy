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
--- OMERO5 development release upgrade from OMERO5.4DEV__0 to OMERO5.4DEV__1.
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

SELECT omero_assert_db_version('OMERO5.4DEV', 0);
DROP FUNCTION omero_assert_db_version(varchar, int);


--
-- Actual upgrade
--

INSERT INTO dbpatch (currentVersion, currentPatch, previousVersion, previousPatch)
             VALUES ('OMERO5.4DEV',  1,            'OMERO5.4DEV',   0);

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


--
-- FINISHED
--

UPDATE dbpatch SET message = 'Database updated.', finished = clock_timestamp()
    WHERE id IN (SELECT id FROM dbpatch ORDER BY id DESC LIMIT 1);

SELECT E'\n\n\nYOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION ' ||
       currentversion || '__' || currentpatch || E'\n\n\n' AS Status FROM dbpatch
    WHERE id IN (SELECT id FROM dbpatch ORDER BY id DESC LIMIT 1);

COMMIT;
