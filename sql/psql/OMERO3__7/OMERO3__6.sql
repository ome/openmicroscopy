-- 
-- Copyright 2007 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

--
-- Adds ScriptJob
--

BEGIN;

    insert into dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
                 values ('OMERO3',       7,              'OMERO3',            6);

    create table scriptjob (
        job_id int8 not null,
        description varchar(255),
        primary key (job_id)
    );

    alter table joboriginalfilelink 
        add constraint FKjoboriginalfilelink_parent_scriptjob
        foreign key (parent) 
        references scriptjob;

    alter table scriptjob 
        add constraint FKscriptjob_job_id_job
        foreign key (job_id) 
        references job;

    update dbpatch set message = 'Database updated.', finished = now()
    where currentVersion   = 'OMERO3'    and
           currentPatch    = 7           and
           previousVersion = 'OMERO3'    and
           previousPatch   = 6;

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

COMMIT;
