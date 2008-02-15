-- 
-- Copyright 2007 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

BEGIN;

    insert into dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
                 values ('OMERO3',       5,              'OMERO3',            4);

    alter table password add column dn text;

    update dbpatch set message = 'Database updated.', finished = now()
    where currentVersion   = 'OMERO3'    and
           currentPatch    = 5           and
           previousVersion = 'OMERO3'    and
           previousPatch   = 4;

COMMIT;
