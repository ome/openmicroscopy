--
-- Copyright 2008 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

-- This script upgrades a OMERO3__7 database to OMERO3A__1

--
-- A few non-conditional changes which may cause errors
-- since they are not present in certain versions
--
ALTER TABLE joboriginalfilelink DROP CONSTRAINT fkjoboriginalfilelink_parent_importjob;
ALTER TABLE joboriginalfilelink DROP CONSTRAINT fkjoboriginalfilelink_parent_scriptjob;

BEGIN;

CREATE OR REPLACE FUNCTION OMERO3A__1__upgrade() RETURNS varchar(255) AS $$
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

    SELECT INTO ann nextval('seq_annotation');
    INSERT INTO annotation
        (discriminator, id, owner_id, group_id, creation_id, permissions, external_id, textValue, name)
        VALUES
        ('/basic/text/', ann, mviews.owner_id, mviews.group_id, mviews.creation_id,
         mviews.permissions, mviews.external_id, mviews.content, '');

    INSERT INTO imageannotationlink
        (id, permissions, owner_id, creation_id, update_id, child, parent, group_id)
        VALUES
        (nextval('seq_imageannotationlink'),mviews.permissions, mviews.owner_id, mviews.creation_id, mviews.update_id, ann, mviews.image, mviews.group_id);

  END LOOP;
  DELETE FROM imageannotation;

  --
  -- Convert all dataset annotations to use new annotation framework
  --
  FOR mviews IN SELECT id, owner_id, group_id, creation_id, update_id, permissions, external_id, version, dataset, content
  FROM datasetannotation LOOP

    SELECT INTO ann nextval('seq_annotation');
    INSERT INTO annotation
      (discriminator, id, owner_id, group_id, creation_id, permissions, external_id, textValue, name)
      VALUES
      ('/basic/text/', ann, mviews.owner_id, mviews.group_id, mviews.creation_id,
      mviews.permissions, mviews.external_id, mviews.content, '');
    INSERT INTO datasetannotationlink
        (id, permissions, owner_id, creation_id, update_id, child, parent, group_id)
        VALUES
        (nextval('seq_datasetannotationlink'),mviews.permissions, mviews.owner_id, mviews.creation_id, mviews.update_id, ann, mviews.dataset, mviews.group_id);

  END LOOP;
  DELETE FROM datasetannotation;

  --
  -- Convert all project annotations to use new annotation framework
  --
  FOR mviews IN SELECT id, owner_id, group_id, creation_id, update_id, permissions, external_id, version, project, content
  FROM projectannotation LOOP

    SELECT INTO ann nextval('seq_annotation');
    INSERT INTO annotation
      (discriminator, id, owner_id, group_id, creation_id, permissions, external_id, textValue, name)
      VALUES
      ('/basic/text/', ann, mviews.owner_id, mviews.group_id, mviews.creation_id,
      mviews.permissions, mviews.external_id, mviews.content, '');
    INSERT INTO projectannotationlink
        (id, permissions, owner_id, creation_id, update_id, child, parent, group_id)
        VALUES
        (nextval('seq_projectannotationlink'),mviews.permissions, mviews.owner_id, mviews.creation_id, mviews.update_id, ann, mviews.project, mviews.group_id);

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

  RETURN 'success';
END;
$$ LANGUAGE plpgsql;

INSERT INTO dbpatch (currentVersion, currentPatch, previousVersion, previousPatch) values ('OMERO3A', 1, 'OMERO3', 7);
SELECT OMERO3A__1__upgrade();
DROP FUNCTION OMERO3A__1__upgrade();
UPDATE dbpatch SET message = 'Database updated.', finished = now()
    WHERE currentVersion  = 'OMERO3A'   AND
          currentPatch    = 1           AND
          previousVersion = 'OMERO3'    AND
          previousPatch   = 7;
COMMIT;


