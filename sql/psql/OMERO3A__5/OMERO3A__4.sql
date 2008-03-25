-- 
-- Copyright 2007 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

--
-- Adds URLs to repository types for OMERO.fs and multiple repository
-- extensions. Also updates format table with new requested formats that
-- are part of #887 and #892.
--

BEGIN;

    insert into dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
                 values ('OMERO3A',      5,              'OMERO3A',           4);

    alter table pixels add column url varchar(2048);
    
    alter table originalfile add column url varchar(2048);
    
    alter table thumbnail add column url varchar(2048);

    update format set value = 'Deltavision' where value = 'DV';
    update format set value = 'Tiff' where value = 'TIFF';
    update format set value = 'Metamorph' where value = 'STK';

    update dbpatch set message = 'Database updated.', finished = now()
    where currentVersion  = 'OMERO3A' and
          currentPatch    = 5         and
          previousVersion = 'OMERO3A' and
          previousPatch   = 4;

--
-- Formats for importer (#892)
--

    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'PNG';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'JPEG';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'PGM';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'Fits';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'GIF';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'BMP';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'Dicom';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'BioRad';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'IPLab';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'MRC';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'Gatan';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'Imaris';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'OpenlabRaw';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'OMEXML';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'LIF';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'AVI';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'QT';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'Pict';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'SDT';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'EPS';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'Slidebook';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'Alicona';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'MNG';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'NRRD';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'Khoros';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'Visitech';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'LIM';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'PSD';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'InCell';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'ICS';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'PerkinElmer';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'TCS';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'FV1000';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'ZeissZVI';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'IPW';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'LegacyND2';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'ND2';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'PCI';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'ImarisHDF';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'ZeissLSM';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'SEQ';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'Gel';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'ImarisTiff';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'Flex';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'SVS';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'Leica';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'Nikon';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'Fluoview';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'Prairie';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'Micromanager';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'ImprovisionTiff';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'OMETiff';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'MetamorphTiff';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'Openlab';

--
-- Formats for Insight and structured annotations (#887)
--

    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'application/ms-excel';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'application/ms-powerpoint';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'application/ms-word';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'text/rtf';
    insert into format (id,permissions,owner_id,group_id,creation_id,value)
    select nextval('seq_format'),-35,0,0,0,'text/html';

COMMIT;
