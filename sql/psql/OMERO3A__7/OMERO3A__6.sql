--
-- Copyright 2008 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

--
-- Add annotations to Sessions and ExperimenterGroup
--

BEGIN;

    insert into dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
                 values ('OMERO3A',      7,              'OMERO3A',           6);

    create table count_ExperimenterGroup_annotationLinks_by_owner (
        experimentergroup_id int8 not null,
        count int8 not null,
        owner_id int8,
        primary key (experimentergroup_id, owner_id)
    );

    create table count_Session_annotationLinks_by_owner (
        session_id int8 not null,
        count int8 not null,
        owner_id int8,
        primary key (session_id, owner_id)
    );

    create table experimentergroupannotationlink (
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

    create table sessionannotationlink (
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

    alter table count_ExperimenterGroup_annotationLinks_by_owner 
        add constraint FK_count_to_ExperimenterGroup_annotationLinks 
        foreign key (experimentergroup_id) 
        references experimentergroup;

    alter table count_Session_annotationLinks_by_owner 
        add constraint FK_count_to_Session_annotationLinks 
        foreign key (session_id) 
        references session;

    alter table experimentergroupannotationlink 
        add constraint FKexperimentergroupannotationlink_child_annotation 
        foreign key (child) 
        references annotation;

    alter table experimentergroupannotationlink 
        add constraint FKexperimentergroupannotationlink_update_id_event 
        foreign key (update_id) 
        references event;

    alter table experimentergroupannotationlink 
        add constraint FKexperimentergroupannotationlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter;

    alter table experimentergroupannotationlink 
        add constraint FKexperimentergroupannotationlink_creation_id_event 
        foreign key (creation_id) 
        references event;

    alter table experimentergroupannotationlink 
        add constraint FKexperimentergroupannotationlink_parent_experimentergroup 
        foreign key (parent) 
        references experimentergroup;

    alter table experimentergroupannotationlink 
        add constraint FKexperimentergroupannotationlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup;

    alter table experimentergroupannotationlink 
        add constraint FKexperimentergroupannotationlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo;

    alter table sessionannotationlink 
        add constraint FKsessionannotationlink_child_annotation 
        foreign key (child) 
        references annotation;

    alter table sessionannotationlink 
        add constraint FKsessionannotationlink_update_id_event 
        foreign key (update_id) 
        references event;

    alter table sessionannotationlink 
        add constraint FKsessionannotationlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter;

    alter table sessionannotationlink 
        add constraint FKsessionannotationlink_creation_id_event 
        foreign key (creation_id) 
        references event;

    alter table sessionannotationlink 
        add constraint FKsessionannotationlink_parent_session 
        foreign key (parent) 
        references session;

    alter table sessionannotationlink 
        add constraint FKsessionannotationlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup;

    alter table sessionannotationlink 
        add constraint FKsessionannotationlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo;

    create sequence seq_experimentergroupannotationlink;

    create sequence seq_sessionannotationlink;

   DROP TABLE count_ExperimenterGroup_annotationLinks_by_owner;

   CREATE OR REPLACE VIEW count_ExperimenterGroup_annotationLinks_by_owner (ExperimenterGroup_id, owner_id, count) AS select parent, owner_id, count(*)
     FROM ExperimenterGroupAnnotationLink GROUP BY parent, owner_id ORDER BY parent;

   DROP TABLE count_Session_annotationLinks_by_owner;

   CREATE OR REPLACE VIEW count_Session_annotationLinks_by_owner (Session_id, owner_id, count) AS select parent, owner_id, count(*)
     FROM SessionAnnotationLink GROUP BY parent, owner_id ORDER BY parent;

   UPDATE dbpatch set message = 'Database updated.', finished = now()
    where currentVersion  = 'OMERO3A' and
          currentPatch    = 7         and
          previousVersion = 'OMERO3A' and
          previousPatch   = 6;

COMMIT;
