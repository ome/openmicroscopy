--
-- Copyright 2009 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--


--
-- OMERO-Beta4.2 release
--
--
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

SELECT omero_assert_db_version('OMERO4.1',0);
DROP FUNCTION omero_assert_db_version(varchar, int);

INSERT into dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
             values ('OMERO4.2',     0,              'OMERO4.1',          0);


----
--
-- #1176 : create our own nextval() functionality for more consistent
-- sequence operation in hibernate. This functionality was updated for
-- OMERO 4.2 (#2508) in order to prevent logging during triggers.
--

CREATE OR REPLACE FUNCTION upgrade_sequence(seqname VARCHAR) RETURNS void
    AS '
    BEGIN

        EXECUTE ''CREATE SEQUENCE '' || seqname;
        PERFORM next_val FROM seq_table WHERE sequence_name = seqname;
        IF FOUND THEN
            PERFORM SETVAL(seqname, next_val) FROM seq_table WHERE sequence_name = seqname;
        ELSE
            INSERT INTO seq_table (sequence_name, next_val) VALUES (seqname, 1);
        END IF;

    END;'
LANGUAGE plpgsql;

SELECT upgrade_sequence('seq_wellsampleannotationlink');
SELECT upgrade_sequence('seq_wellannotationlink');
SELECT upgrade_sequence('seq_filtertype');
SELECT upgrade_sequence('seq_dataset');
SELECT upgrade_sequence('seq_plate');
SELECT upgrade_sequence('seq_thumbnail');
SELECT upgrade_sequence('seq_immersion');
SELECT upgrade_sequence('seq_channel');
SELECT upgrade_sequence('seq_imageannotationlink');
SELECT upgrade_sequence('seq_link');
SELECT upgrade_sequence('seq_lightpathemissionfilterlink');
SELECT upgrade_sequence('seq_arctype');
SELECT upgrade_sequence('seq_experimenttype');
SELECT upgrade_sequence('seq_filtersetemissionfilterlink');
SELECT upgrade_sequence('seq_filtersetexcitationfilterlink');
SELECT upgrade_sequence('seq_microscope');
SELECT upgrade_sequence('seq_originalfileannotationlink');
SELECT upgrade_sequence('seq_wellsample');
SELECT upgrade_sequence('seq_planeinfo');
SELECT upgrade_sequence('seq_lightpathexcitationfilterlink');
SELECT upgrade_sequence('seq_groupexperimentermap');
SELECT upgrade_sequence('seq_planeinfoannotationlink');
SELECT upgrade_sequence('seq_transmittancerange');
SELECT upgrade_sequence('seq_wellreagentlink');
SELECT upgrade_sequence('seq_eventlog');
SELECT upgrade_sequence('seq_quantumdef');
SELECT upgrade_sequence('seq_namespace');
SELECT upgrade_sequence('seq_image');
SELECT upgrade_sequence('seq_renderingmodel');
SELECT upgrade_sequence('seq_microbeammanipulation');
SELECT upgrade_sequence('seq_joboriginalfilelink');
SELECT upgrade_sequence('seq_experimentergroup');
SELECT upgrade_sequence('seq_renderingdef');
SELECT upgrade_sequence('seq_datasetimagelink');
SELECT upgrade_sequence('seq_codomainmapcontext');
SELECT upgrade_sequence('seq_eventtype');
SELECT upgrade_sequence('seq_project');
SELECT upgrade_sequence('seq_microscopetype');
SELECT upgrade_sequence('seq_channelannotationlink');
SELECT upgrade_sequence('seq_filamenttype');
SELECT upgrade_sequence('seq_stagelabel');
SELECT upgrade_sequence('seq_photometricinterpretation');
SELECT upgrade_sequence('seq_experimentergroupannotationlink');
SELECT upgrade_sequence('seq_pixels');
SELECT upgrade_sequence('seq_lightpath');
SELECT upgrade_sequence('seq_roi');
SELECT upgrade_sequence('seq_roiannotationlink');
SELECT upgrade_sequence('seq_externalinfo');
SELECT upgrade_sequence('seq_annotationannotationlink');
SELECT upgrade_sequence('seq_objectivesettings');
SELECT upgrade_sequence('seq_lasertype');
SELECT upgrade_sequence('seq_nodeannotationlink');
SELECT upgrade_sequence('seq_dimensionorder');
SELECT upgrade_sequence('seq_binning');
SELECT upgrade_sequence('seq_instrument');
SELECT upgrade_sequence('seq_namespaceannotationlink');
SELECT upgrade_sequence('seq_well');
SELECT upgrade_sequence('seq_family');
SELECT upgrade_sequence('seq_imagingenvironment');
SELECT upgrade_sequence('seq_illumination');
SELECT upgrade_sequence('seq_projectannotationlink');
SELECT upgrade_sequence('seq_detectortype');
SELECT upgrade_sequence('seq_reagent');
SELECT upgrade_sequence('seq_pulse');
SELECT upgrade_sequence('seq_detector');
SELECT upgrade_sequence('seq_otf');
SELECT upgrade_sequence('seq_reagentannotationlink');
SELECT upgrade_sequence('seq_lightsettings');
SELECT upgrade_sequence('seq_originalfile');
SELECT upgrade_sequence('seq_lightsource');
SELECT upgrade_sequence('seq_annotation');
SELECT upgrade_sequence('seq_job');
SELECT upgrade_sequence('seq_sharemember');
SELECT upgrade_sequence('seq_dbpatch');
SELECT upgrade_sequence('seq_filterset');
SELECT upgrade_sequence('seq_projectdatasetlink');
SELECT upgrade_sequence('seq_plateannotationlink');
SELECT upgrade_sequence('seq_experimenterannotationlink');
SELECT upgrade_sequence('seq_channelbinding');
SELECT upgrade_sequence('seq_microbeammanipulationtype');
SELECT upgrade_sequence('seq_medium');
SELECT upgrade_sequence('seq_statsinfo');
SELECT upgrade_sequence('seq_lasermedium');
SELECT upgrade_sequence('seq_pixelstype');
SELECT upgrade_sequence('seq_screen');
SELECT upgrade_sequence('seq_dichroic');
SELECT upgrade_sequence('seq_session');
SELECT upgrade_sequence('seq_plateacquisition');
SELECT upgrade_sequence('seq_screenannotationlink');
SELECT upgrade_sequence('seq_format');
SELECT upgrade_sequence('seq_node');
SELECT upgrade_sequence('seq_pixelsannotationlink');
SELECT upgrade_sequence('seq_objective');
SELECT upgrade_sequence('seq_datasetannotationlink');
SELECT upgrade_sequence('seq_experiment');
SELECT upgrade_sequence('seq_detectorsettings');
SELECT upgrade_sequence('seq_correction');
SELECT upgrade_sequence('seq_filter');
SELECT upgrade_sequence('seq_plateacquisitionannotationlink');
SELECT upgrade_sequence('seq_pixelsoriginalfilemap');
SELECT upgrade_sequence('seq_logicalchannel');
SELECT upgrade_sequence('seq_sessionannotationlink');
SELECT upgrade_sequence('seq_screenplatelink');
SELECT upgrade_sequence('seq_shape');
SELECT upgrade_sequence('seq_experimenter');
SELECT upgrade_sequence('seq_acquisitionmode');
SELECT upgrade_sequence('seq_event');
SELECT upgrade_sequence('seq_jobstatus');
SELECT upgrade_sequence('seq_contrastmethod');
DROP FUNCTION upgrade_sequence(VARCHAR);

CREATE OR REPLACE FUNCTION ome_nextval(seq VARCHAR) RETURNS INT8 AS '
BEGIN
      RETURN ome_nextval(seq, 1);
END;' LANGUAGE plpgsql;

-- These renamings allow us to reuse the Hibernate-generated tables
-- for sequence generation. Eventually, a method might be found to
-- make Hibernate generate them for us.
CREATE SEQUENCE _lock_seq;
ALTER TABLE seq_table RENAME TO _lock_ids;
ALTER TABLE _lock_ids RENAME COLUMN sequence_name TO name;
ALTER TABLE _lock_ids DROP CONSTRAINT seq_table_pkey;
ALTER TABLE _lock_ids DROP COLUMN next_val;
ALTER TABLE _lock_ids ADD COLUMN id int PRIMARY KEY DEFAULT nextval('_lock_seq');
CREATE UNIQUE INDEX _lock_ids_name ON _lock_ids (name);

CREATE OR REPLACE FUNCTION ome_nextval(seq VARCHAR, increment int4) RETURNS INT8 AS '
DECLARE
      Lid  int4;
      nv   int8;
      sql  varchar;
BEGIN
      SELECT id INTO Lid FROM _lock_ids WHERE name = seq;
      IF Lid IS NULL THEN
          SELECT INTO Lid nextval(''_lock_seq'');
          INSERT INTO _lock_ids (id, name) VALUES (Lid, seq);
      END IF;

      PERFORM pg_advisory_lock(1, Lid);
      PERFORM nextval(seq) FROM generate_series(1, increment);
      SELECT currval(seq) INTO nv;
      PERFORM pg_advisory_unlock(1, Lid);

      RETURN nv;

END;' LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION ome_nextval(seq unknown) RETURNS INT8 AS '
DECLARE
  nv int8;
BEGIN
    SELECT ome_nextval(seq::text) INTO nv;
    RETURN nv;
END;' LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION ome_nextval(seq unknown, increment int4) RETURNS INT8 AS '
DECLARE
  nv int8;
BEGIN
    SELECT ome_nextval(seq::text, increment) INTO nv;
    RETURN nv;
END;' LANGUAGE plpgsql;

-- Replace the one table which had a default of nextval
ALTER TABLE dbpatch ALTER COLUMN id SET DEFAULT ome_nextval('seq_dbpatch');

----
--
-- Add new SPW data structures
--

CREATE TABLE plateacquisitionannotationlink (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	version integer,
	child bigint NOT NULL,
	creation_id bigint NOT NULL,
	external_id bigint,
	group_id bigint NOT NULL,
	owner_id bigint NOT NULL,
	update_id bigint NOT NULL,
	parent bigint NOT NULL
);

CREATE TABLE plateacquisition (
	id bigint NOT NULL,
	description text,
	permissions bigint NOT NULL,
	maximumfieldcount integer,
	name character varying(255) NOT NULL,
        starttime timestamp without time zone,
        endtime timestamp without time zone,
        plate bigint NOT NULL,
	version integer,
	creation_id bigint NOT NULL,
	external_id bigint,
	group_id bigint NOT NULL,
	owner_id bigint NOT NULL,
	update_id bigint NOT NULL
);

ALTER TABLE plateacquisition
	ADD CONSTRAINT plateacquisition_pkey PRIMARY KEY (id);

ALTER TABLE plate
	ADD COLUMN cols integer,
	ADD COLUMN rows integer;

ALTER TABLE wellsample ADD COLUMN plateacquisition bigint;
ALTER TABLE wellsample ADD COLUMN new_timepoint timestamp without time zone;
UPDATE wellsample SET new_timepoint = null; -- TODO fixme
ALTER TABLE wellsample DROP COLUMN timepoint;
ALTER TABLE wellsample RENAME COLUMN new_timepoint to timepoint;
ALTER TABLE wellsample
	ADD CONSTRAINT fkwellsample_plateacquisition_plateacquisition
        FOREIGN KEY (plateacquisition) REFERENCES plateacquisition(id);

CREATE OR REPLACE FUNCTION omero_convert_spw_42() RETURNS void AS '
DECLARE
    rec RECORD;
BEGIN

--TODO
--POSSIBLY USE RENAME ON TABLE
--reuse screen acquisition ids and update the seq_table ??
--DO MOD
    SELECT INTO rec *
           FROM screenacquisition;

END;' LANGUAGE plpgsql;
SELECT omero_convert_spw_42();
DROP FUNCTION omero_convert_spw_42();

----
--
-- Remove old SPW data structures
--
DROP VIEW count_screenacquisition_annotationlinks_by_owner;
DROP VIEW count_wellsample_screenacquisitionlinks_by_owner;
DROP VIEW count_screenacquisition_wellsamplelinks_by_owner;
DROP TABLE screenacquisitionannotationlink;
DROP TABLE screenacquisitionwellsamplelink;
DROP TABLE screenacquisition;


----
--
-- Add new instrument data structures
--

ALTER TABLE filter
	ADD COLUMN filterset bigint;

ALTER TABLE logicalchannel
	ADD COLUMN lightpath bigint;

CREATE TABLE lightpathemissionfilterlink (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	version integer,
	child bigint NOT NULL,
	creation_id bigint NOT NULL,
	external_id bigint,
	group_id bigint NOT NULL,
	owner_id bigint NOT NULL,
	update_id bigint NOT NULL,
	parent bigint NOT NULL
);

CREATE TABLE lightpathexcitationfilterlink (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	version integer,
	child bigint NOT NULL,
	creation_id bigint NOT NULL,
	external_id bigint,
	group_id bigint NOT NULL,
	owner_id bigint NOT NULL,
	update_id bigint NOT NULL,
	parent bigint NOT NULL,
	parent_index integer NOT NULL
);

    create table filtersetemissionfilterlink (
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

    create table filtersetexcitationfilterlink (
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

CREATE TABLE lightpath (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	version integer,
	creation_id bigint NOT NULL,
	external_id bigint,
	group_id bigint NOT NULL,
	owner_id bigint NOT NULL,
	update_id bigint NOT NULL,
	dichroic bigint
);

  CREATE OR REPLACE FUNCTION lightpathexcitationfilterlink_parent_index_move() RETURNS "trigger" AS '
    DECLARE
      duplicate INT8;
    BEGIN

      -- Avoids a query if the new and old values of x are the same.
      IF new.parent = old.parent AND new.parent_index = old.parent_index THEN
          RETURN new;
      END IF;

      -- At most, there should be one duplicate
      SELECT id INTO duplicate
        FROM lightpathexcitationfilterlink
       WHERE parent = new.parent AND parent_index = new.parent_index
      OFFSET 0
       LIMIT 1;

      IF duplicate IS NOT NULL THEN
          RAISE NOTICE ''Remapping lightpathexcitationfilterlink % via (-1 - oldvalue )'', duplicate;
          UPDATE lightpathexcitationfilterlink SET parent_index = -1 - parent_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;' LANGUAGE plpgsql;

  CREATE TRIGGER lightpathexcitationfilterlink_parent_index_trigger
        BEFORE UPDATE ON lightpathexcitationfilterlink
        FOR EACH ROW EXECUTE PROCEDURE lightpathexcitationfilterlink_parent_index_move ();

----
--
-- Convert old instrument data
--


--TODO GATHER FILTERS


----
--
-- Remove old instrument data structures
--

ALTER TABLE logicalchannel
	DROP COLUMN secondaryemissionfilter,
	DROP COLUMN secondaryexcitationfilter;


----
--
-- New system types
--

CREATE table namespace (
        id int8 not null,
        description text,
        permissions int8 not null,
        display bool,
        keywords text[],
        multivalued bool,
        name varchar(255) not null,
        version int4,
        creation_id int8 not null,
        external_id int8,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null
    );;

CREATE TABLE namespaceannotationlink (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	version integer,
	child bigint NOT NULL,
	creation_id bigint NOT NULL,
	external_id bigint,
	group_id bigint NOT NULL,
	owner_id bigint NOT NULL,
	update_id bigint NOT NULL,
	parent bigint NOT NULL
);

CREATE TABLE parsejob (
	params bytea,
	job_id bigint NOT NULL
);


ALTER TABLE session
	DROP COLUMN defaultpermissions;


CREATE OR REPLACE FUNCTION upgrade_original_metadata_txt() RETURNS void AS '
DECLARE
    rec RECORD;
BEGIN

    FOR rec IN SELECT o.id as file, o.path, o.name, i.id as image FROM originalfile o, image i, imageannotationlink l, annotation a
                WHERE o.id = a.file AND a.id = l.child AND l.parent = i.id
                  AND o.path LIKE ''%tmp%omero_%metadata%.txt'' AND o.name LIKE ''original_metadata.txt''
                  AND a.ns = ''openmicroscopy.org/omero/import/companionFile'' LOOP

        -- An original_metadata.txt should not be attached to multiple images
        IF substring(rec.path from 1 for 16) == ''/imported_image/'' THEN
            RAISE EXCEPTION ''Already modified! Image:%'', rec.image;
        END IF;

        UPDATE originalfile SET path = ''/imported_image/''||rec.image||''/'' WHERE id = rec.file;
        -- TODO needs work
    END LOOP;

END;' LANGUAGE plpgsql;

SELECT upgrade_original_metadata_txt();
DROP FUNCTION upgrade_original_metadata_txt();


alter  table originalfile drop column url;
alter  table originalfile alter column path TYPE text;
alter  table originalfile add column mimetype varchar(255) default 'application/octet-stream';
update originalfile set mimetype = fmt.value from Format fmt where format = fmt.id;
alter  table originalfile drop column format;

alter  table originalfile add column repo varchar(36);
alter  table originalfile add column params text[2][];
create index originalfile_mime_index on originalfile (mimetype);
create index originalfile_repo_index on originalfile (repo);
create unique index originalfile_repo_path_index on originalfile (repo, path, name) where repo is not null;

ALTER TABLE pixels
	DROP COLUMN url,
	ADD COLUMN path text,
	ADD COLUMN name text,
	ADD COLUMN repo character varying(36),
	ADD COLUMN params text[];

CREATE INDEX pixels_repo_index ON pixels USING btree (repo);

ALTER TABLE thumbnail DROP COLUMN url;

----
--
-- Modify system types
--


----
--
-- Remove old system types
--

DROP VIEW count_experimentergroup_groupexperimentermap_by_owner;
DROP VIEW count_experimenter_groupexperimentermap_by_owner;

ALTER TABLE groupexperimentermap ADD COLUMN owner boolean;
CREATE OR REPLACE FUNCTION upgrade_group_owners() RETURNS void AS '
DECLARE
    rec RECORD;
BEGIN

    FOR rec IN SELECT o.id as file, o.path, o.name, i.id as image FROM originalfile o, image i, imageannotationlink l, annotation a
                WHERE o.id = a.file AND a.id = l.child AND l.parent = i.id
                  AND o.path LIKE ''%tmp%omero_%metadata%.txt'' AND o.name LIKE ''original_metadata.txt''
                  AND a.ns = ''openmicroscopy.org/omero/import/companionFile'' LOOP

        -- An original_metadata.txt should not be attached to multiple images
        IF substring(rec.path from 1 for 16) == ''/imported_image/'' THEN
            RAISE EXCEPTION ''Already modified! Image:%'', rec.image;
        END IF;

        UPDATE originalfile SET path = ''/imported_image/''||rec.image||''/'' WHERE id = rec.file;
        -- TODO needs work
    END LOOP;

END;' LANGUAGE plpgsql;

SELECT upgrade_group_owners();
DROP FUNCTION upgrade_group_owners();

-- TODO for every owner, if there's no link, insert one with true.
-- if there is one, set true.
-- then set all others to false.
UPDATE groupexperimentermap SET owner = FALSE WHERE owner IS NULL;
ALTER TABLE groupexperimentermap
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id,
	DROP COLUMN update_id;

ALTER TABLE groupexperimentermap ALTER COLUMN owner SET NOT NULL;
----
--
-- ROI modifications
--

ALTER TABLE roi
	ADD COLUMN keywords text[],
	ADD COLUMN namespaces text[];

ALTER TABLE shape
	ADD COLUMN thec integer,
	ALTER COLUMN points TYPE text,
	ALTER COLUMN d TYPE text;

-- r7154
ALTER TABLE shape ADD COLUMN new_fillcolor integer;
ALTER TABLE shape ADD COLUMN new_strokecolor integer;
-- TODO parse colors
ALTER TABLE shape DROP COLUMN fillopacity;
ALTER TABLE shape DROP COLUMN strokeopacity;
ALTER TABLE shape DROP COLUMN fillcolor;
ALTER TABLE shape DROP COLUMN strokecolor;

ALTER TABLE shape RENAME COLUMN new_fillcolor to fillcolor;
ALTER TABLE shape RENAME COLUMN new_strokecolor to strokecolor;

-- TODO FIX DISCRIMINATOR HERE? OR JUST LEAVE IT

----
--
-- Make all enumerations system types
--

ALTER TABLE acquisitionmode
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE arctype
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE binning
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE contrastmethod
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE correction
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE detectortype
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE dimensionorder
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE eventtype
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE experimentergroup
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id,
	DROP COLUMN update_id;

ALTER TABLE experimenttype
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE family
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE filamenttype
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE filterset
	DROP COLUMN emfilter,
	DROP COLUMN exfilter;

ALTER TABLE filtertype
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE format
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE illumination
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE image
	ADD COLUMN partial boolean,
	ADD COLUMN format bigint;

ALTER TABLE immersion
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE jobstatus
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE lasermedium
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE lasertype
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE medium
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE microbeammanipulationtype
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE microscopetype
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE photometricinterpretation
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE pixelstype
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE pulse
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

ALTER TABLE renderingmodel
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id;

--
-- Enumeration values
--
insert into acquisitionmode (id,permissions,value)
    select ome_nextval('seq_acquisitionmode'),-35,'FSM';
insert into acquisitionmode (id,permissions,value)
    select ome_nextval('seq_acquisitionmode'),-35,'PALM';
insert into acquisitionmode (id,permissions,value)
    select ome_nextval('seq_acquisitionmode'),-35,'STED';
insert into acquisitionmode (id,permissions,value)
    select ome_nextval('seq_acquisitionmode'),-35,'STORM';
insert into acquisitionmode (id,permissions,value)
    select ome_nextval('seq_acquisitionmode'),-35,'TIRF';
insert into acquisitionmode (id,permissions,value)
    select ome_nextval('seq_acquisitionmode'),-35,'LaserScanningConfocalMicroscopy';

-- TODO find all the other two and update them
delete from acquisitionmode where value in ('LaserScanningConfocal', 'LaserScanningMicroscopy');

insert into detectortype (id,permissions,value)
    select ome_nextval('seq_detectortype'),-35,'EBCCD';

insert into filtertype (id,permissions,value)
    select ome_nextval('seq_filtertype'),-35,'Dichroic';
insert into filtertype (id,permissions,value)
    select ome_nextval('seq_filtertype'),-35,'NeutralDensity';

insert into microbeammanipulationtype (id,permissions,value)
    select ome_nextval('seq_microbeammanipulationtype'),-35,'FLIP';
insert into microbeammanipulationtype (id,permissions,value)
    select ome_nextval('seq_microbeammanipulationtype'),-35,'InverseFRAP';

-- TODO decide whether or not to fix Format

----
--
-- Activate constraints
--

ALTER TABLE lightpathemissionfilterlink
	ADD CONSTRAINT lightpathemissionfilterlink_pkey PRIMARY KEY (id);

ALTER TABLE lightpathexcitationfilterlink
	ADD CONSTRAINT lightpathexcitationfilterlink_pkey PRIMARY KEY (id);

ALTER TABLE plateacquisitionannotationlink
	ADD CONSTRAINT plateacquisitionannotationlink_pkey PRIMARY KEY (id);

ALTER TABLE lightpath
	ADD CONSTRAINT lightpath_pkey PRIMARY KEY (id);

ALTER TABLE namespace
	ADD CONSTRAINT namespace_pkey PRIMARY KEY (id);

ALTER TABLE namespaceannotationlink
	ADD CONSTRAINT namespaceannotationlink_pkey PRIMARY KEY (id);

ALTER TABLE parsejob
	ADD CONSTRAINT parsejob_pkey PRIMARY KEY (job_id);

ALTER TABLE annotationannotationlink
	DROP CONSTRAINT fkannotationannotationlink_child_annotation;

ALTER TABLE annotationannotationlink
	DROP CONSTRAINT fkannotationannotationlink_parent_annotation;

ALTER TABLE annotationannotationlink
	ADD CONSTRAINT fkannotationannotationlink_child_child FOREIGN KEY (child) REFERENCES annotation(id);

ALTER TABLE annotationannotationlink
	ADD CONSTRAINT fkannotationannotationlink_parent_parent FOREIGN KEY (parent) REFERENCES annotation(id);

ALTER TABLE lightpathemissionfilterlink
	ADD CONSTRAINT lightpathemissionfilterlink_external_id_key UNIQUE (external_id);

ALTER TABLE lightpathemissionfilterlink
	ADD CONSTRAINT lightpathemissionfilterlink_parent_key UNIQUE (parent, child);

ALTER TABLE lightpathemissionfilterlink
	ADD CONSTRAINT fklightpathemissionfilterlink_child_filter FOREIGN KEY (child) REFERENCES filter(id);

ALTER TABLE lightpathemissionfilterlink
	ADD CONSTRAINT fklightpathemissionfilterlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);

ALTER TABLE lightpathemissionfilterlink
	ADD CONSTRAINT fklightpathemissionfilterlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE lightpathemissionfilterlink
	ADD CONSTRAINT fklightpathemissionfilterlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);

ALTER TABLE lightpathemissionfilterlink
	ADD CONSTRAINT fklightpathemissionfilterlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);

