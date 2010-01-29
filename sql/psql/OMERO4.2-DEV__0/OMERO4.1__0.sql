--
-- Copyright 2010 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

--
-- OMERO-Beta4.2 First DB Modification
--
--
BEGIN;

CREATE OR REPLACE FUNCTION omero_assert_omero4_1_0() RETURNS void AS '
DECLARE
    rec RECORD;
BEGIN

    SELECT INTO rec *
           FROM dbpatch
          WHERE id = ( SELECT id FROM dbpatch ORDER BY id DESC LIMIT 1 )
            AND currentversion = ''OMERO4.1''
            AND currentpatch = 0;

    IF NOT FOUND THEN
        RAISE EXCEPTION ''Current version is not OMERO4.1__0! Aborting...'';
    END IF;

END;' LANGUAGE plpgsql;
SELECT omero_assert_omero4_1_0();
DROP FUNCTION omero_assert_omero4_1_0();

INSERT into dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
             values ('OMERO4.2-DEV', 0,              'OMERO4.1',          0);
----

--
-- Group-owner modification (ticket:1434)
--

ALTER TABLE groupexperimentermap ADD COLUMN owner boolean;

UPDATE groupexperimentermap SET owner = true
      FROM experimentergroup, experimenter
     WHERE experimentergroup.owner_id == experimenter.id
       AND experimentergroup.id = groupexperimentermap.parent
       AND experimenterid. = groupexperimentermap.child;

ALTER TABLE groupexperimentermap SET COLUMN owner NOT NULL;

ALTER TABLE experimentergroup DROP COLUMN owner_id;

ALTER TABLE experimentergroup DROP COLUMN creation_id;

ALTER TABLE experimentergroup DROP COLUMN update_id;

----
UPDATE dbpatch set message = 'Database updated.', finished = now()
 WHERE    currentVersion  = 'OMERO4.2' and
          currentPatch    = 0          and
          previousVersion = 'OMERO4.1' and
          previousPatch   = 0;

COMMIT;
