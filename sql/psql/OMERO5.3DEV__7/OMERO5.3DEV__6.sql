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
--- OMERO5 development release upgrade from OMERO5.3DEV__6 to OMERO5.3DEV__7.
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

SELECT omero_assert_db_version('OMERO5.3DEV', 6);
DROP FUNCTION omero_assert_db_version(varchar, int);


--
-- Actual upgrade
--

INSERT INTO dbpatch (currentVersion, currentPatch, previousVersion, previousPatch)
             VALUES ('OMERO5.3DEV',  7,            'OMERO5.3DEV',   6);

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
    "start" nonnegative_int,
    "end" nonnegative_int,
    version INTEGER,
    creation_id BIGINT NOT NULL,
    external_id BIGINT UNIQUE,
    group_id BIGINT NOT NULL,
    owner_id BIGINT NOT NULL,
    update_id BIGINT NOT NULL,
    renderingdef BIGINT,
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


--
-- FINISHED
--

UPDATE dbpatch SET message = 'Database updated.', finished = clock_timestamp()
    WHERE currentVersion  = 'OMERO5.3DEV' AND
          currentPatch    = 7             AND
          previousVersion = 'OMERO5.3DEV' AND
          previousPatch   = 6;

SELECT CHR(10)||CHR(10)||CHR(10)||'YOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION OMERO5.3DEV__7'||CHR(10)||CHR(10)||CHR(10) AS Status;

COMMIT;