ALTER TABLE lightpathemissionfilterlink
	ADD CONSTRAINT fklightpathemissionfilterlink_parent_lightpath FOREIGN KEY (parent) REFERENCES lightpath(id);

ALTER TABLE lightpathemissionfilterlink
	ADD CONSTRAINT fklightpathemissionfilterlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE lightpathexcitationfilterlink
	ADD CONSTRAINT lightpathexcitationfilterlink_external_id_key UNIQUE (external_id);

ALTER TABLE lightpathexcitationfilterlink
	ADD CONSTRAINT lightpathexcitationfilterlink_parent_key UNIQUE (parent, parent_index);

ALTER TABLE lightpathexcitationfilterlink
	ADD CONSTRAINT lightpathexcitationfilterlink_parent_key1 UNIQUE (parent, child);

ALTER TABLE lightpathexcitationfilterlink
	ADD CONSTRAINT fklightpathexcitationfilterlink_child_filter FOREIGN KEY (child) REFERENCES filter(id);

ALTER TABLE lightpathexcitationfilterlink
	ADD CONSTRAINT fklightpathexcitationfilterlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);

ALTER TABLE lightpathexcitationfilterlink
	ADD CONSTRAINT fklightpathexcitationfilterlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE lightpathexcitationfilterlink
	ADD CONSTRAINT fklightpathexcitationfilterlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);

