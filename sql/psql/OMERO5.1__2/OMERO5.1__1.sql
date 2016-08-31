-- Copyright (C) 2014 Glencoe Software, Inc. All rights reserved.
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
--- OMERO5 development release upgrade from OMERO5.1__1 to OMERO5.1__2.
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

SELECT omero_assert_db_version('OMERO5.1', 1);
DROP FUNCTION omero_assert_db_version(varchar, int);

INSERT INTO dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
             VALUES ('OMERO5.1',     2,              'OMERO5.1',          1);

--
-- Actual upgrade
--

create table alias (
    id int8 not null,
    permissions int8 not null,
    name varchar(255) unique,
    ns varchar(255),
    origin text not null,
    referrer varchar(255),
    version int4,
    external_id int8 unique,
    primary key (id)
);;

create table aliasannotationlink (
    id int8 not null,
    permissions int8 not null,
    version int4,
    child int8 not null,
    creation_id int8 not null,
    external_id int8 unique,
    group_id int8 not null,
    owner_id int8 not null,
    update_id int8 not null,
    parent int8 not null,
    primary key (id),
    unique (parent, child, owner_id)
);;

create table count_Alias_annotationLinks_by_owner (
    Alias_id int8 not null,
    count int8 not null,
    owner_id int8 not null,
    primary key (Alias_id, owner_id)
);;

alter table alias 
    add constraint FKalias_external_id_externalinfo 
    foreign key (external_id) 
    references externalinfo  ;;

alter table aliasannotationlink 
    add constraint FKaliasannotationlink_update_id_event 
    foreign key (update_id) 
    references event  ;;

alter table aliasannotationlink 
    add constraint FKaliasannotationlink_parent_alias 
    foreign key (parent) 
    references alias  ;;

alter table aliasannotationlink 
    add constraint FKaliasannotationlink_group_id_experimentergroup 
    foreign key (group_id) 
    references experimentergroup  ;;

alter table aliasannotationlink 
    add constraint FKaliasannotationlink_owner_id_experimenter 
    foreign key (owner_id) 
    references experimenter  ;;

alter table aliasannotationlink 
    add constraint FKaliasannotationlink_external_id_externalinfo 
    foreign key (external_id) 
    references externalinfo  ;;

alter table aliasannotationlink 
    add constraint FKaliasannotationlink_creation_id_event 
    foreign key (creation_id) 
    references event  ;;

alter table aliasannotationlink 
    add constraint FKaliasannotationlink_child_annotation 
    foreign key (child) 
    references annotation  ;;

alter table count_Alias_annotationLinks_by_owner 
    add constraint FK_count_to_Alias_annotationLinks 
    foreign key (Alias_id) 
    references alias  ;;

--
-- Indexes
--
CREATE INDEX i_aliasannotationlink_owner ON aliasannotationlink(owner_id);
CREATE INDEX i_aliasannotationlink_group ON aliasannotationlink(group_id);
CREATE INDEX i_AliasAnnotationLink_parent ON aliasannotationlink(parent);
CREATE INDEX i_AliasAnnotationLink_child ON aliasannotationlink(child);

CREATE SEQUENCE seq_alias; INSERT INTO _lock_ids (name, id) SELECT 'seq_alias', nextval('_lock_seq');
CREATE SEQUENCE seq_aliasannotationlink; INSERT INTO _lock_ids (name, id) SELECT 'seq_aliasannotationlink', nextval('_lock_seq');

CREATE OR REPLACE FUNCTION annotation_update_event_trigger() RETURNS TRIGGER AS $$

  DECLARE
      pid BIGINT;
      eid BIGINT;

  BEGIN
      SELECT INTO eid _current_or_new_event();

      FOR pid IN SELECT DISTINCT parent FROM aliasannotationlink WHERE child = new.id
      LOOP
          INSERT INTO _updated_annotations (event_id, entity_type, entity_id)
              SELECT eid, 'ome.model.meta.Alias', pid
              WHERE NOT EXISTS (SELECT 1 FROM _updated_annotations AS ua
                  WHERE ua.event_id = eid AND ua.entity_type = 'ome.model.meta.Alias' AND ua.entity_id = pid);
      END LOOP;

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

CREATE TRIGGER alias_annotation_link_event_trigger
        AFTER UPDATE ON aliasannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.meta.Alias');
CREATE TRIGGER alias_annotation_link_event_trigger_insert
        AFTER INSERT ON aliasannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.meta.Alias');

CREATE TRIGGER alias_annotation_link_delete_trigger
        BEFORE DELETE ON aliasannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_delete_trigger('ome.model.meta.Alias');

--
-- FINISHED
--

UPDATE dbpatch SET message = 'Database updated.', finished = clock_timestamp()
    WHERE currentVersion  = 'OMERO5.1'    AND
          currentPatch    = 2             AND
          previousVersion = 'OMERO5.1'    AND
          previousPatch   = 1;

SELECT CHR(10)||CHR(10)||CHR(10)||'YOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION OMERO5.1__2'||CHR(10)||CHR(10)||CHR(10) AS Status;

COMMIT;
