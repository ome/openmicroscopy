--
-- Copyright 2008 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

--
-- This upgrade script is the concatenation of all scripts
-- from OMERO3__5 to OMERO3A__3, with the version numbering,
-- extra BEGINs and COMMITs, and a few internal corrections
-- removed.
--

-- This is added out side of the transaction because there are
-- a few database upgrade paths which did not have this created.

ALTER TABLE joboriginalfilelink
        DROP CONSTRAINT fkjoboriginalfilelink_parent_importjob;

-- OMERO3__5 --> OMERO3__6

BEGIN;

    alter  table channelbinding     add column  OMERO6_inputStart        double precision;
    update       channelbinding     set OMERO6_inputStart = (inputStart::numeric)::float8;
    alter  table channelbinding     drop column inputStart;
    alter  table channelbinding     rename OMERO6_inputStart to inputStart;
    alter  table channelbinding     alter column inputStart set not null;

    alter  table channelbinding     add column OMERO6_inputEnd double precision;
    update       channelbinding     set OMERO6_inputEnd = (inputEnd::numeric)::float8;
    alter  table channelbinding     drop column inputEnd;
    alter  table channelbinding     rename OMERO6_inputEnd to inputEnd;
    alter  table channelbinding     alter column inputEnd set not null;

    alter  table detector           add column OMERO6_voltage double precision;
    update       detector           set OMERO6_voltage = (voltage::numeric)::float8;
    alter  table detector           drop column voltage;
    alter  table detector           rename OMERO6_voltage to voltage;

    alter  table detector           add column OMERO6_gain double precision;
    update       detector           set OMERO6_gain = (gain::numeric)::float8;
    alter  table detector           drop column gain;
    alter  table detector           rename OMERO6_gain to gain;

    alter  table detector           add column OMERO6_offsetValue double precision;
    update       detector           set OMERO6_offsetValue = (offsetValue::numeric)::float8;
    alter  table detector           drop column offsetValue;
    alter  table detector           rename OMERO6_offsetValue to offsetValue;

    alter  table detectorsettings   add column OMERO6_voltage double precision;
    update       detectorsettings   set OMERO6_voltage = (voltage::numeric)::float8;
    alter  table detectorsettings   drop column voltage;
    alter  table detectorsettings   rename OMERO6_voltage to voltage;

    alter  table detectorsettings   add column OMERO6_gain double precision;
    update       detectorsettings   set OMERO6_gain = (gain::numeric)::float8;
    alter  table detectorsettings   drop column gain;
    alter  table detectorsettings   rename OMERO6_gain to gain;

    alter  table detectorsettings   add column OMERO6_offsetValue double precision;
    update       detectorsettings   set OMERO6_offsetValue = (offsetValue::numeric)::float8;
    alter  table detectorsettings   drop column offsetValue;
    alter  table detectorsettings   rename OMERO6_offsetValue to offsetValue;

    alter  table detectorsettings   add column OMERO6_readOutRate double precision;
    update       detectorsettings   set OMERO6_readOutRate = (readOutRate::numeric)::float8;
    alter  table detectorsettings   drop column readOutRate;
    alter  table detectorsettings   rename OMERO6_readOutRate to readOutRate;

    alter  table detectorsettings   add column OMERO6_amplification double precision;
    update       detectorsettings   set OMERO6_amplification = (amplification::numeric)::float8;
    alter  table detectorsettings   drop column amplification;
    alter  table detectorsettings   rename OMERO6_amplification to amplification;

    alter  table dummystatistics    add column OMERO6_example double precision;
    update       dummystatistics    set OMERO6_example = (example::numeric)::float8;
    alter  table dummystatistics    drop column example;
    alter  table dummystatistics    rename OMERO6_example to example;
    alter  table dummystatistics    alter column example set not null;

    alter  table imagingenvironment add column OMERO6_temperature double precision;
    update       imagingenvironment set OMERO6_temperature = (temperature::numeric)::float8;
    alter  table imagingenvironment drop column temperature;
    alter  table imagingenvironment rename OMERO6_temperature to temperature;

    alter  table imagingenvironment add column OMERO6_airPressure double precision;
    update       imagingenvironment set OMERO6_airPressure = (airPressure::numeric)::float8;
    alter  table imagingenvironment drop column airPressure;
    alter  table imagingenvironment rename OMERO6_airPressure to airPressure;

    alter  table imagingenvironment add column OMERO6_humidity double precision;
    update       imagingenvironment set OMERO6_humidity = (humidity::numeric)::float8;
    alter  table imagingenvironment drop column humidity;
    alter  table imagingenvironment rename OMERO6_humidity to humidity;

    alter  table imagingenvironment add column OMERO6_co2percent double precision;
    update       imagingenvironment set OMERO6_co2percent = (co2percent::numeric)::float8;
    alter  table imagingenvironment drop column co2percent;
    alter  table imagingenvironment rename OMERO6_co2percent to co2percent;

    alter  table lightsettings      add column OMERO6_power double precision;
    update       lightsettings      set OMERO6_power = (power::numeric)::float8;
    alter  table lightsettings      drop column power;
    alter  table lightsettings      rename OMERO6_power to power;

    alter  table lightsource        add column OMERO6_power double precision;
    update       lightsource        set OMERO6_power = (power::numeric)::float8;
    alter  table lightsource        drop column power;
    alter  table lightsource        rename OMERO6_power to power;
    alter  table lightsource        alter column power set not null;

    alter  table logicalchannel     add column OMERO6_ndFilter double precision;
    update       logicalchannel     set OMERO6_ndFilter = (ndFilter::numeric)::float8;
    alter  table logicalchannel     drop column ndFilter;
    alter  table logicalchannel     rename OMERO6_ndFilter to ndFilter;

    alter  table objective          add column OMERO6_lensNA double precision;
    update       objective          set OMERO6_lensNA = (lensNA::numeric)::float8;
    alter  table objective          drop column lensNA;
    alter  table objective          rename OMERO6_lensNA to lensNA;
    alter  table objective          alter column lensNA set not null;

    alter  table objectivesettings  add column OMERO6_correctionCollar double precision;
    update       objectivesettings  set OMERO6_correctionCollar = (correctionCollar::numeric)::float8;
    alter  table objectivesettings  drop column correctionCollar;
    alter  table objectivesettings  rename OMERO6_correctionCollar to correctionCollar;

    alter  table objectivesettings  add column OMERO6_refractiveIndex double precision;
    update       objectivesettings  set OMERO6_refractiveIndex = (refractiveIndex::numeric)::float8;
    alter  table objectivesettings  drop column refractiveIndex;
    alter  table objectivesettings  rename OMERO6_refractiveIndex to refractiveIndex;

    alter  table pixelsdimensions   add column OMERO6_sizeX double precision;
    update       pixelsdimensions   set OMERO6_sizeX = (sizeX::numeric)::float8;
    alter  table pixelsdimensions   drop column sizeX;
    alter  table pixelsdimensions   rename OMERO6_sizeX to sizeX;
    alter  table pixelsdimensions   alter column sizeX set not null;

    alter  table pixelsdimensions   add column OMERO6_sizeY double precision;
    update       pixelsdimensions   set OMERO6_sizeY = (sizeY::numeric)::float8;
    alter  table pixelsdimensions   drop column sizeY;
    alter  table pixelsdimensions   rename OMERO6_sizeY to sizeY;
    alter  table pixelsdimensions   alter column sizeY set not null;

    alter  table pixelsdimensions   add column OMERO6_sizeZ double precision;
    update       pixelsdimensions   set OMERO6_sizeZ = (sizeZ::numeric)::float8;
    alter  table pixelsdimensions   drop column sizeZ;
    alter  table pixelsdimensions   rename OMERO6_sizeZ to sizeZ;
    alter  table pixelsdimensions   alter column sizeZ set not null;

    alter  table planeinfo          add column OMERO6_timestamp double precision;
    update       planeinfo          set OMERO6_timestamp = (timestamp::numeric)::float8;
    alter  table planeinfo          drop column timestamp;
    alter  table planeinfo          rename OMERO6_timestamp to timestamp;
    alter  table planeinfo          alter column timestamp set not null;

    alter  table planeinfo          add column OMERO6_positionX double precision;
    update       planeinfo          set OMERO6_positionX = (positionX::numeric)::float8;
    alter  table planeinfo          drop column positionX;
    alter  table planeinfo          rename OMERO6_positionX to positionX;

    alter  table planeinfo          add column OMERO6_positionY double precision;
    update       planeinfo          set OMERO6_positionY = (positionY::numeric)::float8;
    alter  table planeinfo          drop column positionY;
    alter  table planeinfo          rename OMERO6_positionY to positionY;

    alter  table planeinfo          add column OMERO6_positionZ double precision;
    update       planeinfo          set OMERO6_positionZ = (positionZ::numeric)::float8;
    alter  table planeinfo          drop column positionZ;
    alter  table planeinfo          rename OMERO6_positionZ to positionZ;

    alter  table planeinfo          add column OMERO6_exposureTime double precision;
    update       planeinfo          set OMERO6_exposureTime = (exposureTime::numeric)::float8;
    alter  table planeinfo          drop column exposureTime;
    alter  table planeinfo          rename OMERO6_exposureTime to exposureTime;

    alter  table stagelabel         add column OMERO6_positionX double precision;
    update       stagelabel         set OMERO6_positionX = (positionX::numeric)::float8;
    alter  table stagelabel         drop column positionX;
    alter  table stagelabel         rename OMERO6_positionX to positionX;
    alter  table stagelabel         alter column positionX set not null;

    alter  table stagelabel         add column OMERO6_positionY double precision;
    update       stagelabel         set OMERO6_positionY = (positionY::numeric)::float8;
    alter  table stagelabel         drop column positionY;
    alter  table stagelabel         rename OMERO6_positionY to positionY;
    alter  table stagelabel         alter column positionY set not null;

    alter  table stagelabel         add column OMERO6_positionZ double precision;
    update       stagelabel         set OMERO6_positionZ = (positionZ::numeric)::float8;
    alter  table stagelabel         drop column positionZ;
    alter  table stagelabel         rename OMERO6_positionZ to positionZ;
    alter  table stagelabel         alter column positionZ set not null;

    alter  table transmittancerange add column OMERO6_transmittance double precision;
    update       transmittancerange set OMERO6_transmittance = (transmittance::numeric)::float8;
    alter  table transmittancerange drop column transmittance;
    alter  table transmittancerange rename OMERO6_transmittance to transmittance;
    alter  table transmittancerange alter column transmittance set not null;


