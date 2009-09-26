--
-- Copyright 2009 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

--
-- OMERO-Beta4.1 release
--
--
BEGIN;

--
-- OMERO-Beta4.1 release.
--
BEGIN;

CREATE OR REPLACE FUNCTION omero_assert_omero4_0() RETURNS void AS '
DECLARE
    rec RECORD;
BEGIN

    SELECT INTO rec *
           FROM dbpatch
          WHERE id = ( SELECT id FROM dbpatch ORDER BY id DESC LIMIT 1 )
            AND currentversion = ''OMERO4''
            AND currentpatch = 0;

    IF NOT FOUND THEN
        RAISE EXCEPTION ''Current version is not OMERO4__0! Aborting...'';
    END IF;

END;' LANGUAGE plpgsql;
SELECT omero_assert_omero4_0();
DROP FUNCTION omero_assert_omero4_0();

INSERT into dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
             values ('OMERO4.1',     0,              'OMERO4',            0);

ALTER TABLE password add PRIMARY KEY (experimenter_id);

ALTER TABLE node    ADD UNIQUE (uuid);

ALTER TABLE session ADD UNIQUE (uuid);

ALTER TABLE plate
    ADD COLUMN columnNamingConvention varchar(255),
    ADD COLUMN rowNamingConvention varchar(255),
    ADD COLUMN defaultSample int4,
    ADD COLUMN wellOriginX float8,
    ADD COLUMN wellOriginY float8;

ALTER TABLE well
    ADD COLUMN red   int4,
    ADD COLUMN green int4,
    ADD COLUMN blue  int4,
    ADD COLUMN alpha int4;

ALTER table logicalchannel add column shapes int8;

create table roi (
    id int8 not null,
    description text,
    permissions int8 not null,
    version int4,
    creation_id int8 not null,
    external_id int8 unique,
    group_id int8 not null,
    owner_id int8 not null,
    update_id int8 not null,
    image int8 not null,
    source int8,
    primary key (id)
    );;

create table roiannotationlink (
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
    unique (parent, child)
    );;

    create table shape (
        discriminator varchar(31) not null,
        id int8 not null,
        permissions int8 not null,
        fillColor varchar(255),
        fillOpacity double precision,
        fillRule varchar(255),
        g varchar(255),
        locked bool,
        strokeColor varchar(255),
        strokeDashArray varchar(255),
        strokeDashOffset int4,
        strokeLineCap varchar(255),
        strokeLineJoin varchar(255),
        strokeMiterLimit int4,
        strokeOpacity double precision,
        strokeWidth int4,
        theT int4,
        theZ int4,
        transform varchar(255),
        vectorEffect varchar(255),
        version int4,
        visibility bool,
        cx float8,
        cy float8,
        rx float8,
        ry float8,
        height float8,
        width float8,
        x float8,
        y float8,
        points varchar(255),
        x1 float8,
        x2 float8,
        y1 float8,
        y2 float8,
        bytes bytea,
        d varchar(255),
        anchor varchar(255),
        baselineShift varchar(255),
        decoration varchar(255),
        direction varchar(255),
        fontFamily varchar(255),
        fontSize int4,
        fontStretch varchar(255),
        fontStyle varchar(255),
        fontVariant varchar(255),
        fontWeight varchar(255),
        glyphOrientationVertical int4,
        textValue text,
        writingMode varchar(255),
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        roi int8 not null,
        pixels int8,
        roi_index int4 not null,
        primary key (id),
        unique (roi, roi_index)
    );;

alter table logicalchannel 
    add constraint FKlogicalchannel_shapes_shape 
    foreign key (shapes) 
    references shape;;

alter table roi 
    add constraint FKroi_source_originalfile 
    foreign key (source) 
    references originalfile;;

alter table roi 
    add constraint FKroi_update_id_event 
    foreign key (update_id) 
    references event;;

alter table roi 
    add constraint FKroi_owner_id_experimenter 
    foreign key (owner_id) 
    references experimenter;;

alter table roi 
    add constraint FKroi_creation_id_event 
    foreign key (creation_id) 
    references event;;

alter table roi 
    add constraint FKroi_group_id_experimentergroup 
    foreign key (group_id) 
    references experimentergroup;;

alter table roi 
    add constraint FKroi_external_id_externalinfo 
    foreign key (external_id) 
    references externalinfo;;

alter table roi 
    add constraint FKroi_image_image 
    foreign key (image) 
    references image;;

alter table roiannotationlink 
    add constraint FKroiannotationlink_child_annotation 
    foreign key (child) 
    references annotation;;

alter table roiannotationlink 
    add constraint FKroiannotationlink_update_id_event 
    foreign key (update_id) 
    references event;;

alter table roiannotationlink 
    add constraint FKroiannotationlink_owner_id_experimenter 
    foreign key (owner_id) 
    references experimenter;;

alter table roiannotationlink 
    add constraint FKroiannotationlink_creation_id_event 
    foreign key (creation_id) 
    references event;;

alter table roiannotationlink 
    add constraint FKroiannotationlink_parent_roi 
    foreign key (parent) 
    references roi;;

alter table roiannotationlink 
    add constraint FKroiannotationlink_group_id_experimentergroup 
    foreign key (group_id) 
    references experimentergroup;;

alter table roiannotationlink 
    add constraint FKroiannotationlink_external_id_externalinfo 
    foreign key (external_id) 
    references externalinfo;;

alter table shape 
    add constraint FKmask_pixels_pixels 
    foreign key (pixels) 
    references pixels;;

alter table shape 
    add constraint FKshape_update_id_event 
    foreign key (update_id) 
    references event;;

alter table shape 
    add constraint FKshape_owner_id_experimenter 
    foreign key (owner_id) 
    references experimenter;;

alter table shape 
    add constraint FKshape_roi_roi 
    foreign key (roi) 
    references roi;;

alter table shape 
    add constraint FKshape_creation_id_event 
    foreign key (creation_id) 
    references event;;

alter table shape 
    add constraint FKshape_group_id_experimentergroup 
    foreign key (group_id) 
    references experimentergroup;;

alter table shape 
    add constraint FKshape_external_id_externalinfo 
    foreign key (external_id) 
    references externalinfo;;

CREATE OR REPLACE VIEW count_Roi_annotationLinks_by_owner (Roi_id, owner_id, count) AS select parent, owner_id, count(*)
    FROM RoiAnnotationLink GROUP BY parent, owner_id ORDER BY parent;

CREATE OR REPLACE FUNCTION shape_roi_index_move() RETURNS "trigger" AS '
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
        RAISE NOTICE ''Remapping shape % via (-1 - oldvalue )'', duplicate;
        UPDATE shape SET roi_index = -1 - roi_index WHERE id = duplicate;
    END IF;

    RETURN new;
END;' LANGUAGE plpgsql;

CREATE TRIGGER shape_roi_index_trigger
    BEFORE UPDATE ON shape
    FOR EACH ROW EXECUTE PROCEDURE shape_roi_index_move ();


-- 4D4 --> 4D5

create or replace function uuid() returns character(36)
as '
    select substring(x.my_rand from 1 for 8)||''-''||
           substring(x.my_rand from 9 for 4)||''-4''||
           substring(x.my_rand from 13 for 3)||''-''||x.clock_1||
           substring(x.my_rand from 16 for 3)||''-''||
           substring(x.my_rand from 19 for 12)
