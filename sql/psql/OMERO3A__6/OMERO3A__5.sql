--
-- Copyright 2008 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

--
-- Drop all ScreenPlateWell related tables and recreate.
--

BEGIN;

    insert into dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
                 values ('OMERO3A',      6,              'OMERO3A',           5);

    drop sequence seq_plate;
    drop sequence seq_plateannotationlink;
    drop sequence seq_reagent;
    drop sequence seq_reagentannotationlink;
    drop sequence seq_screen;
    drop sequence seq_screenacquisition;
    drop sequence seq_screenacquisitionannotationlink;
    drop sequence seq_screenannotationlink;
    drop sequence seq_screenplatelink;
    drop sequence seq_screenreagentlink;
    drop sequence seq_well;
    drop sequence seq_wellannotationlink;
    drop sequence seq_wellsample;
    drop sequence seq_wellsampleannotationlink;
    drop sequence seq_wellsampleimagelink;

    create sequence seq_plate;
    create sequence seq_plateannotationlink;
    create sequence seq_reagent;
    create sequence seq_reagentannotationlink;
    create sequence seq_screen;
    create sequence seq_screenacquisition;
    create sequence seq_screenacquisitionannotationlink;
    create sequence seq_screenacquisitionwellsamplelink;
    create sequence seq_screenannotationlink;
    create sequence seq_screenplatelink;
    create sequence seq_well;
    create sequence seq_wellannotationlink;
    create sequence seq_wellreagentlink;
    create sequence seq_wellsample;
    create sequence seq_wellsampleannotationlink;

    drop view count_ScreenAcquisition_annotationLinks_by_owner;
    drop view count_Screen_annotationLinks_by_owner;
    drop view count_Screen_plateLinks_by_owner;
    drop view count_Screen_reagentLinks_by_owner;
    drop view count_Plate_annotationLinks_by_owner;
    drop view count_Plate_screenLinks_by_owner;
    drop view count_WellSample_annotationLinks_by_owner;
    drop view count_WellSample_imageLinks_by_owner;
    drop view count_Well_annotationLinks_by_owner;
    drop view count_Reagent_annotationLinks_by_owner;
    drop view count_Reagent_screenLinks_by_owner;
    drop view count_image_samplelinks_by_owner;

    drop table screenacquisitionannotationlink;
    drop table screenannotationlink;
    drop table screenplatelink;
    drop table screenreagentlink;
    drop table plateannotationlink;
    drop table wellannotationlink;
    drop table wellsampleannotationlink;
    drop table wellsampleimagelink;
    drop table reagentannotationlink;

    drop table wellsample;
    drop table screenacquisition;
    drop table screen;
    drop table well;
    drop table plate;
    drop table reagent;

    create table plate (
        id int8 not null,
        description text,
        permissions int8 not null,
        externalIdentifier varchar(255),
        name varchar(255) not null,
        status varchar(255),
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        primary key (id)
    );

    create table plateannotationlink (
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

    create table reagent (
        id int8 not null,
        description text,
        permissions int8 not null,
        name varchar(255) not null,
        reagentIdentifier varchar(255),
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        screen int8 not null,
        primary key (id)
    );

    create table reagentannotationlink (
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

    create table screen (
        id int8 not null,
        description text,
        permissions int8 not null,
        name varchar(255) not null,
        protocolDescription varchar(255),
        protocolIdentifier varchar(255),
        reagentSetDescription varchar(255),
        reagentSetIdentifier varchar(255),
        type varchar(255),
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        primary key (id)
    );

    create table screenacquisition (
        id int8 not null,
        permissions int8 not null,
        endTime timestamp,
        startTime timestamp,
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        screen int8 not null,
        primary key (id)
    );

    create table screenacquisitionannotationlink (
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

    create table screenacquisitionwellsamplelink (
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

    create table screenannotationlink (
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

    create table screenplatelink (
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

    create table well (
        id int8 not null,
        "column" int4,
        permissions int8 not null,
        externalDescription varchar(255),
        externalIdentifier varchar(255),
        row int4,
        type varchar(255),
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        plate int8 not null,
        primary key (id)
    );

    create table wellannotationlink (
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

    create table wellreagentlink (
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

    create table wellsample (
        id int8 not null,
        permissions int8 not null,
        posX double precision,
        posY double precision,
        timepoint int4,
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        image int8 not null,
        well int8 not null,
        primary key (id)
    );

    create table wellsampleannotationlink (
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

    alter table plate 
        add constraint FKplate_update_id_event 
        foreign key (update_id) 
        references event;

    alter table plate 
        add constraint FKplate_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter;

    alter table plate 
        add constraint FKplate_creation_id_event 
        foreign key (creation_id) 
        references event;

    alter table plate 
        add constraint FKplate_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup;

    alter table plate 
        add constraint FKplate_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo;

    alter table plateannotationlink 
        add constraint FKplateannotationlink_child_annotation 
        foreign key (child) 
        references annotation;

    alter table plateannotationlink 
        add constraint FKplateannotationlink_update_id_event 
        foreign key (update_id) 
        references event;

    alter table plateannotationlink 
        add constraint FKplateannotationlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter;

    alter table plateannotationlink 
        add constraint FKplateannotationlink_creation_id_event 
        foreign key (creation_id) 
        references event;

    alter table plateannotationlink 
        add constraint FKplateannotationlink_parent_plate 
        foreign key (parent) 
        references plate;

    alter table plateannotationlink 
        add constraint FKplateannotationlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup;

    alter table plateannotationlink 
        add constraint FKplateannotationlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo;

    alter table reagent 
        add constraint FKreagent_update_id_event 
        foreign key (update_id) 
        references event;

    alter table reagent 
        add constraint FKreagent_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter;

    alter table reagent 
        add constraint FKreagent_creation_id_event 
        foreign key (creation_id) 
        references event;

    alter table reagent 
        add constraint FKreagent_screen_screen 
        foreign key (screen) 
        references screen;

    alter table reagent 
        add constraint FKreagent_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup;

    alter table reagent 
        add constraint FKreagent_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo;

    alter table reagentannotationlink 
        add constraint FKreagentannotationlink_child_annotation 
        foreign key (child) 
        references annotation;

    alter table reagentannotationlink 
        add constraint FKreagentannotationlink_update_id_event 
        foreign key (update_id) 
        references event;

    alter table reagentannotationlink 
        add constraint FKreagentannotationlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter;

    alter table reagentannotationlink 
        add constraint FKreagentannotationlink_creation_id_event 
        foreign key (creation_id) 
        references event;

    alter table reagentannotationlink 
        add constraint FKreagentannotationlink_parent_reagent 
        foreign key (parent) 
        references reagent;

    alter table reagentannotationlink 
        add constraint FKreagentannotationlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup;

    alter table reagentannotationlink 
        add constraint FKreagentannotationlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo;

    alter table screen 
        add constraint FKscreen_update_id_event 
        foreign key (update_id) 
        references event;

    alter table screen 
        add constraint FKscreen_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter;

    alter table screen 
        add constraint FKscreen_creation_id_event 
        foreign key (creation_id) 
        references event;

    alter table screen 
        add constraint FKscreen_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup;

    alter table screen 
        add constraint FKscreen_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo;

    alter table screenacquisition 
        add constraint FKscreenacquisition_update_id_event 
        foreign key (update_id) 
        references event;

    alter table screenacquisition 
        add constraint FKscreenacquisition_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter;

    alter table screenacquisition 
        add constraint FKscreenacquisition_creation_id_event 
        foreign key (creation_id) 
        references event;

    alter table screenacquisition 
        add constraint FKscreenacquisition_screen_screen 
        foreign key (screen) 
        references screen;

    alter table screenacquisition 
        add constraint FKscreenacquisition_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup;

    alter table screenacquisition 
        add constraint FKscreenacquisition_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo;

    alter table screenacquisitionannotationlink 
        add constraint FKscreenacquisitionannotationlink_child_annotation 
        foreign key (child) 
        references annotation;

    alter table screenacquisitionannotationlink 
        add constraint FKscreenacquisitionannotationlink_update_id_event 
        foreign key (update_id) 
        references event;

    alter table screenacquisitionannotationlink 
        add constraint FKscreenacquisitionannotationlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter;

    alter table screenacquisitionannotationlink 
        add constraint FKscreenacquisitionannotationlink_creation_id_event 
        foreign key (creation_id) 
        references event;

    alter table screenacquisitionannotationlink 
        add constraint FKscreenacquisitionannotationlink_parent_screenacquisition 
        foreign key (parent) 
        references screenacquisition;

    alter table screenacquisitionannotationlink 
        add constraint FKscreenacquisitionannotationlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup;

    alter table screenacquisitionannotationlink 
        add constraint FKscreenacquisitionannotationlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo;

    alter table screenacquisitionwellsamplelink 
        add constraint FKscreenacquisitionwellsamplelink_child_wellsample 
        foreign key (child) 
        references wellsample;

    alter table screenacquisitionwellsamplelink 
        add constraint FKscreenacquisitionwellsamplelink_update_id_event 
        foreign key (update_id) 
        references event;

    alter table screenacquisitionwellsamplelink 
        add constraint FKscreenacquisitionwellsamplelink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter;

    alter table screenacquisitionwellsamplelink 
        add constraint FKscreenacquisitionwellsamplelink_creation_id_event 
        foreign key (creation_id) 
        references event;

    alter table screenacquisitionwellsamplelink 
        add constraint FKscreenacquisitionwellsamplelink_parent_screenacquisition 
        foreign key (parent) 
        references screenacquisition;

    alter table screenacquisitionwellsamplelink 
        add constraint FKscreenacquisitionwellsamplelink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup;

    alter table screenacquisitionwellsamplelink 
        add constraint FKscreenacquisitionwellsamplelink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo;

    alter table screenannotationlink 
        add constraint FKscreenannotationlink_child_annotation 
        foreign key (child) 
        references annotation;

    alter table screenannotationlink 
        add constraint FKscreenannotationlink_update_id_event 
        foreign key (update_id) 
        references event;

    alter table screenannotationlink 
        add constraint FKscreenannotationlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter;

    alter table screenannotationlink 
        add constraint FKscreenannotationlink_creation_id_event 
        foreign key (creation_id) 
        references event;

    alter table screenannotationlink 
        add constraint FKscreenannotationlink_parent_screen 
        foreign key (parent) 
        references screen;

    alter table screenannotationlink 
        add constraint FKscreenannotationlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup;

    alter table screenannotationlink 
        add constraint FKscreenannotationlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo;

    alter table screenplatelink 
        add constraint FKscreenplatelink_child_plate 
        foreign key (child) 
        references plate;

    alter table screenplatelink 
        add constraint FKscreenplatelink_update_id_event 
        foreign key (update_id) 
        references event;

    alter table screenplatelink 
        add constraint FKscreenplatelink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter;

    alter table screenplatelink 
        add constraint FKscreenplatelink_creation_id_event 
        foreign key (creation_id) 
        references event;

    alter table screenplatelink 
        add constraint FKscreenplatelink_parent_screen 
        foreign key (parent) 
        references screen;

    alter table screenplatelink 
        add constraint FKscreenplatelink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup;

    alter table screenplatelink 
        add constraint FKscreenplatelink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo;

    alter table well 
        add constraint FKwell_update_id_event 
        foreign key (update_id) 
        references event;

    alter table well 
        add constraint FKwell_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter;

    alter table well 
        add constraint FKwell_creation_id_event 
        foreign key (creation_id) 
        references event;

    alter table well 
        add constraint FKwell_plate_plate 
        foreign key (plate) 
        references plate;

    alter table well 
        add constraint FKwell_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup;

    alter table well 
        add constraint FKwell_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo;

    alter table wellannotationlink 
        add constraint FKwellannotationlink_child_annotation 
        foreign key (child) 
        references annotation;

    alter table wellannotationlink 
        add constraint FKwellannotationlink_update_id_event 
        foreign key (update_id) 
        references event;

    alter table wellannotationlink 
        add constraint FKwellannotationlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter;

    alter table wellannotationlink 
        add constraint FKwellannotationlink_creation_id_event 
        foreign key (creation_id) 
        references event;

    alter table wellannotationlink 
        add constraint FKwellannotationlink_parent_well 
        foreign key (parent) 
        references well;

    alter table wellannotationlink 
        add constraint FKwellannotationlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup;

    alter table wellannotationlink 
        add constraint FKwellannotationlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo;

    alter table wellreagentlink 
        add constraint FKwellreagentlink_child_reagent 
        foreign key (child) 
        references reagent;

    alter table wellreagentlink 
        add constraint FKwellreagentlink_update_id_event 
        foreign key (update_id) 
        references event;

    alter table wellreagentlink 
        add constraint FKwellreagentlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter;

    alter table wellreagentlink 
        add constraint FKwellreagentlink_creation_id_event 
        foreign key (creation_id) 
        references event;

    alter table wellreagentlink 
        add constraint FKwellreagentlink_parent_well 
        foreign key (parent) 
        references well;

    alter table wellreagentlink 
        add constraint FKwellreagentlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup;

    alter table wellreagentlink 
        add constraint FKwellreagentlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo;

    alter table wellsample 
        add constraint FKwellsample_update_id_event 
        foreign key (update_id) 
        references event;

    alter table wellsample 
        add constraint FKwellsample_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter;

    alter table wellsample 
        add constraint FKwellsample_creation_id_event 
        foreign key (creation_id) 
        references event;

    alter table wellsample 
        add constraint FKwellsample_well_well 
        foreign key (well) 
        references well;

    alter table wellsample 
        add constraint FKwellsample_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup;

    alter table wellsample 
        add constraint FKwellsample_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo;

    alter table wellsample 
        add constraint FKwellsample_image_image 
        foreign key (image) 
        references image;

    alter table wellsampleannotationlink 
        add constraint FKwellsampleannotationlink_child_annotation 
        foreign key (child) 
        references annotation;

    alter table wellsampleannotationlink 
        add constraint FKwellsampleannotationlink_update_id_event 
        foreign key (update_id) 
        references event;

    alter table wellsampleannotationlink 
        add constraint FKwellsampleannotationlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter;

    alter table wellsampleannotationlink 
        add constraint FKwellsampleannotationlink_creation_id_event 
        foreign key (creation_id) 
        references event;

    alter table wellsampleannotationlink 
        add constraint FKwellsampleannotationlink_parent_wellsample 
        foreign key (parent) 
        references wellsample;

    alter table wellsampleannotationlink 
        add constraint FKwellsampleannotationlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup;

    alter table wellsampleannotationlink 
        add constraint FKwellsampleannotationlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo;

  CREATE OR REPLACE VIEW count_Plate_screenLinks_by_owner (Plate_id, owner_id, count) AS select child, owner_id, count(*)
    FROM ScreenPlateLink GROUP BY child, owner_id ORDER BY child;

  CREATE OR REPLACE VIEW count_Plate_annotationLinks_by_owner (Plate_id, owner_id, count) AS select parent, owner_id, count(*)
    FROM PlateAnnotationLink GROUP BY parent, owner_id ORDER BY parent;

  CREATE OR REPLACE VIEW count_Screen_plateLinks_by_owner (Screen_id, owner_id, count) AS select parent, owner_id, count(*)
    FROM ScreenPlateLink GROUP BY parent, owner_id ORDER BY parent;

  CREATE OR REPLACE VIEW count_Screen_annotationLinks_by_owner (Screen_id, owner_id, count) AS select parent, owner_id, count(*)
    FROM ScreenAnnotationLink GROUP BY parent, owner_id ORDER BY parent;

  CREATE OR REPLACE VIEW count_ScreenAcquisition_annotationLinks_by_owner (ScreenAcquisition_id, owner_id, count) AS select parent, owner_id, count(*)
    FROM ScreenAcquisitionAnnotationLink GROUP BY parent, owner_id ORDER BY parent;

  CREATE OR REPLACE VIEW count_ScreenAcquisition_wellSampleLinks_by_owner (ScreenAcquisition_id, owner_id, count) AS select parent, owner_id, count(*)
    FROM ScreenAcquisitionWellSampleLink GROUP BY parent, owner_id ORDER BY parent;

  CREATE OR REPLACE VIEW count_WellSample_screenAcquisitionLinks_by_owner (WellSample_id, owner_id, count) AS select child, owner_id, count(*)
    FROM ScreenAcquisitionWellSampleLink GROUP BY child, owner_id ORDER BY child;

  CREATE OR REPLACE VIEW count_WellSample_annotationLinks_by_owner (WellSample_id, owner_id, count) AS select parent, owner_id, count(*)
    FROM WellSampleAnnotationLink GROUP BY parent, owner_id ORDER BY parent;

  CREATE OR REPLACE VIEW count_Well_reagentLinks_by_owner (Well_id, owner_id, count) AS select parent, owner_id, count(*)
    FROM WellReagentLink GROUP BY parent, owner_id ORDER BY parent;

  CREATE OR REPLACE VIEW count_Well_annotationLinks_by_owner (Well_id, owner_id, count) AS select parent, owner_id, count(*)
    FROM WellAnnotationLink GROUP BY parent, owner_id ORDER BY parent;

  CREATE OR REPLACE VIEW count_Reagent_wellLinks_by_owner (Reagent_id, owner_id, count) AS select child, owner_id, count(*)
    FROM WellReagentLink GROUP BY child, owner_id ORDER BY child;

  CREATE OR REPLACE VIEW count_Reagent_annotationLinks_by_owner (Reagent_id, owner_id, count) AS select parent, owner_id, count(*)
    FROM ReagentAnnotationLink GROUP BY parent, owner_id ORDER BY parent;

  UPDATE dbpatch set message = 'Database updated.', finished = now()
    where currentVersion  = 'OMERO3A' and
          currentPatch    = 6         and
          previousVersion = 'OMERO3A' and
          previousPatch   = 5;

COMMIT;
