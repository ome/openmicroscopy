--
-- Copyright 2008 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

BEGIN;

ALTER TABLE annotations ALTER COLUMN name RENAME to ns;

INSERT INTO dbpatch (currentVersion, currentPatch, previousVersion, previousPatch, message, finished)
        VALUES ('OMERO3A',  4, 'OMERO3A', 3, 'Database updated.', now());

COMMIT;
