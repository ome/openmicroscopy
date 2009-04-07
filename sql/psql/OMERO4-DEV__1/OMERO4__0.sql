--
-- Copyright 2009 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

--
-- OMERO-Beta4.1 release.
--
-- UNDERDEVELOPMENT DO NOT USE !!!
--
BEGIN;

-- Check that we are only applying this against OMERO4__0

CREATE OR REPLACE FUNCTION omero_assert_omero4_0() RETURNS void AS '
DECLARE
    rec RECORD;
BEGIN

    RAISE EXCEPTION ''UNDERDEVELOPMENT: If you want to test this script, comment this line. Aborting...'';

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
             values ('OMERO4-DEV',       1,              'OMERO4',            0);

ALTER TABLE node    ADD UNIQUE (uuid);

ALTER TABLE session ADD UNIQUE (uuid);

ALTER TABLE plate
    ADD COLUMN columnNamingConvention varchar(255),
    ADD COLUMN rowNamingConvention varchar(255),
    ADD COLUMN defaultSample int4,
    ADD COLUMN wellOriginX float8,
    ADD COLUMN wellOriginY float8;

ALTER TABLE well
    ADD COLUMN red   int4,
    ADD COLUMN green int4,
    ADD COLUMN blue  int4,
    ADD COLUMN alpha int4;

UPDATE dbpatch set message = 'Database updated.', finished = now()
 WHERE currentVersion  = 'OMERO4-DEV'     and
          currentPatch    = 1         and
          previousVersion = 'OMERO4'  and
          previousPatch   = 0;

COMMIT;