from
(select md5(now()::text||random()) as my_rand, to_hex(8+(3*random())::int) as clock_1) as x;'
language sql;

insert into configuration (name, value) values ('omero.db.uuid',uuid());

-- fixing seq_format from OMERO4__0.sql upgrade script

update seq_table set next_val = (select id + 1 from format order by id desc limit 1) where sequence_name = 'seq_format';

-- enums_update : generated by dsl/resources/ome/dsl/enums_update.vm

insert into immersion (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_immersion'),-35,0,0,0,'Oil' from immersion where not exists(
        select 1 from immersion where value = 'Oil') limit 1;

insert into immersion (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_immersion'),-35,0,0,0,'Water' from immersion where not exists(
        select 1 from immersion where value = 'Water') limit 1;

insert into immersion (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_immersion'),-35,0,0,0,'WaterDipping' from immersion where not exists(
        select 1 from immersion where value = 'WaterDipping') limit 1;

insert into immersion (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_immersion'),-35,0,0,0,'Air' from immersion where not exists(
        select 1 from immersion where value = 'Air') limit 1;

insert into immersion (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_immersion'),-35,0,0,0,'Multi' from immersion where not exists(
        select 1 from immersion where value = 'Multi') limit 1;

insert into immersion (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_immersion'),-35,0,0,0,'Glycerol' from immersion where not exists(
        select 1 from immersion where value = 'Glycerol') limit 1;

insert into immersion (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_immersion'),-35,0,0,0,'Other' from immersion where not exists(
        select 1 from immersion where value = 'Other') limit 1;

insert into immersion (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_immersion'),-35,0,0,0,'Unknown' from immersion where not exists(
        select 1 from immersion where value = 'Unknown') limit 1;

insert into arctype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_arctype'),-35,0,0,0,'Hg' from arctype where not exists(
        select 1 from arctype where value = 'Hg') limit 1;

insert into arctype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_arctype'),-35,0,0,0,'Xe' from arctype where not exists(
        select 1 from arctype where value = 'Xe') limit 1;

insert into arctype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_arctype'),-35,0,0,0,'HgXe' from arctype where not exists(
        select 1 from arctype where value = 'HgXe') limit 1;

insert into arctype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_arctype'),-35,0,0,0,'Other' from arctype where not exists(
        select 1 from arctype where value = 'Other') limit 1;

insert into arctype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_arctype'),-35,0,0,0,'Unknown' from arctype where not exists(
        select 1 from arctype where value = 'Unknown') limit 1;

insert into renderingmodel (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_renderingmodel'),-35,0,0,0,'rgb' from renderingmodel where not exists(
        select 1 from renderingmodel where value = 'rgb') limit 1;

insert into renderingmodel (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_renderingmodel'),-35,0,0,0,'greyscale' from renderingmodel where not exists(
        select 1 from renderingmodel where value = 'greyscale') limit 1;

insert into acquisitionmode (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_acquisitionmode'),-35,0,0,0,'WideField' from acquisitionmode where not exists(
        select 1 from acquisitionmode where value = 'WideField') limit 1;

insert into acquisitionmode (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_acquisitionmode'),-35,0,0,0,'LaserScanningMicroscopy' from acquisitionmode where not exists(
        select 1 from acquisitionmode where value = 'LaserScanningMicroscopy') limit 1;

insert into acquisitionmode (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_acquisitionmode'),-35,0,0,0,'LaserScanningConfocal' from acquisitionmode where not exists(
        select 1 from acquisitionmode where value = 'LaserScanningConfocal') limit 1;

insert into acquisitionmode (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_acquisitionmode'),-35,0,0,0,'SpinningDiskConfocal' from acquisitionmode where not exists(
        select 1 from acquisitionmode where value = 'SpinningDiskConfocal') limit 1;

insert into acquisitionmode (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_acquisitionmode'),-35,0,0,0,'SlitScanConfocal' from acquisitionmode where not exists(
        select 1 from acquisitionmode where value = 'SlitScanConfocal') limit 1;

insert into acquisitionmode (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_acquisitionmode'),-35,0,0,0,'MultiPhotonMicroscopy' from acquisitionmode where not exists(
        select 1 from acquisitionmode where value = 'MultiPhotonMicroscopy') limit 1;

insert into acquisitionmode (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_acquisitionmode'),-35,0,0,0,'StructuredIllumination' from acquisitionmode where not exists(
        select 1 from acquisitionmode where value = 'StructuredIllumination') limit 1;

insert into acquisitionmode (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_acquisitionmode'),-35,0,0,0,'SingleMoleculeImaging' from acquisitionmode where not exists(
        select 1 from acquisitionmode where value = 'SingleMoleculeImaging') limit 1;

insert into acquisitionmode (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_acquisitionmode'),-35,0,0,0,'TotalInternalReflection' from acquisitionmode where not exists(
        select 1 from acquisitionmode where value = 'TotalInternalReflection') limit 1;

insert into acquisitionmode (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_acquisitionmode'),-35,0,0,0,'FluorescenceLifetime' from acquisitionmode where not exists(
        select 1 from acquisitionmode where value = 'FluorescenceLifetime') limit 1;

insert into acquisitionmode (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_acquisitionmode'),-35,0,0,0,'SpectralImaging' from acquisitionmode where not exists(
        select 1 from acquisitionmode where value = 'SpectralImaging') limit 1;

insert into acquisitionmode (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_acquisitionmode'),-35,0,0,0,'FluorescenceCorrelationSpectroscopy' from acquisitionmode where not exists(
        select 1 from acquisitionmode where value = 'FluorescenceCorrelationSpectroscopy') limit 1;

insert into acquisitionmode (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_acquisitionmode'),-35,0,0,0,'NearFieldScanningOpticalMicroscopy' from acquisitionmode where not exists(
        select 1 from acquisitionmode where value = 'NearFieldScanningOpticalMicroscopy') limit 1;

insert into acquisitionmode (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_acquisitionmode'),-35,0,0,0,'SecondHarmonicGenerationImaging' from acquisitionmode where not exists(
        select 1 from acquisitionmode where value = 'SecondHarmonicGenerationImaging') limit 1;

insert into acquisitionmode (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_acquisitionmode'),-35,0,0,0,'Other' from acquisitionmode where not exists(
        select 1 from acquisitionmode where value = 'Other') limit 1;

insert into acquisitionmode (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_acquisitionmode'),-35,0,0,0,'Unknown' from acquisitionmode where not exists(
        select 1 from acquisitionmode where value = 'Unknown') limit 1;

insert into binning (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_binning'),-35,0,0,0,'1x1' from binning where not exists(
        select 1 from binning where value = '1x1') limit 1;

insert into binning (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_binning'),-35,0,0,0,'2x2' from binning where not exists(
        select 1 from binning where value = '2x2') limit 1;

insert into binning (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_binning'),-35,0,0,0,'4x4' from binning where not exists(
        select 1 from binning where value = '4x4') limit 1;

insert into binning (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_binning'),-35,0,0,0,'8x8' from binning where not exists(
        select 1 from binning where value = '8x8') limit 1;

insert into family (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_family'),-35,0,0,0,'linear' from family where not exists(
        select 1 from family where value = 'linear') limit 1;

insert into family (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_family'),-35,0,0,0,'polynomial' from family where not exists(
        select 1 from family where value = 'polynomial') limit 1;

