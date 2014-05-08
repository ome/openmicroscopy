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
--- OMERO5 development release upgrade from OMERO5.1DEV__5 to OMERO5.1DEV__6.
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

SELECT omero_assert_db_version('OMERO5.1DEV', 5);
DROP FUNCTION omero_assert_db_version(varchar, int);


INSERT INTO dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
             VALUES ('OMERO5.1DEV',     6,              'OMERO5.1DEV',       5);

--
-- Actual upgrade
--

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
          previousVersion = 'OMERO5.1DEV' AND
          previousPatch   = 5;

SELECT CHR(10)||CHR(10)||CHR(10)||'YOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION OMERO5.1DEV__6'||CHR(10)||CHR(10)||CHR(10) AS Status;

COMMIT;