ALTER TABLE lightpathexcitationfilterlink
	ADD CONSTRAINT fklightpathexcitationfilterlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);

ALTER TABLE lightpathexcitationfilterlink
	ADD CONSTRAINT fklightpathexcitationfilterlink_parent_lightpath FOREIGN KEY (parent) REFERENCES lightpath(id);

ALTER TABLE lightpathexcitationfilterlink
	ADD CONSTRAINT fklightpathexcitationfilterlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE plateacquisitionannotationlink
	ADD CONSTRAINT plateacquisitionannotationlink_external_id_key UNIQUE (external_id);

ALTER TABLE plateacquisitionannotationlink
	ADD CONSTRAINT plateacquisitionannotationlink_parent_key UNIQUE (parent, child);

ALTER TABLE plateacquisitionannotationlink
	ADD CONSTRAINT fkplateacquisitionannotationlink_child_annotation FOREIGN KEY (child) REFERENCES annotation(id);

ALTER TABLE plateacquisitionannotationlink
	ADD CONSTRAINT fkplateacquisitionannotationlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);

ALTER TABLE plateacquisitionannotationlink
	ADD CONSTRAINT fkplateacquisitionannotationlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE plateacquisitionannotationlink
	ADD CONSTRAINT fkplateacquisitionannotationlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);

