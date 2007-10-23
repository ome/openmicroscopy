-- 
-- Copyright 2007 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

BEGIN;

    insert into dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
                 values ('OMERO3',       6,              'OMERO3',            5);

    alter table channelbinding     alter column inputStart       TYPE double precision;
    alter table channelbinding     alter column inputEnd         TYPE double precision;
    alter table detector           alter column voltage          TYPE double precision;
    alter table detector           alter column gain             TYPE double precision;
    alter table detector           alter column offsetValue      TYPE double precision;
    alter table detectorsettings   alter column voltage          TYPE double precision;
    alter table detectorsettings   alter column gain             TYPE double precision;
    alter table detectorsettings   alter column offsetValue      TYPE double precision;
    alter table detectorsettings   alter column readOutRate      TYPE double precision;
    alter table detectorsettings   alter column amplification    TYPE double precision;
    alter table dummystatistics    alter column example          TYPE double precision;
    alter table imagingenvironment alter column temperature      TYPE double precision;
    alter table imagingenvironment alter column airPressure      TYPE double precision;
    alter table imagingenvironment alter column humidity         TYPE double precision;
    alter table imagingenvironment alter column co2percent       TYPE double precision;
    alter table lightsettings      alter column power            TYPE double precision;
    alter table lightsource        alter column power            TYPE double precision;
    alter table logicalchannel     alter column ndFilter         TYPE double precision;
    alter table objective          alter column lensNA           TYPE double precision;
    alter table objectivesettings  alter column correctionCollar TYPE double precision;
    alter table objectivesettings  alter column refractiveIndex  TYPE double precision;
    alter table pixelsdimensions   alter column sizeX            TYPE double precision;
    alter table pixelsdimensions   alter column sizeY            TYPE double precision;
    alter table pixelsdimensions   alter column sizeZ            TYPE double precision;
    alter table planeinfo          alter column timestamp        TYPE double precision;
    alter table planeinfo          alter column positionX        TYPE double precision;
    alter table planeinfo          alter column positionY        TYPE double precision;
    alter table planeinfo          alter column positionZ        TYPE double precision;
    alter table planeinfo          alter column exposureTime     TYPE double precision;
    alter table stagelabel         alter column positionX        TYPE double precision;
    alter table stagelabel         alter column positionY        TYPE double precision;
    alter table stagelabel         alter column positionZ        TYPE double precision;
    alter table transmittancerange alter column transmittance    TYPE double precision;

    update dbpatch set message = 'Database updated.', finished = now()
    where currentVersion   = 'OMERO3'    and
           currentPatch    = 6           and
           previousVersion = 'OMERO3'    and
           previousPatch   = 5;

COMMIT;
