-- 
-- Copyright 2007 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

BEGIN;

    insert into dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
                 values ('OMERO3',       6,              'OMERO3',            5);

    alter  table channelbinding     add column  OMERO6_inputStart        double precision;
    update       channelbinding     set OMERO6_inputStart = (inputStart::numeric)::float8;
    alter  table channelbinding     drop column inputStart;
    alter  table channelbinding     rename OMERO6_inputStart to inputStart;
    alter  table channelbinding     alter column inputStart set not null;

    alter  table channelbinding     add column OMERO6_inputEnd double precision;
    update       channelbinding     set OMERO6_inputEnd = (inputEnd::numeric)::float8;
    alter  table channelbinding     drop column inputEnd;
    alter  table channelbinding     rename OMERO6_inputEnd to inputEnd;
    alter  table channelbinding     alter column inputEnd set not null;

    alter  table detector           add column OMERO6_voltage double precision;
    update       detector           set OMERO6_voltage = (voltage::numeric)::float8;
    alter  table detector           drop column voltage;
    alter  table detector           rename OMERO6_voltage to voltage;

    alter  table detector           add column OMERO6_gain double precision;
    update       detector           set OMERO6_gain = (gain::numeric)::float8;
    alter  table detector           drop column gain;
    alter  table detector           rename OMERO6_gain to gain;

    alter  table detector           add column OMERO6_offsetValue double precision;
    update       detector           set OMERO6_offsetValue = (offsetValue::numeric)::float8;
    alter  table detector           drop column offsetValue;
    alter  table detector           rename OMERO6_offsetValue to offsetValue;

    alter  table detectorsettings   add column OMERO6_voltage double precision;
    update       detectorsettings   set OMERO6_voltage = (voltage::numeric)::float8;
    alter  table detectorsettings   drop column voltage;
    alter  table detectorsettings   rename OMERO6_voltage to voltage;

    alter  table detectorsettings   add column OMERO6_gain double precision;
    update       detectorsettings   set OMERO6_gain = (gain::numeric)::float8;
    alter  table detectorsettings   drop column gain;
    alter  table detectorsettings   rename OMERO6_gain to gain;

    alter  table detectorsettings   add column OMERO6_offsetValue double precision;
    update       detectorsettings   set OMERO6_offsetValue = (offsetValue::numeric)::float8;
    alter  table detectorsettings   drop column offsetValue;
    alter  table detectorsettings   rename OMERO6_offsetValue to offsetValue;

    alter  table detectorsettings   add column OMERO6_readOutRate double precision;
    update       detectorsettings   set OMERO6_readOutRate = (readOutRate::numeric)::float8;
    alter  table detectorsettings   drop column readOutRate;
    alter  table detectorsettings   rename OMERO6_readOutRate to readOutRate;

    alter  table detectorsettings   add column OMERO6_amplification double precision;
    update       detectorsettings   set OMERO6_amplification = (amplification::numeric)::float8;
    alter  table detectorsettings   drop column amplification;
    alter  table detectorsettings   rename OMERO6_amplification to amplification;

    alter  table dummystatistics    add column OMERO6_example double precision;
    update       dummystatistics    set OMERO6_example = (example::numeric)::float8;
    alter  table dummystatistics    drop column example;
    alter  table dummystatistics    rename OMERO6_example to example;
    alter  table dummystatistics    alter column example set not null;

    alter  table imagingenvironment add column OMERO6_temperature double precision;
    update       imagingenvironment set OMERO6_temperature = (temperature::numeric)::float8;
    alter  table imagingenvironment drop column temperature;
    alter  table imagingenvironment rename OMERO6_temperature to temperature;

    alter  table imagingenvironment add column OMERO6_airPressure double precision;
    update       imagingenvironment set OMERO6_airPressure = (airPressure::numeric)::float8;
    alter  table imagingenvironment drop column airPressure;
    alter  table imagingenvironment rename OMERO6_airPressure to airPressure;

    alter  table imagingenvironment add column OMERO6_humidity double precision;
    update       imagingenvironment set OMERO6_humidity = (humidity::numeric)::float8;
    alter  table imagingenvironment drop column humidity;
    alter  table imagingenvironment rename OMERO6_humidity to humidity;

    alter  table imagingenvironment add column OMERO6_co2percent double precision;
    update       imagingenvironment set OMERO6_co2percent = (co2percent::numeric)::float8;
    alter  table imagingenvironment drop column co2percent;
    alter  table imagingenvironment rename OMERO6_co2percent to co2percent;

    alter  table lightsettings      add column OMERO6_power double precision;
    update       lightsettings      set OMERO6_power = (power::numeric)::float8;
    alter  table lightsettings      drop column power;
    alter  table lightsettings      rename OMERO6_power to power;

    alter  table lightsource        add column OMERO6_power double precision;
    update       lightsource        set OMERO6_power = (power::numeric)::float8;
    alter  table lightsource        drop column power;
    alter  table lightsource        rename OMERO6_power to power;
    alter  table lightsource        alter column power set not null;

    alter  table logicalchannel     add column OMERO6_ndFilter double precision;
    update       logicalchannel     set OMERO6_ndFilter = (ndFilter::numeric)::float8;
    alter  table logicalchannel     drop column ndFilter;
    alter  table logicalchannel     rename OMERO6_ndFilter to ndFilter;

    alter  table objective          add column OMERO6_lensNA double precision;
    update       objective          set OMERO6_lensNA = (lensNA::numeric)::float8;
    alter  table objective          drop column lensNA;
    alter  table objective          rename OMERO6_lensNA to lensNA;
    alter  table objective          alter column lensNA set not null;

    alter  table objectivesettings  add column OMERO6_correctionCollar double precision;
    update       objectivesettings  set OMERO6_correctionCollar = (correctionCollar::numeric)::float8;
    alter  table objectivesettings  drop column correctionCollar;
    alter  table objectivesettings  rename OMERO6_correctionCollar to correctionCollar;

    alter  table objectivesettings  add column OMERO6_refractiveIndex double precision;
    update       objectivesettings  set OMERO6_refractiveIndex = (refractiveIndex::numeric)::float8;
    alter  table objectivesettings  drop column refractiveIndex;
    alter  table objectivesettings  rename OMERO6_refractiveIndex to refractiveIndex;

    alter  table pixelsdimensions   add column OMERO6_sizeX double precision;
    update       pixelsdimensions   set OMERO6_sizeX = (sizeX::numeric)::float8;
    alter  table pixelsdimensions   drop column sizeX;
    alter  table pixelsdimensions   rename OMERO6_sizeX to sizeX;
    alter  table pixelsdimensions   alter column sizeX set not null;

    alter  table pixelsdimensions   add column OMERO6_sizeY double precision;
    update       pixelsdimensions   set OMERO6_sizeY = (sizeY::numeric)::float8;
    alter  table pixelsdimensions   drop column sizeY;
    alter  table pixelsdimensions   rename OMERO6_sizeY to sizeY;
    alter  table pixelsdimensions   alter column sizeY set not null;

    alter  table pixelsdimensions   add column OMERO6_sizeZ double precision;
    update       pixelsdimensions   set OMERO6_sizeZ = (sizeZ::numeric)::float8;
    alter  table pixelsdimensions   drop column sizeZ;
    alter  table pixelsdimensions   rename OMERO6_sizeZ to sizeZ;
    alter  table pixelsdimensions   alter column sizeZ set not null;

    alter  table planeinfo          add column OMERO6_timestamp double precision;
    update       planeinfo          set OMERO6_timestamp = (timestamp::numeric)::float8;
    alter  table planeinfo          drop column timestamp;
    alter  table planeinfo          rename OMERO6_timestamp to timestamp;
    alter  table planeinfo          alter column timestamp set not null;

    alter  table planeinfo          add column OMERO6_positionX double precision;
    update       planeinfo          set OMERO6_positionX = (positionX::numeric)::float8;
    alter  table planeinfo          drop column positionX;
    alter  table planeinfo          rename OMERO6_positionX to positionX;

    alter  table planeinfo          add column OMERO6_positionY double precision;
    update       planeinfo          set OMERO6_positionY = (positionY::numeric)::float8;
    alter  table planeinfo          drop column positionY;
    alter  table planeinfo          rename OMERO6_positionY to positionY;

    alter  table planeinfo          add column OMERO6_positionZ double precision;
    update       planeinfo          set OMERO6_positionZ = (positionZ::numeric)::float8;
    alter  table planeinfo          drop column positionZ;
    alter  table planeinfo          rename OMERO6_positionZ to positionZ;

    alter  table planeinfo          add column OMERO6_exposureTime double precision;
    update       planeinfo          set OMERO6_exposureTime = (exposureTime::numeric)::float8;
    alter  table planeinfo          drop column exposureTime;
    alter  table planeinfo          rename OMERO6_exposureTime to exposureTime;

    alter  table stagelabel         add column OMERO6_positionX double precision;
    update       stagelabel         set OMERO6_positionX = (positionX::numeric)::float8;
    alter  table stagelabel         drop column positionX;
    alter  table stagelabel         rename OMERO6_positionX to positionX;
    alter  table stagelabel         alter column positionX set not null;

    alter  table stagelabel         add column OMERO6_positionY double precision;
    update       stagelabel         set OMERO6_positionY = (positionY::numeric)::float8;
    alter  table stagelabel         drop column positionY;
    alter  table stagelabel         rename OMERO6_positionY to positionY;
    alter  table stagelabel         alter column positionY set not null;

    alter  table stagelabel         add column OMERO6_positionZ double precision;
    update       stagelabel         set OMERO6_positionZ = (positionZ::numeric)::float8;
    alter  table stagelabel         drop column positionZ;
    alter  table stagelabel         rename OMERO6_positionZ to positionZ;
    alter  table stagelabel         alter column positionZ set not null;

    alter  table transmittancerange add column OMERO6_transmittance double precision;
    update       transmittancerange set OMERO6_transmittance = (transmittance::numeric)::float8;
    alter  table transmittancerange drop column transmittance;
    alter  table transmittancerange rename OMERO6_transmittance to transmittance;
    alter  table transmittancerange alter column transmittance set not null;

    update dbpatch set message = 'Database updated.', finished = now()
    where currentVersion   = 'OMERO3'    and
           currentPatch    = 6           and
           previousVersion = 'OMERO3'    and
           previousPatch   = 5;

COMMIT;