ALTER TABLE plateacquisitionannotationlink
	ADD CONSTRAINT fkplateacquisitionannotationlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);

ALTER TABLE plateacquisitionannotationlink
	ADD CONSTRAINT fkplateacquisitionannotationlink_parent_plateacquisition FOREIGN KEY (parent) REFERENCES plateacquisition(id);

ALTER TABLE plateacquisitionannotationlink
	ADD CONSTRAINT fkplateacquisitionannotationlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE filter
	ADD CONSTRAINT fkfilter_filterset_filterset FOREIGN KEY (filterset) REFERENCES filterset(id);

ALTER TABLE image
	ADD CONSTRAINT fkimage_format_format FOREIGN KEY (format) REFERENCES format(id);

ALTER TABLE lightpath
	ADD CONSTRAINT lightpath_external_id_key UNIQUE (external_id);

ALTER TABLE lightpath
	ADD CONSTRAINT fklightpath_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);

ALTER TABLE lightpath
	ADD CONSTRAINT fklightpath_dichroic_dichroic FOREIGN KEY (dichroic) REFERENCES dichroic(id);

ALTER TABLE lightpath
	ADD CONSTRAINT fklightpath_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE lightpath
	ADD CONSTRAINT fklightpath_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);

ALTER TABLE lightpath
	ADD CONSTRAINT fklightpath_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);

