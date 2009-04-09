--
-- Copyright 2009 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

--
-- OMERO-Beta4.1 release.
--
BEGIN;

-- Check that we are only applying this against OMERO4__0

CREATE OR REPLACE FUNCTION omero_assert_omero4_0() RETURNS void AS '
DECLARE
    rec RECORD;
BEGIN

    SELECT INTO rec *
           FROM dbpatch
          WHERE id = ( SELECT id FROM dbpatch ORDER BY id DESC LIMIT 1 )
            AND currentversion = ''OMERO4''
            AND currentpatch = 0;

    IF NOT FOUND THEN
        RAISE EXCEPTION ''Current version is not OMERO4__0! Aborting...'';
    END IF;

END;' LANGUAGE plpgsql;
SELECT omero_assert_omero4_0();
DROP FUNCTION omero_assert_omero4_0();

INSERT into dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
             values ('OMERO4',       1,              'OMERO4',            0);


ALTER TABLE password add PRIMARY KEY (experimenter_id);

UPDATE dbpatch set message = 'Database updated.', finished = now()
 WHERE currentVersion  = 'OMERO4'     and
          currentPatch    = 1         and
          previousVersion = 'OMERO4'  and
          previousPatch   = 0;

COMMIT;