insert into family (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_family'),-35,0,0,0,'exponential' from family where not exists(
        select 1 from family where value = 'exponential') limit 1;

insert into family (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_family'),-35,0,0,0,'logarithmic' from family where not exists(
        select 1 from family where value = 'logarithmic') limit 1;

insert into medium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_medium'),-35,0,0,0,'Air' from medium where not exists(
        select 1 from medium where value = 'Air') limit 1;

insert into medium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_medium'),-35,0,0,0,'Oil' from medium where not exists(
        select 1 from medium where value = 'Oil') limit 1;

insert into medium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_medium'),-35,0,0,0,'Water' from medium where not exists(
        select 1 from medium where value = 'Water') limit 1;

insert into medium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_medium'),-35,0,0,0,'Glycerol' from medium where not exists(
        select 1 from medium where value = 'Glycerol') limit 1;

insert into medium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_medium'),-35,0,0,0,'Other' from medium where not exists(
        select 1 from medium where value = 'Other') limit 1;

insert into medium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_medium'),-35,0,0,0,'Unknown' from medium where not exists(
        select 1 from medium where value = 'Unknown') limit 1;

insert into pixelstype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_pixelstype'),-35,0,0,0,'bit' from pixelstype where not exists(
        select 1 from pixelstype where value = 'bit') limit 1;

insert into pixelstype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_pixelstype'),-35,0,0,0,'int8' from pixelstype where not exists(
        select 1 from pixelstype where value = 'int8') limit 1;

insert into pixelstype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_pixelstype'),-35,0,0,0,'int16' from pixelstype where not exists(
        select 1 from pixelstype where value = 'int16') limit 1;

insert into pixelstype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_pixelstype'),-35,0,0,0,'int32' from pixelstype where not exists(
        select 1 from pixelstype where value = 'int32') limit 1;

insert into pixelstype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_pixelstype'),-35,0,0,0,'uint8' from pixelstype where not exists(
        select 1 from pixelstype where value = 'uint8') limit 1;

insert into pixelstype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_pixelstype'),-35,0,0,0,'uint16' from pixelstype where not exists(
        select 1 from pixelstype where value = 'uint16') limit 1;

insert into pixelstype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_pixelstype'),-35,0,0,0,'uint32' from pixelstype where not exists(
        select 1 from pixelstype where value = 'uint32') limit 1;

insert into pixelstype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_pixelstype'),-35,0,0,0,'float' from pixelstype where not exists(
        select 1 from pixelstype where value = 'float') limit 1;

insert into pixelstype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_pixelstype'),-35,0,0,0,'double' from pixelstype where not exists(
        select 1 from pixelstype where value = 'double') limit 1;

insert into pixelstype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_pixelstype'),-35,0,0,0,'complex' from pixelstype where not exists(
        select 1 from pixelstype where value = 'complex') limit 1;