ALTER TABLE lightpath
	ADD CONSTRAINT fklightpath_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

-- Unneeded:
-- ALTER TABLE logicalchannel
--	DROP CONSTRAINT fklogicalchannel_secondaryemissionfilter_filter;
-- ALTER TABLE logicalchannel
--	DROP CONSTRAINT fklogicalchannel_secondaryexcitationfilter_filter;

ALTER TABLE logicalchannel
	ADD CONSTRAINT fklogicalchannel_lightpath_lightpath FOREIGN KEY (lightpath) REFERENCES lightpath(id);

ALTER TABLE namespace
	ADD CONSTRAINT namespace_external_id_key UNIQUE (external_id);

ALTER TABLE namespace
	ADD CONSTRAINT fknamespace_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE namespaceannotationlink
	ADD CONSTRAINT namespaceannotationlink_external_id_key UNIQUE (external_id);

ALTER TABLE namespaceannotationlink
	ADD CONSTRAINT namespaceannotationlink_parent_key UNIQUE (parent, child);

ALTER TABLE namespaceannotationlink
	ADD CONSTRAINT fknamespaceannotationlink_child_annotation FOREIGN KEY (child) REFERENCES annotation(id);

ALTER TABLE namespaceannotationlink
	ADD CONSTRAINT fknamespaceannotationlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);

ALTER TABLE namespaceannotationlink
	ADD CONSTRAINT fknamespaceannotationlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE namespaceannotationlink
	ADD CONSTRAINT fknamespaceannotationlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);

ALTER TABLE namespaceannotationlink
	ADD CONSTRAINT fknamespaceannotationlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);

ALTER TABLE namespaceannotationlink
	ADD CONSTRAINT fknamespaceannotationlink_parent_namespace FOREIGN KEY (parent) REFERENCES namespace(id);

