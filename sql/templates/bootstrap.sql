--
-- Template bootstrap script
-- ===========================================
-- This script is intended to be used on any
-- OMERO db which does not have a dbpatch table.
-- This is true for all databases previous to 
-- 3.0-Beta2. An existing db without a dbpatch table
-- is considered to be at patch 0. This script
-- increments the patch to 1.
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

    insert into dbpatch (currentVersion, currentPatch, previousVersion, previousPatch, message, finished) 
                 values ('@CURRENTVERSION@',0,'@CURRENTVERSION@',1, 'Bootstrapped', now());

commit;