insert into pixelstype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_pixelstype'),-35,0,0,0,'double-complex' from pixelstype where not exists(
        select 1 from pixelstype where value = 'double-complex') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'PNG' from format where not exists(
        select 1 from format where value = 'PNG') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/PNG' from format where not exists(
        select 1 from format where value = 'Companion/PNG') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'JPEG' from format where not exists(
        select 1 from format where value = 'JPEG') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/JPEG' from format where not exists(
        select 1 from format where value = 'Companion/JPEG') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'PGM' from format where not exists(
        select 1 from format where value = 'PGM') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/PGM' from format where not exists(
        select 1 from format where value = 'Companion/PGM') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Fits' from format where not exists(
        select 1 from format where value = 'Fits') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/Fits' from format where not exists(
        select 1 from format where value = 'Companion/Fits') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'GIF' from format where not exists(
        select 1 from format where value = 'GIF') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/GIF' from format where not exists(
        select 1 from format where value = 'Companion/GIF') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'BMP' from format where not exists(
        select 1 from format where value = 'BMP') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/BMP' from format where not exists(
        select 1 from format where value = 'Companion/BMP') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Dicom' from format where not exists(
        select 1 from format where value = 'Dicom') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/Dicom' from format where not exists(
        select 1 from format where value = 'Companion/Dicom') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'BioRad' from format where not exists(
        select 1 from format where value = 'BioRad') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/BioRad' from format where not exists(
        select 1 from format where value = 'Companion/BioRad') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'IPLab' from format where not exists(
        select 1 from format where value = 'IPLab') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/IPLab' from format where not exists(
        select 1 from format where value = 'Companion/IPLab') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Deltavision' from format where not exists(
        select 1 from format where value = 'Deltavision') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/Deltavision' from format where not exists(
        select 1 from format where value = 'Companion/Deltavision') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'MRC' from format where not exists(
        select 1 from format where value = 'MRC') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/MRC' from format where not exists(
        select 1 from format where value = 'Companion/MRC') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Gatan' from format where not exists(
        select 1 from format where value = 'Gatan') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/Gatan' from format where not exists(
        select 1 from format where value = 'Companion/Gatan') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Imaris' from format where not exists(
        select 1 from format where value = 'Imaris') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/Imaris' from format where not exists(
        select 1 from format where value = 'Companion/Imaris') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'OpenlabRaw' from format where not exists(
        select 1 from format where value = 'OpenlabRaw') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/OpenlabRaw' from format where not exists(
        select 1 from format where value = 'Companion/OpenlabRaw') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'OMEXML' from format where not exists(
        select 1 from format where value = 'OMEXML') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/OMEXML' from format where not exists(
        select 1 from format where value = 'Companion/OMEXML') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'LIF' from format where not exists(
        select 1 from format where value = 'LIF') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/LIF' from format where not exists(
        select 1 from format where value = 'Companion/LIF') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'AVI' from format where not exists(
        select 1 from format where value = 'AVI') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/AVI' from format where not exists(
        select 1 from format where value = 'Companion/AVI') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'QT' from format where not exists(
        select 1 from format where value = 'QT') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/QT' from format where not exists(
        select 1 from format where value = 'Companion/QT') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Pict' from format where not exists(
        select 1 from format where value = 'Pict') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/Pict' from format where not exists(
        select 1 from format where value = 'Companion/Pict') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'SDT' from format where not exists(
        select 1 from format where value = 'SDT') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/SDT' from format where not exists(
        select 1 from format where value = 'Companion/SDT') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'EPS' from format where not exists(
        select 1 from format where value = 'EPS') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/EPS' from format where not exists(
        select 1 from format where value = 'Companion/EPS') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Slidebook' from format where not exists(
        select 1 from format where value = 'Slidebook') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/Slidebook' from format where not exists(
        select 1 from format where value = 'Companion/Slidebook') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Alicona' from format where not exists(
        select 1 from format where value = 'Alicona') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/Alicona' from format where not exists(
        select 1 from format where value = 'Companion/Alicona') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'MNG' from format where not exists(
        select 1 from format where value = 'MNG') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/MNG' from format where not exists(
        select 1 from format where value = 'Companion/MNG') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'NRRD' from format where not exists(
        select 1 from format where value = 'NRRD') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/NRRD' from format where not exists(
        select 1 from format where value = 'Companion/NRRD') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Khoros' from format where not exists(
        select 1 from format where value = 'Khoros') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/Khoros' from format where not exists(
        select 1 from format where value = 'Companion/Khoros') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Visitech' from format where not exists(
        select 1 from format where value = 'Visitech') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/Visitech' from format where not exists(
        select 1 from format where value = 'Companion/Visitech') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'LIM' from format where not exists(
        select 1 from format where value = 'LIM') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/LIM' from format where not exists(
        select 1 from format where value = 'Companion/LIM') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'PSD' from format where not exists(
        select 1 from format where value = 'PSD') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/PSD' from format where not exists(
        select 1 from format where value = 'Companion/PSD') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'InCell' from format where not exists(
        select 1 from format where value = 'InCell') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/InCell' from format where not exists(
        select 1 from format where value = 'Companion/InCell') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'ICS' from format where not exists(
        select 1 from format where value = 'ICS') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/ICS' from format where not exists(
        select 1 from format where value = 'Companion/ICS') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'PerkinElmer' from format where not exists(
        select 1 from format where value = 'PerkinElmer') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/PerkinElmer' from format where not exists(
        select 1 from format where value = 'Companion/PerkinElmer') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'TCS' from format where not exists(
        select 1 from format where value = 'TCS') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/TCS' from format where not exists(
        select 1 from format where value = 'Companion/TCS') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'FV1000' from format where not exists(
        select 1 from format where value = 'FV1000') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/FV1000' from format where not exists(
        select 1 from format where value = 'Companion/FV1000') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'ZeissZVI' from format where not exists(
        select 1 from format where value = 'ZeissZVI') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/ZeissZVI' from format where not exists(
        select 1 from format where value = 'Companion/ZeissZVI') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'IPW' from format where not exists(
        select 1 from format where value = 'IPW') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/IPW' from format where not exists(
        select 1 from format where value = 'Companion/IPW') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'LegacyND2' from format where not exists(
        select 1 from format where value = 'LegacyND2') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/LegacyND2' from format where not exists(
        select 1 from format where value = 'Companion/LegacyND2') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'ND2' from format where not exists(
        select 1 from format where value = 'ND2') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/ND2' from format where not exists(
        select 1 from format where value = 'Companion/ND2') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'PCI' from format where not exists(
        select 1 from format where value = 'PCI') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/PCI' from format where not exists(
        select 1 from format where value = 'Companion/PCI') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'ImarisHDF' from format where not exists(
        select 1 from format where value = 'ImarisHDF') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/ImarisHDF' from format where not exists(
        select 1 from format where value = 'Companion/ImarisHDF') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Metamorph' from format where not exists(
        select 1 from format where value = 'Metamorph') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/Metamorph' from format where not exists(
        select 1 from format where value = 'Companion/Metamorph') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'ZeissLSM' from format where not exists(
        select 1 from format where value = 'ZeissLSM') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/ZeissLSM' from format where not exists(
        select 1 from format where value = 'Companion/ZeissLSM') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'SEQ' from format where not exists(
        select 1 from format where value = 'SEQ') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/SEQ' from format where not exists(
        select 1 from format where value = 'Companion/SEQ') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Gel' from format where not exists(
        select 1 from format where value = 'Gel') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/Gel' from format where not exists(
        select 1 from format where value = 'Companion/Gel') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'ImarisTiff' from format where not exists(
        select 1 from format where value = 'ImarisTiff') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/ImarisTiff' from format where not exists(
        select 1 from format where value = 'Companion/ImarisTiff') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Flex' from format where not exists(
        select 1 from format where value = 'Flex') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/Flex' from format where not exists(
        select 1 from format where value = 'Companion/Flex') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'SVS' from format where not exists(
        select 1 from format where value = 'SVS') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/SVS' from format where not exists(
        select 1 from format where value = 'Companion/SVS') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Leica' from format where not exists(
        select 1 from format where value = 'Leica') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/Leica' from format where not exists(
        select 1 from format where value = 'Companion/Leica') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Nikon' from format where not exists(
        select 1 from format where value = 'Nikon') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/Nikon' from format where not exists(
        select 1 from format where value = 'Companion/Nikon') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Fluoview' from format where not exists(
        select 1 from format where value = 'Fluoview') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/Fluoview' from format where not exists(
        select 1 from format where value = 'Companion/Fluoview') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Prairie' from format where not exists(
        select 1 from format where value = 'Prairie') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/Prairie' from format where not exists(
        select 1 from format where value = 'Companion/Prairie') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Micromanager' from format where not exists(
        select 1 from format where value = 'Micromanager') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/Micromanager' from format where not exists(
        select 1 from format where value = 'Companion/Micromanager') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'ImprovisionTiff' from format where not exists(
        select 1 from format where value = 'ImprovisionTiff') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/ImprovisionTiff' from format where not exists(
        select 1 from format where value = 'Companion/ImprovisionTiff') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'OMETiff' from format where not exists(
        select 1 from format where value = 'OMETiff') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/OMETiff' from format where not exists(
        select 1 from format where value = 'Companion/OMETiff') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'MetamorphTiff' from format where not exists(
        select 1 from format where value = 'MetamorphTiff') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/MetamorphTiff' from format where not exists(
        select 1 from format where value = 'Companion/MetamorphTiff') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Tiff' from format where not exists(
        select 1 from format where value = 'Tiff') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/Tiff' from format where not exists(
        select 1 from format where value = 'Companion/Tiff') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Openlab' from format where not exists(
        select 1 from format where value = 'Openlab') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/Openlab' from format where not exists(
        select 1 from format where value = 'Companion/Openlab') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'MIAS' from format where not exists(
        select 1 from format where value = 'MIAS') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Companion/MIAS' from format where not exists(
        select 1 from format where value = 'Companion/MIAS') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'text/csv' from format where not exists(
        select 1 from format where value = 'text/csv') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'text/plain' from format where not exists(
        select 1 from format where value = 'text/plain') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'text/xml' from format where not exists(
        select 1 from format where value = 'text/xml') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'text/html' from format where not exists(
        select 1 from format where value = 'text/html') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'text/ini' from format where not exists(
        select 1 from format where value = 'text/ini') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'text/rtf' from format where not exists(
        select 1 from format where value = 'text/rtf') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'text/richtext' from format where not exists(
        select 1 from format where value = 'text/richtext') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'text/x-python' from format where not exists(
        select 1 from format where value = 'text/x-python') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'application/pdf' from format where not exists(
        select 1 from format where value = 'application/pdf') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'application/vnd.ms-excel' from format where not exists(
        select 1 from format where value = 'application/vnd.ms-excel') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'application/vnd.ms-powerpoint' from format where not exists(
        select 1 from format where value = 'application/vnd.ms-powerpoint') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'application/msword' from format where not exists(
        select 1 from format where value = 'application/msword') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'application/octet-stream' from format where not exists(
        select 1 from format where value = 'application/octet-stream') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'video/jpeg2000' from format where not exists(
        select 1 from format where value = 'video/jpeg2000') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'video/mpeg' from format where not exists(
        select 1 from format where value = 'video/mpeg') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'video/mp4' from format where not exists(
        select 1 from format where value = 'video/mp4') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'video/quicktime' from format where not exists(
        select 1 from format where value = 'video/quicktime') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'image/bmp' from format where not exists(
        select 1 from format where value = 'image/bmp') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'image/gif' from format where not exists(
        select 1 from format where value = 'image/gif') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'image/jpeg' from format where not exists(
        select 1 from format where value = 'image/jpeg') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'image/tiff' from format where not exists(
        select 1 from format where value = 'image/tiff') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'image/png' from format where not exists(
        select 1 from format where value = 'image/png') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'audio/basic' from format where not exists(
        select 1 from format where value = 'audio/basic') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'audio/mpeg' from format where not exists(
        select 1 from format where value = 'audio/mpeg') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'audio/wav' from format where not exists(
        select 1 from format where value = 'audio/wav') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Repository' from format where not exists(
        select 1 from format where value = 'Repository') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'Directory' from format where not exists(
        select 1 from format where value = 'Directory') limit 1;

insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_format'),-35,0,0,0,'OMERO.tables' from format where not exists(
        select 1 from format where value = 'OMERO.tables') limit 1;

insert into pulse (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_pulse'),-35,0,0,0,'CW' from pulse where not exists(
        select 1 from pulse where value = 'CW') limit 1;

insert into pulse (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_pulse'),-35,0,0,0,'Single' from pulse where not exists(
        select 1 from pulse where value = 'Single') limit 1;

insert into pulse (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_pulse'),-35,0,0,0,'QSwitched' from pulse where not exists(
        select 1 from pulse where value = 'QSwitched') limit 1;

insert into pulse (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_pulse'),-35,0,0,0,'Repetitive' from pulse where not exists(
        select 1 from pulse where value = 'Repetitive') limit 1;

insert into pulse (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_pulse'),-35,0,0,0,'ModeLocked' from pulse where not exists(
        select 1 from pulse where value = 'ModeLocked') limit 1;

insert into pulse (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_pulse'),-35,0,0,0,'Other' from pulse where not exists(
        select 1 from pulse where value = 'Other') limit 1;

insert into pulse (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_pulse'),-35,0,0,0,'Unknown' from pulse where not exists(
        select 1 from pulse where value = 'Unknown') limit 1;

insert into lasertype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasertype'),-35,0,0,0,'Excimer' from lasertype where not exists(
        select 1 from lasertype where value = 'Excimer') limit 1;

insert into lasertype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasertype'),-35,0,0,0,'Gas' from lasertype where not exists(
        select 1 from lasertype where value = 'Gas') limit 1;

insert into lasertype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasertype'),-35,0,0,0,'MetalVapor' from lasertype where not exists(
        select 1 from lasertype where value = 'MetalVapor') limit 1;

insert into lasertype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasertype'),-35,0,0,0,'SolidState' from lasertype where not exists(
        select 1 from lasertype where value = 'SolidState') limit 1;

insert into lasertype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasertype'),-35,0,0,0,'Dye' from lasertype where not exists(
        select 1 from lasertype where value = 'Dye') limit 1;

insert into lasertype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasertype'),-35,0,0,0,'Semiconductor' from lasertype where not exists(
        select 1 from lasertype where value = 'Semiconductor') limit 1;

insert into lasertype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasertype'),-35,0,0,0,'FreeElectron' from lasertype where not exists(
        select 1 from lasertype where value = 'FreeElectron') limit 1;

insert into lasertype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasertype'),-35,0,0,0,'Other' from lasertype where not exists(
        select 1 from lasertype where value = 'Other') limit 1;

insert into lasertype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasertype'),-35,0,0,0,'Unknown' from lasertype where not exists(
        select 1 from lasertype where value = 'Unknown') limit 1;

insert into jobstatus (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_jobstatus'),-35,0,0,0,'Submitted' from jobstatus where not exists(
        select 1 from jobstatus where value = 'Submitted') limit 1;

insert into jobstatus (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_jobstatus'),-35,0,0,0,'Resubmitted' from jobstatus where not exists(
        select 1 from jobstatus where value = 'Resubmitted') limit 1;

insert into jobstatus (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_jobstatus'),-35,0,0,0,'Queued' from jobstatus where not exists(
        select 1 from jobstatus where value = 'Queued') limit 1;

insert into jobstatus (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_jobstatus'),-35,0,0,0,'Requeued' from jobstatus where not exists(
        select 1 from jobstatus where value = 'Requeued') limit 1;

insert into jobstatus (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_jobstatus'),-35,0,0,0,'Running' from jobstatus where not exists(
        select 1 from jobstatus where value = 'Running') limit 1;

insert into jobstatus (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_jobstatus'),-35,0,0,0,'Error' from jobstatus where not exists(
        select 1 from jobstatus where value = 'Error') limit 1;

insert into jobstatus (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_jobstatus'),-35,0,0,0,'Waiting' from jobstatus where not exists(
        select 1 from jobstatus where value = 'Waiting') limit 1;

insert into jobstatus (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_jobstatus'),-35,0,0,0,'Finished' from jobstatus where not exists(
        select 1 from jobstatus where value = 'Finished') limit 1;

insert into jobstatus (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_jobstatus'),-35,0,0,0,'Cancelled' from jobstatus where not exists(
        select 1 from jobstatus where value = 'Cancelled') limit 1;

insert into detectortype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_detectortype'),-35,0,0,0,'CCD' from detectortype where not exists(
        select 1 from detectortype where value = 'CCD') limit 1;

insert into detectortype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_detectortype'),-35,0,0,0,'IntensifiedCCD' from detectortype where not exists(
        select 1 from detectortype where value = 'IntensifiedCCD') limit 1;

insert into detectortype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_detectortype'),-35,0,0,0,'AnalogVideo' from detectortype where not exists(
        select 1 from detectortype where value = 'AnalogVideo') limit 1;

insert into detectortype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_detectortype'),-35,0,0,0,'PMT' from detectortype where not exists(
        select 1 from detectortype where value = 'PMT') limit 1;

insert into detectortype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_detectortype'),-35,0,0,0,'Photodiode' from detectortype where not exists(
        select 1 from detectortype where value = 'Photodiode') limit 1;

insert into detectortype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_detectortype'),-35,0,0,0,'Spectroscopy' from detectortype where not exists(
        select 1 from detectortype where value = 'Spectroscopy') limit 1;

insert into detectortype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_detectortype'),-35,0,0,0,'LifetimeImaging' from detectortype where not exists(
        select 1 from detectortype where value = 'LifetimeImaging') limit 1;

insert into detectortype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_detectortype'),-35,0,0,0,'CorrelationSpectroscopy' from detectortype where not exists(
        select 1 from detectortype where value = 'CorrelationSpectroscopy') limit 1;

insert into detectortype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_detectortype'),-35,0,0,0,'FTIR' from detectortype where not exists(
        select 1 from detectortype where value = 'FTIR') limit 1;

insert into detectortype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_detectortype'),-35,0,0,0,'EM-CCD' from detectortype where not exists(
        select 1 from detectortype where value = 'EM-CCD') limit 1;

insert into detectortype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_detectortype'),-35,0,0,0,'APD' from detectortype where not exists(
        select 1 from detectortype where value = 'APD') limit 1;

insert into detectortype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_detectortype'),-35,0,0,0,'CMOS' from detectortype where not exists(
        select 1 from detectortype where value = 'CMOS') limit 1;

insert into detectortype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_detectortype'),-35,0,0,0,'Other' from detectortype where not exists(
        select 1 from detectortype where value = 'Other') limit 1;

insert into detectortype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_detectortype'),-35,0,0,0,'Unknown' from detectortype where not exists(
        select 1 from detectortype where value = 'Unknown') limit 1;

insert into microbeammanipulationtype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_microbeammanipulationtype'),-35,0,0,0,'FRAP' from microbeammanipulationtype where not exists(
        select 1 from microbeammanipulationtype where value = 'FRAP') limit 1;