ALTER TABLE namespaceannotationlink
	ADD CONSTRAINT fknamespaceannotationlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE parsejob
	ADD CONSTRAINT fkparsejob_job_id_job FOREIGN KEY (job_id) REFERENCES job(id);

ALTER TABLE plateacquisition
	ADD CONSTRAINT plateacquisition_external_id_key UNIQUE (external_id);

ALTER TABLE plateacquisition
	ADD CONSTRAINT fkplateacquisition_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);

ALTER TABLE plateacquisition
	ADD CONSTRAINT fkplateacquisition_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE plateacquisition
	ADD CONSTRAINT fkplateacquisition_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);

ALTER TABLE plateacquisition
	ADD CONSTRAINT fkplateacquisition_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);

ALTER TABLE plateacquisition
	ADD CONSTRAINT fkplateacquisition_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);


----
--
-- Unify Annotation types (#2354, r7000)
--

ALTER TABLE annotation
	DROP COLUMN thumbnail,
	ADD COLUMN termvalue text;

-- TODO
-- REMOVE uri, query, thumb (need test framework)
-- ADD term??
CREATE OR REPLACE FUNCTION upgrade_annotations_42() RETURNS void AS '
DECLARE
    rec RECORD;
BEGIN

    DELETE FROM annotation WHERE discriminator IN
        (''/basic/text/uri/'', ''/basic/text/query/'', ''/type/Thumbnail/'');

END;' LANGUAGE plpgsql;

SELECT upgrade_annotations_42();
DROP FUNCTION upgrade_annotations_42();

----
--
-- Fix shares with respect to group permissions (#1434, #2327, r6882)
--

ALTER TABLE share
	ADD COLUMN "group" bigint NOT NULL;

ALTER TABLE sharemember
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id,
	DROP COLUMN update_id;

-- TODO in test, make sure a share exists. the group needs to be updated with that from owner
-- and produce a warning.

----
--
-- #1390 Triggers for keeping the search index up-to-date.
--

CREATE OR REPLACE FUNCTION annotation_update_event_trigger() RETURNS "trigger"
    AS '
    DECLARE
        rec RECORD;
    BEGIN

        FOR rec IN SELECT id, parent FROM imageannotationlink WHERE child = new.id LOOP
            INSERT INTO eventlog (id, action, permissions, entityid, entitytype, event)
                 SELECT ome_nextval(''seq_eventlog''), ''REINDEX'', -35, rec.parent, ''ome.model.core.Image'', 0;
        END LOOP;

        RETURN new;

    END;'
LANGUAGE plpgsql;

CREATE TRIGGER annotation_trigger
        AFTER UPDATE ON annotation
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_update_event_trigger();


CREATE OR REPLACE FUNCTION annotation_link_event_trigger() RETURNS "trigger"
    AS '
    DECLARE
    BEGIN

        INSERT INTO eventlog (id, action, permissions, entityid, entitytype, event)
                SELECT ome_nextval(''seq_eventlog''), ''REINDEX'', -35, new.parent, ''ome.model.core.Image'', 0;

        RETURN new;

    END;'
LANGUAGE plpgsql;

CREATE TRIGGER image_annotation_link_event_trigger
        AFTER UPDATE ON imageannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger();
----
--
-- Create views needed by all other types
--

CREATE VIEW count_filterset_emissionfilterlink_by_owner AS
	SELECT filtersetemissionfilterlink.parent AS filterset_id, filtersetemissionfilterlink.owner_id, count(*) AS count FROM filtersetemissionfilterlink GROUP BY filtersetemissionfilterlink.parent, filtersetemissionfilterlink.owner_id ORDER BY filtersetemissionfilterlink.parent;

CREATE VIEW count_filterset_excitationfilterlink_by_owner AS
	SELECT filtersetexcitationfilterlink.parent AS filterset_id, filtersetexcitationfilterlink.owner_id, count(*) AS count FROM filtersetexcitationfilterlink GROUP BY filtersetexcitationfilterlink.parent, filtersetexcitationfilterlink.owner_id ORDER BY filtersetexcitationfilterlink.parent;

CREATE VIEW count_lightpath_emissionfilterlink_by_owner AS
	SELECT lightpathemissionfilterlink.parent AS lightpath_id, lightpathemissionfilterlink.owner_id, count(*) AS count FROM lightpathemissionfilterlink GROUP BY lightpathemissionfilterlink.parent, lightpathemissionfilterlink.owner_id ORDER BY lightpathemissionfilterlink.parent;

CREATE VIEW count_lightpath_excitationfilterlink_by_owner AS
	SELECT lightpathexcitationfilterlink.parent AS lightpath_id, lightpathexcitationfilterlink.owner_id, count(*) AS count FROM lightpathexcitationfilterlink GROUP BY lightpathexcitationfilterlink.parent, lightpathexcitationfilterlink.owner_id ORDER BY lightpathexcitationfilterlink.parent;

CREATE VIEW count_namespace_annotationlinks_by_owner AS
	SELECT namespaceannotationlink.parent AS namespace_id, namespaceannotationlink.owner_id, count(*) AS count FROM namespaceannotationlink GROUP BY namespaceannotationlink.parent, namespaceannotationlink.owner_id ORDER BY namespaceannotationlink.parent;

CREATE VIEW count_plateacquisition_annotationlinks_by_owner AS
	SELECT plateacquisitionannotationlink.parent AS plateacquisition_id, plateacquisitionannotationlink.owner_id, count(*) AS count FROM plateacquisitionannotationlink GROUP BY plateacquisitionannotationlink.parent, plateacquisitionannotationlink.owner_id ORDER BY plateacquisitionannotationlink.parent;

DROP VIEW count_experimenter_annotationlinks_by_owner;

DROP VIEW count_experimentergroup_annotationlinks_by_owner;

DROP VIEW count_node_annotationlinks_by_owner;

DROP VIEW count_session_annotationlinks_by_owner;

----
--
-- Other changes for group permissions
--
CREATE OR REPLACE FUNCTION ome_perms(p bigint) RETURNS character varying
    LANGUAGE plpgsql
    AS $$
DECLARE
    ur CHAR DEFAULT '-';
    uw CHAR DEFAULT '-';
    gr CHAR DEFAULT '-';
    gw CHAR DEFAULT '-';
    wr CHAR DEFAULT '-';
    ww CHAR DEFAULT '-';
BEGIN
    -- shift 8
    SELECT INTO ur CASE WHEN (cast(p as bit(64)) & cast(1024 as bit(64))) = cast(1024 as bit(64)) THEN 'r' ELSE '-' END;
    SELECT INTO uw CASE WHEN (cast(p as bit(64)) & cast( 512 as bit(64))) = cast( 512 as bit(64)) THEN 'w' ELSE '-' END;
    -- shift 4
    SELECT INTO gr CASE WHEN (cast(p as bit(64)) & cast(  64 as bit(64))) = cast(  64 as bit(64)) THEN 'r' ELSE '-' END;
    SELECT INTO gw CASE WHEN (cast(p as bit(64)) & cast(  32 as bit(64))) = cast(  32 as bit(64)) THEN 'w' ELSE '-' END;
    -- shift 0
    SELECT INTO wr CASE WHEN (cast(p as bit(64)) & cast(   4 as bit(64))) = cast(   4 as bit(64)) THEN 'r' ELSE '-' END;
    SELECT INTO ww CASE WHEN (cast(p as bit(64)) & cast(   2 as bit(64))) = cast(   2 as bit(64)) THEN 'w' ELSE '-' END;

    RETURN ur || uw || gr || gw || wr || ww;
END;$$;


----
--
-- MISC/UNTESTED TODO - why are these here?
--

ALTER TABLE annotationannotationlink
        DROP CONSTRAINT annotationannotationlink_parent_key;

ALTER TABLE annotationannotationlink
        DROP CONSTRAINT fkannotationannotationlink_child_child;

ALTER TABLE annotationannotationlink
        DROP CONSTRAINT fkannotationannotationlink_parent_parent;

ALTER TABLE channelannotationlink
        DROP CONSTRAINT channelannotationlink_parent_key;

ALTER TABLE datasetannotationlink
        DROP CONSTRAINT datasetannotationlink_parent_key;

ALTER TABLE datasetimagelink
        DROP CONSTRAINT datasetimagelink_parent_key;

ALTER TABLE projectdatasetlink
        DROP CONSTRAINT projectdatasetlink_parent_key;

ALTER TABLE imageannotationlink
        DROP CONSTRAINT imageannotationlink_parent_key;

ALTER TABLE joboriginalfilelink
        DROP CONSTRAINT joboriginalfilelink_parent_key;

ALTER TABLE lightpathemissionfilterlink
        DROP CONSTRAINT lightpathemissionfilterlink_parent_key;

ALTER TABLE lightpathexcitationfilterlink
        DROP CONSTRAINT lightpathexcitationfilterlink_parent_key1;

ALTER TABLE namespaceannotationlink
        DROP CONSTRAINT namespaceannotationlink_parent_key;

ALTER TABLE originalfileannotationlink
        DROP CONSTRAINT originalfileannotationlink_parent_key;

ALTER TABLE pixelsoriginalfilemap
        DROP CONSTRAINT pixelsoriginalfilemap_parent_key;

ALTER TABLE pixelsannotationlink
        DROP CONSTRAINT pixelsannotationlink_parent_key;

ALTER TABLE planeinfoannotationlink
        DROP CONSTRAINT planeinfoannotationlink_parent_key;

ALTER TABLE plateannotationlink
        DROP CONSTRAINT plateannotationlink_parent_key;

ALTER TABLE screenplatelink
        DROP CONSTRAINT screenplatelink_parent_key;

ALTER TABLE plateacquisitionannotationlink
        DROP CONSTRAINT plateacquisitionannotationlink_parent_key;

ALTER TABLE projectannotationlink
        DROP CONSTRAINT projectannotationlink_parent_key;

ALTER TABLE reagentannotationlink
        DROP CONSTRAINT reagentannotationlink_parent_key;

ALTER TABLE wellreagentlink
        DROP CONSTRAINT wellreagentlink_parent_key;

ALTER TABLE roiannotationlink
        DROP CONSTRAINT roiannotationlink_parent_key;

ALTER TABLE screenannotationlink
        DROP CONSTRAINT screenannotationlink_parent_key;

ALTER TABLE wellannotationlink
        DROP CONSTRAINT wellannotationlink_parent_key;

ALTER TABLE wellsampleannotationlink
        DROP CONSTRAINT wellsampleannotationlink_parent_key;

ALTER TABLE experimenterannotationlink
        DROP CONSTRAINT experimenterannotationlink_parent_key;

ALTER TABLE experimentergroupannotationlink
        DROP CONSTRAINT experimentergroupannotationlink_parent_key;

ALTER TABLE nodeannotationlink
        DROP CONSTRAINT nodeannotationlink_parent_key;

ALTER TABLE sessionannotationlink
        DROP CONSTRAINT sessionannotationlink_parent_key;

CREATE TABLE count_experimenter_annotationlinks_by_owner (
        experimenter_id bigint NOT NULL,
        count bigint NOT NULL,
        owner_id bigint NOT NULL
);

CREATE TABLE count_experimentergroup_annotationlinks_by_owner (
        experimentergroup_id bigint NOT NULL,
        count bigint NOT NULL,
        owner_id bigint NOT NULL
);

CREATE TABLE count_node_annotationlinks_by_owner (
        node_id bigint NOT NULL,
        count bigint NOT NULL,
        owner_id bigint NOT NULL
);

CREATE TABLE count_session_annotationlinks_by_owner (
        session_id bigint NOT NULL,
        count bigint NOT NULL,
        owner_id bigint NOT NULL
);

ALTER TABLE count_experimenter_annotationlinks_by_owner
        ADD CONSTRAINT count_experimenter_annotationlinks_by_owner_pkey PRIMARY KEY (experimenter_id, owner_id);

ALTER TABLE count_experimentergroup_annotationlinks_by_owner
        ADD CONSTRAINT count_experimentergroup_annotationlinks_by_owner_pkey PRIMARY KEY (experimentergroup_id, owner_id);

ALTER TABLE count_node_annotationlinks_by_owner
        ADD CONSTRAINT count_node_annotationlinks_by_owner_pkey PRIMARY KEY (node_id, owner_id);

ALTER TABLE count_session_annotationlinks_by_owner
        ADD CONSTRAINT count_session_annotationlinks_by_owner_pkey PRIMARY KEY (session_id, owner_id);

ALTER TABLE annotationannotationlink
        ADD CONSTRAINT annotationannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE annotationannotationlink
        ADD CONSTRAINT fkannotationannotationlink_child_annotation FOREIGN KEY (child) REFERENCES annotation(id);

ALTER TABLE annotationannotationlink
        ADD CONSTRAINT fkannotationannotationlink_parent_annotation FOREIGN KEY (parent) REFERENCES annotation(id);

ALTER TABLE channelannotationlink
        ADD CONSTRAINT channelannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE datasetannotationlink
        ADD CONSTRAINT datasetannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE datasetimagelink
        ADD CONSTRAINT datasetimagelink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE projectdatasetlink
        ADD CONSTRAINT projectdatasetlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE count_experimenter_annotationlinks_by_owner
        ADD CONSTRAINT fk_count_to_experimenter_annotationlinks FOREIGN KEY (experimenter_id) REFERENCES experimenter(id);

ALTER TABLE count_experimentergroup_annotationlinks_by_owner
        ADD CONSTRAINT fk_count_to_experimentergroup_annotationlinks FOREIGN KEY (experimentergroup_id) REFERENCES experimentergroup(id);

ALTER TABLE filtersetemissionfilterlink
        ADD CONSTRAINT fkfiltersetemissionfilterlink_child_filter FOREIGN KEY (child) REFERENCES filter(id);

ALTER TABLE filtersetemissionfilterlink
        ADD CONSTRAINT fkfiltersetemissionfilterlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);

ALTER TABLE filtersetemissionfilterlink
        ADD CONSTRAINT fkfiltersetemissionfilterlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE filtersetemissionfilterlink
        ADD CONSTRAINT fkfiltersetemissionfilterlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);

