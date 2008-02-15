-- 
-- Copyright 2007 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

begin;

    create sequence seq_dbpatch;

    create table dbpatch (
        id int8 not null default nextval('seq_dbpatch'),
        permissions int8 not null default -35,
        external_id int8 unique,
        currentVersion varchar(255) not null,
        currentPatch int4 not null,
        previousVersion varchar(255) not null,
        previousPatch int4 not null,
        finished timestamp,
        message varchar(255) default 'Updating',
        primary key (id)
    );

    alter table dbpatch
        add constraint dbpatch_external_info_id
        foreign key (external_id)
        references externalinfo;

    alter table dbpatch 
        add constraint unique_dbpatch 
        unique (currentVersion, currentPatch, previousVersion, previousPatch);

    insert into dbpatch (currentVersion, currentPatch, previousVersion, previousPatch, message,        finished)
                 values ('OMERO3',       0,            'OMERO3',        0,             'Bootstrapped', now());

commit;