insert into microbeammanipulationtype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_microbeammanipulationtype'),-35,0,0,0,'Photoablation' from microbeammanipulationtype where not exists(
        select 1 from microbeammanipulationtype where value = 'Photoablation') limit 1;

insert into microbeammanipulationtype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_microbeammanipulationtype'),-35,0,0,0,'Photoactivation' from microbeammanipulationtype where not exists(
        select 1 from microbeammanipulationtype where value = 'Photoactivation') limit 1;

insert into microbeammanipulationtype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_microbeammanipulationtype'),-35,0,0,0,'Uncaging' from microbeammanipulationtype where not exists(
        select 1 from microbeammanipulationtype where value = 'Uncaging') limit 1;

insert into microbeammanipulationtype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_microbeammanipulationtype'),-35,0,0,0,'OpticalTrapping' from microbeammanipulationtype where not exists(
        select 1 from microbeammanipulationtype where value = 'OpticalTrapping') limit 1;

insert into microbeammanipulationtype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_microbeammanipulationtype'),-35,0,0,0,'Other' from microbeammanipulationtype where not exists(
        select 1 from microbeammanipulationtype where value = 'Other') limit 1;

insert into microbeammanipulationtype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_microbeammanipulationtype'),-35,0,0,0,'Unknown' from microbeammanipulationtype where not exists(
        select 1 from microbeammanipulationtype where value = 'Unknown') limit 1;

insert into illumination (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_illumination'),-35,0,0,0,'Transmitted' from illumination where not exists(
        select 1 from illumination where value = 'Transmitted') limit 1;

insert into illumination (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_illumination'),-35,0,0,0,'Epifluorescence' from illumination where not exists(
        select 1 from illumination where value = 'Epifluorescence') limit 1;

insert into illumination (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_illumination'),-35,0,0,0,'Oblique' from illumination where not exists(
        select 1 from illumination where value = 'Oblique') limit 1;

insert into illumination (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_illumination'),-35,0,0,0,'NonLinear' from illumination where not exists(
        select 1 from illumination where value = 'NonLinear') limit 1;

insert into illumination (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_illumination'),-35,0,0,0,'Other' from illumination where not exists(
        select 1 from illumination where value = 'Other') limit 1;

insert into illumination (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_illumination'),-35,0,0,0,'Unknown' from illumination where not exists(
        select 1 from illumination where value = 'Unknown') limit 1;

insert into photometricinterpretation (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_photometricinterpretation'),-35,0,0,0,'RGB' from photometricinterpretation where not exists(
        select 1 from photometricinterpretation where value = 'RGB') limit 1;

insert into photometricinterpretation (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_photometricinterpretation'),-35,0,0,0,'ARGB' from photometricinterpretation where not exists(
        select 1 from photometricinterpretation where value = 'ARGB') limit 1;

insert into photometricinterpretation (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_photometricinterpretation'),-35,0,0,0,'CMYK' from photometricinterpretation where not exists(
        select 1 from photometricinterpretation where value = 'CMYK') limit 1;

insert into photometricinterpretation (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_photometricinterpretation'),-35,0,0,0,'HSV' from photometricinterpretation where not exists(
        select 1 from photometricinterpretation where value = 'HSV') limit 1;

insert into photometricinterpretation (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_photometricinterpretation'),-35,0,0,0,'Monochrome' from photometricinterpretation where not exists(
        select 1 from photometricinterpretation where value = 'Monochrome') limit 1;

insert into photometricinterpretation (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_photometricinterpretation'),-35,0,0,0,'ColorMap' from photometricinterpretation where not exists(
        select 1 from photometricinterpretation where value = 'ColorMap') limit 1;

insert into correction (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_correction'),-35,0,0,0,'UV' from correction where not exists(
        select 1 from correction where value = 'UV') limit 1;

insert into correction (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_correction'),-35,0,0,0,'PlanApo' from correction where not exists(
        select 1 from correction where value = 'PlanApo') limit 1;

insert into correction (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_correction'),-35,0,0,0,'PlanFluor' from correction where not exists(
        select 1 from correction where value = 'PlanFluor') limit 1;

insert into correction (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_correction'),-35,0,0,0,'SuperFluor' from correction where not exists(
        select 1 from correction where value = 'SuperFluor') limit 1;

insert into correction (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_correction'),-35,0,0,0,'VioletCorrected' from correction where not exists(
        select 1 from correction where value = 'VioletCorrected') limit 1;

insert into correction (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_correction'),-35,0,0,0,'Achro' from correction where not exists(
        select 1 from correction where value = 'Achro') limit 1;

insert into correction (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_correction'),-35,0,0,0,'Achromat' from correction where not exists(
        select 1 from correction where value = 'Achromat') limit 1;

insert into correction (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_correction'),-35,0,0,0,'Fluor' from correction where not exists(
        select 1 from correction where value = 'Fluor') limit 1;

insert into correction (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_correction'),-35,0,0,0,'Fl' from correction where not exists(
        select 1 from correction where value = 'Fl') limit 1;

insert into correction (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_correction'),-35,0,0,0,'Fluar' from correction where not exists(
        select 1 from correction where value = 'Fluar') limit 1;

insert into correction (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_correction'),-35,0,0,0,'Neofluar' from correction where not exists(
        select 1 from correction where value = 'Neofluar') limit 1;

insert into correction (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_correction'),-35,0,0,0,'Fluotar' from correction where not exists(
        select 1 from correction where value = 'Fluotar') limit 1;

insert into correction (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_correction'),-35,0,0,0,'Apo' from correction where not exists(
        select 1 from correction where value = 'Apo') limit 1;

insert into correction (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_correction'),-35,0,0,0,'Other' from correction where not exists(
        select 1 from correction where value = 'Other') limit 1;

insert into correction (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_correction'),-35,0,0,0,'Unknown' from correction where not exists(
        select 1 from correction where value = 'Unknown') limit 1;

insert into eventtype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_eventtype'),-35,0,0,0,'Import' from eventtype where not exists(
        select 1 from eventtype where value = 'Import') limit 1;

insert into eventtype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_eventtype'),-35,0,0,0,'Internal' from eventtype where not exists(
        select 1 from eventtype where value = 'Internal') limit 1;

insert into eventtype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_eventtype'),-35,0,0,0,'Shoola' from eventtype where not exists(
        select 1 from eventtype where value = 'Shoola') limit 1;

insert into eventtype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_eventtype'),-35,0,0,0,'User' from eventtype where not exists(
        select 1 from eventtype where value = 'User') limit 1;

insert into eventtype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_eventtype'),-35,0,0,0,'Task' from eventtype where not exists(
        select 1 from eventtype where value = 'Task') limit 1;

insert into eventtype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_eventtype'),-35,0,0,0,'Test' from eventtype where not exists(
        select 1 from eventtype where value = 'Test') limit 1;

insert into eventtype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_eventtype'),-35,0,0,0,'Processing' from eventtype where not exists(
        select 1 from eventtype where value = 'Processing') limit 1;

insert into eventtype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_eventtype'),-35,0,0,0,'FullText' from eventtype where not exists(
        select 1 from eventtype where value = 'FullText') limit 1;

insert into eventtype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_eventtype'),-35,0,0,0,'Sessions' from eventtype where not exists(
        select 1 from eventtype where value = 'Sessions') limit 1;

insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasermedium'),-35,0,0,0,'Rhodamine6G' from lasermedium where not exists(
        select 1 from lasermedium where value = 'Rhodamine6G') limit 1;

insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasermedium'),-35,0,0,0,'CoumarinC30' from lasermedium where not exists(
        select 1 from lasermedium where value = 'CoumarinC30') limit 1;

insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasermedium'),-35,0,0,0,'ArFl' from lasermedium where not exists(
        select 1 from lasermedium where value = 'ArFl') limit 1;

insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasermedium'),-35,0,0,0,'ArCl' from lasermedium where not exists(
        select 1 from lasermedium where value = 'ArCl') limit 1;

insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasermedium'),-35,0,0,0,'KrFl' from lasermedium where not exists(
        select 1 from lasermedium where value = 'KrFl') limit 1;

insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasermedium'),-35,0,0,0,'KrCl' from lasermedium where not exists(
        select 1 from lasermedium where value = 'KrCl') limit 1;

insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasermedium'),-35,0,0,0,'XeFl' from lasermedium where not exists(
        select 1 from lasermedium where value = 'XeFl') limit 1;

insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasermedium'),-35,0,0,0,'XeCl' from lasermedium where not exists(
        select 1 from lasermedium where value = 'XeCl') limit 1;

insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasermedium'),-35,0,0,0,'XeBr' from lasermedium where not exists(
        select 1 from lasermedium where value = 'XeBr') limit 1;

insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasermedium'),-35,0,0,0,'GaAs' from lasermedium where not exists(
        select 1 from lasermedium where value = 'GaAs') limit 1;

insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasermedium'),-35,0,0,0,'GaAlAs' from lasermedium where not exists(
        select 1 from lasermedium where value = 'GaAlAs') limit 1;

insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasermedium'),-35,0,0,0,'EMinus' from lasermedium where not exists(
        select 1 from lasermedium where value = 'EMinus') limit 1;

insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasermedium'),-35,0,0,0,'Cu' from lasermedium where not exists(
        select 1 from lasermedium where value = 'Cu') limit 1;

insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasermedium'),-35,0,0,0,'Ag' from lasermedium where not exists(
        select 1 from lasermedium where value = 'Ag') limit 1;

insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasermedium'),-35,0,0,0,'N' from lasermedium where not exists(
        select 1 from lasermedium where value = 'N') limit 1;

insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasermedium'),-35,0,0,0,'Ar' from lasermedium where not exists(
        select 1 from lasermedium where value = 'Ar') limit 1;

insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasermedium'),-35,0,0,0,'Kr' from lasermedium where not exists(
        select 1 from lasermedium where value = 'Kr') limit 1;

insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasermedium'),-35,0,0,0,'Xe' from lasermedium where not exists(
        select 1 from lasermedium where value = 'Xe') limit 1;

insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasermedium'),-35,0,0,0,'HeNe' from lasermedium where not exists(
        select 1 from lasermedium where value = 'HeNe') limit 1;

insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasermedium'),-35,0,0,0,'HeCd' from lasermedium where not exists(
        select 1 from lasermedium where value = 'HeCd') limit 1;

insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasermedium'),-35,0,0,0,'CO' from lasermedium where not exists(
        select 1 from lasermedium where value = 'CO') limit 1;

insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasermedium'),-35,0,0,0,'CO2' from lasermedium where not exists(
        select 1 from lasermedium where value = 'CO2') limit 1;

insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasermedium'),-35,0,0,0,'H2O' from lasermedium where not exists(
        select 1 from lasermedium where value = 'H2O') limit 1;

insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasermedium'),-35,0,0,0,'HFl' from lasermedium where not exists(
        select 1 from lasermedium where value = 'HFl') limit 1;

insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasermedium'),-35,0,0,0,'NdGlass' from lasermedium where not exists(
        select 1 from lasermedium where value = 'NdGlass') limit 1;

insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasermedium'),-35,0,0,0,'NdYAG' from lasermedium where not exists(
        select 1 from lasermedium where value = 'NdYAG') limit 1;

insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasermedium'),-35,0,0,0,'ErGlass' from lasermedium where not exists(
        select 1 from lasermedium where value = 'ErGlass') limit 1;

insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasermedium'),-35,0,0,0,'ErYAG' from lasermedium where not exists(
        select 1 from lasermedium where value = 'ErYAG') limit 1;

insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasermedium'),-35,0,0,0,'HoYLF' from lasermedium where not exists(
        select 1 from lasermedium where value = 'HoYLF') limit 1;

insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasermedium'),-35,0,0,0,'HoYAG' from lasermedium where not exists(
        select 1 from lasermedium where value = 'HoYAG') limit 1;

insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasermedium'),-35,0,0,0,'Ruby' from lasermedium where not exists(
        select 1 from lasermedium where value = 'Ruby') limit 1;

insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasermedium'),-35,0,0,0,'TiSapphire' from lasermedium where not exists(
        select 1 from lasermedium where value = 'TiSapphire') limit 1;

insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasermedium'),-35,0,0,0,'Alexandrite' from lasermedium where not exists(
        select 1 from lasermedium where value = 'Alexandrite') limit 1;

insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasermedium'),-35,0,0,0,'Other' from lasermedium where not exists(
        select 1 from lasermedium where value = 'Other') limit 1;

insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_lasermedium'),-35,0,0,0,'Unknown' from lasermedium where not exists(
        select 1 from lasermedium where value = 'Unknown') limit 1;

insert into microscopetype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_microscopetype'),-35,0,0,0,'Upright' from microscopetype where not exists(
        select 1 from microscopetype where value = 'Upright') limit 1;

insert into microscopetype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_microscopetype'),-35,0,0,0,'Inverted' from microscopetype where not exists(
        select 1 from microscopetype where value = 'Inverted') limit 1;

insert into microscopetype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_microscopetype'),-35,0,0,0,'Dissection' from microscopetype where not exists(
        select 1 from microscopetype where value = 'Dissection') limit 1;

insert into microscopetype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_microscopetype'),-35,0,0,0,'Electrophysiology' from microscopetype where not exists(
        select 1 from microscopetype where value = 'Electrophysiology') limit 1;

insert into microscopetype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_microscopetype'),-35,0,0,0,'Other' from microscopetype where not exists(
        select 1 from microscopetype where value = 'Other') limit 1;

insert into microscopetype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_microscopetype'),-35,0,0,0,'Unknown' from microscopetype where not exists(
        select 1 from microscopetype where value = 'Unknown') limit 1;

insert into dimensionorder (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_dimensionorder'),-35,0,0,0,'XYZCT' from dimensionorder where not exists(
        select 1 from dimensionorder where value = 'XYZCT') limit 1;

insert into dimensionorder (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_dimensionorder'),-35,0,0,0,'XYZTC' from dimensionorder where not exists(
        select 1 from dimensionorder where value = 'XYZTC') limit 1;

insert into dimensionorder (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_dimensionorder'),-35,0,0,0,'XYCTZ' from dimensionorder where not exists(
        select 1 from dimensionorder where value = 'XYCTZ') limit 1;

insert into dimensionorder (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_dimensionorder'),-35,0,0,0,'XYCZT' from dimensionorder where not exists(
        select 1 from dimensionorder where value = 'XYCZT') limit 1;