ALTER TABLE filtersetemissionfilterlink
        ADD CONSTRAINT fkfiltersetemissionfilterlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);

ALTER TABLE filtersetemissionfilterlink
        ADD CONSTRAINT fkfiltersetemissionfilterlink_parent_filterset FOREIGN KEY (parent) REFERENCES filterset(id);

ALTER TABLE filtersetemissionfilterlink
        ADD CONSTRAINT fkfiltersetemissionfilterlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE filtersetexcitationfilterlink
        ADD CONSTRAINT fkfiltersetexcitationfilterlink_child_filter FOREIGN KEY (child) REFERENCES filter(id);

ALTER TABLE filtersetexcitationfilterlink
        ADD CONSTRAINT fkfiltersetexcitationfilterlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);

ALTER TABLE filtersetexcitationfilterlink
        ADD CONSTRAINT fkfiltersetexcitationfilterlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE filtersetexcitationfilterlink
        ADD CONSTRAINT fkfiltersetexcitationfilterlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);

ALTER TABLE filtersetexcitationfilterlink
        ADD CONSTRAINT fkfiltersetexcitationfilterlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);

ALTER TABLE filtersetexcitationfilterlink
        ADD CONSTRAINT fkfiltersetexcitationfilterlink_parent_filterset FOREIGN KEY (parent) REFERENCES filterset(id);

