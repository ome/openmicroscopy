--
-- Copyright 2009 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

--
-- OMERO-Beta4.1 release
--
-- UNDERDEVELOPMENT DO NOT USE !!!
--
BEGIN;

-- Check that we are only applying this against OMERO4-DEV__1

CREATE OR REPLACE FUNCTION omero_assert_omero4d1() RETURNS void AS '
DECLARE
    rec RECORD;
BEGIN

    -- RAISE EXCEPTION ''UNDERDEVELOPMENT: If you want to test this script, comment this line. Aborting...'';

    SELECT INTO rec *
           FROM dbpatch
          WHERE id = ( SELECT id FROM dbpatch ORDER BY id DESC LIMIT 1 )
            AND currentversion = ''OMERO4-DEV''
            AND currentpatch = 1;

    IF NOT FOUND THEN
        RAISE EXCEPTION ''Current version is not OMERO4-DEV__1! Aborting...'';
    END IF;

END;' LANGUAGE plpgsql;
SELECT omero_assert_omero4d1();
DROP FUNCTION omero_assert_omero4d1();

INSERT into dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
             values ('OMERO4-DEV',              2,       'OMERO4-DEV',                1);


alter table image add column rois int8;

alter table logicalchannel add column shapes int8;


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
    baselineShift varchar(255),
    permissions int8 not null,
    direction varchar(255),
    fillColor varchar(255),
    fillOpacity double precision,
    fillRule varchar(255),
    fontFamily varchar(255),
    fontSize int4,
    fontStretch varchar(255),
    fontStyle varchar(255),
    fontVariant varchar(255),
    fontWeight varchar(255),
    g varchar(255),
    glyphOrientationVertical int4,
    locked bool,
    strokeColor varchar(255),
    strokeDashArray varchar(255),
    strokeDashOffset int4,
    strokeLineCap varchar(255),
    strokeLineJoin varchar(255),
    strokeMiterLimit int4,
    strokeOpacity double precision,
    strokeWidth int4,
    text text,
    textAnchor varchar(255),
    textDecoration varchar(255),
    textFill varchar(255),
    textStroke varchar(255),
    theT int4,
    theZ int4,
    transform varchar(255),
    vectorEffect varchar(255),
    version int4,
    visibility bool,
    writingMode varchar(255),
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
    d varchar(255),
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

alter table image 
    add constraint FKimage_rois_roi 
    foreign key (rois) 
    references roi;;

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
    
UPDATE dbpatch set message = 'Database updated.', finished = now()
 WHERE currentVersion     = 'OMERO4-DEV'     and
          currentPatch    = 2                and
          previousVersion = 'OMERO4-DEV'     and
          previousPatch   = 1;

COMMIT;