insert into dimensionorder (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_dimensionorder'),-35,0,0,0,'XYTCZ' from dimensionorder where not exists(
        select 1 from dimensionorder where value = 'XYTCZ') limit 1;

insert into dimensionorder (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_dimensionorder'),-35,0,0,0,'XYTZC' from dimensionorder where not exists(
        select 1 from dimensionorder where value = 'XYTZC') limit 1;

insert into experimenttype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_experimenttype'),-35,0,0,0,'FP' from experimenttype where not exists(
        select 1 from experimenttype where value = 'FP') limit 1;

insert into experimenttype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_experimenttype'),-35,0,0,0,'FRET' from experimenttype where not exists(
        select 1 from experimenttype where value = 'FRET') limit 1;

insert into experimenttype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_experimenttype'),-35,0,0,0,'TimeLapse' from experimenttype where not exists(
        select 1 from experimenttype where value = 'TimeLapse') limit 1;

insert into experimenttype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_experimenttype'),-35,0,0,0,'FourDPlus' from experimenttype where not exists(
        select 1 from experimenttype where value = 'FourDPlus') limit 1;

insert into experimenttype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_experimenttype'),-35,0,0,0,'Screen' from experimenttype where not exists(
        select 1 from experimenttype where value = 'Screen') limit 1;

insert into experimenttype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_experimenttype'),-35,0,0,0,'Immunocytochemistry' from experimenttype where not exists(
        select 1 from experimenttype where value = 'Immunocytochemistry') limit 1;

insert into experimenttype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_experimenttype'),-35,0,0,0,'Immunofluorescence' from experimenttype where not exists(
        select 1 from experimenttype where value = 'Immunofluorescence') limit 1;

insert into experimenttype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_experimenttype'),-35,0,0,0,'FISH' from experimenttype where not exists(
        select 1 from experimenttype where value = 'FISH') limit 1;

insert into experimenttype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_experimenttype'),-35,0,0,0,'Electrophysiology' from experimenttype where not exists(
        select 1 from experimenttype where value = 'Electrophysiology') limit 1;

insert into experimenttype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_experimenttype'),-35,0,0,0,'IonImaging' from experimenttype where not exists(
        select 1 from experimenttype where value = 'IonImaging') limit 1;

insert into experimenttype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_experimenttype'),-35,0,0,0,'Colocalization' from experimenttype where not exists(
        select 1 from experimenttype where value = 'Colocalization') limit 1;

insert into experimenttype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_experimenttype'),-35,0,0,0,'PGIDocumentation' from experimenttype where not exists(
        select 1 from experimenttype where value = 'PGIDocumentation') limit 1;

insert into experimenttype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_experimenttype'),-35,0,0,0,'FluorescenceLifetime' from experimenttype where not exists(
        select 1 from experimenttype where value = 'FluorescenceLifetime') limit 1;

insert into experimenttype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_experimenttype'),-35,0,0,0,'SpectralImaging' from experimenttype where not exists(
        select 1 from experimenttype where value = 'SpectralImaging') limit 1;

insert into experimenttype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_experimenttype'),-35,0,0,0,'Photobleaching' from experimenttype where not exists(
        select 1 from experimenttype where value = 'Photobleaching') limit 1;

insert into experimenttype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_experimenttype'),-35,0,0,0,'Other' from experimenttype where not exists(
        select 1 from experimenttype where value = 'Other') limit 1;

insert into experimenttype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_experimenttype'),-35,0,0,0,'Unknown' from experimenttype where not exists(
        select 1 from experimenttype where value = 'Unknown') limit 1;

insert into contrastmethod (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_contrastmethod'),-35,0,0,0,'Brightfield' from contrastmethod where not exists(
        select 1 from contrastmethod where value = 'Brightfield') limit 1;

insert into contrastmethod (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_contrastmethod'),-35,0,0,0,'Phase' from contrastmethod where not exists(
        select 1 from contrastmethod where value = 'Phase') limit 1;

insert into contrastmethod (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_contrastmethod'),-35,0,0,0,'DIC' from contrastmethod where not exists(
        select 1 from contrastmethod where value = 'DIC') limit 1;

insert into contrastmethod (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_contrastmethod'),-35,0,0,0,'HoffmanModulation' from contrastmethod where not exists(
        select 1 from contrastmethod where value = 'HoffmanModulation') limit 1;

insert into contrastmethod (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_contrastmethod'),-35,0,0,0,'ObliqueIllumination' from contrastmethod where not exists(
        select 1 from contrastmethod where value = 'ObliqueIllumination') limit 1;

insert into contrastmethod (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_contrastmethod'),-35,0,0,0,'PolarizedLight' from contrastmethod where not exists(
        select 1 from contrastmethod where value = 'PolarizedLight') limit 1;

insert into contrastmethod (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_contrastmethod'),-35,0,0,0,'Darkfield' from contrastmethod where not exists(
        select 1 from contrastmethod where value = 'Darkfield') limit 1;

insert into contrastmethod (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_contrastmethod'),-35,0,0,0,'Fluorescence' from contrastmethod where not exists(
        select 1 from contrastmethod where value = 'Fluorescence') limit 1;

insert into contrastmethod (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_contrastmethod'),-35,0,0,0,'Other' from contrastmethod where not exists(
        select 1 from contrastmethod where value = 'Other') limit 1;

insert into contrastmethod (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_contrastmethod'),-35,0,0,0,'Unknown' from contrastmethod where not exists(
        select 1 from contrastmethod where value = 'Unknown') limit 1;

insert into filamenttype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_filamenttype'),-35,0,0,0,'Incandescent' from filamenttype where not exists(
        select 1 from filamenttype where value = 'Incandescent') limit 1;

insert into filamenttype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_filamenttype'),-35,0,0,0,'Halogen' from filamenttype where not exists(
        select 1 from filamenttype where value = 'Halogen') limit 1;

insert into filamenttype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_filamenttype'),-35,0,0,0,'Other' from filamenttype where not exists(
        select 1 from filamenttype where value = 'Other') limit 1;

insert into filamenttype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_filamenttype'),-35,0,0,0,'Unknown' from filamenttype where not exists(
        select 1 from filamenttype where value = 'Unknown') limit 1;

insert into filtertype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_filtertype'),-35,0,0,0,'LongPass' from filtertype where not exists(
        select 1 from filtertype where value = 'LongPass') limit 1;

insert into filtertype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_filtertype'),-35,0,0,0,'ShortPass' from filtertype where not exists(
        select 1 from filtertype where value = 'ShortPass') limit 1;

insert into filtertype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_filtertype'),-35,0,0,0,'BandPass' from filtertype where not exists(
        select 1 from filtertype where value = 'BandPass') limit 1;

insert into filtertype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_filtertype'),-35,0,0,0,'MultiPass' from filtertype where not exists(
        select 1 from filtertype where value = 'MultiPass') limit 1;

insert into filtertype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_filtertype'),-35,0,0,0,'Other' from filtertype where not exists(
        select 1 from filtertype where value = 'Other') limit 1;

insert into filtertype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_filtertype'),-35,0,0,0,'Unknown' from filtertype where not exists(
        select 1 from filtertype where value = 'Unknown') limit 1;

UPDATE dbpatch set message = 'Database updated.', finished = now()
 WHERE currentVersion  = 'OMERO4.1'   and
          currentPatch    = 0         and
          previousVersion = 'OMERO4'  and
          previousPatch   = 0;

COMMIT;