ALTER TABLE filtersetexcitationfilterlink
        ADD CONSTRAINT fkfiltersetexcitationfilterlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE imageannotationlink
        ADD CONSTRAINT imageannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE joboriginalfilelink
        ADD CONSTRAINT joboriginalfilelink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE lightpathemissionfilterlink
        ADD CONSTRAINT lightpathemissionfilterlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE lightpathexcitationfilterlink
        ADD CONSTRAINT lightpathexcitationfilterlink_parent_key1 UNIQUE (parent, child, owner_id);

ALTER TABLE namespaceannotationlink
        ADD CONSTRAINT namespaceannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE count_node_annotationlinks_by_owner
        ADD CONSTRAINT fk_count_to_node_annotationlinks FOREIGN KEY (node_id) REFERENCES node(id);

ALTER TABLE originalfileannotationlink
        ADD CONSTRAINT originalfileannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE pixelsoriginalfilemap
        ADD CONSTRAINT pixelsoriginalfilemap_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE pixelsannotationlink
        ADD CONSTRAINT pixelsannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE planeinfoannotationlink
        ADD CONSTRAINT planeinfoannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE plateannotationlink
        ADD CONSTRAINT plateannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE screenplatelink
        ADD CONSTRAINT screenplatelink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE plateacquisitionannotationlink
        ADD CONSTRAINT plateacquisitionannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE projectannotationlink
        ADD CONSTRAINT projectannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE reagentannotationlink
        ADD CONSTRAINT reagentannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE wellreagentlink
        ADD CONSTRAINT wellreagentlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE roiannotationlink
        ADD CONSTRAINT roiannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE screenannotationlink
        ADD CONSTRAINT screenannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE count_session_annotationlinks_by_owner
        ADD CONSTRAINT fk_count_to_session_annotationlinks FOREIGN KEY (session_id) REFERENCES session(id);

ALTER TABLE wellannotationlink
        ADD CONSTRAINT wellannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE wellsampleannotationlink
        ADD CONSTRAINT wellsampleannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE experimenterannotationlink
        ADD CONSTRAINT experimenterannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE experimentergroupannotationlink
        ADD CONSTRAINT experimentergroupannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE namespace
        ADD CONSTRAINT fknamespace_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);

ALTER TABLE namespace
        ADD CONSTRAINT fknamespace_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);

ALTER TABLE namespace
        ADD CONSTRAINT fknamespace_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);

ALTER TABLE namespace
        ADD CONSTRAINT fknamespace_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE nodeannotationlink
        ADD CONSTRAINT nodeannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE plateacquisition
        ADD CONSTRAINT fkplateacquisition_plate_plate FOREIGN KEY (plate) REFERENCES plate(id);

ALTER TABLE sessionannotationlink
        ADD CONSTRAINT sessionannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE share
        ADD CONSTRAINT fkshare_group_experimentergroup FOREIGN KEY ("group") REFERENCES experimentergroup(id);

CREATE UNIQUE INDEX namespace_name ON namespace USING btree (name);


UPDATE dbpatch set message = 'Database updated.', finished = now()
 WHERE currentVersion  = 'OMERO4.2'    and
          currentPatch    = 0          and
          previousVersion = 'OMERO4.1' and
          previousPatch   = 0;

COMMIT;