-- OMERO3__6 --> OMERO3__7

    create table scriptjob (
        job_id int8 not null,
        description varchar(255),
        primary key (job_id)
    );

    alter table scriptjob
        add constraint FKscriptjob_job_id_job
        foreign key (job_id)
        references job;

    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'text/csv';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'text/plain';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'text/xml';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'text/x-python';
    insert into jobstatus (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_jobstatus'),-35,0,0,0,'Cancelled';
    insert into eventtype (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_eventtype'),-35,0,0,0,'Processing';

-- OMERO3__7 --> OMERO3A__1

CREATE OR REPLACE FUNCTION OMERO3A__1__upgrade() RETURNS varchar(255) AS '
DECLARE
    mviews RECORD;
    indexed RECORD;
    cnt INT8;
    ann INT8;
    link INT8;
    new_annotated INT8;
    new_annotation INT8;
BEGIN

  -- Adding configuration table for ticket:800
  CREATE TABLE configuration ( name VARCHAR(255) PRIMARY KEY, value TEXT );

  -- RenderingDef.name
  ALTER TABLE renderingdef ADD COLUMN name VARCHAR(255);

  -- For the ordering in the following scripts to work
  -- properly, it is important that NULLs be set to
  -- FALSE.

  UPDATE groupexperimentermap SET defaultgrouplink = false WHERE defaultgrouplink IS NULL;
  UPDATE pixels               SET defaultpixels = false    WHERE defaultpixels    IS NULL;

  --
  -- Simple renamings as opposed to deletes suggested by apgdiff
  --

  ALTER TABLE channel RENAME COLUMN index TO pixels_index;
  ALTER TABLE channel ALTER  COLUMN          pixels_index SET NOT NULL;
  ALTER TABLE channelbinding RENAME COLUMN index TO renderingdef_index;
  ALTER TABLE channelbinding ALTER  COLUMN          renderingdef_index SET NOT NULL;
  ALTER TABLE codomainmapcontext RENAME COLUMN index TO renderingdef_index;
  ALTER TABLE codomainmapcontext ALTER  COLUMN          renderingdef_index SET NOT NULL;

  ALTER TABLE groupexperimentermap ADD COLUMN child_index int4;
  ALTER TABLE pixels ADD COLUMN image_index int4;

  --
  -- Minor other changes
  --
  ALTER TABLE externalinfo ALTER COLUMN lsid DROP NOT NULL;
  ALTER TABLE externalinfo ADD COLUMN uuid character varying(255);

  --
  -- Creating tables and sequences. Generated.
  -- Should match hibernate declarations of 
  -- DROP SEQ/CREATE SEQ/CREATE TBL/ALTER TBL (constraints)
  --
  -- (skips DROP TBL, and ALTER TBL columns)
  --


  DROP SEQUENCE seq_boundingbox;
  DROP SEQUENCE seq_datasetannotation;
  DROP SEQUENCE seq_imageannotation;
  DROP SEQUENCE seq_metadata;
  DROP SEQUENCE seq_overlaytype;
  DROP SEQUENCE seq_projectannotation;
  DROP SEQUENCE seq_region;
  DROP SEQUENCE seq_regiontype;
  DROP SEQUENCE seq_roi5d;
  DROP SEQUENCE seq_roiextent;
  DROP SEQUENCE seq_roimap;
  DROP SEQUENCE seq_roiset;
  DROP SEQUENCE seq_shapearea;
  DROP SEQUENCE seq_specification;
  DROP SEQUENCE seq_ushape;
  DROP SEQUENCE seq_uslice;
  DROP SEQUENCE seq_xyctoxylink;
  DROP SEQUENCE seq_xycttoxyclink;
  DROP SEQUENCE seq_xycttoxytlink;
  DROP SEQUENCE seq_xyttoxylink;
  DROP SEQUENCE seq_xyzctoxyclink;
  DROP SEQUENCE seq_xyzctoxyzlink;
  DROP SEQUENCE seq_xyzcttoxyctlink;
  DROP SEQUENCE seq_xyzcttoxyzclink;
  DROP SEQUENCE seq_xyzcttoxyztlink;
  DROP SEQUENCE seq_xyztoxylink;
  DROP SEQUENCE seq_xyzttoxytlink;
  DROP SEQUENCE seq_xyzttoxyzlink;
  CREATE SEQUENCE seq_annotation;
  CREATE SEQUENCE seq_annotationannotationlink;
  CREATE SEQUENCE seq_channelannotationlink;
  CREATE SEQUENCE seq_datasetannotationlink;
  CREATE SEQUENCE seq_experimenterannotationlink;
  CREATE SEQUENCE seq_imageannotationlink;
  CREATE SEQUENCE seq_originalfileannotationlink;
  CREATE SEQUENCE seq_pixelsannotationlink;
  CREATE SEQUENCE seq_planeinfoannotationlink;
  CREATE SEQUENCE seq_projectannotationlink;
  CREATE SEQUENCE seq_roi;
  CREATE SEQUENCE seq_roilink;
  CREATE SEQUENCE seq_roilinkannotationlink;
  CREATE TABLE annotation (
        discriminator character varying(31) NOT NULL,
        id bigint NOT NULL,
        permissions bigint NOT NULL,
        name character varying(255),
        textvalue text,
        doublevalue double precision,
        boolvalue boolean,
        longvalue bigint,
        thumbnail bigint,
        timevalue timestamp without time zone,
        group_id bigint NOT NULL,
        owner_id bigint NOT NULL,
        creation_id bigint NOT NULL,
        external_id bigint,
        file bigint
  );
  CREATE TABLE annotationannotationlink (
        id bigint NOT NULL,
        permissions bigint NOT NULL,
        version integer,
        parent bigint NOT NULL,
        child bigint NOT NULL,
        owner_id bigint NOT NULL,
        external_id bigint,
        creation_id bigint NOT NULL,
        update_id bigint NOT NULL,
        group_id bigint NOT NULL
  );
  CREATE TABLE channelannotationlink (
        id bigint NOT NULL,
        permissions bigint NOT NULL,
        version integer,
        parent bigint NOT NULL,
        creation_id bigint NOT NULL,
        owner_id bigint NOT NULL,
        group_id bigint NOT NULL,
        external_id bigint,
        child bigint NOT NULL,
        update_id bigint NOT NULL
  );

  CREATE TABLE datasetannotationlink (
        id bigint NOT NULL,
        permissions bigint NOT NULL,
        version integer,
        creation_id bigint NOT NULL,
        owner_id bigint NOT NULL,
        child bigint NOT NULL,
        update_id bigint NOT NULL,
        group_id bigint NOT NULL,
        parent bigint NOT NULL,
        external_id bigint
  );

  CREATE TABLE experimenterannotationlink (
        id bigint NOT NULL,
        permissions bigint NOT NULL,
        version integer,
        owner_id bigint NOT NULL,
        group_id bigint NOT NULL,
        creation_id bigint NOT NULL,
        child bigint NOT NULL,
        update_id bigint NOT NULL,
        external_id bigint,
        parent bigint NOT NULL
  );

  CREATE TABLE imageannotationlink (
        id bigint NOT NULL,
        permissions bigint NOT NULL,
        version integer,
        owner_id bigint NOT NULL,
        creation_id bigint NOT NULL,
        external_id bigint,
        update_id bigint NOT NULL,
        child bigint NOT NULL,
        parent bigint NOT NULL,
        group_id bigint NOT NULL
  );

  CREATE TABLE originalfileannotationlink (
        id bigint NOT NULL,
        permissions bigint NOT NULL,
        version integer,
        update_id bigint NOT NULL,
        external_id bigint,
        parent bigint NOT NULL,
        group_id bigint NOT NULL,
        creation_id bigint NOT NULL,
        child bigint NOT NULL,
        owner_id bigint NOT NULL
  );

  CREATE TABLE pixelsannotationlink (
        id bigint NOT NULL,
        permissions bigint NOT NULL,
        version integer,
        child bigint NOT NULL,
        creation_id bigint NOT NULL,
        update_id bigint NOT NULL,
        owner_id bigint NOT NULL,
        group_id bigint NOT NULL,
        parent bigint NOT NULL,
        external_id bigint
  );

  CREATE TABLE planeinfoannotationlink (
        id bigint NOT NULL,
        permissions bigint NOT NULL,
        version integer,
        update_id bigint NOT NULL,
        parent bigint NOT NULL,
        owner_id bigint NOT NULL,
        child bigint NOT NULL,
        external_id bigint,
        creation_id bigint NOT NULL,
        group_id bigint NOT NULL
  );

  CREATE TABLE projectannotationlink (
        id bigint NOT NULL,
        permissions bigint NOT NULL,
        version integer,
        group_id bigint NOT NULL,
        creation_id bigint NOT NULL,
        update_id bigint NOT NULL,
        parent bigint NOT NULL,
        child bigint NOT NULL,
        owner_id bigint NOT NULL,
        external_id bigint
  );

  CREATE TABLE roi (
        discriminator character varying(31) NOT NULL,
        id bigint NOT NULL,
        c integer,
        permissions bigint NOT NULL,
        t integer,
        version integer,
        visible boolean NOT NULL,
        xml text,
        z integer,
        owner_id bigint NOT NULL,
        pixels bigint NOT NULL,
        creation_id bigint NOT NULL,
        group_id bigint NOT NULL,
        external_id bigint,
        update_id bigint NOT NULL
  );

  CREATE TABLE roilink (
        id bigint NOT NULL,
        permissions bigint NOT NULL,
        version integer,
        owner_id bigint NOT NULL,
        external_id bigint,
        child bigint NOT NULL,
        group_id bigint NOT NULL,
        update_id bigint NOT NULL,
        parent bigint NOT NULL,
        creation_id bigint NOT NULL
  );

  CREATE TABLE roilinkannotationlink (
        id bigint NOT NULL,
        permissions bigint NOT NULL,
        version integer,
        external_id bigint,
        owner_id bigint NOT NULL,
        group_id bigint NOT NULL,
        creation_id bigint NOT NULL,
        update_id bigint NOT NULL,
        child bigint NOT NULL,
        parent bigint NOT NULL
);

  ALTER TABLE annotation ADD CONSTRAINT annotation_pkey PRIMARY KEY (id);
  ALTER TABLE annotationannotationlink ADD CONSTRAINT annotationannotationlink_pkey PRIMARY KEY (id);
  ALTER TABLE channelannotationlink ADD CONSTRAINT channelannotationlink_pkey PRIMARY KEY (id);
  ALTER TABLE datasetannotationlink ADD CONSTRAINT datasetannotationlink_pkey PRIMARY KEY (id);
  ALTER TABLE experimenterannotationlink ADD CONSTRAINT experimenterannotationlink_pkey PRIMARY KEY (id);
  ALTER TABLE imageannotationlink ADD CONSTRAINT imageannotationlink_pkey PRIMARY KEY (id);
  ALTER TABLE originalfileannotationlink ADD CONSTRAINT originalfileannotationlink_pkey PRIMARY KEY (id);
  ALTER TABLE pixelsannotationlink ADD CONSTRAINT pixelsannotationlink_pkey PRIMARY KEY (id);
  ALTER TABLE planeinfoannotationlink ADD CONSTRAINT planeinfoannotationlink_pkey PRIMARY KEY (id);
  ALTER TABLE projectannotationlink ADD CONSTRAINT projectannotationlink_pkey PRIMARY KEY (id);
  ALTER TABLE roi ADD CONSTRAINT roi_pkey PRIMARY KEY (id);
  ALTER TABLE roilink ADD CONSTRAINT roilink_pkey PRIMARY KEY (id);
  ALTER TABLE roilinkannotationlink ADD CONSTRAINT roilinkannotationlink_pkey PRIMARY KEY (id);
  ALTER TABLE annotation ADD CONSTRAINT annotation_external_id_key UNIQUE (external_id);
  ALTER TABLE annotation ADD CONSTRAINT fkannotation_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);
  ALTER TABLE annotation ADD CONSTRAINT fkannotation_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);
  ALTER TABLE annotation ADD CONSTRAINT fkannotation_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);
  ALTER TABLE annotation ADD CONSTRAINT fkannotation_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);
  ALTER TABLE annotation ADD CONSTRAINT fkfileannotation_file_originalfile FOREIGN KEY (file) REFERENCES originalfile(id);
  ALTER TABLE annotation ADD CONSTRAINT fkthumbnailannotation_thumbnail_thumbnail FOREIGN KEY (thumbnail) REFERENCES thumbnail(id);
  ALTER TABLE annotationannotationlink ADD CONSTRAINT annotationannotationlink_external_id_key UNIQUE (external_id);
  ALTER TABLE annotationannotationlink ADD CONSTRAINT fkannotationannotationlink_child_annotation FOREIGN KEY (child) REFERENCES annotation(id);
  ALTER TABLE annotationannotationlink ADD CONSTRAINT fkannotationannotationlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);
  ALTER TABLE annotationannotationlink ADD CONSTRAINT fkannotationannotationlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);
  ALTER TABLE annotationannotationlink ADD CONSTRAINT fkannotationannotationlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);
  ALTER TABLE annotationannotationlink ADD CONSTRAINT fkannotationannotationlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);
  ALTER TABLE annotationannotationlink ADD CONSTRAINT fkannotationannotationlink_parent_annotation FOREIGN KEY (parent) REFERENCES annotation(id);
  ALTER TABLE annotationannotationlink ADD CONSTRAINT fkannotationannotationlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);
  ALTER TABLE channel ADD CONSTRAINT channel_id_key UNIQUE (id, pixels_index);
  ALTER TABLE channelannotationlink ADD CONSTRAINT channelannotationlink_external_id_key UNIQUE (external_id);
  ALTER TABLE channelannotationlink ADD CONSTRAINT fkchannelannotationlink_child_annotation FOREIGN KEY (child) REFERENCES annotation(id);
  ALTER TABLE channelannotationlink ADD CONSTRAINT fkchannelannotationlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);
  ALTER TABLE channelannotationlink ADD CONSTRAINT fkchannelannotationlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);
  ALTER TABLE channelannotationlink ADD CONSTRAINT fkchannelannotationlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);
  ALTER TABLE channelannotationlink ADD CONSTRAINT fkchannelannotationlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);
  ALTER TABLE channelannotationlink ADD CONSTRAINT fkchannelannotationlink_parent_channel FOREIGN KEY (parent) REFERENCES channel(id);
  ALTER TABLE channelannotationlink ADD CONSTRAINT fkchannelannotationlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);
  ALTER TABLE channelbinding ADD CONSTRAINT channelbinding_id_key UNIQUE (id, renderingdef_index);
  ALTER TABLE codomainmapcontext ADD CONSTRAINT codomainmapcontext_id_key UNIQUE (id, renderingdef_index);
  ALTER TABLE datasetannotationlink ADD CONSTRAINT datasetannotationlink_external_id_key UNIQUE (external_id);
  ALTER TABLE datasetannotationlink ADD CONSTRAINT fkdatasetannotationlink_child_annotation FOREIGN KEY (child) REFERENCES annotation(id);
  ALTER TABLE datasetannotationlink ADD CONSTRAINT fkdatasetannotationlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);
  ALTER TABLE datasetannotationlink ADD CONSTRAINT fkdatasetannotationlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);
  ALTER TABLE datasetannotationlink ADD CONSTRAINT fkdatasetannotationlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);
  ALTER TABLE datasetannotationlink ADD CONSTRAINT fkdatasetannotationlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);
  ALTER TABLE datasetannotationlink ADD CONSTRAINT fkdatasetannotationlink_parent_dataset FOREIGN KEY (parent) REFERENCES dataset(id);
  ALTER TABLE datasetannotationlink ADD CONSTRAINT fkdatasetannotationlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);
  ALTER TABLE experimenterannotationlink ADD CONSTRAINT experimenterannotationlink_external_id_key UNIQUE (external_id);
  ALTER TABLE experimenterannotationlink ADD CONSTRAINT fkexperimenterannotationlink_child_annotation FOREIGN KEY (child) REFERENCES annotation(id);
  ALTER TABLE experimenterannotationlink ADD CONSTRAINT fkexperimenterannotationlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);
  ALTER TABLE experimenterannotationlink ADD CONSTRAINT fkexperimenterannotationlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);
  ALTER TABLE experimenterannotationlink ADD CONSTRAINT fkexperimenterannotationlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);
  ALTER TABLE experimenterannotationlink ADD CONSTRAINT fkexperimenterannotationlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);
  ALTER TABLE experimenterannotationlink ADD CONSTRAINT fkexperimenterannotationlink_parent_experimenter FOREIGN KEY (parent) REFERENCES experimenter(id);
  ALTER TABLE experimenterannotationlink ADD CONSTRAINT fkexperimenterannotationlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);
  ALTER TABLE groupexperimentermap ADD CONSTRAINT groupexperimentermap_id_key UNIQUE (id, child_index);
  ALTER TABLE imageannotationlink ADD CONSTRAINT imageannotationlink_external_id_key UNIQUE (external_id);
  ALTER TABLE imageannotationlink ADD CONSTRAINT fkimageannotationlink_child_annotation FOREIGN KEY (child) REFERENCES annotation(id);
  ALTER TABLE imageannotationlink ADD CONSTRAINT fkimageannotationlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);
  ALTER TABLE imageannotationlink ADD CONSTRAINT fkimageannotationlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);
  ALTER TABLE imageannotationlink ADD CONSTRAINT fkimageannotationlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);
  ALTER TABLE imageannotationlink ADD CONSTRAINT fkimageannotationlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);
  ALTER TABLE imageannotationlink ADD CONSTRAINT fkimageannotationlink_parent_image FOREIGN KEY (parent) REFERENCES image(id);
  ALTER TABLE imageannotationlink ADD CONSTRAINT fkimageannotationlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);
  ALTER TABLE originalfileannotationlink ADD CONSTRAINT originalfileannotationlink_external_id_key UNIQUE (external_id);
  ALTER TABLE originalfileannotationlink ADD CONSTRAINT fkoriginalfileannotationlink_child_annotation FOREIGN KEY (child) REFERENCES annotation(id);
  ALTER TABLE originalfileannotationlink ADD CONSTRAINT fkoriginalfileannotationlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);
  ALTER TABLE originalfileannotationlink ADD CONSTRAINT fkoriginalfileannotationlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);
  ALTER TABLE originalfileannotationlink ADD CONSTRAINT fkoriginalfileannotationlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);
  ALTER TABLE originalfileannotationlink ADD CONSTRAINT fkoriginalfileannotationlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);
  ALTER TABLE originalfileannotationlink ADD CONSTRAINT fkoriginalfileannotationlink_parent_originalfile FOREIGN KEY (parent) REFERENCES originalfile(id);
  ALTER TABLE originalfileannotationlink ADD CONSTRAINT fkoriginalfileannotationlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);
  ALTER TABLE pixels ADD CONSTRAINT pixels_id_key UNIQUE (id, image_index);
  ALTER TABLE pixelsannotationlink ADD CONSTRAINT pixelsannotationlink_external_id_key UNIQUE (external_id);
  ALTER TABLE pixelsannotationlink ADD CONSTRAINT fkpixelsannotationlink_child_annotation FOREIGN KEY (child) REFERENCES annotation(id);
  ALTER TABLE pixelsannotationlink ADD CONSTRAINT fkpixelsannotationlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);
  ALTER TABLE pixelsannotationlink ADD CONSTRAINT fkpixelsannotationlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);
  ALTER TABLE pixelsannotationlink ADD CONSTRAINT fkpixelsannotationlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);
  ALTER TABLE pixelsannotationlink ADD CONSTRAINT fkpixelsannotationlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);
  ALTER TABLE pixelsannotationlink ADD CONSTRAINT fkpixelsannotationlink_parent_pixels FOREIGN KEY (parent) REFERENCES pixels(id);
  ALTER TABLE pixelsannotationlink ADD CONSTRAINT fkpixelsannotationlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);
  ALTER TABLE planeinfoannotationlink ADD CONSTRAINT planeinfoannotationlink_external_id_key UNIQUE (external_id);
  ALTER TABLE planeinfoannotationlink ADD CONSTRAINT fkplaneinfoannotationlink_child_annotation FOREIGN KEY (child) REFERENCES annotation(id);
  ALTER TABLE planeinfoannotationlink ADD CONSTRAINT fkplaneinfoannotationlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);
  ALTER TABLE planeinfoannotationlink ADD CONSTRAINT fkplaneinfoannotationlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);
  ALTER TABLE planeinfoannotationlink ADD CONSTRAINT fkplaneinfoannotationlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);
  ALTER TABLE planeinfoannotationlink ADD CONSTRAINT fkplaneinfoannotationlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);
  ALTER TABLE planeinfoannotationlink ADD CONSTRAINT fkplaneinfoannotationlink_parent_planeinfo FOREIGN KEY (parent) REFERENCES planeinfo(id);
  ALTER TABLE planeinfoannotationlink ADD CONSTRAINT fkplaneinfoannotationlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);
  ALTER TABLE projectannotationlink ADD CONSTRAINT projectannotationlink_external_id_key UNIQUE (external_id);
  ALTER TABLE projectannotationlink ADD CONSTRAINT fkprojectannotationlink_child_annotation FOREIGN KEY (child) REFERENCES annotation(id);
  ALTER TABLE projectannotationlink ADD CONSTRAINT fkprojectannotationlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);
  ALTER TABLE projectannotationlink ADD CONSTRAINT fkprojectannotationlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);
  ALTER TABLE projectannotationlink ADD CONSTRAINT fkprojectannotationlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);
  ALTER TABLE projectannotationlink ADD CONSTRAINT fkprojectannotationlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);
  ALTER TABLE projectannotationlink ADD CONSTRAINT fkprojectannotationlink_parent_project FOREIGN KEY (parent) REFERENCES project(id);
  ALTER TABLE projectannotationlink ADD CONSTRAINT fkprojectannotationlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);
  ALTER TABLE roi ADD CONSTRAINT roi_external_id_key UNIQUE (external_id);
  ALTER TABLE roi ADD CONSTRAINT fkroi_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);
  ALTER TABLE roi ADD CONSTRAINT fkroi_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);
  ALTER TABLE roi ADD CONSTRAINT fkroi_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);
  ALTER TABLE roi ADD CONSTRAINT fkroi_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);
  ALTER TABLE roi ADD CONSTRAINT fkroi_pixels_pixels FOREIGN KEY (pixels) REFERENCES pixels(id);
  ALTER TABLE roi ADD CONSTRAINT fkroi_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);
  ALTER TABLE roilink ADD CONSTRAINT roilink_external_id_key UNIQUE (external_id);
  ALTER TABLE roilink ADD CONSTRAINT fkroilink_child_roi FOREIGN KEY (child) REFERENCES roi(id);
  ALTER TABLE roilink ADD CONSTRAINT fkroilink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);
  ALTER TABLE roilink ADD CONSTRAINT fkroilink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);
  ALTER TABLE roilink ADD CONSTRAINT fkroilink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);
  ALTER TABLE roilink ADD CONSTRAINT fkroilink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);
  ALTER TABLE roilink ADD CONSTRAINT fkroilink_parent_roi FOREIGN KEY (parent) REFERENCES roi(id);
  ALTER TABLE roilink ADD CONSTRAINT fkroilink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);
  ALTER TABLE roilinkannotationlink ADD CONSTRAINT roilinkannotationlink_external_id_key UNIQUE (external_id);
  ALTER TABLE roilinkannotationlink ADD CONSTRAINT fkroilinkannotationlink_child_annotation FOREIGN KEY (child) REFERENCES annotation(id);
  ALTER TABLE roilinkannotationlink ADD CONSTRAINT fkroilinkannotationlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);
  ALTER TABLE roilinkannotationlink ADD CONSTRAINT fkroilinkannotationlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);
  ALTER TABLE roilinkannotationlink ADD CONSTRAINT fkroilinkannotationlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);
  ALTER TABLE roilinkannotationlink ADD CONSTRAINT fkroilinkannotationlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);
  ALTER TABLE roilinkannotationlink ADD CONSTRAINT fkroilinkannotationlink_parent_roilink FOREIGN KEY (parent) REFERENCES roilink(id);
  ALTER TABLE roilinkannotationlink ADD CONSTRAINT fkroilinkannotationlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

  --
  -- Convert all groupexperimentermaps to indexed arrays
  --
  FOR indexed IN SELECT child as id FROM groupexperimentermap GROUP BY child LOOP
    cnt := 0;
    FOR mviews IN SELECT id FROM groupexperimentermap WHERE child = indexed.id ORDER BY defaultGroupLink DESC LOOP

        UPDATE groupexperimentermap SET child_index = cnt where id = mviews.id;
        cnt := cnt + 1;

    END LOOP;
  END LOOP;
  ALTER TABLE groupexperimentermap ALTER COLUMN child_index SET NOT NULL;
  ALTER TABLE groupexperimentermap DROP COLUMN defaultgrouplink;

  --
  -- Convert all pixels to indexed arrays
  FOR indexed IN SELECT image as id FROM pixels GROUP BY image LOOP
    cnt := 0;
    FOR mviews IN SELECT id FROM pixels WHERE image = indexed.id ORDER BY defaultPixels DESC LOOP

        UPDATE pixels SET image_index = cnt where id = mviews.id;
        cnt := cnt + 1;

    END LOOP;
  END LOOP;
  ALTER TABLE pixels ALTER COLUMN image_index SET NOT NULL;
  ALTER TABLE pixels DROP COLUMN defaultpixels;

  --
  -- Convert all image annotations to use new annotation framework
  --
  FOR mviews IN SELECT id, owner_id, group_id, creation_id, update_id, permissions, external_id, version, image, content
  FROM imageannotation LOOP

    SELECT INTO ann nextval(''seq_annotation'');
    INSERT INTO annotation
        (discriminator, id, owner_id, group_id, creation_id, permissions, external_id, textValue, name)
        VALUES
        (''/basic/text/'', ann, mviews.owner_id, mviews.group_id, mviews.creation_id,
         mviews.permissions, mviews.external_id, mviews.content, '''');

    INSERT INTO imageannotationlink
        (id, permissions, owner_id, creation_id, update_id, child, parent, group_id)
        VALUES
        (nextval(''seq_imageannotationlink''),mviews.permissions, mviews.owner_id, mviews.creation_id, mviews.update_id, ann, mviews.image, mviews.group_id);

  END LOOP;
  DELETE FROM imageannotation;

  --
  -- Convert all dataset annotations to use new annotation framework
  --
  FOR mviews IN SELECT id, owner_id, group_id, creation_id, update_id, permissions, external_id, version, dataset, content
  FROM datasetannotation LOOP

    SELECT INTO ann nextval(''seq_annotation'');
    INSERT INTO annotation
      (discriminator, id, owner_id, group_id, creation_id, permissions, external_id, textValue, name)
      VALUES
      (''/basic/text/'', ann, mviews.owner_id, mviews.group_id, mviews.creation_id,
      mviews.permissions, mviews.external_id, mviews.content, '''');
    INSERT INTO datasetannotationlink
        (id, permissions, owner_id, creation_id, update_id, child, parent, group_id)
        VALUES
        (nextval(''seq_datasetannotationlink''),mviews.permissions, mviews.owner_id, mviews.creation_id, mviews.update_id, ann, mviews.dataset, mviews.group_id);

  END LOOP;
  DELETE FROM datasetannotation;

  --
  -- Convert all project annotations to use new annotation framework
  --
  FOR mviews IN SELECT id, owner_id, group_id, creation_id, update_id, permissions, external_id, version, project, content
  FROM projectannotation LOOP

    SELECT INTO ann nextval(''seq_annotation'');
    INSERT INTO annotation
      (discriminator, id, owner_id, group_id, creation_id, permissions, external_id, textValue, name)
      VALUES
      (''/basic/text/'', ann, mviews.owner_id, mviews.group_id, mviews.creation_id,
      mviews.permissions, mviews.external_id, mviews.content, '''');
    INSERT INTO projectannotationlink
        (id, permissions, owner_id, creation_id, update_id, child, parent, group_id)
        VALUES
        (nextval(''seq_projectannotationlink''),mviews.permissions, mviews.owner_id, mviews.creation_id, mviews.update_id, ann, mviews.project, mviews.group_id);

  END LOOP;
  DELETE FROM projectannotation;

  --
  -- Table drops
  --
  DROP TABLE boundingbox CASCADE;
  DROP TABLE datasetannotation;
  DROP TABLE dummystatistics CASCADE;
  DROP TABLE imageannotation;
  DROP TABLE metadata CASCADE;
  DROP TABLE overlay CASCADE;
  DROP TABLE overlaytype CASCADE;
  DROP TABLE projectannotation;
  DROP TABLE region CASCADE;
  DROP TABLE regiontype CASCADE;
  DROP TABLE roi5d CASCADE;
  DROP TABLE roiextent CASCADE;
  DROP TABLE roimap CASCADE;
  DROP TABLE roiset CASCADE;
  DROP TABLE shapearea CASCADE;
  DROP TABLE specification CASCADE;
  DROP TABLE square CASCADE;
  DROP TABLE uroi CASCADE;
  DROP TABLE ushape CASCADE;
  DROP TABLE uslice CASCADE;
  DROP TABLE usquare CASCADE;
  DROP TABLE xy CASCADE;
  DROP TABLE xyc CASCADE;
  DROP TABLE xyct CASCADE;
  DROP TABLE xyctoxylink CASCADE;
  DROP TABLE xycttoxyclink CASCADE;
  DROP TABLE xycttoxytlink CASCADE;
  DROP TABLE xyt CASCADE;
  DROP TABLE xyttoxylink CASCADE;
  DROP TABLE xyz CASCADE;
  DROP TABLE xyzc CASCADE;
  DROP TABLE xyzct CASCADE;
  DROP TABLE xyzctoxyclink CASCADE;
  DROP TABLE xyzctoxyzlink CASCADE;
  DROP TABLE xyzcttoxyctlink CASCADE;
  DROP TABLE xyzcttoxyzclink CASCADE;
  DROP TABLE xyzcttoxyztlink CASCADE;
  DROP TABLE xyzt CASCADE;
  DROP TABLE xyztoxylink CASCADE;
  DROP TABLE xyzttoxytlink CASCADE;
  DROP TABLE xyzttoxyzlink CASCADE;

  --
  -- Adding count tables
  --

    create table count_Annotation_annotationLinks_by_owner (
        annotation_id int8 not null,
        count int8 not null,
        owner_id int8,
        primary key (annotation_id, owner_id)
    );

    create table count_CategoryGroup_categoryLinks_by_owner (
        categorygroup_id int8 not null,
        count int8 not null,
        owner_id int8,
        primary key (categorygroup_id, owner_id)
    );

    create table count_Category_categoryGroupLinks_by_owner (
        category_id int8 not null,
        count int8 not null,
        owner_id int8,
        primary key (category_id, owner_id)
    );

    create table count_Category_imageLinks_by_owner (
        category_id int8 not null,
        count int8 not null,
        owner_id int8,
        primary key (category_id, owner_id)
    );

    create table count_Channel_annotationLinks_by_owner (
        channel_id int8 not null,
        count int8 not null,
        owner_id int8,
        primary key (channel_id, owner_id)
    );

    create table count_Dataset_annotationLinks_by_owner (
        dataset_id int8 not null,
        count int8 not null,
        owner_id int8,
        primary key (dataset_id, owner_id)
    );

    create table count_Dataset_imageLinks_by_owner (
        dataset_id int8 not null,
        count int8 not null,
        owner_id int8,
        primary key (dataset_id, owner_id)
    );

    create table count_Dataset_projectLinks_by_owner (
        dataset_id int8 not null,
        count int8 not null,
        owner_id int8,
        primary key (dataset_id, owner_id)
    );

    create table count_ExperimenterGroup_groupExperimenterMap_by_owner (
        experimentergroup_id int8 not null,
        count int8 not null,
        owner_id int8,
        primary key (experimentergroup_id, owner_id)
    );

    create table count_Experimenter_annotationLinks_by_owner (
        experimenter_id int8 not null,
        count int8 not null,
        owner_id int8,
        primary key (experimenter_id, owner_id)
    );

    create table count_Experimenter_groupExperimenterMap_by_owner (
        experimenter_id int8 not null,
        count int8 not null,
        owner_id int8,
        primary key (experimenter_id, owner_id)
    );

    create table count_Image_annotationLinks_by_owner (
        image_id int8 not null,
        count int8 not null,
        owner_id int8,
        primary key (image_id, owner_id)
    );

    create table count_Image_categoryLinks_by_owner (
        image_id int8 not null,
        count int8 not null,
        owner_id int8,
        primary key (image_id, owner_id)
    );

    create table count_Image_datasetLinks_by_owner (
        image_id int8 not null,
        count int8 not null,
        owner_id int8,
        primary key (image_id, owner_id)
    );

    create table count_Job_originalFileLinks_by_owner (
        job_id int8 not null,
        count int8 not null,
        owner_id int8,
        primary key (job_id, owner_id)
    );

    create table count_OriginalFile_annotationLinks_by_owner (
        originalfile_id int8 not null,
        count int8 not null,
        owner_id int8,
        primary key (originalfile_id, owner_id)
    );

    create table count_OriginalFile_pixelsFileMaps_by_owner (
        originalfile_id int8 not null,
        count int8 not null,
        owner_id int8,
        primary key (originalfile_id, owner_id)
    );

    create table count_Pixels_annotationLinks_by_owner (
        pixels_id int8 not null,
        count int8 not null,
        owner_id int8,
        primary key (pixels_id, owner_id)
    );

    create table count_Pixels_pixelsFileMaps_by_owner (
        pixels_id int8 not null,
        count int8 not null,
        owner_id int8,
        primary key (pixels_id, owner_id)
    );

    create table count_PlaneInfo_annotationLinks_by_owner (
        planeinfo_id int8 not null,
        count int8 not null,
        owner_id int8,
        primary key (planeinfo_id, owner_id)
    );

    create table count_Project_annotationLinks_by_owner (
        project_id int8 not null,
        count int8 not null,
        owner_id int8,
        primary key (project_id, owner_id)
    );

    create table count_Project_datasetLinks_by_owner (
        project_id int8 not null,
        count int8 not null,
        owner_id int8,
        primary key (project_id, owner_id)
    );

    create table count_RoiLink_annotationLinks_by_owner (
        roilink_id int8 not null,
        count int8 not null,
        owner_id int8,
        primary key (roilink_id, owner_id)
    );

    alter table count_Annotation_annotationLinks_by_owner
        add constraint FK_count_to_Annotation_annotationLinks
        foreign key (annotation_id)
        references annotation;

    alter table count_CategoryGroup_categoryLinks_by_owner
        add constraint FK_count_to_CategoryGroup_categoryLinks
        foreign key (categorygroup_id)
        references categorygroup;

    alter table count_Category_categoryGroupLinks_by_owner
        add constraint FK_count_to_Category_categoryGroupLinks
        foreign key (category_id)
        references category;

    alter table count_Category_imageLinks_by_owner
        add constraint FK_count_to_Category_imageLinks
        foreign key (category_id)
        references category;

    alter table count_Channel_annotationLinks_by_owner
        add constraint FK_count_to_Channel_annotationLinks
        foreign key (channel_id)
        references channel;

    alter table count_Dataset_annotationLinks_by_owner
        add constraint FK_count_to_Dataset_annotationLinks
        foreign key (dataset_id)
        references dataset;

    alter table count_Dataset_imageLinks_by_owner
        add constraint FK_count_to_Dataset_imageLinks
        foreign key (dataset_id)
        references dataset;

    alter table count_Dataset_projectLinks_by_owner
        add constraint FK_count_to_Dataset_projectLinks
        foreign key (dataset_id)
        references dataset;

    alter table count_ExperimenterGroup_groupExperimenterMap_by_owner
        add constraint FK_count_to_ExperimenterGroup_groupExperimenterMap
        foreign key (experimentergroup_id)
        references experimentergroup;

    alter table count_Experimenter_annotationLinks_by_owner
        add constraint FK_count_to_Experimenter_annotationLinks
        foreign key (experimenter_id)
        references experimenter;

    alter table count_Experimenter_groupExperimenterMap_by_owner
        add constraint FK_count_to_Experimenter_groupExperimenterMap
        foreign key (experimenter_id)
        references experimenter;

    alter table count_Image_annotationLinks_by_owner
        add constraint FK_count_to_Image_annotationLinks
        foreign key (image_id)
        references image;

    alter table count_Image_categoryLinks_by_owner
        add constraint FK_count_to_Image_categoryLinks
        foreign key (image_id)
        references image;

    alter table count_Image_datasetLinks_by_owner
        add constraint FK_count_to_Image_datasetLinks
        foreign key (image_id)
        references image;

    alter table count_Job_originalFileLinks_by_owner
        add constraint FK_count_to_Job_originalFileLinks
        foreign key (job_id)
        references job;

    alter table count_OriginalFile_annotationLinks_by_owner
        add constraint FK_count_to_OriginalFile_annotationLinks
        foreign key (originalfile_id)
        references originalfile;

    alter table count_OriginalFile_pixelsFileMaps_by_owner
        add constraint FK_count_to_OriginalFile_pixelsFileMaps
        foreign key (originalfile_id)
        references originalfile;

    alter table count_Pixels_annotationLinks_by_owner
        add constraint FK_count_to_Pixels_annotationLinks
        foreign key (pixels_id)
        references pixels;

    alter table count_Pixels_pixelsFileMaps_by_owner
        add constraint FK_count_to_Pixels_pixelsFileMaps
        foreign key (pixels_id)
        references pixels;

    alter table count_PlaneInfo_annotationLinks_by_owner
        add constraint FK_count_to_PlaneInfo_annotationLinks
        foreign key (planeinfo_id)
        references planeinfo;

    alter table count_Project_annotationLinks_by_owner
        add constraint FK_count_to_Project_annotationLinks
        foreign key (project_id)
        references project;

    alter table count_Project_datasetLinks_by_owner
        add constraint FK_count_to_Project_datasetLinks
        foreign key (project_id)
        references project;

    alter table count_RoiLink_annotationLinks_by_owner
        add constraint FK_count_to_RoiLink_annotationLinks
        foreign key (roilink_id)
        references roilink;

  RETURN ''success'';
END;
' LANGUAGE plpgsql;

SELECT OMERO3A__1__upgrade();
DROP FUNCTION OMERO3A__1__upgrade();

-- OMERO3A__1 --> OMERO3A__2

CREATE SEQUENCE seq_session;

CREATE TABLE session (
	id bigint NOT NULL,
	closed timestamp without time zone,
	defaulteventtype character varying(255) NOT NULL,
	defaultpermissions character varying(255) NOT NULL,
	permissions bigint NOT NULL,
	message character varying(255),
	started timestamp without time zone NOT NULL,
	timetoidle bigint NOT NULL,
	timetolive bigint NOT NULL,
	useragent character varying(255),
	uuid character varying(255) NOT NULL,
	external_id bigint
);

ALTER TABLE event
	ADD COLUMN session bigint;

UPDATE event SET session = 1;

INSERT INTO experimenter (id,permissions,version,omename,firstname,lastname)
        VALUES (nextval('seq_experimenter'),0,0,'guest','Guest','Account');

INSERT INTO session
        (id,permissions,timetoidle,timetolive,started,closed,defaultpermissions,defaulteventtype,uuid)
        SELECT 0,-35,0,0,now(),now(),'rw----','BOOTSTRAP',0000;

INSERT INTO session
        (id,permissions,timetoidle,timetolive,started,closed,defaultpermissions,defaulteventtype,uuid)
        SELECT nextval('seq_session'),-35, 0,0,now(),now(),'rw----','PREVIOUSITEMS','1111';

INSERT INTO EVENTTYPE (id,permissions,owner_id,group_id,creation_id,value)
        SELECT nextval('seq_eventtype'),-35,0,0,0,'Sessions';

INSERT INTO experimentergroup (id,permissions,version,owner_id,group_id,creation_id,update_id,name)
        VALUES (nextval('seq_experimentergroup'),-35,0,0,0,0,0,'guest');

INSERT INTO groupexperimentermap
        (id,permissions,version,owner_id,group_id,creation_id,update_id, parent, child, child_index)
        SELECT nextval('seq_groupexperimentermap'),-35,0,0,0,0,0,g.id,e.id,0 FROM
        experimenter e, experimentergroup g WHERE e.omeName = 'guest' and g.name = 'guest';

INSERT INTO password SELECT id AS experimenter_id, '' AS hash FROM experimenter WHERE omename = 'guest';

ALTER TABLE event
        ALTER COLUMN session SET NOT NULL;

ALTER TABLE session
	ADD CONSTRAINT session_pkey PRIMARY KEY (id);

ALTER TABLE event
	ADD CONSTRAINT fkevent_session_session FOREIGN KEY ("session") REFERENCES "session"(id);

ALTER TABLE session
	ADD CONSTRAINT session_external_id_key UNIQUE (external_id);

ALTER TABLE session
	ADD CONSTRAINT fksession_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

-- OMERO3A__2 --> OMERO3A__3

UPDATE experimenttype SET value = 'Photoablation' WHERE value = 'Photablation';

CREATE SEQUENCE seq_plate;

CREATE SEQUENCE seq_plateannotationlink;

CREATE SEQUENCE seq_reagent;

CREATE SEQUENCE seq_reagentannotationlink;

CREATE SEQUENCE seq_screen;

CREATE SEQUENCE seq_screenacquisition;

CREATE SEQUENCE seq_screenacquisitionannotationlink;

CREATE SEQUENCE seq_screenannotationlink;

CREATE SEQUENCE seq_screenplatelink;

CREATE SEQUENCE seq_screenreagentlink;

CREATE SEQUENCE seq_well;

CREATE SEQUENCE seq_wellannotationlink;

CREATE SEQUENCE seq_wellsample;

CREATE SEQUENCE seq_wellsampleannotationlink;

CREATE SEQUENCE seq_wellsampleimagelink;

CREATE TABLE count_image_samplelinks_by_owner (
	image_id bigint NOT NULL,
	count bigint NOT NULL,
	owner_id bigint NOT NULL
);

CREATE TABLE count_plate_annotationlinks_by_owner (
	plate_id bigint NOT NULL,
	count bigint NOT NULL,
	owner_id bigint NOT NULL
);

CREATE TABLE count_plate_screenlinks_by_owner (
	plate_id bigint NOT NULL,
	count bigint NOT NULL,
	owner_id bigint NOT NULL
);

CREATE TABLE count_reagent_annotationlinks_by_owner (
	reagent_id bigint NOT NULL,
	count bigint NOT NULL,
	owner_id bigint NOT NULL
);

CREATE TABLE count_reagent_screenlinks_by_owner (
	reagent_id bigint NOT NULL,
	count bigint NOT NULL,
	owner_id bigint NOT NULL
);

CREATE TABLE count_screen_annotationlinks_by_owner (
	screen_id bigint NOT NULL,
	count bigint NOT NULL,
	owner_id bigint NOT NULL
);

CREATE TABLE count_screen_platelinks_by_owner (
	screen_id bigint NOT NULL,
	count bigint NOT NULL,
	owner_id bigint NOT NULL
);

CREATE TABLE count_screen_reagentlinks_by_owner (
	screen_id bigint NOT NULL,
	count bigint NOT NULL,
	owner_id bigint NOT NULL
);

CREATE TABLE count_screenacquisition_annotationlinks_by_owner (
	screenacquisition_id bigint NOT NULL,
	count bigint NOT NULL,
	owner_id bigint NOT NULL
);

CREATE TABLE count_well_annotationlinks_by_owner (
	well_id bigint NOT NULL,
	count bigint NOT NULL,
	owner_id bigint NOT NULL
);

CREATE TABLE count_wellsample_annotationlinks_by_owner (
	wellsample_id bigint NOT NULL,
	count bigint NOT NULL,
	owner_id bigint NOT NULL
);

CREATE TABLE count_wellsample_imagelinks_by_owner (
	wellsample_id bigint NOT NULL,
	count bigint NOT NULL,
	owner_id bigint NOT NULL
);

CREATE TABLE plate (
	id bigint NOT NULL,
	description text,
	permissions bigint NOT NULL,
	externalidentifier character varying(255),
	name character varying(255) NOT NULL,
	status character varying(255),
	version integer,
	external_id bigint,
	creation_id bigint NOT NULL,
	owner_id bigint NOT NULL,
	update_id bigint NOT NULL,
	group_id bigint NOT NULL
);

CREATE TABLE plateannotationlink (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	version integer,
	update_id bigint NOT NULL,
	child bigint NOT NULL,
	owner_id bigint NOT NULL,
	creation_id bigint NOT NULL,
	external_id bigint,
	parent bigint NOT NULL,
	group_id bigint NOT NULL
);

CREATE TABLE reagent (
	id bigint NOT NULL,
	description text,
	permissions bigint NOT NULL,
	name character varying(255) NOT NULL,
	reagentidentifier character varying(255),
	version integer,
	owner_id bigint NOT NULL,
	creation_id bigint NOT NULL,
	update_id bigint NOT NULL,
	external_id bigint,
	group_id bigint NOT NULL
);

CREATE TABLE reagentannotationlink (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	version integer,
	update_id bigint NOT NULL,
	group_id bigint NOT NULL,
	external_id bigint,
	parent bigint NOT NULL,
	creation_id bigint NOT NULL,
	owner_id bigint NOT NULL,
	child bigint NOT NULL
);

CREATE TABLE screen (
	id bigint NOT NULL,
	description text,
	permissions bigint NOT NULL,
	name character varying(255) NOT NULL,
	protocoldescription character varying(255),
	protocolidentifier character varying(255),
	reagentsetdescription character varying(255),
	reagentsetidentifier character varying(255),
	type character varying(255),
	version integer,
	update_id bigint NOT NULL,
	external_id bigint,
	owner_id bigint NOT NULL,
	creation_id bigint NOT NULL,
	group_id bigint NOT NULL
);

CREATE TABLE screenacquisition (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	endtime timestamp without time zone,
	starttime timestamp without time zone,
	version integer,
	group_id bigint NOT NULL,
	external_id bigint,
	creation_id bigint NOT NULL,
	screen bigint NOT NULL,
	update_id bigint NOT NULL,
	owner_id bigint NOT NULL
);

CREATE TABLE screenacquisitionannotationlink (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	version integer,
	owner_id bigint NOT NULL,
	update_id bigint NOT NULL,
	child bigint NOT NULL,
	creation_id bigint NOT NULL,
	group_id bigint NOT NULL,
	external_id bigint,
	parent bigint NOT NULL
);

CREATE TABLE screenannotationlink (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	version integer,
	owner_id bigint NOT NULL,
	creation_id bigint NOT NULL,
	child bigint NOT NULL,
	update_id bigint NOT NULL,
	parent bigint NOT NULL,
	group_id bigint NOT NULL,
	external_id bigint
);

CREATE TABLE screenplatelink (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	version integer,
	external_id bigint,
	parent bigint NOT NULL,
	owner_id bigint NOT NULL,
	update_id bigint NOT NULL,
	child bigint NOT NULL,
	creation_id bigint NOT NULL,
	group_id bigint NOT NULL
);

CREATE TABLE screenreagentlink (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	version integer,
	update_id bigint NOT NULL,
	external_id bigint,
	parent bigint NOT NULL,
	owner_id bigint NOT NULL,
	group_id bigint NOT NULL,
	creation_id bigint NOT NULL,
	child bigint NOT NULL
);

CREATE TABLE well (
	id bigint NOT NULL,
	"column" integer,
	permissions bigint NOT NULL,
	externaldescription character varying(255),
	externalidentifier character varying(255),
	row integer,
	type character varying(255),
	version integer,
	reagent bigint,
	group_id bigint NOT NULL,
	plate bigint NOT NULL,
	external_id bigint,
	creation_id bigint NOT NULL,
	owner_id bigint NOT NULL,
	update_id bigint NOT NULL
);

CREATE TABLE wellannotationlink (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	version integer,
	parent bigint NOT NULL,
	child bigint NOT NULL,
	creation_id bigint NOT NULL,
	group_id bigint NOT NULL,
	external_id bigint,
	owner_id bigint NOT NULL,
	update_id bigint NOT NULL
);

CREATE TABLE wellsample (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	posx double precision,
	posy double precision,
	timepoint integer,
	version integer,
	screenacquisition bigint NOT NULL,
	creation_id bigint NOT NULL,
	external_id bigint,
	update_id bigint NOT NULL,
	well bigint NOT NULL,
	owner_id bigint NOT NULL,
	group_id bigint NOT NULL,
	well_index integer NOT NULL
);

CREATE TABLE wellsampleannotationlink (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	version integer,
	update_id bigint NOT NULL,
	external_id bigint,
	group_id bigint NOT NULL,
	creation_id bigint NOT NULL,
	child bigint NOT NULL,
	owner_id bigint NOT NULL,
	parent bigint NOT NULL
);

CREATE TABLE wellsampleimagelink (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	version integer,
	parent bigint NOT NULL,
	external_id bigint,
	owner_id bigint NOT NULL,
	creation_id bigint NOT NULL,
	group_id bigint NOT NULL,
	child bigint NOT NULL,
	update_id bigint NOT NULL
);

ALTER TABLE logicalchannel DROP COLUMN pockelcellsetting;

ALTER TABLE logicalchannel ADD COLUMN pockelcellsetting integer;

ALTER TABLE planeinfo
	ALTER COLUMN timestamp DROP NOT NULL;

ALTER TABLE count_image_samplelinks_by_owner
	ADD CONSTRAINT count_image_samplelinks_by_owner_pkey PRIMARY KEY (image_id, owner_id);

ALTER TABLE count_plate_annotationlinks_by_owner
	ADD CONSTRAINT count_plate_annotationlinks_by_owner_pkey PRIMARY KEY (plate_id, owner_id);

ALTER TABLE count_plate_screenlinks_by_owner
	ADD CONSTRAINT count_plate_screenlinks_by_owner_pkey PRIMARY KEY (plate_id, owner_id);

ALTER TABLE count_reagent_annotationlinks_by_owner
	ADD CONSTRAINT count_reagent_annotationlinks_by_owner_pkey PRIMARY KEY (reagent_id, owner_id);

ALTER TABLE count_reagent_screenlinks_by_owner
	ADD CONSTRAINT count_reagent_screenlinks_by_owner_pkey PRIMARY KEY (reagent_id, owner_id);

ALTER TABLE count_screen_annotationlinks_by_owner
	ADD CONSTRAINT count_screen_annotationlinks_by_owner_pkey PRIMARY KEY (screen_id, owner_id);

ALTER TABLE count_screen_platelinks_by_owner
	ADD CONSTRAINT count_screen_platelinks_by_owner_pkey PRIMARY KEY (screen_id, owner_id);

ALTER TABLE count_screen_reagentlinks_by_owner
	ADD CONSTRAINT count_screen_reagentlinks_by_owner_pkey PRIMARY KEY (screen_id, owner_id);

ALTER TABLE count_screenacquisition_annotationlinks_by_owner
	ADD CONSTRAINT count_screenacquisition_annotationlinks_by_owner_pkey PRIMARY KEY (screenacquisition_id, owner_id);

ALTER TABLE count_well_annotationlinks_by_owner
	ADD CONSTRAINT count_well_annotationlinks_by_owner_pkey PRIMARY KEY (well_id, owner_id);

ALTER TABLE count_wellsample_annotationlinks_by_owner
	ADD CONSTRAINT count_wellsample_annotationlinks_by_owner_pkey PRIMARY KEY (wellsample_id, owner_id);

ALTER TABLE count_wellsample_imagelinks_by_owner
	ADD CONSTRAINT count_wellsample_imagelinks_by_owner_pkey PRIMARY KEY (wellsample_id, owner_id);

ALTER TABLE plate
	ADD CONSTRAINT plate_pkey PRIMARY KEY (id);

ALTER TABLE plateannotationlink
	ADD CONSTRAINT plateannotationlink_pkey PRIMARY KEY (id);

ALTER TABLE reagent
	ADD CONSTRAINT reagent_pkey PRIMARY KEY (id);

ALTER TABLE reagentannotationlink
	ADD CONSTRAINT reagentannotationlink_pkey PRIMARY KEY (id);

ALTER TABLE screen
	ADD CONSTRAINT screen_pkey PRIMARY KEY (id);

ALTER TABLE screenacquisition
	ADD CONSTRAINT screenacquisition_pkey PRIMARY KEY (id);

ALTER TABLE screenacquisitionannotationlink
	ADD CONSTRAINT screenacquisitionannotationlink_pkey PRIMARY KEY (id);

ALTER TABLE screenannotationlink
	ADD CONSTRAINT screenannotationlink_pkey PRIMARY KEY (id);

ALTER TABLE screenplatelink
	ADD CONSTRAINT screenplatelink_pkey PRIMARY KEY (id);

ALTER TABLE screenreagentlink
	ADD CONSTRAINT screenreagentlink_pkey PRIMARY KEY (id);

ALTER TABLE well
	ADD CONSTRAINT well_pkey PRIMARY KEY (id);

ALTER TABLE wellannotationlink
	ADD CONSTRAINT wellannotationlink_pkey PRIMARY KEY (id);

ALTER TABLE wellsample
	ADD CONSTRAINT wellsample_pkey PRIMARY KEY (id);

ALTER TABLE wellsampleannotationlink
	ADD CONSTRAINT wellsampleannotationlink_pkey PRIMARY KEY (id);

ALTER TABLE wellsampleimagelink
	ADD CONSTRAINT wellsampleimagelink_pkey PRIMARY KEY (id);

ALTER TABLE count_image_samplelinks_by_owner
	ADD CONSTRAINT fk_count_to_image_samplelinks FOREIGN KEY (image_id) REFERENCES image(id);

ALTER TABLE count_plate_annotationlinks_by_owner
	ADD CONSTRAINT fk_count_to_plate_annotationlinks FOREIGN KEY (plate_id) REFERENCES plate(id);

ALTER TABLE count_plate_screenlinks_by_owner
	ADD CONSTRAINT fk_count_to_plate_screenlinks FOREIGN KEY (plate_id) REFERENCES plate(id);

ALTER TABLE count_reagent_annotationlinks_by_owner
	ADD CONSTRAINT fk_count_to_reagent_annotationlinks FOREIGN KEY (reagent_id) REFERENCES reagent(id);

ALTER TABLE count_reagent_screenlinks_by_owner
	ADD CONSTRAINT fk_count_to_reagent_screenlinks FOREIGN KEY (reagent_id) REFERENCES reagent(id);

ALTER TABLE count_screen_annotationlinks_by_owner
	ADD CONSTRAINT fk_count_to_screen_annotationlinks FOREIGN KEY (screen_id) REFERENCES screen(id);

ALTER TABLE count_screen_platelinks_by_owner
	ADD CONSTRAINT fk_count_to_screen_platelinks FOREIGN KEY (screen_id) REFERENCES screen(id);

ALTER TABLE count_screen_reagentlinks_by_owner
	ADD CONSTRAINT fk_count_to_screen_reagentlinks FOREIGN KEY (screen_id) REFERENCES screen(id);

ALTER TABLE count_screenacquisition_annotationlinks_by_owner
	ADD CONSTRAINT fk_count_to_screenacquisition_annotationlinks FOREIGN KEY (screenacquisition_id) REFERENCES screenacquisition(id);

ALTER TABLE count_well_annotationlinks_by_owner
	ADD CONSTRAINT fk_count_to_well_annotationlinks FOREIGN KEY (well_id) REFERENCES well(id);

ALTER TABLE count_wellsample_annotationlinks_by_owner
	ADD CONSTRAINT fk_count_to_wellsample_annotationlinks FOREIGN KEY (wellsample_id) REFERENCES wellsample(id);

ALTER TABLE count_wellsample_imagelinks_by_owner
	ADD CONSTRAINT fk_count_to_wellsample_imagelinks FOREIGN KEY (wellsample_id) REFERENCES wellsample(id);

ALTER TABLE plate
	ADD CONSTRAINT plate_external_id_key UNIQUE (external_id);

ALTER TABLE plate
	ADD CONSTRAINT fkplate_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);

ALTER TABLE plate
	ADD CONSTRAINT fkplate_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE plate
	ADD CONSTRAINT fkplate_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);

ALTER TABLE plate
	ADD CONSTRAINT fkplate_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);

ALTER TABLE plate
	ADD CONSTRAINT fkplate_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE plateannotationlink
	ADD CONSTRAINT plateannotationlink_external_id_key UNIQUE (external_id);

ALTER TABLE plateannotationlink
	ADD CONSTRAINT fkplateannotationlink_child_annotation FOREIGN KEY (child) REFERENCES annotation(id);

ALTER TABLE plateannotationlink
	ADD CONSTRAINT fkplateannotationlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);

ALTER TABLE plateannotationlink
	ADD CONSTRAINT fkplateannotationlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE plateannotationlink
	ADD CONSTRAINT fkplateannotationlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);

ALTER TABLE plateannotationlink
	ADD CONSTRAINT fkplateannotationlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);

ALTER TABLE plateannotationlink
	ADD CONSTRAINT fkplateannotationlink_parent_plate FOREIGN KEY (parent) REFERENCES plate(id);

ALTER TABLE plateannotationlink
	ADD CONSTRAINT fkplateannotationlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE reagent
	ADD CONSTRAINT reagent_external_id_key UNIQUE (external_id);

ALTER TABLE reagent
	ADD CONSTRAINT fkreagent_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);

ALTER TABLE reagent
	ADD CONSTRAINT fkreagent_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE reagent
	ADD CONSTRAINT fkreagent_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);

ALTER TABLE reagent
	ADD CONSTRAINT fkreagent_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);

ALTER TABLE reagent
	ADD CONSTRAINT fkreagent_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE reagentannotationlink
	ADD CONSTRAINT reagentannotationlink_external_id_key UNIQUE (external_id);

ALTER TABLE reagentannotationlink
	ADD CONSTRAINT fkreagentannotationlink_child_annotation FOREIGN KEY (child) REFERENCES annotation(id);

ALTER TABLE reagentannotationlink
	ADD CONSTRAINT fkreagentannotationlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);

ALTER TABLE reagentannotationlink
	ADD CONSTRAINT fkreagentannotationlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE reagentannotationlink
	ADD CONSTRAINT fkreagentannotationlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);

ALTER TABLE reagentannotationlink
	ADD CONSTRAINT fkreagentannotationlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);

ALTER TABLE reagentannotationlink
	ADD CONSTRAINT fkreagentannotationlink_parent_reagent FOREIGN KEY (parent) REFERENCES reagent(id);

ALTER TABLE reagentannotationlink
	ADD CONSTRAINT fkreagentannotationlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE screen
	ADD CONSTRAINT screen_external_id_key UNIQUE (external_id);

ALTER TABLE screen
	ADD CONSTRAINT fkscreen_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);

ALTER TABLE screen
	ADD CONSTRAINT fkscreen_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE screen
	ADD CONSTRAINT fkscreen_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);

ALTER TABLE screen
	ADD CONSTRAINT fkscreen_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);

ALTER TABLE screen
	ADD CONSTRAINT fkscreen_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE screenacquisition
	ADD CONSTRAINT screenacquisition_external_id_key UNIQUE (external_id);

ALTER TABLE screenacquisition
	ADD CONSTRAINT fkscreenacquisition_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);

ALTER TABLE screenacquisition
	ADD CONSTRAINT fkscreenacquisition_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE screenacquisition
	ADD CONSTRAINT fkscreenacquisition_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);

ALTER TABLE screenacquisition
	ADD CONSTRAINT fkscreenacquisition_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);

ALTER TABLE screenacquisition
	ADD CONSTRAINT fkscreenacquisition_screen_screen FOREIGN KEY (screen) REFERENCES screen(id);

ALTER TABLE screenacquisition
	ADD CONSTRAINT fkscreenacquisition_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE screenacquisitionannotationlink
	ADD CONSTRAINT screenacquisitionannotationlink_external_id_key UNIQUE (external_id);

ALTER TABLE screenacquisitionannotationlink
	ADD CONSTRAINT fkscreenacquisitionannotationlink_child_annotation FOREIGN KEY (child) REFERENCES annotation(id);

ALTER TABLE screenacquisitionannotationlink
	ADD CONSTRAINT fkscreenacquisitionannotationlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);

ALTER TABLE screenacquisitionannotationlink
	ADD CONSTRAINT fkscreenacquisitionannotationlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE screenacquisitionannotationlink
	ADD CONSTRAINT fkscreenacquisitionannotationlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);

ALTER TABLE screenacquisitionannotationlink
	ADD CONSTRAINT fkscreenacquisitionannotationlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);

ALTER TABLE screenacquisitionannotationlink
	ADD CONSTRAINT fkscreenacquisitionannotationlink_parent_screenacquisition FOREIGN KEY (parent) REFERENCES screenacquisition(id);

ALTER TABLE screenacquisitionannotationlink
	ADD CONSTRAINT fkscreenacquisitionannotationlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE screenannotationlink
	ADD CONSTRAINT screenannotationlink_external_id_key UNIQUE (external_id);

ALTER TABLE screenannotationlink
	ADD CONSTRAINT fkscreenannotationlink_child_annotation FOREIGN KEY (child) REFERENCES annotation(id);

ALTER TABLE screenannotationlink
	ADD CONSTRAINT fkscreenannotationlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);

ALTER TABLE screenannotationlink
	ADD CONSTRAINT fkscreenannotationlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE screenannotationlink
	ADD CONSTRAINT fkscreenannotationlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);

ALTER TABLE screenannotationlink
	ADD CONSTRAINT fkscreenannotationlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);

ALTER TABLE screenannotationlink
	ADD CONSTRAINT fkscreenannotationlink_parent_screen FOREIGN KEY (parent) REFERENCES screen(id);

ALTER TABLE screenannotationlink
	ADD CONSTRAINT fkscreenannotationlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE screenplatelink
	ADD CONSTRAINT screenplatelink_external_id_key UNIQUE (external_id);

ALTER TABLE screenplatelink
	ADD CONSTRAINT fkscreenplatelink_child_plate FOREIGN KEY (child) REFERENCES plate(id);

ALTER TABLE screenplatelink
	ADD CONSTRAINT fkscreenplatelink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);

ALTER TABLE screenplatelink
	ADD CONSTRAINT fkscreenplatelink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE screenplatelink
	ADD CONSTRAINT fkscreenplatelink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);

ALTER TABLE screenplatelink
	ADD CONSTRAINT fkscreenplatelink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);

ALTER TABLE screenplatelink
	ADD CONSTRAINT fkscreenplatelink_parent_screen FOREIGN KEY (parent) REFERENCES screen(id);

ALTER TABLE screenplatelink
	ADD CONSTRAINT fkscreenplatelink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE screenreagentlink
	ADD CONSTRAINT screenreagentlink_external_id_key UNIQUE (external_id);

ALTER TABLE screenreagentlink
	ADD CONSTRAINT fkscreenreagentlink_child_reagent FOREIGN KEY (child) REFERENCES reagent(id);

ALTER TABLE screenreagentlink
	ADD CONSTRAINT fkscreenreagentlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);

ALTER TABLE screenreagentlink
	ADD CONSTRAINT fkscreenreagentlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE screenreagentlink
	ADD CONSTRAINT fkscreenreagentlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);

ALTER TABLE screenreagentlink
	ADD CONSTRAINT fkscreenreagentlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);

ALTER TABLE screenreagentlink
	ADD CONSTRAINT fkscreenreagentlink_parent_screen FOREIGN KEY (parent) REFERENCES screen(id);

ALTER TABLE screenreagentlink
	ADD CONSTRAINT fkscreenreagentlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE well
	ADD CONSTRAINT well_external_id_key UNIQUE (external_id);

ALTER TABLE well
	ADD CONSTRAINT fkwell_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);

ALTER TABLE well
	ADD CONSTRAINT fkwell_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE well
	ADD CONSTRAINT fkwell_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);

ALTER TABLE well
	ADD CONSTRAINT fkwell_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);

ALTER TABLE well
	ADD CONSTRAINT fkwell_plate_plate FOREIGN KEY (plate) REFERENCES plate(id);

ALTER TABLE well
	ADD CONSTRAINT fkwell_reagent_reagent FOREIGN KEY (reagent) REFERENCES reagent(id);

ALTER TABLE well
	ADD CONSTRAINT fkwell_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE wellannotationlink
	ADD CONSTRAINT wellannotationlink_external_id_key UNIQUE (external_id);

ALTER TABLE wellannotationlink
	ADD CONSTRAINT fkwellannotationlink_child_annotation FOREIGN KEY (child) REFERENCES annotation(id);

ALTER TABLE wellannotationlink
	ADD CONSTRAINT fkwellannotationlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);

ALTER TABLE wellannotationlink
	ADD CONSTRAINT fkwellannotationlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE wellannotationlink
	ADD CONSTRAINT fkwellannotationlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);

ALTER TABLE wellannotationlink
	ADD CONSTRAINT fkwellannotationlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);

ALTER TABLE wellannotationlink
	ADD CONSTRAINT fkwellannotationlink_parent_well FOREIGN KEY (parent) REFERENCES well(id);

ALTER TABLE wellannotationlink
	ADD CONSTRAINT fkwellannotationlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE wellsample
	ADD CONSTRAINT wellsample_external_id_key UNIQUE (external_id);

ALTER TABLE wellsample
	ADD CONSTRAINT fkwellsample_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);

ALTER TABLE wellsample
	ADD CONSTRAINT fkwellsample_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE wellsample
	ADD CONSTRAINT fkwellsample_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);

ALTER TABLE wellsample
	ADD CONSTRAINT fkwellsample_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);

ALTER TABLE wellsample
	ADD CONSTRAINT fkwellsample_screenacquisition_screenacquisition FOREIGN KEY (screenacquisition) REFERENCES screenacquisition(id);

ALTER TABLE wellsample
	ADD CONSTRAINT fkwellsample_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE wellsample
	ADD CONSTRAINT fkwellsample_well_well FOREIGN KEY (well) REFERENCES well(id);

ALTER TABLE wellsampleannotationlink
	ADD CONSTRAINT wellsampleannotationlink_external_id_key UNIQUE (external_id);

ALTER TABLE wellsampleannotationlink
	ADD CONSTRAINT fkwellsampleannotationlink_child_annotation FOREIGN KEY (child) REFERENCES annotation(id);

ALTER TABLE wellsampleannotationlink
	ADD CONSTRAINT fkwellsampleannotationlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);

ALTER TABLE wellsampleannotationlink
	ADD CONSTRAINT fkwellsampleannotationlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE wellsampleannotationlink
	ADD CONSTRAINT fkwellsampleannotationlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);

ALTER TABLE wellsampleannotationlink
	ADD CONSTRAINT fkwellsampleannotationlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);

ALTER TABLE wellsampleannotationlink
	ADD CONSTRAINT fkwellsampleannotationlink_parent_wellsample FOREIGN KEY (parent) REFERENCES wellsample(id);

ALTER TABLE wellsampleannotationlink
	ADD CONSTRAINT fkwellsampleannotationlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE wellsampleimagelink
	ADD CONSTRAINT wellsampleimagelink_external_id_key UNIQUE (external_id);

ALTER TABLE wellsampleimagelink
	ADD CONSTRAINT fkwellsampleimagelink_child_image FOREIGN KEY (child) REFERENCES image(id);

ALTER TABLE wellsampleimagelink
	ADD CONSTRAINT fkwellsampleimagelink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);

ALTER TABLE wellsampleimagelink
	ADD CONSTRAINT fkwellsampleimagelink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE wellsampleimagelink
	ADD CONSTRAINT fkwellsampleimagelink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);

ALTER TABLE wellsampleimagelink
	ADD CONSTRAINT fkwellsampleimagelink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);

ALTER TABLE wellsampleimagelink
	ADD CONSTRAINT fkwellsampleimagelink_parent_wellsample FOREIGN KEY (parent) REFERENCES wellsample(id);

ALTER TABLE wellsampleimagelink
	ADD CONSTRAINT fkwellsampleimagelink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE annotation RENAME COLUMN name TO ns;

INSERT INTO format (id,permissions,owner_id,group_id,creation_id,value)
    SELECT nextval('seq_format'),-35,0,0,0,'application/pdf';

INSERT INTO eventtype (id,permissions,owner_id,group_id,creation_id,value)
        SELECT nextval('seq_eventtype'),-35,0,0,0,'FullText';

INSERT INTO dbpatch (currentVersion, currentPatch, previousVersion, previousPatch, message, finished)
        VALUES ('OMERO3A',  4, 'OMERO3', 5, 'Database updated.', now());

COMMIT;
