--
-- Copyright 2009 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

--
-- OMERO-Beta4.1 release
--
-- UNDERDEVELOPMENT DO NOT USE !!!
--
BEGIN;

--
-- OMERO-Beta4.1 release.
--
BEGIN;

CREATE OR REPLACE FUNCTION omero_assert_omero4_dev_4() RETURNS void AS '
DECLARE
    rec RECORD;
BEGIN

    RAISE EXCEPTION ''UNDERDEVELOPMENT: If you want to test this script, comment this line. Aborting...'';

    SELECT INTO rec *
           FROM dbpatch
          WHERE id = ( SELECT id FROM dbpatch ORDER BY id DESC LIMIT 1 )
            AND currentversion = ''OMERO4-DEV''
            AND currentpatch = 4;

    IF NOT FOUND THEN
        RAISE EXCEPTION ''Current version is not OMERO4-DEV__4! Aborting...'';
    END IF;

END;' LANGUAGE plpgsql;
SELECT omero_assert_omero4_dev_4();
DROP FUNCTION omero_assert_omero4_dev_4();

INSERT into dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
             values ('OMERO4-DEV',   5,              'OMERO4-DEV',            4);

 insert into pixelstype (id,permissions,owner_id,group_id,creation_id,value)
    select ome_nextval('seq_pixelstype'),-35,0,0,0,'bit';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/PNG';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/JPEG';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/PGM';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/Fits';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/GIF';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/BMP';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/Dicom';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/BioRad';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/IPLab';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/Deltavision';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/MRC';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/Gatan';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/Imaris';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/OpenlabRaw';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/OMEXML';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/LIF';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/AVI';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/QT';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/Pict';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/SDT';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/EPS';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/Slidebook';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/Alicona';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/MNG';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/NRRD';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/Khoros';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/Visitech';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/LIM';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/PSD';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/InCell';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/ICS';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/PerkinElmer';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/TCS';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/FV1000';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/ZeissZVI';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/IPW';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/LegacyND2';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/ND2';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/PCI';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/ImarisHDF';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/Metamorph';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/ZeissLSM';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/SEQ';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/Gel';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/ImarisTiff';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/Flex';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/SVS';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/Leica';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/Nikon';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/Fluoview';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/Prairie';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/Micromanager';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/ImprovisionTiff';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/OMETiff';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/MetamorphTiff';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/Tiff';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Companion/Openlab';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Repository';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'Directory';
 insert into format (id,permissions,owner_id,group_id,creation_id,value)
     select ome_nextval('seq_format'),-35,0,0,0,'OMERO.tables';

create or replace function uuid() returns character(36)
as '
    select substring(x.my_rand from 1 for 8)||''-''||
           substring(x.my_rand from 9 for 4)||''-4''||
           substring(x.my_rand from 13 for 3)||''-''||x.clock_1||
           substring(x.my_rand from 16 for 3)||''-''||
           substring(x.my_rand from 19 for 12)
from
(select md5(now()::text||random()) as my_rand, to_hex(8+(3*random())::int) as clock_1) as x;'
language sql;

insert into configuration (name, value) values ('omero.db.uuid',uuid());

UPDATE dbpatch set message = 'Database updated.', finished = now()
 WHERE currentVersion  = 'OMERO4-DEV'     and
          currentPatch    = 5             and
          previousVersion = 'OMERO4-DEV'  and
          previousPatch   = 4;

COMMIT;
