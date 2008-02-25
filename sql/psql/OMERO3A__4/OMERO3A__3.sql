--
-- Copyright 2008 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

BEGIN;

ALTER TABLE annotation RENAME COLUMN name TO ns;

INSERT INTO format (id,permissions,owner_id,group_id,creation_id,value)
    SELECT nextval('seq_format'),-35,0,0,0,'application/pdf';

INSERT INTO eventtype (id,permissions,owner_id,group_id,creation_id,value)
        SELECT nextval('seq_eventtype'),-35,0,0,0,'FullText';

INSERT INTO dbpatch (currentVersion, currentPatch, previousVersion, previousPatch, message, finished)
        VALUES ('OMERO3A',  4, 'OMERO3A', 3, 'Database updated.', now());

COMMIT;
