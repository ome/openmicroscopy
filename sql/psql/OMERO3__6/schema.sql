-- 
-- Copyright 2007 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

BEGIN;

    create table aberrationcorrection (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        value varchar(255) not null unique,
        primary key (id)
    );

    create table acquisitionmode (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        value varchar(255) not null unique,
        primary key (id)
    );

    create table arc (
        lightsource_id int8 not null,
        type int8 not null,
        primary key (lightsource_id)
    );

    create table arctype (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        value varchar(255) not null unique,
        primary key (id)
    );

    create table binning (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        value varchar(255) not null unique,
        primary key (id)
    );

    create table boundingbox (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        region int8 not null,
        x1 int4 not null,
        x2 int4 not null,
        y1 int4 not null,
        y2 int4 not null,
        primary key (id)
    );

    create table category (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        name varchar(256) not null,
        description text,
        primary key (id)
    );

    create table categorygroup (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        name varchar(256) not null,
        description text,
        primary key (id)
    );

    create table categorygroupcategorylink (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        parent int8 not null,
        child int8 not null,
        primary key (id)
    );

    create table categoryimagelink (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        parent int8 not null,
        child int8 not null,
        primary key (id)
    );

    create table cellarea (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        area float8 not null,
        primary key (id)
    );

    create table celleccentricity (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        eccentricity float8 not null,
        primary key (id)
    );

    create table cellextent (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        extent float8 not null,
        primary key (id)
    );

    create table cellmajoraxislength (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        length float8 not null,
        primary key (id)
    );

    create table cellminoraxislength (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        length float8 not null,
        primary key (id)
    );

    create table cellperimeter (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        perimeter float8 not null,
        primary key (id)
    );

    create table cellposition (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        xPosition float8 not null,
        yPosition float8 not null,
        zPosition float8 not null,
        primary key (id)
    );

    create table cellsolidity (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        solidity float8 not null,
        primary key (id)
    );

    create table channel (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        statsInfo int8,
        colorComponent int8,
        logicalChannel int8,
        pixels int8 not null,
        index int4,
        primary key (id)
    );

    create table channelbinding (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        renderingDef int8 not null,
        family int8 not null,
        coefficient float8 not null,
        inputStart double precision not null,
        inputEnd double precision not null,
        active bool not null,
        noiseReduction bool not null,
        color int8 not null,
        index int4,
        primary key (id)
    );

    create table coating (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        value varchar(255) not null unique,
        primary key (id)
    );

    create table codomainmapcontext (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        renderingDef int8 not null,
        index int4,
        primary key (id)
    );

    create table color (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        red int4 not null,
        green int4 not null,
        blue int4 not null,
        alpha int4 not null,
        primary key (id)
    );

    create table contrastmethod (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        value varchar(255) not null unique,
        primary key (id)
    );

    create table contraststretchingcontext (
        codomainmapcontext_id int8 not null,
        xstart int4 not null,
        ystart int4 not null,
        xend int4 not null,
        yend int4 not null,
        primary key (codomainmapcontext_id)
    );

    create table customizedfilterset (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        manufacturer varchar(255) not null,
        model varchar(255) not null,
        serialNumber varchar(255),
        excitationFilter int8 not null,
        dichroic int8 not null,
        emissionFilter int8 not null,
        transmittanceRange int8,
        primary key (id)
    );

    create table dataset (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        name varchar(256) not null,
        description text,
        primary key (id)
    );

    create table datasetannotation (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        dataset int8 not null,
        content text not null,
        primary key (id)
    );

    create table datasetimagelink (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        parent int8 not null,
        child int8 not null,
        primary key (id)
    );

    create table dbpatch (
        id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        currentVersion varchar(255) not null,
        currentPatch int4 not null,
        previousVersion varchar(255) not null,
        previousPatch int4 not null,
        finished timestamp,
        message varchar(255),
        primary key (id)
    );

    create table detector (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        manufacturer varchar(255) not null,
        model varchar(255) not null,
        serialNumber varchar(255),
        voltage double precision,
        gain double precision,
        offsetValue double precision,
        type int8 not null,
        instrument int8 not null,
        primary key (id)
    );

    create table detectorsettings (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        voltage double precision,
        gain double precision,
        offsetValue double precision,
        readOutRate double precision,
        binning int8,
        amplification double precision,
        detector int8 not null,
        primary key (id)
    );

    create table detectortype (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        value varchar(255) not null unique,
        primary key (id)
    );

    create table dichroic (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        primary key (id)
    );

    create table dimensionorder (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        value varchar(255) not null unique,
        primary key (id)
    );

    create table dummystatistics (
        metadata_id int8 not null,
        example double precision not null,
        primary key (metadata_id)
    );

    create table emissionfilter (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        type int8 not null,
        transmittanceRange int8,
        primary key (id)
    );

    create table event (
        id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        status varchar(255),
        time timestamp not null,
        experimenter int8 not null,
        experimenterGroup int8,
        type int8,
        containingEvent int8,
        primary key (id)
    );

    create table eventlog (
        id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        entityId int8 not null,
        entityType varchar(255) not null,
        action varchar(255) not null,
        event int8 not null,
        primary key (id)
    );

    create table eventtype (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        value varchar(255) not null unique,
        primary key (id)
    );

    create table excitationfilter (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        type int8 not null,
        transmittanceRange int8,
        primary key (id)
    );

    create table experiment (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        description text,
        type int8 not null,
        primary key (id)
    );

    create table experimenter (
        id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        omeName varchar(255) not null unique,
        firstName varchar(255) not null,
        middleName varchar(255),
        lastName varchar(255) not null,
        institution varchar(255),
        email varchar(255),
        primary key (id)
    );

    create table experimentergroup (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        description text,
        name varchar(255) not null unique,
        primary key (id)
    );

    create table experimenttype (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        value varchar(255) not null unique,
        primary key (id)
    );

    create table externalinfo (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        entityId int8 not null,
        entityType varchar(255) not null,
        lsid varchar(255) not null,
        primary key (id)
    );

    create table family (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        value varchar(255) not null unique,
        primary key (id)
    );

    create table filament (
        lightsource_id int8 not null,
        type int8 not null,
        primary key (lightsource_id)
    );

    create table filamenttype (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        value varchar(255) not null unique,
        primary key (id)
    );

    create table filter (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        customized bool not null,
        filterSet int8,
        customizedFilterSet int8,
        instrument int8 not null,
        primary key (id)
    );

    create table filterset (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        manufacturer varchar(255) not null,
        model varchar(255) not null,
        serialNumber varchar(255),
        transmittanceRange int8,
        primary key (id)
    );

    create table filtertype (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        value varchar(255) not null unique,
        primary key (id)
    );

    create table format (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        value varchar(255) not null unique,
        primary key (id)
    );

    create table frequencymultiplication (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        value varchar(255) not null unique,
        primary key (id)
    );

    create table groupexperimentermap (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        parent int8 not null,
        defaultGroupLink bool,
        child int8 not null,
        primary key (id)
    );

    create table illumination (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        value varchar(255) not null unique,
        primary key (id)
    );

    create table image (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        name varchar(256) not null,
        description text,
        condition int8,
        objectiveSettings int8,
        setup int8,
        position int8,
        context int8,
        primary key (id)
    );

    create table imageannotation (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        image int8 not null,
        content text not null,
        primary key (id)
    );

    create table imagecellcount (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        count int4 not null,
        primary key (id)
    );

    create table imagenucleascount (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        count int4 not null,
        primary key (id)
    );

    create table imagingenvironment (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        temperature double precision,
        airPressure double precision,
        humidity double precision,
        co2percent double precision,
        primary key (id)
    );

    create table immersion (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        value varchar(255) not null unique,
        primary key (id)
    );

    create table importjob (
        job_id int8 not null,
        imageName varchar(255) not null,
        imageDescription varchar(255) not null,
        primary key (job_id)
    );

    create table instrument (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        microscope int8 not null,
        primary key (id)
    );

    create table irisdiaphragm (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        value varchar(255) not null unique,
        primary key (id)
    );

    create table job (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        username varchar(255) not null,
        groupname varchar(255) not null,
        type varchar(255) not null,
        message varchar(255) not null,
        status int8 not null,
        submitted timestamp not null,
        scheduledFor timestamp not null,
        started timestamp,
        finished timestamp,
        primary key (id)
    );

    create table joboriginalfilelink (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        parent int8 not null,
        child int8 not null,
        primary key (id)
    );

    create table jobstatus (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        value varchar(255) not null unique,
        primary key (id)
    );

    create table laser (
        lightsource_id int8 not null,
        type int8 not null,
        laserMedium int8 not null,
        frequencyMultiplication int8 not null,
        tunable bool not null,
        pulse int8,
        wavelength int4,
        pockelCell bool not null,
        pump int8,
        primary key (lightsource_id)
    );

    create table lasermedium (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        value varchar(255) not null unique,
        primary key (id)
    );

    create table lasertype (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        value varchar(255) not null unique,
        primary key (id)
    );

    create table lightsettings (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        power double precision,
        technique varchar(255),
        lightSource int8 not null,
        primary key (id)
    );

    create table lightsource (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        manufacturer varchar(255) not null,
        model varchar(255) not null,
        power double precision not null,
        serialNumber varchar(255),
        instrument int8 not null,
        primary key (id)
    );

    create table link (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        primary key (id)
    );

    create table logicalchannel (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        name varchar(255),
        pinHoleSize int4,
        illumination int8,
        contrastMethod int8,
        excitationWave int4,
        emissionWave int4,
        fluor varchar(255),
        ndFilter double precision,
        otf int8,
        detectorSettings int8,
        lightSource int8,
        auxLightSource int8,
        photometricInterpretation int8,
        mode int8,
        pockelCellSetting varchar(255),
        primary key (id)
    );

    create table medium (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        value varchar(255) not null unique,
        primary key (id)
    );

    create table metadata (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        region int8 not null,
        primary key (id)
    );

    create table microscope (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        manufacturer varchar(255) not null,
        model varchar(255) not null,
        serialNumber varchar(255),
        type int8 not null,
        primary key (id)
    );

    create table microscopetype (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        value varchar(255) not null unique,
        primary key (id)
    );

    create table nucleusarea (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        area float8 not null,
        primary key (id)
    );

    create table nucleuseccentricity (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        eccentricity float8 not null,
        primary key (id)
    );

    create table nucleusextent (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        extent float8 not null,
        primary key (id)
    );

    create table nucleusmajoraxislength (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        length float8 not null,
        primary key (id)
    );

    create table nucleusminoraxislength (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        length float8 not null,
        primary key (id)
    );

    create table nucleusperimeter (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        perimenter float8 not null,
        primary key (id)
    );

    create table nucleusposition (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        xPosition float8 not null,
        yPosition float8 not null,
        zPosition float8 not null,
        primary key (id)
    );

    create table nucleussolidity (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        solidity float8 not null,
        primary key (id)
    );

    create table objective (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        manufacturer varchar(255) not null,
        model varchar(255) not null,
        serialNumber varchar(255),
        magnificiation float8 not null,
        lensNA double precision not null,
        immersion int8,
        coating int8,
        instrument int8 not null,
        primary key (id)
    );

    create table objectivesettings (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        correctionCollar double precision,
        medium int8,
        refractiveIndex double precision,
        objective int8 not null,
        primary key (id)
    );

    create table originalfile (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        path varchar(255) not null,
        name varchar(255) not null,
        size int8 not null,
        atime timestamp,
        mtime timestamp,
        ctime timestamp,
        sha1 varchar(255) not null,
        format int8 not null,
        primary key (id)
    );

    create table otf (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        sizeX int4 not null,
        sizeY int4 not null,
        opticalAxisAvg bool not null,
        pixelType int8 not null,
        path varchar(255) not null,
        primary key (id)
    );

    create table overlay (
        specification_id int8 not null,
        plane int8 not null,
        type int8 not null,
        color varchar(255),
        text varchar(255),
        filled bool,
        grouped varchar(255),
        primary key (specification_id)
    );

    create table overlaytype (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        value varchar(255) not null unique,
        primary key (id)
    );

    create table photometricinterpretation (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        value varchar(255) not null unique,
        primary key (id)
    );

    create table pixels (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        defaultPixels bool,
        image int8 not null,
        relatedTo int8,
        pixelsType int8 not null,
        sizeX int4 not null,
        sizeY int4 not null,
        sizeZ int4 not null,
        sizeC int4 not null,
        sizeT int4 not null,
        sha1 varchar(255) not null,
        dimensionOrder int8 not null,
        methodology varchar(255),
        pixelsDimensions int8 not null,
        primary key (id)
    );

    create table pixelsdimensions (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        sizeX double precision not null,
        sizeY double precision not null,
        sizeZ double precision not null,
        primary key (id)
    );

    create table pixelsoriginalfilemap (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        parent int8 not null,
        child int8 not null,
        primary key (id)
    );

    create table pixelstype (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        value varchar(255) not null unique,
        primary key (id)
    );

    create table planeinfo (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        pixels int8 not null,
        theZ int4 not null,
        theC int4 not null,
        theT int4 not null,
        timestamp double precision not null,
        positionX double precision,
        positionY double precision,
        positionZ double precision,
        exposureTime double precision,
        primary key (id)
    );

    create table planeslicingcontext (
        codomainmapcontext_id int8 not null,
        upperLimit int4 not null,
        lowerLimit int4 not null,
        planeSelected int4 not null,
        planePrevious int4 not null,
        constant bool not null,
        primary key (codomainmapcontext_id)
    );

    create table project (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        name varchar(256) not null,
        description text,
        primary key (id)
    );

    create table projectannotation (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        project int8 not null,
        content text not null,
        primary key (id)
    );

    create table projectdatasetlink (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        parent int8 not null,
        child int8 not null,
        primary key (id)
    );

    create table pulse (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        value varchar(255) not null unique,
        primary key (id)
    );

    create table quantumdef (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        cdStart int4 not null,
        cdEnd int4 not null,
        bitResolution int4 not null,
        primary key (id)
    );

    create table region (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        type int8,
        pixels int8 not null,
        primary key (id)
    );

    create table regiontype (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        value varchar(255) not null unique,
        primary key (id)
    );

    create table renderingdef (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        pixels int8 not null,
        defaultZ int4 not null,
        defaultT int4 not null,
        model int8 not null,
        quantization int8 not null,
        primary key (id)
    );

    create table renderingmodel (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        value varchar(255) not null unique,
        primary key (id)
    );

    create table reverseintensitycontext (
        codomainmapcontext_id int8 not null,
        reverse bool not null,
        primary key (codomainmapcontext_id)
    );

    create table roi5d (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        pixels int8 not null,
        primary key (id)
    );

    create table roiextent (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        zindexMin int4 not null,
        zindexMax int4 not null,
        tindexMin int4 not null,
        tindexMax int4 not null,
        cindexMin int4 not null,
        cindexMax int4 not null,
        roi5d int8 not null,
        primary key (id)
    );

    create table roimap (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        parent int8 not null,
        child int8 not null,
        primary key (id)
    );

    create table roiset (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        sourceType varchar(255),
        sourceId int4,
        primary key (id)
    );

    create table shapearea (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        extent int8 not null,
        roiextent int8 not null,
        primary key (id)
    );

    create table specification (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        region int8 not null,
        primary key (id)
    );

    create table square (
        shapearea_id int8 not null,
        upperLeftX int4 not null,
        upperLeftY int4 not null,
        lowerRightX int4 not null,
        lowerRightY int4 not null,
        primary key (shapearea_id)
    );

    create table stagelabel (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        name varchar(255) not null,
        positionX double precision not null,
        positionY double precision not null,
        positionZ double precision not null,
        primary key (id)
    );

    create table statsinfo (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        globalMin float8 not null,
        globalMax float8 not null,
        primary key (id)
    );

    create table thumbnail (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        pixels int8 not null,
        mimeType varchar(255) not null,
        sizeX int4 not null,
        sizeY int4 not null,
        ref varchar(255),
        primary key (id)
    );

    create table transmittancerange (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        cutIn int4 not null,
        cutOut int4 not null,
        cutInTolerance int4,
        cutOutTolerance int4,
        transmittance double precision not null,
        primary key (id)
    );

    create table uroi (
        specification_id int8 not null,
        box int8,
        primary key (specification_id)
    );

    create table ushape (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        uslice int8 not null,
        primary key (id)
    );

    create table uslice (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        z int4 not null,
        c int4 not null,
        t int4 not null,
        uroi int8 not null,
        primary key (id)
    );

    create table usquare (
        ushape_id int8 not null,
        upperLeftX int4 not null,
        upperLeftY int4 not null,
        lowerRightX int4 not null,
        lowerRightY int4 not null,
        primary key (ushape_id)
    );

    create table xy (
        boundingbox_id int8 not null,
        z int4,
        c int4,
        t int4,
        primary key (boundingbox_id)
    );

    create table xyc (
        boundingbox_id int8 not null,
        c1 int4 not null,
        c2 int4 not null,
        z int4,
        t int4,
        primary key (boundingbox_id)
    );

    create table xyct (
        boundingbox_id int8 not null,
        c1 int4 not null,
        c2 int4 not null,
        t1 int4 not null,
        t2 int4 not null,
        z int4,
        primary key (boundingbox_id)
    );

    create table xyctoxylink (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        parent int8 not null,
        child int8 not null,
        primary key (id)
    );

    create table xycttoxyclink (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        parent int8 not null,
        child int8 not null,
        primary key (id)
    );

    create table xycttoxytlink (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        parent int8 not null,
        child int8 not null,
        primary key (id)
    );

    create table xyt (
        boundingbox_id int8 not null,
        t1 int4 not null,
        t2 int4 not null,
        z int4,
        c int4,
        primary key (boundingbox_id)
    );

    create table xyttoxylink (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        parent int8 not null,
        child int8 not null,
        primary key (id)
    );

    create table xyz (
        boundingbox_id int8 not null,
        z1 int4 not null,
        z2 int4 not null,
        c int4,
        t int4,
        primary key (boundingbox_id)
    );

    create table xyzc (
        boundingbox_id int8 not null,
        z1 int4 not null,
        z2 int4 not null,
        c1 int4 not null,
        c2 int4 not null,
        t int4,
        primary key (boundingbox_id)
    );

    create table xyzct (
        boundingbox_id int8 not null,
        z1 int4 not null,
        z2 int4 not null,
        c1 int4 not null,
        c2 int4 not null,
        t1 int4 not null,
        t2 int4 not null,
        primary key (boundingbox_id)
    );

    create table xyzctoxyclink (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        parent int8 not null,
        child int8 not null,
        primary key (id)
    );

    create table xyzctoxyzlink (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        parent int8 not null,
        child int8 not null,
        primary key (id)
    );

    create table xyzcttoxyctlink (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        parent int8 not null,
        child int8 not null,
        primary key (id)
    );

    create table xyzcttoxyzclink (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        parent int8 not null,
        child int8 not null,
        primary key (id)
    );

    create table xyzcttoxyztlink (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        parent int8 not null,
        child int8 not null,
        primary key (id)
    );

    create table xyzt (
        boundingbox_id int8 not null,
        z1 int4 not null,
        z2 int4 not null,
        t1 int4 not null,
        t2 int4 not null,
        c int4,
        primary key (boundingbox_id)
    );

    create table xyztoxylink (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        parent int8 not null,
        child int8 not null,
        primary key (id)
    );

    create table xyzttoxytlink (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        parent int8 not null,
        child int8 not null,
        primary key (id)
    );

    create table xyzttoxyzlink (
        id int8 not null,
        owner_id int8 not null,
        group_id int8 not null,
        creation_id int8 not null,
        update_id int8 not null,
        permissions int8 not null,
        external_id int8 unique,
        version int4 default 0,
        parent int8 not null,
        child int8 not null,
        primary key (id)
    );

    alter table aberrationcorrection 
        add constraint FKaberrationcorrection_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table aberrationcorrection 
        add constraint FKaberrationcorrection_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table aberrationcorrection 
        add constraint FKaberrationcorrection_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table aberrationcorrection 
        add constraint FKaberrationcorrection_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table acquisitionmode 
        add constraint FKacquisitionmode_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table acquisitionmode 
        add constraint FKacquisitionmode_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table acquisitionmode 
        add constraint FKacquisitionmode_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table acquisitionmode 
        add constraint FKacquisitionmode_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table arc 
        add constraint FKarc_type_arctype
        foreign key (type) 
        references arctype;

    alter table arc 
        add constraint FKarc_lightsource_id_lightsource
        foreign key (lightsource_id) 
        references lightsource;

    alter table arctype 
        add constraint FKarctype_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table arctype 
        add constraint FKarctype_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table arctype 
        add constraint FKarctype_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table arctype 
        add constraint FKarctype_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table binning 
        add constraint FKbinning_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table binning 
        add constraint FKbinning_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table binning 
        add constraint FKbinning_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table binning 
        add constraint FKbinning_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table boundingbox 
        add constraint FKboundingbox_update_id_event
        foreign key (update_id) 
        references event;

    alter table boundingbox 
        add constraint FKboundingbox_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table boundingbox 
        add constraint FKboundingbox_region_region
        foreign key (region) 
        references region;

    alter table boundingbox 
        add constraint FKboundingbox_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table boundingbox 
        add constraint FKboundingbox_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table boundingbox 
        add constraint FKboundingbox_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table category 
        add constraint FKcategory_update_id_event
        foreign key (update_id) 
        references event;

    alter table category 
        add constraint FKcategory_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table category 
        add constraint FKcategory_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table category 
        add constraint FKcategory_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table category 
        add constraint FKcategory_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table categorygroup 
        add constraint FKcategorygroup_update_id_event
        foreign key (update_id) 
        references event;

    alter table categorygroup 
        add constraint FKcategorygroup_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table categorygroup 
        add constraint FKcategorygroup_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table categorygroup 
        add constraint FKcategorygroup_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table categorygroup 
        add constraint FKcategorygroup_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table categorygroupcategorylink 
        add constraint FKcategorygroupcategorylink_child_category
        foreign key (child) 
        references category;

    alter table categorygroupcategorylink 
        add constraint FKcategorygroupcategorylink_update_id_event
        foreign key (update_id) 
        references event;

    alter table categorygroupcategorylink 
        add constraint FKcategorygroupcategorylink_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table categorygroupcategorylink 
        add constraint FKcategorygroupcategorylink_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table categorygroupcategorylink 
        add constraint FKcategorygroupcategorylink_parent_categorygroup
        foreign key (parent) 
        references categorygroup;

    alter table categorygroupcategorylink 
        add constraint FKcategorygroupcategorylink_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table categorygroupcategorylink 
        add constraint FKcategorygroupcategorylink_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table categoryimagelink 
        add constraint FKcategoryimagelink_child_image
        foreign key (child) 
        references image;

    alter table categoryimagelink 
        add constraint FKcategoryimagelink_update_id_event
        foreign key (update_id) 
        references event;

    alter table categoryimagelink 
        add constraint FKcategoryimagelink_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table categoryimagelink 
        add constraint FKcategoryimagelink_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table categoryimagelink 
        add constraint FKcategoryimagelink_parent_category
        foreign key (parent) 
        references category;

    alter table categoryimagelink 
        add constraint FKcategoryimagelink_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table categoryimagelink 
        add constraint FKcategoryimagelink_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table cellarea 
        add constraint FKcellarea_update_id_event
        foreign key (update_id) 
        references event;

    alter table cellarea 
        add constraint FKcellarea_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table cellarea 
        add constraint FKcellarea_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table cellarea 
        add constraint FKcellarea_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table cellarea 
        add constraint FKcellarea_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table celleccentricity 
        add constraint FKcelleccentricity_update_id_event
        foreign key (update_id) 
        references event;

    alter table celleccentricity 
        add constraint FKcelleccentricity_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table celleccentricity 
        add constraint FKcelleccentricity_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table celleccentricity 
        add constraint FKcelleccentricity_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table celleccentricity 
        add constraint FKcelleccentricity_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table cellextent 
        add constraint FKcellextent_update_id_event
        foreign key (update_id) 
        references event;

    alter table cellextent 
        add constraint FKcellextent_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table cellextent 
        add constraint FKcellextent_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table cellextent 
        add constraint FKcellextent_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table cellextent 
        add constraint FKcellextent_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table cellmajoraxislength 
        add constraint FKcellmajoraxislength_update_id_event
        foreign key (update_id) 
        references event;

    alter table cellmajoraxislength 
        add constraint FKcellmajoraxislength_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table cellmajoraxislength 
        add constraint FKcellmajoraxislength_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table cellmajoraxislength 
        add constraint FKcellmajoraxislength_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table cellmajoraxislength 
        add constraint FKcellmajoraxislength_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table cellminoraxislength 
        add constraint FKcellminoraxislength_update_id_event
        foreign key (update_id) 
        references event;

    alter table cellminoraxislength 
        add constraint FKcellminoraxislength_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table cellminoraxislength 
        add constraint FKcellminoraxislength_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table cellminoraxislength 
        add constraint FKcellminoraxislength_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table cellminoraxislength 
        add constraint FKcellminoraxislength_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table cellperimeter 
        add constraint FKcellperimeter_update_id_event
        foreign key (update_id) 
        references event;

    alter table cellperimeter 
        add constraint FKcellperimeter_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table cellperimeter 
        add constraint FKcellperimeter_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table cellperimeter 
        add constraint FKcellperimeter_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table cellperimeter 
        add constraint FKcellperimeter_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table cellposition 
        add constraint FKcellposition_update_id_event
        foreign key (update_id) 
        references event;

    alter table cellposition 
        add constraint FKcellposition_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table cellposition 
        add constraint FKcellposition_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table cellposition 
        add constraint FKcellposition_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table cellposition 
        add constraint FKcellposition_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table cellsolidity 
        add constraint FKcellsolidity_update_id_event
        foreign key (update_id) 
        references event;

    alter table cellsolidity 
        add constraint FKcellsolidity_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table cellsolidity 
        add constraint FKcellsolidity_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table cellsolidity 
        add constraint FKcellsolidity_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table cellsolidity 
        add constraint FKcellsolidity_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table channel 
        add constraint FKchannel_colorComponent_color
        foreign key (colorComponent) 
        references color;

    alter table channel 
        add constraint FKchannel_pixels_pixels
        foreign key (pixels) 
        references pixels;

    alter table channel 
        add constraint FKchannel_update_id_event
        foreign key (update_id) 
        references event;

    alter table channel 
        add constraint FKchannel_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table channel 
        add constraint FKchannel_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table channel 
        add constraint FKchannel_logicalChannel_logicalchannel
        foreign key (logicalChannel) 
        references logicalchannel;

    alter table channel 
        add constraint FKchannel_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table channel 
        add constraint FKchannel_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table channel 
        add constraint FKchannel_statsInfo_statsinfo
        foreign key (statsInfo) 
        references statsinfo;

    alter table channelbinding 
        add constraint FKchannelbinding_color_color
        foreign key (color) 
        references color;

    alter table channelbinding 
        add constraint FKchannelbinding_update_id_event
        foreign key (update_id) 
        references event;

    alter table channelbinding 
        add constraint FKchannelbinding_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table channelbinding 
        add constraint FKchannelbinding_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table channelbinding 
        add constraint FKchannelbinding_renderingDef_renderingdef
        foreign key (renderingDef) 
        references renderingdef;

    alter table channelbinding 
        add constraint FKchannelbinding_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table channelbinding 
        add constraint FKchannelbinding_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table channelbinding 
        add constraint FKchannelbinding_family_family
        foreign key (family) 
        references family;

    alter table coating 
        add constraint FKcoating_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table coating 
        add constraint FKcoating_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table coating 
        add constraint FKcoating_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table coating 
        add constraint FKcoating_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table codomainmapcontext 
        add constraint FKcodomainmapcontext_update_id_event
        foreign key (update_id) 
        references event;

    alter table codomainmapcontext 
        add constraint FKcodomainmapcontext_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table codomainmapcontext 
        add constraint FKcodomainmapcontext_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table codomainmapcontext 
        add constraint FKcodomainmapcontext_renderingDef_renderingdef
        foreign key (renderingDef) 
        references renderingdef;

    alter table codomainmapcontext 
        add constraint FKcodomainmapcontext_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table codomainmapcontext 
        add constraint FKcodomainmapcontext_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table color 
        add constraint FKcolor_update_id_event
        foreign key (update_id) 
        references event;

    alter table color 
        add constraint FKcolor_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table color 
        add constraint FKcolor_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table color 
        add constraint FKcolor_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table color 
        add constraint FKcolor_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table contrastmethod 
        add constraint FKcontrastmethod_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table contrastmethod 
        add constraint FKcontrastmethod_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table contrastmethod 
        add constraint FKcontrastmethod_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table contrastmethod 
        add constraint FKcontrastmethod_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table contraststretchingcontext 
        add constraint FKcontraststretchingcontext_codomainmapcontext_id_codomainmapcontext
        foreign key (codomainmapcontext_id) 
        references codomainmapcontext;

    alter table customizedfilterset 
        add constraint FKcustomizedfilterset_excitationFilter_excitationfilter
        foreign key (excitationFilter) 
        references excitationfilter;

    alter table customizedfilterset 
        add constraint FKcustomizedfilterset_update_id_event
        foreign key (update_id) 
        references event;

    alter table customizedfilterset 
        add constraint FKcustomizedfilterset_transmittanceRange_transmittancerange
        foreign key (transmittanceRange) 
        references transmittancerange;

    alter table customizedfilterset 
        add constraint FKcustomizedfilterset_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table customizedfilterset 
        add constraint FKcustomizedfilterset_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table customizedfilterset 
        add constraint FKcustomizedfilterset_emissionFilter_emissionfilter
        foreign key (emissionFilter) 
        references emissionfilter;

    alter table customizedfilterset 
        add constraint FKcustomizedfilterset_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table customizedfilterset 
        add constraint FKcustomizedfilterset_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table customizedfilterset 
        add constraint FKcustomizedfilterset_dichroic_dichroic
        foreign key (dichroic) 
        references dichroic;

    alter table dataset 
        add constraint FKdataset_update_id_event
        foreign key (update_id) 
        references event;

    alter table dataset 
        add constraint FKdataset_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table dataset 
        add constraint FKdataset_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table dataset 
        add constraint FKdataset_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table dataset 
        add constraint FKdataset_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table datasetannotation 
        add constraint FKdatasetannotation_update_id_event
        foreign key (update_id) 
        references event;

    alter table datasetannotation 
        add constraint FKdatasetannotation_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table datasetannotation 
        add constraint FKdatasetannotation_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table datasetannotation 
        add constraint FKdatasetannotation_dataset_dataset
        foreign key (dataset) 
        references dataset;

    alter table datasetannotation 
        add constraint FKdatasetannotation_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table datasetannotation 
        add constraint FKdatasetannotation_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table datasetimagelink 
        add constraint FKdatasetimagelink_child_image
        foreign key (child) 
        references image;

    alter table datasetimagelink 
        add constraint FKdatasetimagelink_update_id_event
        foreign key (update_id) 
        references event;

    alter table datasetimagelink 
        add constraint FKdatasetimagelink_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table datasetimagelink 
        add constraint FKdatasetimagelink_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table datasetimagelink 
        add constraint FKdatasetimagelink_parent_dataset
        foreign key (parent) 
        references dataset;

    alter table datasetimagelink 
        add constraint FKdatasetimagelink_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table datasetimagelink 
        add constraint FKdatasetimagelink_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table dbpatch 
        add constraint FKdbpatch_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table detector 
        add constraint FKdetector_instrument_instrument
        foreign key (instrument) 
        references instrument;

    alter table detector 
        add constraint FKdetector_update_id_event
        foreign key (update_id) 
        references event;

    alter table detector 
        add constraint FKdetector_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table detector 
        add constraint FKdetector_type_detectortype
        foreign key (type) 
        references detectortype;

    alter table detector 
        add constraint FKdetector_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table detector 
        add constraint FKdetector_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table detector 
        add constraint FKdetector_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table detectorsettings 
        add constraint FKdetectorsettings_binning_binning
        foreign key (binning) 
        references binning;

    alter table detectorsettings 
        add constraint FKdetectorsettings_detector_detector
        foreign key (detector) 
        references detector;

    alter table detectorsettings 
        add constraint FKdetectorsettings_update_id_event
        foreign key (update_id) 
        references event;

    alter table detectorsettings 
        add constraint FKdetectorsettings_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table detectorsettings 
        add constraint FKdetectorsettings_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table detectorsettings 
        add constraint FKdetectorsettings_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table detectorsettings 
        add constraint FKdetectorsettings_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table detectortype 
        add constraint FKdetectortype_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table detectortype 
        add constraint FKdetectortype_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table detectortype 
        add constraint FKdetectortype_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table detectortype 
        add constraint FKdetectortype_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table dichroic 
        add constraint FKdichroic_update_id_event
        foreign key (update_id) 
        references event;

    alter table dichroic 
        add constraint FKdichroic_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table dichroic 
        add constraint FKdichroic_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table dichroic 
        add constraint FKdichroic_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table dichroic 
        add constraint FKdichroic_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table dimensionorder 
        add constraint FKdimensionorder_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table dimensionorder 
        add constraint FKdimensionorder_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table dimensionorder 
        add constraint FKdimensionorder_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table dimensionorder 
        add constraint FKdimensionorder_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table dummystatistics 
        add constraint FKdummystatistics_metadata_id_metadata
        foreign key (metadata_id) 
        references metadata;

    alter table emissionfilter 
        add constraint FKemissionfilter_update_id_event
        foreign key (update_id) 
        references event;

    alter table emissionfilter 
        add constraint FKemissionfilter_transmittanceRange_transmittancerange
        foreign key (transmittanceRange) 
        references transmittancerange;

    alter table emissionfilter 
        add constraint FKemissionfilter_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table emissionfilter 
        add constraint FKemissionfilter_type_filtertype
        foreign key (type) 
        references filtertype;

    alter table emissionfilter 
        add constraint FKemissionfilter_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table emissionfilter 
        add constraint FKemissionfilter_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table emissionfilter 
        add constraint FKemissionfilter_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table event 
        add constraint FKevent_type_eventtype
        foreign key (type) 
        references eventtype;

    alter table event 
        add constraint FKevent_experimenterGroup_experimentergroup
        foreign key (experimenterGroup) 
        references experimentergroup;

    alter table event 
        add constraint FKevent_containingEvent_event
        foreign key (containingEvent) 
        references event;

    alter table event 
        add constraint FKevent_experimenter_experimenter
        foreign key (experimenter) 
        references experimenter;

    alter table event 
        add constraint FKevent_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table eventlog 
        add constraint FKeventlog_event_event
        foreign key (event) 
        references event;

    alter table eventlog 
        add constraint FKeventlog_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table eventtype 
        add constraint FKeventtype_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table eventtype 
        add constraint FKeventtype_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table eventtype 
        add constraint FKeventtype_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table eventtype 
        add constraint FKeventtype_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table excitationfilter 
        add constraint FKexcitationfilter_update_id_event
        foreign key (update_id) 
        references event;

    alter table excitationfilter 
        add constraint FKexcitationfilter_transmittanceRange_transmittancerange
        foreign key (transmittanceRange) 
        references transmittancerange;

    alter table excitationfilter 
        add constraint FKexcitationfilter_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table excitationfilter 
        add constraint FKexcitationfilter_type_filtertype
        foreign key (type) 
        references filtertype;

    alter table excitationfilter 
        add constraint FKexcitationfilter_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table excitationfilter 
        add constraint FKexcitationfilter_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table excitationfilter 
        add constraint FKexcitationfilter_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table experiment 
        add constraint FKexperiment_update_id_event
        foreign key (update_id) 
        references event;

    alter table experiment 
        add constraint FKexperiment_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table experiment 
        add constraint FKexperiment_type_experimenttype
        foreign key (type) 
        references experimenttype;

    alter table experiment 
        add constraint FKexperiment_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table experiment 
        add constraint FKexperiment_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table experiment 
        add constraint FKexperiment_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table experimenter 
        add constraint FKexperimenter_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table experimentergroup 
        add constraint FKexperimentergroup_update_id_event
        foreign key (update_id) 
        references event;

    alter table experimentergroup 
        add constraint FKexperimentergroup_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table experimentergroup 
        add constraint FKexperimentergroup_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table experimentergroup 
        add constraint FKexperimentergroup_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table experimentergroup 
        add constraint FKexperimentergroup_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table experimenttype 
        add constraint FKexperimenttype_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table experimenttype 
        add constraint FKexperimenttype_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table experimenttype 
        add constraint FKexperimenttype_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table experimenttype 
        add constraint FKexperimenttype_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table externalinfo 
        add constraint FKexternalinfo_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table externalinfo 
        add constraint FKexternalinfo_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table externalinfo 
        add constraint FKexternalinfo_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table externalinfo 
        add constraint FKexternalinfo_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table family 
        add constraint FKfamily_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table family 
        add constraint FKfamily_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table family 
        add constraint FKfamily_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table family 
        add constraint FKfamily_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table filament 
        add constraint FKfilament_type_filamenttype
        foreign key (type) 
        references filamenttype;

    alter table filament 
        add constraint FKfilament_lightsource_id_lightsource
        foreign key (lightsource_id) 
        references lightsource;

    alter table filamenttype 
        add constraint FKfilamenttype_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table filamenttype 
        add constraint FKfilamenttype_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table filamenttype 
        add constraint FKfilamenttype_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table filamenttype 
        add constraint FKfilamenttype_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table filter 
        add constraint FKfilter_instrument_instrument
        foreign key (instrument) 
        references instrument;

    alter table filter 
        add constraint FKfilter_filterSet_filterset
        foreign key (filterSet) 
        references filterset;

    alter table filter 
        add constraint FKfilter_update_id_event
        foreign key (update_id) 
        references event;

    alter table filter 
        add constraint FKfilter_customizedFilterSet_customizedfilterset
        foreign key (customizedFilterSet) 
        references customizedfilterset;

    alter table filter 
        add constraint FKfilter_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table filter 
        add constraint FKfilter_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table filter 
        add constraint FKfilter_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table filter 
        add constraint FKfilter_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table filterset 
        add constraint FKfilterset_update_id_event
        foreign key (update_id) 
        references event;

    alter table filterset 
        add constraint FKfilterset_transmittanceRange_transmittancerange
        foreign key (transmittanceRange) 
        references transmittancerange;

    alter table filterset 
        add constraint FKfilterset_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table filterset 
        add constraint FKfilterset_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table filterset 
        add constraint FKfilterset_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table filterset 
        add constraint FKfilterset_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table filtertype 
        add constraint FKfiltertype_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table filtertype 
        add constraint FKfiltertype_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table filtertype 
        add constraint FKfiltertype_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table filtertype 
        add constraint FKfiltertype_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table format 
        add constraint FKformat_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table format 
        add constraint FKformat_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table format 
        add constraint FKformat_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table format 
        add constraint FKformat_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table frequencymultiplication 
        add constraint FKfrequencymultiplication_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table frequencymultiplication 
        add constraint FKfrequencymultiplication_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table frequencymultiplication 
        add constraint FKfrequencymultiplication_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table frequencymultiplication 
        add constraint FKfrequencymultiplication_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table groupexperimentermap 
        add constraint FKgroupexperimentermap_child_experimenter
        foreign key (child) 
        references experimenter;

    alter table groupexperimentermap 
        add constraint FKgroupexperimentermap_update_id_event
        foreign key (update_id) 
        references event;

    alter table groupexperimentermap 
        add constraint FKgroupexperimentermap_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table groupexperimentermap 
        add constraint FKgroupexperimentermap_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table groupexperimentermap 
        add constraint FKgroupexperimentermap_parent_experimentergroup
        foreign key (parent) 
        references experimentergroup;

    alter table groupexperimentermap 
        add constraint FKgroupexperimentermap_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table groupexperimentermap 
        add constraint FKgroupexperimentermap_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table illumination 
        add constraint FKillumination_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table illumination 
        add constraint FKillumination_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table illumination 
        add constraint FKillumination_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table illumination 
        add constraint FKillumination_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table image 
        add constraint FKimage_context_experiment
        foreign key (context) 
        references experiment;

    alter table image 
        add constraint FKimage_update_id_event
        foreign key (update_id) 
        references event;

    alter table image 
        add constraint FKimage_position_stagelabel
        foreign key (position) 
        references stagelabel;

    alter table image 
        add constraint FKimage_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table image 
        add constraint FKimage_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table image 
        add constraint FKimage_condition_imagingenvironment
        foreign key (condition) 
        references imagingenvironment;

    alter table image 
        add constraint FKimage_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table image 
        add constraint FKimage_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table image 
        add constraint FKimage_setup_instrument
        foreign key (setup) 
        references instrument;

    alter table image 
        add constraint FKimage_objectiveSettings_objectivesettings
        foreign key (objectiveSettings) 
        references objectivesettings;

    alter table imageannotation 
        add constraint FKimageannotation_update_id_event
        foreign key (update_id) 
        references event;

    alter table imageannotation 
        add constraint FKimageannotation_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table imageannotation 
        add constraint FKimageannotation_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table imageannotation 
        add constraint FKimageannotation_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table imageannotation 
        add constraint FKimageannotation_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table imageannotation 
        add constraint FKimageannotation_image_image
        foreign key (image) 
        references image;

    alter table imagecellcount 
        add constraint FKimagecellcount_update_id_event
        foreign key (update_id) 
        references event;

    alter table imagecellcount 
        add constraint FKimagecellcount_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table imagecellcount 
        add constraint FKimagecellcount_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table imagecellcount 
        add constraint FKimagecellcount_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table imagecellcount 
        add constraint FKimagecellcount_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table imagenucleascount 
        add constraint FKimagenucleascount_update_id_event
        foreign key (update_id) 
        references event;

    alter table imagenucleascount 
        add constraint FKimagenucleascount_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table imagenucleascount 
        add constraint FKimagenucleascount_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table imagenucleascount 
        add constraint FKimagenucleascount_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table imagenucleascount 
        add constraint FKimagenucleascount_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table imagingenvironment 
        add constraint FKimagingenvironment_update_id_event
        foreign key (update_id) 
        references event;

    alter table imagingenvironment 
        add constraint FKimagingenvironment_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table imagingenvironment 
        add constraint FKimagingenvironment_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table imagingenvironment 
        add constraint FKimagingenvironment_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table imagingenvironment 
        add constraint FKimagingenvironment_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table immersion 
        add constraint FKimmersion_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table immersion 
        add constraint FKimmersion_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table immersion 
        add constraint FKimmersion_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table immersion 
        add constraint FKimmersion_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table importjob 
        add constraint FKimportjob_job_id_job
        foreign key (job_id) 
        references job;

    alter table instrument 
        add constraint FKinstrument_microscope_microscope
        foreign key (microscope) 
        references microscope;

    alter table instrument 
        add constraint FKinstrument_update_id_event
        foreign key (update_id) 
        references event;

    alter table instrument 
        add constraint FKinstrument_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table instrument 
        add constraint FKinstrument_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table instrument 
        add constraint FKinstrument_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table instrument 
        add constraint FKinstrument_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table irisdiaphragm 
        add constraint FKirisdiaphragm_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table irisdiaphragm 
        add constraint FKirisdiaphragm_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table irisdiaphragm 
        add constraint FKirisdiaphragm_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table irisdiaphragm 
        add constraint FKirisdiaphragm_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table job 
        add constraint FKjob_update_id_event
        foreign key (update_id) 
        references event;

    alter table job 
        add constraint FKjob_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table job 
        add constraint FKjob_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table job 
        add constraint FKjob_status_jobstatus
        foreign key (status) 
        references jobstatus;

    alter table job 
        add constraint FKjob_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table job 
        add constraint FKjob_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table joboriginalfilelink 
        add constraint FKjoboriginalfilelink_child_originalfile
        foreign key (child) 
        references originalfile;

    alter table joboriginalfilelink 
        add constraint FKjoboriginalfilelink_update_id_event
        foreign key (update_id) 
        references event;

    alter table joboriginalfilelink 
        add constraint FKjoboriginalfilelink_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table joboriginalfilelink 
        add constraint FKjoboriginalfilelink_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table joboriginalfilelink 
        add constraint FKjoboriginalfilelink_parent_importjob
        foreign key (parent) 
        references importjob;

    alter table joboriginalfilelink 
        add constraint FKjoboriginalfilelink_parent_job
        foreign key (parent) 
        references job;

    alter table joboriginalfilelink 
        add constraint FKjoboriginalfilelink_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table joboriginalfilelink 
        add constraint FKjoboriginalfilelink_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table jobstatus 
        add constraint FKjobstatus_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table jobstatus 
        add constraint FKjobstatus_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table jobstatus 
        add constraint FKjobstatus_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table jobstatus 
        add constraint FKjobstatus_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table laser 
        add constraint FKlaser_laserMedium_lasermedium
        foreign key (laserMedium) 
        references lasermedium;

    alter table laser 
        add constraint FKlaser_pulse_pulse
        foreign key (pulse) 
        references pulse;

    alter table laser 
        add constraint FKlaser_frequencyMultiplication_frequencymultiplication
        foreign key (frequencyMultiplication) 
        references frequencymultiplication;

    alter table laser 
        add constraint FKlaser_type_lasertype
        foreign key (type) 
        references lasertype;

    alter table laser 
        add constraint FKlaser_lightsource_id_lightsource
        foreign key (lightsource_id) 
        references lightsource;

    alter table laser 
        add constraint FKlaser_pump_laser
        foreign key (pump) 
        references laser;

    alter table lasermedium 
        add constraint FKlasermedium_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table lasermedium 
        add constraint FKlasermedium_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table lasermedium 
        add constraint FKlasermedium_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table lasermedium 
        add constraint FKlasermedium_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table lasertype 
        add constraint FKlasertype_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table lasertype 
        add constraint FKlasertype_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table lasertype 
        add constraint FKlasertype_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table lasertype 
        add constraint FKlasertype_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table lightsettings 
        add constraint FKlightsettings_update_id_event
        foreign key (update_id) 
        references event;

    alter table lightsettings 
        add constraint FKlightsettings_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table lightsettings 
        add constraint FKlightsettings_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table lightsettings 
        add constraint FKlightsettings_lightSource_lightsource
        foreign key (lightSource) 
        references lightsource;

    alter table lightsettings 
        add constraint FKlightsettings_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table lightsettings 
        add constraint FKlightsettings_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table lightsource 
        add constraint FKlightsource_instrument_instrument
        foreign key (instrument) 
        references instrument;

    alter table lightsource 
        add constraint FKlightsource_update_id_event
        foreign key (update_id) 
        references event;

    alter table lightsource 
        add constraint FKlightsource_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table lightsource 
        add constraint FKlightsource_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table lightsource 
        add constraint FKlightsource_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table lightsource 
        add constraint FKlightsource_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table link 
        add constraint FKlink_update_id_event
        foreign key (update_id) 
        references event;

    alter table link 
        add constraint FKlink_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table link 
        add constraint FKlink_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table link 
        add constraint FKlink_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table link 
        add constraint FKlink_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table logicalchannel 
        add constraint FKlogicalchannel_contrastMethod_contrastmethod
        foreign key (contrastMethod) 
        references contrastmethod;

    alter table logicalchannel 
        add constraint FKlogicalchannel_illumination_illumination
        foreign key (illumination) 
        references illumination;

    alter table logicalchannel 
        add constraint FKlogicalchannel_otf_otf
        foreign key (otf) 
        references otf;

    alter table logicalchannel 
        add constraint FKlogicalchannel_mode_acquisitionmode
        foreign key (mode) 
        references acquisitionmode;

    alter table logicalchannel 
        add constraint FKlogicalchannel_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table logicalchannel 
        add constraint FKlogicalchannel_lightSource_lightsettings
        foreign key (lightSource) 
        references lightsettings;

    alter table logicalchannel 
        add constraint FKlogicalchannel_update_id_event
        foreign key (update_id) 
        references event;

    alter table logicalchannel 
        add constraint FKlogicalchannel_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table logicalchannel 
        add constraint FKlogicalchannel_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table logicalchannel 
        add constraint FKlogicalchannel_detectorSettings_detectorsettings
        foreign key (detectorSettings) 
        references detectorsettings;

    alter table logicalchannel 
        add constraint FKlogicalchannel_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table logicalchannel 
        add constraint FKlogicalchannel_auxLightSource_lightsettings
        foreign key (auxLightSource) 
        references lightsettings;

    alter table logicalchannel 
        add constraint FKlogicalchannel_photometricInterpretation_photometricinterpretation
        foreign key (photometricInterpretation) 
        references photometricinterpretation;

    alter table medium 
        add constraint FKmedium_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table medium 
        add constraint FKmedium_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table medium 
        add constraint FKmedium_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table medium 
        add constraint FKmedium_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table metadata 
        add constraint FKmetadata_update_id_event
        foreign key (update_id) 
        references event;

    alter table metadata 
        add constraint FKmetadata_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table metadata 
        add constraint FKmetadata_region_region
        foreign key (region) 
        references region;

    alter table metadata 
        add constraint FKmetadata_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table metadata 
        add constraint FKmetadata_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table metadata 
        add constraint FKmetadata_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table microscope 
        add constraint FKmicroscope_update_id_event
        foreign key (update_id) 
        references event;

    alter table microscope 
        add constraint FKmicroscope_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table microscope 
        add constraint FKmicroscope_type_microscopetype
        foreign key (type) 
        references microscopetype;

    alter table microscope 
        add constraint FKmicroscope_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table microscope 
        add constraint FKmicroscope_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table microscope 
        add constraint FKmicroscope_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table microscopetype 
        add constraint FKmicroscopetype_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table microscopetype 
        add constraint FKmicroscopetype_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table microscopetype 
        add constraint FKmicroscopetype_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table microscopetype 
        add constraint FKmicroscopetype_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table nucleusarea 
        add constraint FKnucleusarea_update_id_event
        foreign key (update_id) 
        references event;

    alter table nucleusarea 
        add constraint FKnucleusarea_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table nucleusarea 
        add constraint FKnucleusarea_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table nucleusarea 
        add constraint FKnucleusarea_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table nucleusarea 
        add constraint FKnucleusarea_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table nucleuseccentricity 
        add constraint FKnucleuseccentricity_update_id_event
        foreign key (update_id) 
        references event;

    alter table nucleuseccentricity 
        add constraint FKnucleuseccentricity_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table nucleuseccentricity 
        add constraint FKnucleuseccentricity_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table nucleuseccentricity 
        add constraint FKnucleuseccentricity_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table nucleuseccentricity 
        add constraint FKnucleuseccentricity_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table nucleusextent 
        add constraint FKnucleusextent_update_id_event
        foreign key (update_id) 
        references event;

    alter table nucleusextent 
        add constraint FKnucleusextent_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table nucleusextent 
        add constraint FKnucleusextent_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table nucleusextent 
        add constraint FKnucleusextent_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table nucleusextent 
        add constraint FKnucleusextent_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table nucleusmajoraxislength 
        add constraint FKnucleusmajoraxislength_update_id_event
        foreign key (update_id) 
        references event;

    alter table nucleusmajoraxislength 
        add constraint FKnucleusmajoraxislength_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table nucleusmajoraxislength 
        add constraint FKnucleusmajoraxislength_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table nucleusmajoraxislength 
        add constraint FKnucleusmajoraxislength_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table nucleusmajoraxislength 
        add constraint FKnucleusmajoraxislength_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table nucleusminoraxislength 
        add constraint FKnucleusminoraxislength_update_id_event
        foreign key (update_id) 
        references event;

    alter table nucleusminoraxislength 
        add constraint FKnucleusminoraxislength_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table nucleusminoraxislength 
        add constraint FKnucleusminoraxislength_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table nucleusminoraxislength 
        add constraint FKnucleusminoraxislength_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table nucleusminoraxislength 
        add constraint FKnucleusminoraxislength_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table nucleusperimeter 
        add constraint FKnucleusperimeter_update_id_event
        foreign key (update_id) 
        references event;

    alter table nucleusperimeter 
        add constraint FKnucleusperimeter_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table nucleusperimeter 
        add constraint FKnucleusperimeter_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table nucleusperimeter 
        add constraint FKnucleusperimeter_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table nucleusperimeter 
        add constraint FKnucleusperimeter_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table nucleusposition 
        add constraint FKnucleusposition_update_id_event
        foreign key (update_id) 
        references event;

    alter table nucleusposition 
        add constraint FKnucleusposition_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table nucleusposition 
        add constraint FKnucleusposition_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table nucleusposition 
        add constraint FKnucleusposition_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table nucleusposition 
        add constraint FKnucleusposition_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table nucleussolidity 
        add constraint FKnucleussolidity_update_id_event
        foreign key (update_id) 
        references event;

    alter table nucleussolidity 
        add constraint FKnucleussolidity_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table nucleussolidity 
        add constraint FKnucleussolidity_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table nucleussolidity 
        add constraint FKnucleussolidity_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table nucleussolidity 
        add constraint FKnucleussolidity_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table objective 
        add constraint FKobjective_instrument_instrument
        foreign key (instrument) 
        references instrument;

    alter table objective 
        add constraint FKobjective_update_id_event
        foreign key (update_id) 
        references event;

    alter table objective 
        add constraint FKobjective_immersion_immersion
        foreign key (immersion) 
        references immersion;

    alter table objective 
        add constraint FKobjective_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table objective 
        add constraint FKobjective_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table objective 
        add constraint FKobjective_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table objective 
        add constraint FKobjective_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table objective 
        add constraint FKobjective_coating_coating
        foreign key (coating) 
        references coating;

    alter table objectivesettings 
        add constraint FKobjectivesettings_update_id_event
        foreign key (update_id) 
        references event;

    alter table objectivesettings 
        add constraint FKobjectivesettings_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table objectivesettings 
        add constraint FKobjectivesettings_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table objectivesettings 
        add constraint FKobjectivesettings_medium_medium
        foreign key (medium) 
        references medium;

    alter table objectivesettings 
        add constraint FKobjectivesettings_objective_objective
        foreign key (objective) 
        references objective;

    alter table objectivesettings 
        add constraint FKobjectivesettings_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table objectivesettings 
        add constraint FKobjectivesettings_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table originalfile 
        add constraint FKoriginalfile_format_format
        foreign key (format) 
        references format;

    alter table originalfile 
        add constraint FKoriginalfile_update_id_event
        foreign key (update_id) 
        references event;

    alter table originalfile 
        add constraint FKoriginalfile_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table originalfile 
        add constraint FKoriginalfile_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table originalfile 
        add constraint FKoriginalfile_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table originalfile 
        add constraint FKoriginalfile_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table otf 
        add constraint FKotf_pixelType_pixelstype
        foreign key (pixelType) 
        references pixelstype;

    alter table otf 
        add constraint FKotf_update_id_event
        foreign key (update_id) 
        references event;

    alter table otf 
        add constraint FKotf_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table otf 
        add constraint FKotf_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table otf 
        add constraint FKotf_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table otf 
        add constraint FKotf_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table overlay 
        add constraint FKoverlay_type_overlaytype
        foreign key (type) 
        references overlaytype;

    alter table overlay 
        add constraint FKoverlay_specification_id_specification
        foreign key (specification_id) 
        references specification;

    alter table overlay 
        add constraint FKoverlay_plane_xy
        foreign key (plane) 
        references xy;

    alter table overlaytype 
        add constraint FKoverlaytype_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table overlaytype 
        add constraint FKoverlaytype_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table overlaytype 
        add constraint FKoverlaytype_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table overlaytype 
        add constraint FKoverlaytype_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table photometricinterpretation 
        add constraint FKphotometricinterpretation_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table photometricinterpretation 
        add constraint FKphotometricinterpretation_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table photometricinterpretation 
        add constraint FKphotometricinterpretation_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table photometricinterpretation 
        add constraint FKphotometricinterpretation_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table pixels 
        add constraint FKpixels_pixelsType_pixelstype
        foreign key (pixelsType) 
        references pixelstype;

    alter table pixels 
        add constraint FKpixels_relatedTo_pixels
        foreign key (relatedTo) 
        references pixels;

    alter table pixels 
        add constraint FKpixels_update_id_event
        foreign key (update_id) 
        references event;

    alter table pixels 
        add constraint FKpixels_dimensionOrder_dimensionorder
        foreign key (dimensionOrder) 
        references dimensionorder;

    alter table pixels 
        add constraint FKpixels_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table pixels 
        add constraint FKpixels_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table pixels 
        add constraint FKpixels_pixelsDimensions_pixelsdimensions
        foreign key (pixelsDimensions) 
        references pixelsdimensions;

    alter table pixels 
        add constraint FKpixels_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table pixels 
        add constraint FKpixels_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table pixels 
        add constraint FKpixels_image_image
        foreign key (image) 
        references image;

    alter table pixelsdimensions 
        add constraint FKpixelsdimensions_update_id_event
        foreign key (update_id) 
        references event;

    alter table pixelsdimensions 
        add constraint FKpixelsdimensions_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table pixelsdimensions 
        add constraint FKpixelsdimensions_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table pixelsdimensions 
        add constraint FKpixelsdimensions_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table pixelsdimensions 
        add constraint FKpixelsdimensions_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table pixelsoriginalfilemap 
        add constraint FKpixelsoriginalfilemap_child_pixels
        foreign key (child) 
        references pixels;

    alter table pixelsoriginalfilemap 
        add constraint FKpixelsoriginalfilemap_update_id_event
        foreign key (update_id) 
        references event;

    alter table pixelsoriginalfilemap 
        add constraint FKpixelsoriginalfilemap_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table pixelsoriginalfilemap 
        add constraint FKpixelsoriginalfilemap_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table pixelsoriginalfilemap 
        add constraint FKpixelsoriginalfilemap_parent_originalfile
        foreign key (parent) 
        references originalfile;

    alter table pixelsoriginalfilemap 
        add constraint FKpixelsoriginalfilemap_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table pixelsoriginalfilemap 
        add constraint FKpixelsoriginalfilemap_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table pixelstype 
        add constraint FKpixelstype_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table pixelstype 
        add constraint FKpixelstype_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table pixelstype 
        add constraint FKpixelstype_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table pixelstype 
        add constraint FKpixelstype_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table planeinfo 
        add constraint FKplaneinfo_pixels_pixels
        foreign key (pixels) 
        references pixels;

    alter table planeinfo 
        add constraint FKplaneinfo_update_id_event
        foreign key (update_id) 
        references event;

    alter table planeinfo 
        add constraint FKplaneinfo_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table planeinfo 
        add constraint FKplaneinfo_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table planeinfo 
        add constraint FKplaneinfo_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table planeinfo 
        add constraint FKplaneinfo_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table planeslicingcontext 
        add constraint FKplaneslicingcontext_codomainmapcontext_id_codomainmapcontext
        foreign key (codomainmapcontext_id) 
        references codomainmapcontext;

    alter table project 
        add constraint FKproject_update_id_event
        foreign key (update_id) 
        references event;

    alter table project 
        add constraint FKproject_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table project 
        add constraint FKproject_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table project 
        add constraint FKproject_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table project 
        add constraint FKproject_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table projectannotation 
        add constraint FKprojectannotation_update_id_event
        foreign key (update_id) 
        references event;

    alter table projectannotation 
        add constraint FKprojectannotation_project_project
        foreign key (project) 
        references project;

    alter table projectannotation 
        add constraint FKprojectannotation_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table projectannotation 
        add constraint FKprojectannotation_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table projectannotation 
        add constraint FKprojectannotation_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table projectannotation 
        add constraint FKprojectannotation_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table projectdatasetlink 
        add constraint FKprojectdatasetlink_child_dataset
        foreign key (child) 
        references dataset;

    alter table projectdatasetlink 
        add constraint FKprojectdatasetlink_update_id_event
        foreign key (update_id) 
        references event;

    alter table projectdatasetlink 
        add constraint FKprojectdatasetlink_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table projectdatasetlink 
        add constraint FKprojectdatasetlink_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table projectdatasetlink 
        add constraint FKprojectdatasetlink_parent_project
        foreign key (parent) 
        references project;

    alter table projectdatasetlink 
        add constraint FKprojectdatasetlink_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table projectdatasetlink 
        add constraint FKprojectdatasetlink_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table pulse 
        add constraint FKpulse_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table pulse 
        add constraint FKpulse_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table pulse 
        add constraint FKpulse_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table pulse 
        add constraint FKpulse_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table quantumdef 
        add constraint FKquantumdef_update_id_event
        foreign key (update_id) 
        references event;

    alter table quantumdef 
        add constraint FKquantumdef_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table quantumdef 
        add constraint FKquantumdef_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table quantumdef 
        add constraint FKquantumdef_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table quantumdef 
        add constraint FKquantumdef_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table region 
        add constraint FKregion_pixels_pixels
        foreign key (pixels) 
        references pixels;

    alter table region 
        add constraint FKregion_update_id_event
        foreign key (update_id) 
        references event;

    alter table region 
        add constraint FKregion_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table region 
        add constraint FKregion_type_regiontype
        foreign key (type) 
        references regiontype;

    alter table region 
        add constraint FKregion_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table region 
        add constraint FKregion_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table region 
        add constraint FKregion_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table regiontype 
        add constraint FKregiontype_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table regiontype 
        add constraint FKregiontype_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table regiontype 
        add constraint FKregiontype_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table regiontype 
        add constraint FKregiontype_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table renderingdef 
        add constraint FKrenderingdef_pixels_pixels
        foreign key (pixels) 
        references pixels;

    alter table renderingdef 
        add constraint FKrenderingdef_quantization_quantumdef
        foreign key (quantization) 
        references quantumdef;

    alter table renderingdef 
        add constraint FKrenderingdef_update_id_event
        foreign key (update_id) 
        references event;

    alter table renderingdef 
        add constraint FKrenderingdef_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table renderingdef 
        add constraint FKrenderingdef_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table renderingdef 
        add constraint FKrenderingdef_model_renderingmodel
        foreign key (model) 
        references renderingmodel;

    alter table renderingdef 
        add constraint FKrenderingdef_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table renderingdef 
        add constraint FKrenderingdef_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table renderingmodel 
        add constraint FKrenderingmodel_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table renderingmodel 
        add constraint FKrenderingmodel_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table renderingmodel 
        add constraint FKrenderingmodel_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table renderingmodel 
        add constraint FKrenderingmodel_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table reverseintensitycontext 
        add constraint FKreverseintensitycontext_codomainmapcontext_id_codomainmapcontext
        foreign key (codomainmapcontext_id) 
        references codomainmapcontext;

    alter table roi5d 
        add constraint FKroi5d_pixels_pixels
        foreign key (pixels) 
        references pixels;

    alter table roi5d 
        add constraint FKroi5d_update_id_event
        foreign key (update_id) 
        references event;

    alter table roi5d 
        add constraint FKroi5d_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table roi5d 
        add constraint FKroi5d_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table roi5d 
        add constraint FKroi5d_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table roi5d 
        add constraint FKroi5d_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table roiextent 
        add constraint FKroiextent_roi5d_roi5d
        foreign key (roi5d) 
        references roi5d;

    alter table roiextent 
        add constraint FKroiextent_update_id_event
        foreign key (update_id) 
        references event;

    alter table roiextent 
        add constraint FKroiextent_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table roiextent 
        add constraint FKroiextent_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table roiextent 
        add constraint FKroiextent_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table roiextent 
        add constraint FKroiextent_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table roimap 
        add constraint FKroimap_child_roi5d
        foreign key (child) 
        references roi5d;

    alter table roimap 
        add constraint FKroimap_update_id_event
        foreign key (update_id) 
        references event;

    alter table roimap 
        add constraint FKroimap_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table roimap 
        add constraint FKroimap_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table roimap 
        add constraint FKroimap_parent_roiset
        foreign key (parent) 
        references roiset;

    alter table roimap 
        add constraint FKroimap_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table roimap 
        add constraint FKroimap_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table roiset 
        add constraint FKroiset_update_id_event
        foreign key (update_id) 
        references event;

    alter table roiset 
        add constraint FKroiset_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table roiset 
        add constraint FKroiset_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table roiset 
        add constraint FKroiset_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table roiset 
        add constraint FKroiset_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table shapearea 
        add constraint FKshapearea_roiextent_roiextent
        foreign key (roiextent) 
        references roiextent;

    alter table shapearea 
        add constraint FKshapearea_update_id_event
        foreign key (update_id) 
        references event;

    alter table shapearea 
        add constraint FKshapearea_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table shapearea 
        add constraint FKshapearea_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table shapearea 
        add constraint FKshapearea_extent_roiextent
        foreign key (extent) 
        references roiextent;

    alter table shapearea 
        add constraint FKshapearea_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table shapearea 
        add constraint FKshapearea_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table specification 
        add constraint FKspecification_update_id_event
        foreign key (update_id) 
        references event;

    alter table specification 
        add constraint FKspecification_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table specification 
        add constraint FKspecification_region_region
        foreign key (region) 
        references region;

    alter table specification 
        add constraint FKspecification_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table specification 
        add constraint FKspecification_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table specification 
        add constraint FKspecification_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table square 
        add constraint FKsquare_shapearea_id_shapearea
        foreign key (shapearea_id) 
        references shapearea;

    alter table stagelabel 
        add constraint FKstagelabel_update_id_event
        foreign key (update_id) 
        references event;

    alter table stagelabel 
        add constraint FKstagelabel_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table stagelabel 
        add constraint FKstagelabel_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table stagelabel 
        add constraint FKstagelabel_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table stagelabel 
        add constraint FKstagelabel_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table statsinfo 
        add constraint FKstatsinfo_update_id_event
        foreign key (update_id) 
        references event;

    alter table statsinfo 
        add constraint FKstatsinfo_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table statsinfo 
        add constraint FKstatsinfo_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table statsinfo 
        add constraint FKstatsinfo_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table statsinfo 
        add constraint FKstatsinfo_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table thumbnail 
        add constraint FKthumbnail_pixels_pixels
        foreign key (pixels) 
        references pixels;

    alter table thumbnail 
        add constraint FKthumbnail_update_id_event
        foreign key (update_id) 
        references event;

    alter table thumbnail 
        add constraint FKthumbnail_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table thumbnail 
        add constraint FKthumbnail_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table thumbnail 
        add constraint FKthumbnail_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table thumbnail 
        add constraint FKthumbnail_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table transmittancerange 
        add constraint FKtransmittancerange_update_id_event
        foreign key (update_id) 
        references event;

    alter table transmittancerange 
        add constraint FKtransmittancerange_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table transmittancerange 
        add constraint FKtransmittancerange_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table transmittancerange 
        add constraint FKtransmittancerange_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table transmittancerange 
        add constraint FKtransmittancerange_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table uroi 
        add constraint FKuroi_box_boundingbox
        foreign key (box) 
        references boundingbox;

    alter table uroi 
        add constraint FKuroi_specification_id_specification
        foreign key (specification_id) 
        references specification;

    alter table ushape 
        add constraint FKushape_update_id_event
        foreign key (update_id) 
        references event;

    alter table ushape 
        add constraint FKushape_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table ushape 
        add constraint FKushape_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table ushape 
        add constraint FKushape_uslice_uslice
        foreign key (uslice) 
        references uslice;

    alter table ushape 
        add constraint FKushape_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table ushape 
        add constraint FKushape_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table uslice 
        add constraint FKuslice_update_id_event
        foreign key (update_id) 
        references event;

    alter table uslice 
        add constraint FKuslice_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table uslice 
        add constraint FKuslice_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table uslice 
        add constraint FKuslice_uroi_uroi
        foreign key (uroi) 
        references uroi;

    alter table uslice 
        add constraint FKuslice_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table uslice 
        add constraint FKuslice_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table usquare 
        add constraint FKusquare_ushape_id_ushape
        foreign key (ushape_id) 
        references ushape;

    alter table xy 
        add constraint FKxy_boundingbox_id_boundingbox
        foreign key (boundingbox_id) 
        references boundingbox;

    alter table xyc 
        add constraint FKxyc_boundingbox_id_boundingbox
        foreign key (boundingbox_id) 
        references boundingbox;

    alter table xyct 
        add constraint FKxyct_boundingbox_id_boundingbox
        foreign key (boundingbox_id) 
        references boundingbox;

    alter table xyctoxylink 
        add constraint FKxyctoxylink_child_xy
        foreign key (child) 
        references xy;

    alter table xyctoxylink 
        add constraint FKxyctoxylink_update_id_event
        foreign key (update_id) 
        references event;

    alter table xyctoxylink 
        add constraint FKxyctoxylink_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table xyctoxylink 
        add constraint FKxyctoxylink_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table xyctoxylink 
        add constraint FKxyctoxylink_parent_xyc
        foreign key (parent) 
        references xyc;

    alter table xyctoxylink 
        add constraint FKxyctoxylink_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table xyctoxylink 
        add constraint FKxyctoxylink_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table xycttoxyclink 
        add constraint FKxycttoxyclink_child_xyc
        foreign key (child) 
        references xyc;

    alter table xycttoxyclink 
        add constraint FKxycttoxyclink_update_id_event
        foreign key (update_id) 
        references event;

    alter table xycttoxyclink 
        add constraint FKxycttoxyclink_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table xycttoxyclink 
        add constraint FKxycttoxyclink_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table xycttoxyclink 
        add constraint FKxycttoxyclink_parent_xyct
        foreign key (parent) 
        references xyct;

    alter table xycttoxyclink 
        add constraint FKxycttoxyclink_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table xycttoxyclink 
        add constraint FKxycttoxyclink_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table xycttoxytlink 
        add constraint FKxycttoxytlink_child_xyt
        foreign key (child) 
        references xyt;

    alter table xycttoxytlink 
        add constraint FKxycttoxytlink_update_id_event
        foreign key (update_id) 
        references event;

    alter table xycttoxytlink 
        add constraint FKxycttoxytlink_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table xycttoxytlink 
        add constraint FKxycttoxytlink_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table xycttoxytlink 
        add constraint FKxycttoxytlink_parent_xyct
        foreign key (parent) 
        references xyct;

    alter table xycttoxytlink 
        add constraint FKxycttoxytlink_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table xycttoxytlink 
        add constraint FKxycttoxytlink_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table xyt 
        add constraint FKxyt_boundingbox_id_boundingbox
        foreign key (boundingbox_id) 
        references boundingbox;

    alter table xyttoxylink 
        add constraint FKxyttoxylink_child_xy
        foreign key (child) 
        references xy;

    alter table xyttoxylink 
        add constraint FKxyttoxylink_update_id_event
        foreign key (update_id) 
        references event;

    alter table xyttoxylink 
        add constraint FKxyttoxylink_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table xyttoxylink 
        add constraint FKxyttoxylink_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table xyttoxylink 
        add constraint FKxyttoxylink_parent_xyt
        foreign key (parent) 
        references xyt;

    alter table xyttoxylink 
        add constraint FKxyttoxylink_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table xyttoxylink 
        add constraint FKxyttoxylink_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table xyz 
        add constraint FKxyz_boundingbox_id_boundingbox
        foreign key (boundingbox_id) 
        references boundingbox;

    alter table xyzc 
        add constraint FKxyzc_boundingbox_id_boundingbox
        foreign key (boundingbox_id) 
        references boundingbox;

    alter table xyzct 
        add constraint FKxyzct_boundingbox_id_boundingbox
        foreign key (boundingbox_id) 
        references boundingbox;

    alter table xyzctoxyclink 
        add constraint FKxyzctoxyclink_child_xyc
        foreign key (child) 
        references xyc;

    alter table xyzctoxyclink 
        add constraint FKxyzctoxyclink_update_id_event
        foreign key (update_id) 
        references event;

    alter table xyzctoxyclink 
        add constraint FKxyzctoxyclink_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table xyzctoxyclink 
        add constraint FKxyzctoxyclink_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table xyzctoxyclink 
        add constraint FKxyzctoxyclink_parent_xyzc
        foreign key (parent) 
        references xyzc;

    alter table xyzctoxyclink 
        add constraint FKxyzctoxyclink_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table xyzctoxyclink 
        add constraint FKxyzctoxyclink_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table xyzctoxyzlink 
        add constraint FKxyzctoxyzlink_child_xyz
        foreign key (child) 
        references xyz;

    alter table xyzctoxyzlink 
        add constraint FKxyzctoxyzlink_update_id_event
        foreign key (update_id) 
        references event;

    alter table xyzctoxyzlink 
        add constraint FKxyzctoxyzlink_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table xyzctoxyzlink 
        add constraint FKxyzctoxyzlink_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table xyzctoxyzlink 
        add constraint FKxyzctoxyzlink_parent_xyzc
        foreign key (parent) 
        references xyzc;

    alter table xyzctoxyzlink 
        add constraint FKxyzctoxyzlink_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table xyzctoxyzlink 
        add constraint FKxyzctoxyzlink_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table xyzcttoxyctlink 
        add constraint FKxyzcttoxyctlink_child_xyct
        foreign key (child) 
        references xyct;

    alter table xyzcttoxyctlink 
        add constraint FKxyzcttoxyctlink_update_id_event
        foreign key (update_id) 
        references event;

    alter table xyzcttoxyctlink 
        add constraint FKxyzcttoxyctlink_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table xyzcttoxyctlink 
        add constraint FKxyzcttoxyctlink_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table xyzcttoxyctlink 
        add constraint FKxyzcttoxyctlink_parent_xyzct
        foreign key (parent) 
        references xyzct;

    alter table xyzcttoxyctlink 
        add constraint FKxyzcttoxyctlink_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table xyzcttoxyctlink 
        add constraint FKxyzcttoxyctlink_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table xyzcttoxyzclink 
        add constraint FKxyzcttoxyzclink_child_xyzc
        foreign key (child) 
        references xyzc;

    alter table xyzcttoxyzclink 
        add constraint FKxyzcttoxyzclink_update_id_event
        foreign key (update_id) 
        references event;

    alter table xyzcttoxyzclink 
        add constraint FKxyzcttoxyzclink_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table xyzcttoxyzclink 
        add constraint FKxyzcttoxyzclink_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table xyzcttoxyzclink 
        add constraint FKxyzcttoxyzclink_parent_xyzct
        foreign key (parent) 
        references xyzct;

    alter table xyzcttoxyzclink 
        add constraint FKxyzcttoxyzclink_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table xyzcttoxyzclink 
        add constraint FKxyzcttoxyzclink_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table xyzcttoxyztlink 
        add constraint FKxyzcttoxyztlink_child_xyzt
        foreign key (child) 
        references xyzt;

    alter table xyzcttoxyztlink 
        add constraint FKxyzcttoxyztlink_update_id_event
        foreign key (update_id) 
        references event;

    alter table xyzcttoxyztlink 
        add constraint FKxyzcttoxyztlink_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table xyzcttoxyztlink 
        add constraint FKxyzcttoxyztlink_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table xyzcttoxyztlink 
        add constraint FKxyzcttoxyztlink_parent_xyzct
        foreign key (parent) 
        references xyzct;

    alter table xyzcttoxyztlink 
        add constraint FKxyzcttoxyztlink_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table xyzcttoxyztlink 
        add constraint FKxyzcttoxyztlink_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table xyzt 
        add constraint FKxyzt_boundingbox_id_boundingbox
        foreign key (boundingbox_id) 
        references boundingbox;

    alter table xyztoxylink 
        add constraint FKxyztoxylink_child_xy
        foreign key (child) 
        references xy;

    alter table xyztoxylink 
        add constraint FKxyztoxylink_update_id_event
        foreign key (update_id) 
        references event;

    alter table xyztoxylink 
        add constraint FKxyztoxylink_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table xyztoxylink 
        add constraint FKxyztoxylink_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table xyztoxylink 
        add constraint FKxyztoxylink_parent_xyz
        foreign key (parent) 
        references xyz;

    alter table xyztoxylink 
        add constraint FKxyztoxylink_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table xyztoxylink 
        add constraint FKxyztoxylink_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table xyzttoxytlink 
        add constraint FKxyzttoxytlink_child_xyt
        foreign key (child) 
        references xyt;

    alter table xyzttoxytlink 
        add constraint FKxyzttoxytlink_update_id_event
        foreign key (update_id) 
        references event;

    alter table xyzttoxytlink 
        add constraint FKxyzttoxytlink_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table xyzttoxytlink 
        add constraint FKxyzttoxytlink_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table xyzttoxytlink 
        add constraint FKxyzttoxytlink_parent_xyzt
        foreign key (parent) 
        references xyzt;

    alter table xyzttoxytlink 
        add constraint FKxyzttoxytlink_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table xyzttoxytlink 
        add constraint FKxyzttoxytlink_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    alter table xyzttoxyzlink 
        add constraint FKxyzttoxyzlink_child_xyz
        foreign key (child) 
        references xyz;

    alter table xyzttoxyzlink 
        add constraint FKxyzttoxyzlink_update_id_event
        foreign key (update_id) 
        references event;

    alter table xyzttoxyzlink 
        add constraint FKxyzttoxyzlink_owner_id_experimenter
        foreign key (owner_id) 
        references experimenter;

    alter table xyzttoxyzlink 
        add constraint FKxyzttoxyzlink_creation_id_event
        foreign key (creation_id) 
        references event;

    alter table xyzttoxyzlink 
        add constraint FKxyzttoxyzlink_parent_xyzt
        foreign key (parent) 
        references xyzt;

    alter table xyzttoxyzlink 
        add constraint FKxyzttoxyzlink_external_id_externalinfo
        foreign key (external_id) 
        references externalinfo;

    alter table xyzttoxyzlink 
        add constraint FKxyzttoxyzlink_group_id_experimentergroup
        foreign key (group_id) 
        references experimentergroup;

    create sequence seq_aberrationcorrection;

    create sequence seq_acquisitionmode;

    create sequence seq_arctype;

    create sequence seq_binning;

    create sequence seq_boundingbox;

    create sequence seq_category;

    create sequence seq_categorygroup;

    create sequence seq_categorygroupcategorylink;

    create sequence seq_categoryimagelink;

    create sequence seq_cellarea;

    create sequence seq_celleccentricity;

    create sequence seq_cellextent;

    create sequence seq_cellmajoraxislength;

    create sequence seq_cellminoraxislength;

    create sequence seq_cellperimeter;

    create sequence seq_cellposition;

    create sequence seq_cellsolidity;

    create sequence seq_channel;

    create sequence seq_channelbinding;

    create sequence seq_coating;

    create sequence seq_codomainmapcontext;

    create sequence seq_color;

    create sequence seq_contrastmethod;

    create sequence seq_customizedfilterset;

    create sequence seq_dataset;

    create sequence seq_datasetannotation;

    create sequence seq_datasetimagelink;

    create sequence seq_dbpatch;

    create sequence seq_detector;

    create sequence seq_detectorsettings;

    create sequence seq_detectortype;

    create sequence seq_dichroic;

    create sequence seq_dimensionorder;

    create sequence seq_emissionfilter;

    create sequence seq_event;

    create sequence seq_eventlog;

    create sequence seq_eventtype;

    create sequence seq_excitationfilter;

    create sequence seq_experiment;

    create sequence seq_experimenter;

    create sequence seq_experimentergroup;

    create sequence seq_experimenttype;

    create sequence seq_externalinfo;

    create sequence seq_family;

    create sequence seq_filamenttype;

    create sequence seq_filter;

    create sequence seq_filterset;

    create sequence seq_filtertype;

    create sequence seq_format;

    create sequence seq_frequencymultiplication;

    create sequence seq_groupexperimentermap;

    create sequence seq_illumination;

    create sequence seq_image;

    create sequence seq_imageannotation;

    create sequence seq_imagecellcount;

    create sequence seq_imagenucleascount;

    create sequence seq_imagingenvironment;

    create sequence seq_immersion;

    create sequence seq_instrument;

    create sequence seq_irisdiaphragm;

    create sequence seq_job;

    create sequence seq_joboriginalfilelink;

    create sequence seq_jobstatus;

    create sequence seq_lasermedium;

    create sequence seq_lasertype;

    create sequence seq_lightsettings;

    create sequence seq_lightsource;

    create sequence seq_link;

    create sequence seq_logicalchannel;

    create sequence seq_medium;

    create sequence seq_metadata;

    create sequence seq_microscope;

    create sequence seq_microscopetype;

    create sequence seq_nucleusarea;

    create sequence seq_nucleuseccentricity;

    create sequence seq_nucleusextent;

    create sequence seq_nucleusmajoraxislength;

    create sequence seq_nucleusminoraxislength;

    create sequence seq_nucleusperimeter;

    create sequence seq_nucleusposition;

    create sequence seq_nucleussolidity;

    create sequence seq_objective;

    create sequence seq_objectivesettings;

    create sequence seq_originalfile;

    create sequence seq_otf;

    create sequence seq_overlaytype;

    create sequence seq_photometricinterpretation;

    create sequence seq_pixels;

    create sequence seq_pixelsdimensions;

    create sequence seq_pixelsoriginalfilemap;

    create sequence seq_pixelstype;

    create sequence seq_planeinfo;

    create sequence seq_project;

    create sequence seq_projectannotation;

    create sequence seq_projectdatasetlink;

    create sequence seq_pulse;

    create sequence seq_quantumdef;

    create sequence seq_region;

    create sequence seq_regiontype;

    create sequence seq_renderingdef;

    create sequence seq_renderingmodel;

    create sequence seq_roi5d;

    create sequence seq_roiextent;

    create sequence seq_roimap;

    create sequence seq_roiset;

    create sequence seq_shapearea;

    create sequence seq_specification;

    create sequence seq_stagelabel;

    create sequence seq_statsinfo;

    create sequence seq_thumbnail;

    create sequence seq_transmittancerange;

    create sequence seq_ushape;

    create sequence seq_uslice;

    create sequence seq_xyctoxylink;

    create sequence seq_xycttoxyclink;

    create sequence seq_xycttoxytlink;

    create sequence seq_xyttoxylink;

    create sequence seq_xyzctoxyclink;

    create sequence seq_xyzctoxyzlink;

    create sequence seq_xyzcttoxyctlink;

    create sequence seq_xyzcttoxyzclink;

    create sequence seq_xyzcttoxyztlink;

    create sequence seq_xyztoxylink;

    create sequence seq_xyzttoxytlink;

    create sequence seq_xyzttoxyzlink;

COMMIT;
