--
-- Copyright 2010 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

---
--- OMERO-Beta4.2 release upgrade from OMERO4.1__0 to OMERO4.2__0
---
--- The following setting is used in the omero-4.1-permissions-report.sql
--- file to decide how issues should be handled.
---
--- Valid values are: '''ABORT''', '''FIX''', '''DELETE'''
---
--- ABORT causes this script to exit and prints out a list of all the
--- issues which must be resolved before upgrade can complete.
---

\set ACTION '''ABORT'''


BEGIN;

\timing

-- Requirements:
--  * Applies only to OMERO4.1__0
--  * No annotations of deleted types may exist: query, thumbnail, url
--  * No Format with the value of a mimetype may be left over after original files are updated.
--  * No channels point to shapes.
--  * wellsample.timepoint should be null, and will be ignored.
--
-- If any of the requirements are not met, you
-- will need to contact the OME developers for
-- help on upgrading your data.
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

    SELECT INTO rec count(id) as count
           FROM annotation
          WHERE discriminator in (''/basic/text/uri/'', ''/basic/text/query/'', ''/type/Thumbnail/'');

    IF rec.count > 0 THEN
        RAISE EXCEPTION ''ASSERTION ERROR: Found annotations of type: (query, thumbnail, or uri). Count=%'', rec.count;
    END IF;

    SELECT INTO rec count(id) as count
           FROM logicalchannel
          WHERE shapes IS NOT NULL;

    IF rec.count > 0 THEN
        RAISE EXCEPTION ''ASSERTION ERROR: Found channels pointing to shapes: Count=%'', rec.count;
    END IF;

END;' LANGUAGE plpgsql;

SELECT omero_assert_db_version('OMERO4.1',0);
DROP FUNCTION omero_assert_db_version(varchar, int);


INSERT into dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
             values ('OMERO4.2',     0,              'OMERO4.1',          0);


----
--
-- Before anything else make changes for group permissions (#1434)
-- These are the maximal changes that can be made without user intervention.
-- Afterwards, a report is run and any discrepancies will have to be handled
-- manually.
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

-- Moving user photos to "user"

UPDATE experimenterannotationlink SET group_id = 1
  FROM annotation
 WHERE annotation.ns = 'openmicroscopy.org/omero/experimenter/photo'
   AND annotation.id = experimenterannotationlink.child;

UPDATE annotation SET group_id = 1
 WHERE annotation.ns = 'openmicroscopy.org/omero/experimenter/photo';

UPDATE originalfile SET group_id = 1
  FROM annotation
 WHERE annotation.ns = 'openmicroscopy.org/omero/experimenter/photo'
   AND annotation.file = originalfile.id;

-- #2204: Various perm changes that can be made globally
--
-- Moving scripts from 4.1 into "user" group to keep old jobs readable.
UPDATE originalfile SET group_id = user_group_id, permissions = user_group_permissions
  FROM (select id as user_group_id, permissions as user_group_permissions
          from experimentergroup where name = 'user') AS ug
 WHERE group_id = 0
   AND (cast(permissions as bit(64)) & cast(   4 as bit(64))) = cast(   4 as bit(64))
   AND name in ('populateroi.py', 'makemovie.py');

DELETE
   FROM thumbnail
  USING pixels
  WHERE thumbnail.pixels = pixels.id
    AND pixels.group_id <> thumbnail.group_id;

DELETE
   FROM channelbinding
  USING pixels, renderingdef
  WHERE channelbinding.renderingdef = renderingdef.id
    AND renderingdef.pixels = pixels.id
    AND pixels.group_id <> renderingdef.group_id;

DELETE
   FROM renderingdef
  USING pixels
  WHERE renderingdef.pixels = pixels.id
    AND pixels.group_id <> renderingdef.group_id;


----
-- This section is a copy of omero-4.1-permissions-report.sql EXCEPT for the actual execute,
-- "select omero_41_check()". That function is used to calculate any non-updateable structures,
-- and if present, stop the upgrade.

\i omero-4.1-permissions-report.sql


----
--
-- #1176 : create our own nextval() functionality for more consistent
-- sequence operation in hibernate. This functionality was updated for
-- OMERO 4.2 (#2508) in order to prevent logging during triggers, which
-- requires re-creating a sequence for every OMERO model type.
--

CREATE OR REPLACE FUNCTION upgrade_sequence(seqname VARCHAR) RETURNS void
    AS '
    BEGIN

        PERFORM c.relname AS sequencename FROM pg_class c WHERE (c.relkind = ''S'') AND c.relname = seqname;
        IF NOT FOUND THEN
            EXECUTE ''CREATE SEQUENCE '' || seqname;
        END IF;

        PERFORM next_val FROM seq_table WHERE sequence_name = seqname;
        IF FOUND THEN
            PERFORM SETVAL(seqname, next_val) FROM seq_table WHERE sequence_name = seqname;
        ELSE
            INSERT INTO seq_table (sequence_name, next_val) VALUES (seqname, 1);
        END IF;

    END;'
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION ome_unnest(anyarray)
  RETURNS SETOF anyelement AS '
    SELECT $1[i] FROM
        generate_series(array_lower($1,1),
                        array_upper($1,1)) i;
' LANGUAGE 'sql' IMMUTABLE;

SELECT count(upgrade_sequence(x)) FROM ome_unnest(string_to_array(
    'seq_wellsampleannotationlink,seq_wellannotationlink,seq_filtertype,seq_dataset,seq_plate,seq_thumbnail,'||
    'seq_immersion,seq_channel,seq_imageannotationlink,seq_link,seq_lightpathemissionfilterlink,seq_arctype,'||
    'seq_experimenttype,seq_filtersetemissionfilterlink,seq_filtersetexcitationfilterlink,seq_microscope,'||
    'seq_originalfileannotationlink,seq_wellsample,seq_planeinfo,seq_lightpathexcitationfilterlink,'||
    'seq_groupexperimentermap,seq_planeinfoannotationlink,seq_transmittancerange,seq_wellreagentlink,'||
    'seq_eventlog,seq_quantumdef,seq_namespace,seq_image,seq_renderingmodel,seq_microbeammanipulation,'||
    'seq_joboriginalfilelink,seq_experimentergroup,seq_renderingdef,seq_datasetimagelink,seq_codomainmapcontext,'||
    'seq_eventtype,seq_project,seq_microscopetype,seq_channelannotationlink,seq_filamenttype,seq_stagelabel,'||
    'seq_photometricinterpretation,seq_experimentergroupannotationlink,seq_pixels,seq_lightpath,seq_roi,'||
    'seq_roiannotationlink,seq_externalinfo,seq_annotationannotationlink,seq_objectivesettings,seq_lasertype,'||
    'seq_nodeannotationlink,seq_dimensionorder,seq_binning,seq_instrument,seq_namespaceannotationlink,seq_well,'||
    'seq_family,seq_imagingenvironment,seq_illumination,seq_projectannotationlink,seq_detectortype,seq_reagent,'||
    'seq_pulse,seq_detector,seq_otf,seq_reagentannotationlink,seq_lightsettings,seq_originalfile,seq_lightsource,'||
    'seq_annotation,seq_job,seq_sharemember,seq_dbpatch,seq_filterset,seq_projectdatasetlink,seq_plateannotationlink,'||
    'seq_experimenterannotationlink,seq_channelbinding,seq_microbeammanipulationtype,seq_medium,seq_statsinfo,'||
    'seq_lasermedium,seq_pixelstype,seq_screen,seq_dichroic,seq_session,seq_plateacquisition,seq_screenannotationlink,'||
    'seq_format,seq_node,seq_pixelsannotationlink,seq_objective,seq_datasetannotationlink,seq_experiment,seq_detectorsettings,'||
    'seq_correction,seq_filter,seq_plateacquisitionannotationlink,seq_pixelsoriginalfilemap,seq_logicalchannel,'||
    'seq_sessionannotationlink,seq_screenplatelink,seq_shape,seq_experimenter,seq_acquisitionmode,seq_event,seq_jobstatus,seq_contrastmethod', ',')) as x;

DROP FUNCTION upgrade_sequence(VARCHAR);
DROP FUNCTION ome_unnest(anyarray);

-- These renamings allow us to reuse the Hibernate-generated tables
-- for sequence generation. Eventually, a method might be found to
-- make Hibernate generate them for us.

CREATE SEQUENCE _lock_seq;
DROP TABLE seq_table;
CREATE TABLE _lock_ids (name VARCHAR(255) NOT NULL);
ALTER TABLE _lock_ids ADD COLUMN id int PRIMARY KEY DEFAULT nextval('_lock_seq');
CREATE UNIQUE INDEX _lock_ids_name ON _lock_ids (name);

-- The primary nextval function used by OMERO. Acquires an advisory lock
-- then generates a sequence of ids as quickly as possible using generate_series.
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

      BEGIN
        PERFORM pg_advisory_lock(1, Lid);
      EXCEPTION
        WHEN undefined_function THEN
          RAISE DEBUG ''No function pg_advisory_lock'';
      END;
      PERFORM nextval(seq) FROM generate_series(1, increment);
      SELECT currval(seq) INTO nv;
      BEGIN
        PERFORM pg_advisory_unlock(1, Lid);
      EXCEPTION
        WHEN undefined_function THEN
          RAISE DEBUG ''No function pg_advisory_unlock'';
      END;

      RETURN nv;

END;' LANGUAGE plpgsql;

--
-- Aliases
--

CREATE OR REPLACE FUNCTION ome_nextval(seq VARCHAR) RETURNS INT8 AS '
BEGIN
      RETURN ome_nextval(seq, 1);
END;' LANGUAGE plpgsql;

--
-- Replace the one table which had a default of nextval
--
ALTER TABLE dbpatch ALTER COLUMN id SET DEFAULT ome_nextval('seq_dbpatch');

----
--
-- SPW
--

-- Add new SPW data structures
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

ALTER TABLE wellsample
        ADD COLUMN plateacquisition bigint;

ALTER TABLE wellsample
	ADD CONSTRAINT fkwellsample_plateacquisition_plateacquisition
        FOREIGN KEY (plateacquisition) REFERENCES plateacquisition(id);

ALTER TABLE wellsample
        RENAME COLUMN timepoint to old_timepoint;

ALTER TABLE wellsample
        ADD COLUMN timepoint timestamp without time zone;

ALTER TABLE plateacquisitionannotationlink
	ADD CONSTRAINT plateacquisitionannotationlink_external_id_key UNIQUE (external_id),
        ADD CONSTRAINT plateacquisitionannotationlink_parent_key UNIQUE (parent, child, owner_id),
	ADD CONSTRAINT fkplateacquisitionannotationlink_child_annotation FOREIGN KEY (child) REFERENCES annotation(id),
	ADD CONSTRAINT fkplateacquisitionannotationlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id),
	ADD CONSTRAINT fkplateacquisitionannotationlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id),
	ADD CONSTRAINT fkplateacquisitionannotationlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id),
	ADD CONSTRAINT fkplateacquisitionannotationlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id),
	ADD CONSTRAINT fkplateacquisitionannotationlink_parent_plateacquisition FOREIGN KEY (parent) REFERENCES plateacquisition(id),
	ADD CONSTRAINT fkplateacquisitionannotationlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id),
	ADD CONSTRAINT plateacquisitionannotationlink_pkey PRIMARY KEY (id);

ALTER TABLE plateacquisition
	ADD CONSTRAINT plateacquisition_external_id_key UNIQUE (external_id),
	ADD CONSTRAINT fkplateacquisition_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id),
	ADD CONSTRAINT fkplateacquisition_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id),
	ADD CONSTRAINT fkplateacquisition_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id),
	ADD CONSTRAINT fkplateacquisition_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id),
	ADD CONSTRAINT fkplateacquisition_update_id_event FOREIGN KEY (update_id) REFERENCES event(id),
        ADD CONSTRAINT fkplateacquisition_plate_plate FOREIGN KEY (plate) REFERENCES plate(id);

-- Leaving timepoint null

CREATE OR REPLACE FUNCTION omero_convert_spw_42() RETURNS void AS '
DECLARE
    rec RECORD;
BEGIN

    UPDATE plate SET cols = max.c, rows = max.r
      FROM
        (SELECT p2.id as plate_id, max("column") as c, max(row) as r
           FROM plate p2, well w
          WHERE p2.id = w.plate
       GROUP BY p2.id) as max
     WHERE id = max.plate_id;

    INSERT INTO plateacquisition (id, permissions, starttime, endtime, version, creation_id, external_id, group_id, owner_id, update_id, plate, maximumfieldcount, name)
    SELECT sa.sa_id, permissions, starttime, endtime, version, creation_id, external_id, group_id, owner_id, update_id, plate.plate_id, count.maxfieldcount, ''''
      FROM
        (SELECT sa.id as sa_id, sa.permissions, sa.starttime, sa.endtime, sa.version, sa.creation_id, sa.external_id, sa.group_id, sa.owner_id, sa.update_id
           FROM screenacquisition sa) as sa,
        (SELECT distinct link.parent as sa_id, p.id as plate_id
           FROM plate p, well w, wellsample ws, screenacquisitionwellsamplelink link
          WHERE link.child = ws.id AND ws.well = w.id AND w.plate = p.id) as plate,
        (SELECT plate_id, max(fieldcount) as maxfieldcount
           FROM
            (SELECT p.id as plate_id, w.id as well_id, count(ws.id) as fieldcount
               FROM wellsample ws, well w, plate p
              WHERE p.id = w.plate and w.id = ws.well
           GROUP BY p.id, w.id) as inner_count
       GROUP BY plate_id) as count
     WHERE sa.sa_id = plate.sa_id AND plate.plate_id = count.plate_id;

    UPDATE wellsample SET plateacquisition = sa.id
      FROM screenacquisition sa, screenacquisitionwellsamplelink link
     WHERE sa.id = link.parent AND link.child = wellsample.id;

    INSERT INTO plateacquisitionannotationlink (id, permissions, version, child, creation_id, external_id, group_id, owner_id, update_id, parent)
    SELECT id, permissions, version, child, creation_id, external_id, group_id, owner_id, update_id, parent FROM screenacquisitionannotationlink
     WHERE id IN (SELECT id FROM plateacquisition);

    PERFORM setval(''seq_plateacquisition'', nextval(''seq_plateacquisition''));
    PERFORM setval(''seq_plateacquisitionannotationlink'', nextval(''seq_plateacquisitionannotationlink''));

END;' LANGUAGE plpgsql;
SELECT omero_convert_spw_42();
DROP FUNCTION omero_convert_spw_42();

-- Remove old SPW data structures
DROP VIEW count_screenacquisition_annotationlinks_by_owner;
DROP VIEW count_wellsample_screenacquisitionlinks_by_owner;
DROP VIEW count_screenacquisition_wellsamplelinks_by_owner;
DROP TABLE screenacquisitionannotationlink;
DROP TABLE screenacquisitionwellsamplelink;
DROP TABLE screenacquisition;

-- #2428 convention namings

UPDATE plate SET rowNamingConvention = 'letter'
 WHERE rowNamingConvention ~ '[a-zA-Z]';

UPDATE plate SET rowNamingConvention = 'number'
 WHERE rowNamingConvention ~ '[0-9]';

UPDATE plate SET columnNamingConvention = 'letter'
 WHERE columnNamingConvention ~ '[a-zA-Z]';

UPDATE plate SET columnNamingConvention = 'number'
 WHERE columnNamingConvention ~ '[0-9]';

-- #1640

create unique index well_col_row on well(plate, "column", "row");

----
--
-- Instruments/Filters
--

-- Add new instrument data structures
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

CREATE TABLE filtersetemissionfilterlink (
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
        primary key (id)
);

CREATE TABLE filtersetexcitationfilterlink (
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
        primary key (id)
);

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

ALTER TABLE lightpath
	ADD CONSTRAINT lightpath_pkey PRIMARY KEY (id),
	ADD CONSTRAINT lightpath_external_id_key UNIQUE (external_id),
	ADD CONSTRAINT fklightpath_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id),
	ADD CONSTRAINT fklightpath_dichroic_dichroic FOREIGN KEY (dichroic) REFERENCES dichroic(id),
	ADD CONSTRAINT fklightpath_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id),
	ADD CONSTRAINT fklightpath_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id),
	ADD CONSTRAINT fklightpath_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id),
	ADD CONSTRAINT fklightpath_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE logicalchannel
	ADD CONSTRAINT fklogicalchannel_lightpath_lightpath FOREIGN KEY (lightpath) REFERENCES lightpath(id);

ALTER TABLE filtersetemissionfilterlink
        ADD CONSTRAINT filtersetemissionfilterlink_parent_key UNIQUE (parent, child, owner_id),
        ADD CONSTRAINT fkfiltersetemissionfilterlink_child_filter FOREIGN KEY (child) REFERENCES filter(id),
        ADD CONSTRAINT fkfiltersetemissionfilterlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id),
        ADD CONSTRAINT fkfiltersetemissionfilterlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id),
        ADD CONSTRAINT fkfiltersetemissionfilterlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id),
        ADD CONSTRAINT fkfiltersetemissionfilterlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id),
        ADD CONSTRAINT fkfiltersetemissionfilterlink_parent_filterset FOREIGN KEY (parent) REFERENCES filterset(id),
        ADD CONSTRAINT fkfiltersetemissionfilterlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE filtersetexcitationfilterlink
        ADD CONSTRAINT filtersetexcitationfilterlink_parent_key UNIQUE (parent, child, owner_id),
        ADD CONSTRAINT fkfiltersetexcitationfilterlink_child_filter FOREIGN KEY (child) REFERENCES filter(id),
        ADD CONSTRAINT fkfiltersetexcitationfilterlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id),
        ADD CONSTRAINT fkfiltersetexcitationfilterlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id),
        ADD CONSTRAINT fkfiltersetexcitationfilterlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id),
        ADD CONSTRAINT fkfiltersetexcitationfilterlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id),
        ADD CONSTRAINT fkfiltersetexcitationfilterlink_parent_filterset FOREIGN KEY (parent) REFERENCES filterset(id),
        ADD CONSTRAINT fkfiltersetexcitationfilterlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE lightpathemissionfilterlink
        ADD CONSTRAINT lightpathemissionfilterlink_parent_key UNIQUE (parent, child, owner_id),
	ADD CONSTRAINT lightpathemissionfilterlink_external_id_key UNIQUE (external_id),
	ADD CONSTRAINT fklightpathemissionfilterlink_child_filter FOREIGN KEY (child) REFERENCES filter(id),
	ADD CONSTRAINT fklightpathemissionfilterlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id),
	ADD CONSTRAINT fklightpathemissionfilterlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id),
	ADD CONSTRAINT fklightpathemissionfilterlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id),
	ADD CONSTRAINT fklightpathemissionfilterlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id),
	ADD CONSTRAINT fklightpathemissionfilterlink_parent_lightpath FOREIGN KEY (parent) REFERENCES lightpath(id),
	ADD CONSTRAINT fklightpathemissionfilterlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id),
	ADD CONSTRAINT lightpathemissionfilterlink_pkey PRIMARY KEY (id);

ALTER TABLE lightpathexcitationfilterlink
        ADD CONSTRAINT lightpathexcitationfilterlink_parent_key1 UNIQUE (parent, child, owner_id),
	ADD CONSTRAINT lightpathexcitationfilterlink_parent_key UNIQUE (parent, parent_index),
	ADD CONSTRAINT lightpathexcitationfilterlink_external_id_key UNIQUE (external_id),
	ADD CONSTRAINT fklightpathexcitationfilterlink_child_filter FOREIGN KEY (child) REFERENCES filter(id),
	ADD CONSTRAINT fklightpathexcitationfilterlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id),
	ADD CONSTRAINT fklightpathexcitationfilterlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id),
	ADD CONSTRAINT fklightpathexcitationfilterlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id),
	ADD CONSTRAINT fklightpathexcitationfilterlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id),
	ADD CONSTRAINT fklightpathexcitationfilterlink_parent_lightpath FOREIGN KEY (parent) REFERENCES lightpath(id),
	ADD CONSTRAINT fklightpathexcitationfilterlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id),
	ADD CONSTRAINT lightpathexcitationfilterlink_pkey PRIMARY KEY (id);

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

CREATE OR REPLACE FUNCTION omero_convert_instruments_42() RETURNS void AS '
DECLARE
    rec RECORD;         -- General purpose record
    fs_rec RECORD;      -- Filterset for any given logicalchannel
    lightpath_id  INT8; -- Newly created lightpath per logicalchannel
    ex_count INT4 := 0; -- Count of excitation for setting parent_index
BEGIN

    FOR rec IN SELECT * FROM logicalchannel
                WHERE filterset IS NOT NULL OR secondaryemissionfilter IS NOT NULL OR secondaryexcitationfilter IS NOT NULL LOOP

        -- If any of the 3 fields is not null, then we will need to generate a light path object to hold them.
        SELECT INTO lightpath_id nextval(''seq_lightpath'');
        INSERT INTO lightpath (id, permissions, creation_id, group_id, owner_id, update_id)
             SELECT lightpath_id, rec.permissions, rec.creation_id, rec.group_id, rec.owner_id, rec.update_id;

        -- First, we parse the filterset if present
        IF rec.filterset IS NOT NULL THEN
            SELECT INTO fs_rec * FROM filterset WHERE id = rec.filterset;
            UPDATE lightpath SET dichroic = fs_rec.dichroic WHERE id = lightpath_id;

            IF fs_rec.emfilter IS NOT NULL THEN
                INSERT INTO lightpathemissionfilterlink (id, permissions, child, creation_id, group_id, owner_id, update_id, parent)
                     SELECT nextval(''seq_lightpathemissionfilterlink''), rec.permissions, fs_rec.emfilter, rec.creation_id, rec.group_id, rec.owner_id, rec.update_id, lightpath_id;
            END IF;

            IF fs_rec.exfilter IS NOT NULL THEN
                INSERT INTO lightpathexcitationfilterlink (id, permissions, child, creation_id, group_id, owner_id, update_id, parent, parent_index)
                     SELECT nextval(''seq_lightpathexcitationfilterlink''), rec.permissions, fs_rec.exfilter, rec.creation_id, rec.group_id, rec.owner_id, rec.update_id, lightpath_id, ex_count;
                ex_count := ex_count + 1;
            END IF;

        END IF;

        -- Now we parse out the secondary filters, which may become primary filters if no filterset was present.
        IF rec.secondaryemissionfilter IS NOT NULL THEN
            INSERT INTO lightpathemissionfilterlink (id, permissions, child, creation_id, group_id, owner_id, update_id, parent)
                 SELECT nextval(''seq_lightpathemissionfilterlink''), rec.permissions, rec.secondaryemissionfilter, rec.creation_id, rec.group_id, rec.owner_id, rec.update_id, lightpath_id;
        END IF;

        IF rec.secondaryexcitationfilter IS NOT NULL THEN
            INSERT INTO lightpathexcitationfilterlink (id, permissions, child, creation_id, group_id, owner_id, update_id, parent, parent_index)
                 SELECT nextval(''seq_lightpathexcitationfilterlink''), rec.permissions, rec.secondaryexcitationfilter, rec.creation_id, rec.group_id, rec.owner_id, rec.update_id, lightpath_id, ex_count;
            ex_count := ex_count + 1;
        END IF;

        UPDATE logicalchannel SET lightpath = lightpath_id WHERE id = rec.id;

    END LOOP;

    -- Now that we haveve culled the logicalchannels, update the filtersets themselves
    -- by creating links for the two columns which will be deleted.
    FOR rec IN SELECT * FROM filterset
                WHERE emfilter IS NOT NULL or exfilter IS NOT NULL LOOP

        IF rec.emfilter IS NOT NULL THEN
            INSERT INTO filtersetemissionfilterlink (id, permissions, child, creation_id, group_id, owner_id, update_id, parent)
                 SELECT nextval(''seq_filtersetemissionfilterlink''), rec.permissions, rec.emfilter, rec.creation_id, rec.group_id, rec.owner_id, rec.update_id, rec.id;
        END IF;

        IF rec.exfilter IS NOT NULL THEN
            INSERT INTO filtersetexcitationfilterlink (id, permissions, child, creation_id, group_id, owner_id, update_id, parent)
                 SELECT nextval(''seq_filtersetexcitationfilterlink''), rec.permissions, rec.exfilter, rec.creation_id, rec.group_id, rec.owner_id, rec.update_id, rec.id;
        END IF;
    END LOOP;


END;' LANGUAGE plpgsql;
SELECT omero_convert_instruments_42();
DROP FUNCTION omero_convert_instruments_42();


-- Remove old instrument data structures
ALTER TABLE logicalchannel
	DROP COLUMN secondaryemissionfilter,
	DROP COLUMN secondaryexcitationfilter,
        DROP CONSTRAINT fklogicalchannel_shapes_shape,
        DROP COLUMN shapes;

ALTER TABLE filterset
	DROP COLUMN emfilter,
	DROP COLUMN exfilter;

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

ALTER TABLE namespace
	ADD CONSTRAINT namespace_pkey PRIMARY KEY (id),
	ADD CONSTRAINT namespace_external_id_key UNIQUE (external_id),
	ADD CONSTRAINT fknamespace_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id),
        ADD CONSTRAINT fknamespace_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id),
        ADD CONSTRAINT fknamespace_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id),
        ADD CONSTRAINT fknamespace_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id),
        ADD CONSTRAINT fknamespace_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE namespaceannotationlink
	ADD CONSTRAINT namespaceannotationlink_external_id_key UNIQUE (external_id),
        ADD CONSTRAINT namespaceannotationlink_parent_key UNIQUE (parent, child, owner_id),
	ADD CONSTRAINT fknamespaceannotationlink_child_annotation FOREIGN KEY (child) REFERENCES annotation(id),
	ADD CONSTRAINT fknamespaceannotationlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id),
	ADD CONSTRAINT fknamespaceannotationlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id),
	ADD CONSTRAINT fknamespaceannotationlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id),
	ADD CONSTRAINT fknamespaceannotationlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id),
	ADD CONSTRAINT fknamespaceannotationlink_parent_namespace FOREIGN KEY (parent) REFERENCES namespace(id),
	ADD CONSTRAINT fknamespaceannotationlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id),
        ADD CONSTRAINT namespaceannotationlink_pkey PRIMARY KEY (id);


CREATE UNIQUE INDEX namespace_name ON namespace USING btree (name);

CREATE TABLE parsejob (
	params bytea,
	job_id bigint NOT NULL
);

ALTER TABLE parsejob
	ADD CONSTRAINT fkparsejob_job_id_job FOREIGN KEY (job_id) REFERENCES job(id),
	ADD CONSTRAINT parsejob_pkey PRIMARY KEY (job_id);

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
        IF substring(rec.path from 1 for 16) = ''/imported_image/'' THEN
            RAISE EXCEPTION ''Already modified! Image:%'', rec.image;
        END IF;

        UPDATE originalfile SET path = ''/openmicroscopy.org/omero/image_files/''||rec.image||''/'' WHERE id = rec.file;
    END LOOP;

END;' LANGUAGE plpgsql;

SELECT upgrade_original_metadata_txt();
DROP FUNCTION upgrade_original_metadata_txt();

UPDATE annotation SET ns = 'openmicroscopy.org/omero/movie' WHERE ns IN
    ( 'openmicroscopy.org/omero/movie/mpeg',
      'openmicroscopy.org/omero/movie/qt',
      'openmicroscopy.org/omero/movie/wmv');

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

ALTER TABLE image
	ADD COLUMN partial boolean,
	ADD COLUMN format bigint;

ALTER TABLE image
	ADD CONSTRAINT fkimage_format_format FOREIGN KEY (format) REFERENCES format(id);

-- Not attempting to fill Image.format

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
CREATE OR REPLACE FUNCTION upgrade_group_owners() RETURNS void AS '
DECLARE
    mid INT8;
    rec RECORD;
BEGIN

    ALTER TABLE groupexperimentermap ADD COLUMN owner boolean;

    -- For every group, if the owner is not in the group, add them
    -- If they are in the group, set the boolean flag.
    FOR rec IN SELECT * FROM experimentergroup LOOP
        SELECT INTO mid id FROM groupexperimentermap WHERE child = rec.owner_id;
        IF NOT FOUND THEN
            SELECT INTO mid nextval(''seq_groupexperimentermap'');
            INSERT INTO groupexperimentermap (id, permissions, version, parent, child, child_index)
                 SELECT mid, -35, 0, rec.id, rec.owner_id, max(child_index) + 1
                   FROM groupexperimentermap WHERE child = rec.owner_id;
        ELSE
            UPDATE groupexperimentermap SET owner = true WHERE id = mid;
        END IF;
    END LOOP;

    UPDATE groupexperimentermap SET owner = FALSE WHERE owner IS NULL;

    ALTER TABLE groupexperimentermap ALTER COLUMN owner SET NOT NULL;

END;' LANGUAGE plpgsql;

SELECT upgrade_group_owners();
DROP FUNCTION upgrade_group_owners();

----
--
-- Remove old system types
--
DROP VIEW count_experimentergroup_groupexperimentermap_by_owner;
DROP VIEW count_experimenter_groupexperimentermap_by_owner;

ALTER TABLE groupexperimentermap
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id,
	DROP COLUMN update_id;

----
--
-- ROI modifications
--

CREATE OR REPLACE FUNCTION hex_to_dec(t text) RETURNS integer AS $$
DECLARE
    r RECORD;
    sql VARCHAR;
BEGIN
    sql := 'SELECT x';
    sql := sql || E'\'';
    sql := sql || t;
    sql := sql || E'\'';
    sql := sql || '::integer AS hex';
    FOR r IN EXECUTE sql LOOP
        RETURN r.hex;
    END LOOP;
END;$$ LANGUAGE plpgsql IMMUTABLE STRICT;

CREATE OR REPLACE FUNCTION hex_to_argb(color VARCHAR, opacity FLOAT8) RETURNS INT8 AS '
DECLARE

    OFFSET INT8 := 4294967296;
    MAXINT INT8 := 2147483647;
    MININT INT8 := -2147483648;

    rval INT8;
    gval INT8;
    bval INT8;
    aval INT8;
    argb INT8;
BEGIN

    IF opacity < 0.0 or opacity > 1.0 THEN
        RAISE EXCEPTION ''Opacity out of bounds: %'', opacity;
    ELSIF substring(color from 1 for 1) = ''#'' THEN
        aval := cast(round((255.0 * opacity)::numeric) as int8);
        rval := hex_to_dec(substring(color from 2 for 2));
        gval := hex_to_dec(substring(color from 4 for 2));
        bval := hex_to_dec(substring(color from 6 for 2));
    ELSE
        RAISE EXCEPTION ''Unknown color format: %'', color;
    END IF;

    argb := aval << 24;
    argb := argb + (rval << 16);
    argb := argb + (gval << 8);
    argb := argb + bval;

    IF argb < 0 or argb > OFFSET THEN
        RAISE EXCEPTION ''Overflow: % (color=%, opacity=%, argb=(%,%,%,%))'',
            argb, color, opacity, aval, rval, gval, bval;
    ELSIF argb > MAXINT THEN
        argb := argb - OFFSET;
    END IF;

    IF argb < MININT or argb > MAXINT THEN
        RAISE EXCEPTION ''Late overflow: %'', argv;
    END IF;

    RETURN argb;

END;' LANGUAGE plpgsql IMMUTABLE STRICT;

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
UPDATE shape SET new_fillcolor = hex_to_argb(fillcolor, fillopacity);
UPDATE shape SET new_strokecolor = hex_to_argb(strokecolor, strokeopacity);
ALTER TABLE shape DROP COLUMN fillopacity;
ALTER TABLE shape DROP COLUMN strokeopacity;
ALTER TABLE shape DROP COLUMN fillcolor;
ALTER TABLE shape DROP COLUMN strokecolor;
ALTER TABLE shape RENAME COLUMN new_fillcolor to fillcolor;
ALTER TABLE shape RENAME COLUMN new_strokecolor to strokecolor;
DROP FUNCTION hex_to_dec(text);
DROP FUNCTION hex_to_argb(VARCHAR, FLOAT8);

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
    select nextval('seq_acquisitionmode'),-35,'LCM';
insert into acquisitionmode (id,permissions,value)
    select nextval('seq_acquisitionmode'),-35,'FSM';
insert into acquisitionmode (id,permissions,value)
    select nextval('seq_acquisitionmode'),-35,'PALM';
insert into acquisitionmode (id,permissions,value)
    select nextval('seq_acquisitionmode'),-35,'STED';
insert into acquisitionmode (id,permissions,value)
    select nextval('seq_acquisitionmode'),-35,'STORM';
insert into acquisitionmode (id,permissions,value)
    select nextval('seq_acquisitionmode'),-35,'TIRF';
insert into acquisitionmode (id,permissions,value)
    select nextval('seq_acquisitionmode'),-35,'LaserScanningConfocalMicroscopy';

update logicalchannel set mode = am_new.id
  from acquisitionmode am_old, acquisitionmode am_new
 where mode = am_old.id
   and am_old.value in ('LaserScanningConfocal', 'LaserScanningMicroscopy')
   and am_new.value = 'LaserScanningConfocalMicroscopy';

delete from acquisitionmode where value in ('LaserScanningConfocal', 'LaserScanningMicroscopy');

insert into detectortype (id,permissions,value)
    select nextval('seq_detectortype'),-35,'EBCCD';

insert into filtertype (id,permissions,value)
    select nextval('seq_filtertype'),-35,'Dichroic';
insert into filtertype (id,permissions,value)
    select nextval('seq_filtertype'),-35,'NeutralDensity';

insert into microbeammanipulationtype (id,permissions,value)
    select nextval('seq_microbeammanipulationtype'),-35,'FLIP';
insert into microbeammanipulationtype (id,permissions,value)
    select nextval('seq_microbeammanipulationtype'),-35,'InverseFRAP';

-- Deleting from Format. If any of these have been assigned to an Image
-- this will fail.
delete from format where value in
    ('application/msword', 'application/octet-stream', 'application/pdf', 'application/vnd.ms-excel',
     'application/vnd.ms-powerpoint', 'audio/basic', 'audio/mpeg', 'audio/wav',
     'image/bmp', 'image/gif', 'image/jpeg', 'image/png', 'image/tiff',
     'text/csv', 'text/html', 'text/ini', 'text/plain', 'text/richtext',
     'text/rtf', 'text/x-python', 'text/xml',
     'video/jpeg2000', 'video/mp4', 'video/mpeg', 'video/quicktime');

----
--
-- Unify Annotation types (#2354, r7000)
--

ALTER TABLE annotation
	DROP COLUMN thumbnail,
	ADD COLUMN termvalue text;

----
--
-- Fix shares with respect to group permissions (#1434, #2327, r6882)
--

ALTER TABLE share
        ADD COLUMN "group" bigint;

ALTER TABLE share
        ADD CONSTRAINT fkshare_group_experimentergroup FOREIGN KEY ("group") REFERENCES experimentergroup(id);

UPDATE share
        SET "group" = m.parent
            FROM session sess, experimenter e, groupexperimentermap m
           WHERE sess.id = session_id AND sess.owner = e.id AND e.id = m.child AND m.child_index = 0;

ALTER TABLE share
        ALTER COLUMN "group" SET NOT NULL;

ALTER TABLE sharemember
	DROP COLUMN creation_id,
	DROP COLUMN group_id,
	DROP COLUMN owner_id,
	DROP COLUMN update_id;

----
--
-- #1390 Triggers for keeping the search index up-to-date.
--

CREATE OR REPLACE FUNCTION annotation_update_event_trigger() RETURNS "trigger"
    AS '
    DECLARE
        rec RECORD;
        eid int8;
    BEGIN

        FOR rec IN SELECT id, parent FROM imageannotationlink WHERE child = new.id LOOP
            SELECT into eid ome_nextval(''seq_event'');
            INSERT INTO event (id, permissions, status, time, experimenter, experimentergroup, session, type)
                 SELECT eid, -35, ''TRIGGERED'', now(), 0, 0, 0, 0;
            INSERT INTO eventlog (id, action, permissions, entityid, entitytype, event)
                 SELECT ome_nextval(''seq_eventlog''), ''REINDEX'', -35, rec.parent, ''ome.model.core.Image'', eid;
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
        eid int8;
    BEGIN

        SELECT into eid ome_nextval(''seq_event'');
        INSERT INTO event (id, permissions, status, time, experimenter, experimentergroup, session, type)
                SELECT eid, -35, ''TRIGGERED'', now(), 0, 0, 0, 0;
        INSERT INTO eventlog (id, action, permissions, entityid, entitytype, event)
                SELECT ome_nextval(''seq_eventlog''), ''REINDEX'', -35, new.parent, ''ome.model.core.Image'', eid;

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

CREATE VIEW count_filter_emissionfilterlink_by_owner AS
        SELECT filtersetemissionfilterlink.child AS filter_id, filtersetemissionfilterlink.owner_id, count(*) AS count FROM filtersetemissionfilterlink GROUP BY filtersetemissionfilterlink.child, filtersetemissionfilterlink.owner_id ORDER BY filtersetemissionfilterlink.child;

CREATE VIEW count_filter_excitationfilterlink_by_owner AS
        SELECT filtersetexcitationfilterlink.child AS filter_id, filtersetexcitationfilterlink.owner_id, count(*) AS count FROM filtersetexcitationfilterlink GROUP BY filtersetexcitationfilterlink.child, filtersetexcitationfilterlink.owner_id ORDER BY filtersetexcitationfilterlink.child;

DROP VIEW count_experimenter_annotationlinks_by_owner;

DROP VIEW count_experimentergroup_annotationlinks_by_owner;

DROP VIEW count_node_annotationlinks_by_owner;

DROP VIEW count_session_annotationlinks_by_owner;


----
--
-- Replacing (parent, child) indexes with (parent, child, owner_id) indexes.
-- Note: other places in this file may have similar replacements which are
-- made.
--

ALTER TABLE annotationannotationlink
        DROP CONSTRAINT annotationannotationlink_parent_key;

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

ALTER TABLE annotationannotationlink
        ADD CONSTRAINT annotationannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE channelannotationlink
        ADD CONSTRAINT channelannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE datasetannotationlink
        ADD CONSTRAINT datasetannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE datasetimagelink
        ADD CONSTRAINT datasetimagelink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE projectdatasetlink
        ADD CONSTRAINT projectdatasetlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE imageannotationlink
        ADD CONSTRAINT imageannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE joboriginalfilelink
        ADD CONSTRAINT joboriginalfilelink_parent_key UNIQUE (parent, child, owner_id);

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

ALTER TABLE wellannotationlink
        ADD CONSTRAINT wellannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE wellsampleannotationlink
        ADD CONSTRAINT wellsampleannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE experimenterannotationlink
        ADD CONSTRAINT experimenterannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE experimentergroupannotationlink
        ADD CONSTRAINT experimentergroupannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE nodeannotationlink
        ADD CONSTRAINT nodeannotationlink_parent_key UNIQUE (parent, child, owner_id);

ALTER TABLE sessionannotationlink
        ADD CONSTRAINT sessionannotationlink_parent_key UNIQUE (parent, child, owner_id);


----
--
-- Newly annotatable types
--
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

ALTER TABLE count_experimenter_annotationlinks_by_owner
        ADD CONSTRAINT fk_count_to_experimenter_annotationlinks FOREIGN KEY (experimenter_id) REFERENCES experimenter(id);

ALTER TABLE count_experimentergroup_annotationlinks_by_owner
        ADD CONSTRAINT fk_count_to_experimentergroup_annotationlinks FOREIGN KEY (experimentergroup_id) REFERENCES experimentergroup(id);

ALTER TABLE count_node_annotationlinks_by_owner
        ADD CONSTRAINT fk_count_to_node_annotationlinks FOREIGN KEY (node_id) REFERENCES node(id);

ALTER TABLE count_session_annotationlinks_by_owner
        ADD CONSTRAINT fk_count_to_session_annotationlinks FOREIGN KEY (session_id) REFERENCES session(id);


----
--
-- #2574 Fixing triggers for ordered collections
--

DROP TRIGGER channel_pixels_index_trigger ON channel;

DROP TRIGGER channelbinding_renderingdef_index_trigger ON channelbinding;

DROP TRIGGER codomainmapcontext_renderingdef_index_trigger ON codomainmapcontext;

DROP TRIGGER lightpathexcitationfilterlink_parent_index_trigger ON lightpathexcitationfilterlink;

DROP TRIGGER groupexperimentermap_child_index_trigger ON groupexperimentermap;

DROP TRIGGER pixels_image_index_trigger ON pixels;

DROP TRIGGER shape_roi_index_trigger ON shape;

DROP TRIGGER wellsample_well_index_trigger ON wellsample;


DROP FUNCTION channel_pixels_index_move();

DROP FUNCTION channelbinding_renderingdef_index_move();

DROP FUNCTION codomainmapcontext_renderingdef_index_move();

DROP FUNCTION groupexperimentermap_child_index_move();

DROP FUNCTION lightpathexcitationfilterlink_parent_index_move();

DROP FUNCTION pixels_image_index_move();

DROP FUNCTION shape_roi_index_move();

DROP FUNCTION wellsample_well_index_move();

CREATE OR REPLACE FUNCTION channel_pixels_index_insert() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    DECLARE
      duplicate INT8;
    BEGIN

      -- At most, there should be one duplicate
      SELECT id INTO duplicate
        FROM channel
       WHERE pixels = new.pixels AND pixels_index = new.pixels_index
      OFFSET 0
       LIMIT 1;

      IF duplicate IS NOT NULL THEN
          RAISE NOTICE 'Remapping channel % via (-1 - oldvalue )', duplicate;
          UPDATE channel SET pixels_index = -1 - pixels_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;$$;


CREATE OR REPLACE FUNCTION channel_pixels_index_update() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    DECLARE
      duplicate INT8;
  BEGIN
      -- Avoids a query if the new and old values of x are the same.
      IF new.pixels = old.pixels AND new.pixels_index = old.pixels_index THEN
          RETURN new;
      END IF;

      -- At most, there should be one duplicate
      SELECT id INTO duplicate
        FROM channel
       WHERE pixels = new.pixels AND pixels_index = new.pixels_index
      OFFSET 0
       LIMIT 1;

      IF duplicate IS NOT NULL THEN
          RAISE NOTICE 'Remapping channel % ', duplicate;
          UPDATE channel SET pixels_index = -1 - pixels_index WHERE id = duplicate;
      END IF;

      RETURN new;
  END;$$;


CREATE OR REPLACE FUNCTION channelbinding_renderingdef_index_insert() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    DECLARE
      duplicate INT8;
    BEGIN

      -- At most, there should be one duplicate
      SELECT id INTO duplicate
        FROM channelbinding
       WHERE renderingDef = new.renderingDef AND renderingDef_index = new.renderingDef_index
      OFFSET 0
       LIMIT 1;

      IF duplicate IS NOT NULL THEN
          RAISE NOTICE 'Remapping channelbinding % via (-1 - oldvalue )', duplicate;
          UPDATE channelbinding SET renderingDef_index = -1 - renderingDef_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;$$;


CREATE OR REPLACE FUNCTION channelbinding_renderingdef_index_update() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    DECLARE
      duplicate INT8;
    BEGIN

      -- Avoids a query if the new and old values of x are the same.
      IF new.renderingDef = old.renderingDef AND new.renderingDef_index = old.renderingDef_index THEN
          RETURN new;
      END IF;

      -- At most, there should be one duplicate
      SELECT id INTO duplicate
        FROM channelbinding
       WHERE renderingDef = new.renderingDef AND renderingDef_index = new.renderingDef_index
      OFFSET 0
       LIMIT 1;

      IF duplicate IS NOT NULL THEN
          RAISE NOTICE 'Remapping channelbinding % via (-1 - oldvalue )', duplicate;
          UPDATE channelbinding SET renderingDef_index = -1 - renderingDef_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;$$;


CREATE OR REPLACE FUNCTION codomainmapcontext_renderingdef_index_insert() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    DECLARE
      duplicate INT8;
    BEGIN

      -- At most, there should be one duplicate
      SELECT id INTO duplicate
        FROM codomainmapcontext
       WHERE renderingDef = new.renderingDef AND renderingDef_index = new.renderingDef_index
      OFFSET 0
       LIMIT 1;

      IF duplicate IS NOT NULL THEN
          RAISE NOTICE 'Remapping codomainmapcontext % via (-1 - oldvalue )', duplicate;
          UPDATE codomainmapcontext SET renderingDef_index = -1 - renderingDef_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;$$;


CREATE OR REPLACE FUNCTION codomainmapcontext_renderingdef_index_update() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    DECLARE
      duplicate INT8;
    BEGIN

      -- Avoids a query if the new and old values of x are the same.
      IF new.renderingDef = old.renderingDef AND new.renderingDef_index = old.renderingDef_index THEN
          RETURN new;
      END IF;

      -- At most, there should be one duplicate
      SELECT id INTO duplicate
        FROM codomainmapcontext
       WHERE renderingDef = new.renderingDef AND renderingDef_index = new.renderingDef_index
      OFFSET 0
       LIMIT 1;

      IF duplicate IS NOT NULL THEN
          RAISE NOTICE 'Remapping codomainmapcontext % via (-1 - oldvalue )', duplicate;
          UPDATE codomainmapcontext SET renderingDef_index = -1 - renderingDef_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;$$;


CREATE OR REPLACE FUNCTION groupexperimentermap_child_index_insert() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    DECLARE
      duplicate INT8;
    BEGIN

      -- At most, there should be one duplicate
      SELECT id INTO duplicate
        FROM groupexperimentermap
       WHERE child = new.child AND child_index = new.child_index
      OFFSET 0
       LIMIT 1;

      IF duplicate IS NOT NULL THEN
          RAISE NOTICE 'Remapping groupexperimentermap % via (-1 - oldvalue )', duplicate;
          UPDATE groupexperimentermap SET child_index = -1 - child_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;$$;


CREATE OR REPLACE FUNCTION groupexperimentermap_child_index_update() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    DECLARE
      duplicate INT8;
    BEGIN

      -- Avoids a query if the new and old values of x are the same.
      IF new.child = old.child AND new.child_index = old.child_index THEN
          RETURN new;
      END IF;

      -- At most, there should be one duplicate
      SELECT id INTO duplicate
        FROM groupexperimentermap
       WHERE child = new.child AND child_index = new.child_index
      OFFSET 0
       LIMIT 1;

      IF duplicate IS NOT NULL THEN
          RAISE NOTICE 'Remapping groupexperimentermap % via (-1 - oldvalue )', duplicate;
          UPDATE groupexperimentermap SET child_index = -1 - child_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;$$;


CREATE OR REPLACE FUNCTION lightpathexcitationfilterlink_parent_index_insert() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    DECLARE
      duplicate INT8;
    BEGIN

      -- At most, there should be one duplicate
      SELECT id INTO duplicate
        FROM lightpathexcitationfilterlink
       WHERE parent = new.parent AND parent_index = new.parent_index
      OFFSET 0
       LIMIT 1;

      IF duplicate IS NOT NULL THEN
          RAISE NOTICE 'Remapping lightpathexcitationfilterlink % via (-1 - oldvalue )', duplicate;
          UPDATE lightpathexcitationfilterlink SET parent_index = -1 - parent_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;$$;


CREATE OR REPLACE FUNCTION lightpathexcitationfilterlink_parent_index_update() RETURNS trigger
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
        FROM lightpathexcitationfilterlink
       WHERE parent = new.parent AND parent_index = new.parent_index
      OFFSET 0
       LIMIT 1;

      IF duplicate IS NOT NULL THEN
          RAISE NOTICE 'Remapping lightpathexcitationfilterlink % via (-1 - oldvalue )', duplicate;
          UPDATE lightpathexcitationfilterlink SET parent_index = -1 - parent_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;$$;


CREATE OR REPLACE FUNCTION pixels_image_index_insert() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    DECLARE
      duplicate INT8;
    BEGIN

      -- At most, there should be one duplicate
      SELECT id INTO duplicate
        FROM pixels
       WHERE image = new.image AND image_index = new.image_index
      OFFSET 0
       LIMIT 1;

      IF duplicate IS NOT NULL THEN
          RAISE NOTICE 'Remapping pixels % via (-1 - oldvalue )', duplicate;
          UPDATE pixels SET image_index = -1 - image_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;$$;


CREATE OR REPLACE FUNCTION pixels_image_index_update() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    DECLARE
      duplicate INT8;
    BEGIN

      -- Avoids a query if the new and old values of x are the same.
      IF new.image = old.image AND new.image_index = old.image_index THEN
          RETURN new;
      END IF;

      -- At most, there should be one duplicate
      SELECT id INTO duplicate
        FROM pixels
       WHERE image = new.image AND image_index = new.image_index
      OFFSET 0
       LIMIT 1;

      IF duplicate IS NOT NULL THEN
          RAISE NOTICE 'Remapping pixels % via (-1 - oldvalue )', duplicate;
          UPDATE pixels SET image_index = -1 - image_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;$$;


CREATE OR REPLACE FUNCTION shape_roi_index_insert() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    DECLARE
      duplicate INT8;
    BEGIN

      -- At most, there should be one duplicate
      SELECT id INTO duplicate
        FROM shape
       WHERE roi = new.roi AND roi_index = new.roi_index
      OFFSET 0
       LIMIT 1;

      IF duplicate IS NOT NULL THEN
          RAISE NOTICE 'Remapping shape % via (-1 - oldvalue )', duplicate;
          UPDATE shape SET roi_index = -1 - roi_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;$$;


CREATE OR REPLACE FUNCTION shape_roi_index_update() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    DECLARE
      duplicate INT8;
    BEGIN

      -- Avoids a query if the new and old values of x are the same.
      IF new.roi = old.roi AND new.roi_index = old.roi_index THEN
          RETURN new;
      END IF;

      -- At most, there should be one duplicate
      SELECT id INTO duplicate
        FROM shape
       WHERE roi = new.roi AND roi_index = new.roi_index
      OFFSET 0
       LIMIT 1;

      IF duplicate IS NOT NULL THEN
          RAISE NOTICE 'Remapping shape % via (-1 - oldvalue )', duplicate;
          UPDATE shape SET roi_index = -1 - roi_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;$$;


CREATE OR REPLACE FUNCTION wellsample_well_index_insert() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    DECLARE
      duplicate INT8;
    BEGIN

      -- At most, there should be one duplicate
      SELECT id INTO duplicate
        FROM wellsample
       WHERE well = new.well AND well_index = new.well_index
      OFFSET 0
       LIMIT 1;

      IF duplicate IS NOT NULL THEN
          RAISE NOTICE 'Remapping wellsample % via (-1 - oldvalue )', duplicate;
          UPDATE wellsample SET well_index = -1 - well_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;$$;


CREATE OR REPLACE FUNCTION wellsample_well_index_update() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
    DECLARE
      duplicate INT8;
    BEGIN

      -- Avoids a query if the new and old values of x are the same.
      IF new.well = old.well AND new.well_index = old.well_index THEN
          RETURN new;
      END IF;

      -- At most, there should be one duplicate
      SELECT id INTO duplicate
        FROM wellsample
       WHERE well = new.well AND well_index = new.well_index
      OFFSET 0
       LIMIT 1;

      IF duplicate IS NOT NULL THEN
          RAISE NOTICE 'Remapping wellsample % via (-1 - oldvalue )', duplicate;
          UPDATE wellsample SET well_index = -1 - well_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;$$;


CREATE TRIGGER channel_pixels_index_trigger_insert
        BEFORE INSERT ON channel
        FOR EACH ROW
        EXECUTE PROCEDURE channel_pixels_index_insert();

CREATE TRIGGER channel_pixels_index_trigger_update
        BEFORE UPDATE ON channel
        FOR EACH ROW
        EXECUTE PROCEDURE channel_pixels_index_update();

CREATE TRIGGER channelbinding_renderingdef_index_trigger_insert
        BEFORE INSERT ON channelbinding
        FOR EACH ROW
        EXECUTE PROCEDURE channelbinding_renderingdef_index_insert();

CREATE TRIGGER channelbinding_renderingdef_index_trigger_update
        BEFORE UPDATE ON channelbinding
        FOR EACH ROW
        EXECUTE PROCEDURE channelbinding_renderingdef_index_update();

CREATE TRIGGER codomainmapcontext_renderingdef_index_trigger_insert
        BEFORE INSERT ON codomainmapcontext
        FOR EACH ROW
        EXECUTE PROCEDURE codomainmapcontext_renderingdef_index_insert();

CREATE TRIGGER codomainmapcontext_renderingdef_index_trigger_update
        BEFORE UPDATE ON codomainmapcontext
        FOR EACH ROW
        EXECUTE PROCEDURE codomainmapcontext_renderingdef_index_update();

CREATE TRIGGER lightpathexcitationfilterlink_parent_index_trigger_insert
        BEFORE INSERT ON lightpathexcitationfilterlink
        FOR EACH ROW
        EXECUTE PROCEDURE lightpathexcitationfilterlink_parent_index_insert();

CREATE TRIGGER lightpathexcitationfilterlink_parent_index_trigger_update
        BEFORE UPDATE ON lightpathexcitationfilterlink
        FOR EACH ROW
        EXECUTE PROCEDURE lightpathexcitationfilterlink_parent_index_update();

CREATE TRIGGER groupexperimentermap_child_index_trigger_insert
        BEFORE INSERT ON groupexperimentermap
        FOR EACH ROW
        EXECUTE PROCEDURE groupexperimentermap_child_index_insert();

CREATE TRIGGER groupexperimentermap_child_index_trigger_update
        BEFORE UPDATE ON groupexperimentermap
        FOR EACH ROW
        EXECUTE PROCEDURE groupexperimentermap_child_index_update();

CREATE TRIGGER pixels_image_index_trigger_insert
        BEFORE INSERT ON pixels
        FOR EACH ROW
        EXECUTE PROCEDURE pixels_image_index_insert();

CREATE TRIGGER pixels_image_index_trigger_update
        BEFORE UPDATE ON pixels
        FOR EACH ROW
        EXECUTE PROCEDURE pixels_image_index_update();

CREATE TRIGGER shape_roi_index_trigger_insert
        BEFORE INSERT ON shape
        FOR EACH ROW
        EXECUTE PROCEDURE shape_roi_index_insert();

CREATE TRIGGER shape_roi_index_trigger_update
        BEFORE UPDATE ON shape
        FOR EACH ROW
        EXECUTE PROCEDURE shape_roi_index_update();

CREATE TRIGGER wellsample_well_index_trigger_insert
        BEFORE INSERT ON wellsample
        FOR EACH ROW
        EXECUTE PROCEDURE wellsample_well_index_insert();

CREATE TRIGGER wellsample_well_index_trigger_update
        BEFORE UPDATE ON wellsample
        FOR EACH ROW
        EXECUTE PROCEDURE wellsample_well_index_update();

-- #2573

CREATE INDEX planeinfo_pixels ON planeinfo (pixels);

-- #2565

CREATE OR REPLACE FUNCTION omero_42_check_pg_advisory_lock() RETURNS text AS '
DECLARE
    txt text;
BEGIN
      BEGIN
        PERFORM pg_advisory_lock(1, 1);
        PERFORM pg_advisory_unlock(1, 1);
        RETURN ''ok'';
      EXCEPTION
        WHEN undefined_function THEN
         txt := chr(10) ||
            ''====================================================================================='' || chr(10) ||
            ''pg_advisory_lock does not exist!'' || chr(10) || chr(10) ||
            ''You should consider upgrading to PostgreSQL 8.2 or above'' || chr(10) ||
            ''Until then, you may experience infrequent key constraint issues on insert.'' || chr(10) ||
            ''====================================================================================='' || chr(10) || chr(10);
          RAISE WARNING ''%'', txt;
          RETURN txt;
      END;

END;' LANGUAGE plpgsql;
SELECT omero_42_check_pg_advisory_lock();
DROP FUNCTION omero_42_check_pg_advisory_lock();

--
-- FINISHED
--

UPDATE dbpatch set message = 'Database updated.', finished = now()
 WHERE currentVersion  = 'OMERO4.2'    and
          currentPatch    = 0          and
          previousVersion = 'OMERO4.1' and
          previousPatch   = 0;

COMMIT;
