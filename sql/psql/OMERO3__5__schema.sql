
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
        inputStart float4 not null,
        inputEnd float4 not null,
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
        voltage float4,
        gain float4,
        offsetValue float4,
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
        voltage float4,
        gain float4,
        offsetValue float4,
        readOutRate float4,
        binning int8,
        amplification float4,
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
        example float4 not null,
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
        temperature float4,
        airPressure float4,
        humidity float4,
        co2percent float4,
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
        power float4,
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
        power float4 not null,
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
        ndFilter float4,
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
        lensNA float4 not null,
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
        correctionCollar float4,
        medium int8,
        refractiveIndex float4,
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
        sizeX float4 not null,
        sizeY float4 not null,
        sizeZ float4 not null,
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
        timestamp float4 not null,
        positionX float4,
        positionY float4,
        positionZ float4,
        exposureTime float4,
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

    create table scriptjob (
        job_id int8 not null,
        description varchar(255),
        primary key (job_id)
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
        positionX float4 not null,
        positionY float4 not null,
        positionZ float4 not null,
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
        transmittance float4 not null,
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
        add constraint FK394171EF3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table aberrationcorrection 
        add constraint FK394171EF3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table aberrationcorrection 
        add constraint FK394171EF54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table aberrationcorrection 
        add constraint FK394171EF97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table acquisitionmode 
        add constraint FK57ED3AB03680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table acquisitionmode 
        add constraint FK57ED3AB03EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table acquisitionmode 
        add constraint FK57ED3AB054212865 
        foreign key (external_id) 
        references externalinfo;

    alter table acquisitionmode 
        add constraint FK57ED3AB097E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table arc 
        add constraint FK17A52C9D2D93E 
        foreign key (type) 
        references arctype;

    alter table arc 
        add constraint FK17A521044E20D 
        foreign key (lightsource_id) 
        references lightsource;

    alter table arctype 
        add constraint FKD36E908C3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table arctype 
        add constraint FKD36E908C3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table arctype 
        add constraint FKD36E908C54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table arctype 
        add constraint FKD36E908C97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table binning 
        add constraint FKF9913A3B3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table binning 
        add constraint FKF9913A3B3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table binning 
        add constraint FKF9913A3B54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table binning 
        add constraint FKF9913A3B97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table boundingbox 
        add constraint FKB4C6E5A7E046324E 
        foreign key (update_id) 
        references event;

    alter table boundingbox 
        add constraint FKB4C6E5A73680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table boundingbox 
        add constraint FKB4C6E5A7803AA373 
        foreign key (region) 
        references region;

    alter table boundingbox 
        add constraint FKB4C6E5A73EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table boundingbox 
        add constraint FKB4C6E5A754212865 
        foreign key (external_id) 
        references externalinfo;

    alter table boundingbox 
        add constraint FKB4C6E5A797E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table category 
        add constraint FK302BCFEE046324E 
        foreign key (update_id) 
        references event;

    alter table category 
        add constraint FK302BCFE3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table category 
        add constraint FK302BCFE3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table category 
        add constraint FK302BCFE54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table category 
        add constraint FK302BCFE97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table categorygroup 
        add constraint FK14A2A841E046324E 
        foreign key (update_id) 
        references event;

    alter table categorygroup 
        add constraint FK14A2A8413680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table categorygroup 
        add constraint FK14A2A8413EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table categorygroup 
        add constraint FK14A2A84154212865 
        foreign key (external_id) 
        references externalinfo;

    alter table categorygroup 
        add constraint FK14A2A84197E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table categorygroupcategorylink 
        add constraint FKD69E0CB984044D6A 
        foreign key (child) 
        references category;

    alter table categorygroupcategorylink 
        add constraint FKD69E0CB9E046324E 
        foreign key (update_id) 
        references event;

    alter table categorygroupcategorylink 
        add constraint FKD69E0CB93680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table categorygroupcategorylink 
        add constraint FKD69E0CB93EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table categorygroupcategorylink 
        add constraint FKD69E0CB9F5F9B77B 
        foreign key (parent) 
        references categorygroup;

    alter table categorygroupcategorylink 
        add constraint FKD69E0CB954212865 
        foreign key (external_id) 
        references externalinfo;

    alter table categorygroupcategorylink 
        add constraint FKD69E0CB997E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table categoryimagelink 
        add constraint FK5AE234979DF143F4 
        foreign key (child) 
        references image;

    alter table categoryimagelink 
        add constraint FK5AE23497E046324E 
        foreign key (update_id) 
        references event;

    alter table categoryimagelink 
        add constraint FK5AE234973680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table categoryimagelink 
        add constraint FK5AE234973EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table categoryimagelink 
        add constraint FK5AE23497430B60F8 
        foreign key (parent) 
        references category;

    alter table categoryimagelink 
        add constraint FK5AE2349754212865 
        foreign key (external_id) 
        references externalinfo;

    alter table categoryimagelink 
        add constraint FK5AE2349797E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table cellarea 
        add constraint FKC954D8EFE046324E 
        foreign key (update_id) 
        references event;

    alter table cellarea 
        add constraint FKC954D8EF3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table cellarea 
        add constraint FKC954D8EF3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table cellarea 
        add constraint FKC954D8EF54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table cellarea 
        add constraint FKC954D8EF97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table celleccentricity 
        add constraint FK9052454AE046324E 
        foreign key (update_id) 
        references event;

    alter table celleccentricity 
        add constraint FK9052454A3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table celleccentricity 
        add constraint FK9052454A3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table celleccentricity 
        add constraint FK9052454A54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table celleccentricity 
        add constraint FK9052454A97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table cellextent 
        add constraint FKCEB1370CE046324E 
        foreign key (update_id) 
        references event;

    alter table cellextent 
        add constraint FKCEB1370C3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table cellextent 
        add constraint FKCEB1370C3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table cellextent 
        add constraint FKCEB1370C54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table cellextent 
        add constraint FKCEB1370C97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table cellmajoraxislength 
        add constraint FKB1F80E9EE046324E 
        foreign key (update_id) 
        references event;

    alter table cellmajoraxislength 
        add constraint FKB1F80E9E3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table cellmajoraxislength 
        add constraint FKB1F80E9E3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table cellmajoraxislength 
        add constraint FKB1F80E9E54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table cellmajoraxislength 
        add constraint FKB1F80E9E97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table cellminoraxislength 
        add constraint FK7AF2759AE046324E 
        foreign key (update_id) 
        references event;

    alter table cellminoraxislength 
        add constraint FK7AF2759A3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table cellminoraxislength 
        add constraint FK7AF2759A3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table cellminoraxislength 
        add constraint FK7AF2759A54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table cellminoraxislength 
        add constraint FK7AF2759A97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table cellperimeter 
        add constraint FK46E310FBE046324E 
        foreign key (update_id) 
        references event;

    alter table cellperimeter 
        add constraint FK46E310FB3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table cellperimeter 
        add constraint FK46E310FB3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table cellperimeter 
        add constraint FK46E310FB54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table cellperimeter 
        add constraint FK46E310FB97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table cellposition 
        add constraint FK805AA08BE046324E 
        foreign key (update_id) 
        references event;

    alter table cellposition 
        add constraint FK805AA08B3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table cellposition 
        add constraint FK805AA08B3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table cellposition 
        add constraint FK805AA08B54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table cellposition 
        add constraint FK805AA08B97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table cellsolidity 
        add constraint FKAC04F585E046324E 
        foreign key (update_id) 
        references event;

    alter table cellsolidity 
        add constraint FKAC04F5853680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table cellsolidity 
        add constraint FKAC04F5853EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table cellsolidity 
        add constraint FKAC04F58554212865 
        foreign key (external_id) 
        references externalinfo;

    alter table cellsolidity 
        add constraint FKAC04F58597E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table channel 
        add constraint FK2C0B7D032F40FBE5 
        foreign key (colorComponent) 
        references color;

    alter table channel 
        add constraint FK2C0B7D03423F077D 
        foreign key (pixels) 
        references pixels;

    alter table channel 
        add constraint FK2C0B7D03E046324E 
        foreign key (update_id) 
        references event;

    alter table channel 
        add constraint FK2C0B7D033680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table channel 
        add constraint FK2C0B7D033EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table channel 
        add constraint FK2C0B7D03F2F9B8B7 
        foreign key (logicalChannel) 
        references logicalchannel;

    alter table channel 
        add constraint FK2C0B7D0354212865 
        foreign key (external_id) 
        references externalinfo;

    alter table channel 
        add constraint FK2C0B7D0397E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table channel 
        add constraint FK2C0B7D0329F8D3DF 
        foreign key (statsInfo) 
        references statsinfo;

    alter table channelbinding 
        add constraint FK65AE18222DF49E4E 
        foreign key (color) 
        references color;

    alter table channelbinding 
        add constraint FK65AE1822E046324E 
        foreign key (update_id) 
        references event;

    alter table channelbinding 
        add constraint FK65AE18223680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table channelbinding 
        add constraint FK65AE18223EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table channelbinding 
        add constraint FK65AE1822F315434A 
        foreign key (renderingDef) 
        references renderingdef;

    alter table channelbinding 
        add constraint FK65AE182254212865 
        foreign key (external_id) 
        references externalinfo;

    alter table channelbinding 
        add constraint FK65AE182297E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table channelbinding 
        add constraint FK65AE1822902A6670 
        foreign key (family) 
        references family;

    alter table coating 
        add constraint FK380011E33680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table coating 
        add constraint FK380011E33EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table coating 
        add constraint FK380011E354212865 
        foreign key (external_id) 
        references externalinfo;

    alter table coating 
        add constraint FK380011E397E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table codomainmapcontext 
        add constraint FK9AD37503E046324E 
        foreign key (update_id) 
        references event;

    alter table codomainmapcontext 
        add constraint FK9AD375033680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table codomainmapcontext 
        add constraint FK9AD375033EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table codomainmapcontext 
        add constraint FK9AD37503F315434A 
        foreign key (renderingDef) 
        references renderingdef;

    alter table codomainmapcontext 
        add constraint FK9AD3750354212865 
        foreign key (external_id) 
        references externalinfo;

    alter table codomainmapcontext 
        add constraint FK9AD3750397E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table color 
        add constraint FK5A72F63E046324E 
        foreign key (update_id) 
        references event;

    alter table color 
        add constraint FK5A72F633680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table color 
        add constraint FK5A72F633EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table color 
        add constraint FK5A72F6354212865 
        foreign key (external_id) 
        references externalinfo;

    alter table color 
        add constraint FK5A72F6397E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table contrastmethod 
        add constraint FKE26D1A633680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table contrastmethod 
        add constraint FKE26D1A633EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table contrastmethod 
        add constraint FKE26D1A6354212865 
        foreign key (external_id) 
        references externalinfo;

    alter table contrastmethod 
        add constraint FKE26D1A6397E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table contraststretchingcontext 
        add constraint FK17C6119027E86052 
        foreign key (codomainmapcontext_id) 
        references codomainmapcontext;

    alter table customizedfilterset 
        add constraint FK68522289F2635351 
        foreign key (excitationFilter) 
        references excitationfilter;

    alter table customizedfilterset 
        add constraint FK68522289E046324E 
        foreign key (update_id) 
        references event;

    alter table customizedfilterset 
        add constraint FK6852228940AEB2C1 
        foreign key (transmittanceRange) 
        references transmittancerange;

    alter table customizedfilterset 
        add constraint FK685222893680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table customizedfilterset 
        add constraint FK685222893EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table customizedfilterset 
        add constraint FK685222891E1228EB 
        foreign key (emissionFilter) 
        references emissionfilter;

    alter table customizedfilterset 
        add constraint FK6852228954212865 
        foreign key (external_id) 
        references externalinfo;

    alter table customizedfilterset 
        add constraint FK6852228997E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table customizedfilterset 
        add constraint FK68522289CFB2C88F 
        foreign key (dichroic) 
        references dichroic;

    alter table dataset 
        add constraint FK5605B478E046324E 
        foreign key (update_id) 
        references event;

    alter table dataset 
        add constraint FK5605B4783680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table dataset 
        add constraint FK5605B4783EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table dataset 
        add constraint FK5605B47854212865 
        foreign key (external_id) 
        references externalinfo;

    alter table dataset 
        add constraint FK5605B47897E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table datasetannotation 
        add constraint FK8FF869A7E046324E 
        foreign key (update_id) 
        references event;

    alter table datasetannotation 
        add constraint FK8FF869A73680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table datasetannotation 
        add constraint FK8FF869A73EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table datasetannotation 
        add constraint FK8FF869A72BE523A0 
        foreign key (dataset) 
        references dataset;

    alter table datasetannotation 
        add constraint FK8FF869A754212865 
        foreign key (external_id) 
        references externalinfo;

    alter table datasetannotation 
        add constraint FK8FF869A797E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table datasetimagelink 
        add constraint FK9145065D9DF143F4 
        foreign key (child) 
        references image;

    alter table datasetimagelink 
        add constraint FK9145065DE046324E 
        foreign key (update_id) 
        references event;

    alter table datasetimagelink 
        add constraint FK9145065D3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table datasetimagelink 
        add constraint FK9145065D3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table datasetimagelink 
        add constraint FK9145065D9A8A77D2 
        foreign key (parent) 
        references dataset;

    alter table datasetimagelink 
        add constraint FK9145065D54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table datasetimagelink 
        add constraint FK9145065D97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table dbpatch 
        add constraint FK5782328A54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table detector 
        add constraint FK3E7B17C6F615B39B 
        foreign key (instrument) 
        references instrument;

    alter table detector 
        add constraint FK3E7B17C6E046324E 
        foreign key (update_id) 
        references event;

    alter table detector 
        add constraint FK3E7B17C63680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table detector 
        add constraint FK3E7B17C6BC2DA902 
        foreign key (type) 
        references detectortype;

    alter table detector 
        add constraint FK3E7B17C63EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table detector 
        add constraint FK3E7B17C654212865 
        foreign key (external_id) 
        references externalinfo;

    alter table detector 
        add constraint FK3E7B17C697E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table detectorsettings 
        add constraint FKBBE4ADE9E95EB9CE 
        foreign key (binning) 
        references binning;

    alter table detectorsettings 
        add constraint FKBBE4ADE96223AA99 
        foreign key (detector) 
        references detector;

    alter table detectorsettings 
        add constraint FKBBE4ADE9E046324E 
        foreign key (update_id) 
        references event;

    alter table detectorsettings 
        add constraint FKBBE4ADE93680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table detectorsettings 
        add constraint FKBBE4ADE93EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table detectorsettings 
        add constraint FKBBE4ADE954212865 
        foreign key (external_id) 
        references externalinfo;

    alter table detectorsettings 
        add constraint FKBBE4ADE997E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table detectortype 
        add constraint FKD83454003680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table detectortype 
        add constraint FKD83454003EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table detectortype 
        add constraint FKD834540054212865 
        foreign key (external_id) 
        references externalinfo;

    alter table detectortype 
        add constraint FKD834540097E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table dichroic 
        add constraint FKF542A6C1E046324E 
        foreign key (update_id) 
        references event;

    alter table dichroic 
        add constraint FKF542A6C13680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table dichroic 
        add constraint FKF542A6C13EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table dichroic 
        add constraint FKF542A6C154212865 
        foreign key (external_id) 
        references externalinfo;

    alter table dichroic 
        add constraint FKF542A6C197E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table dimensionorder 
        add constraint FKF2C089A83680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table dimensionorder 
        add constraint FKF2C089A83EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table dimensionorder 
        add constraint FKF2C089A854212865 
        foreign key (external_id) 
        references externalinfo;

    alter table dimensionorder 
        add constraint FKF2C089A897E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table dummystatistics 
        add constraint FK82F503CB3B4C0E25 
        foreign key (metadata_id) 
        references metadata;

    alter table emissionfilter 
        add constraint FKBB8B7FBFE046324E 
        foreign key (update_id) 
        references event;

    alter table emissionfilter 
        add constraint FKBB8B7FBF40AEB2C1 
        foreign key (transmittanceRange) 
        references transmittancerange;

    alter table emissionfilter 
        add constraint FKBB8B7FBF3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table emissionfilter 
        add constraint FKBB8B7FBF536E7DB4 
        foreign key (type) 
        references filtertype;

    alter table emissionfilter 
        add constraint FKBB8B7FBF3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table emissionfilter 
        add constraint FKBB8B7FBF54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table emissionfilter 
        add constraint FKBB8B7FBF97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table event 
        add constraint FK5C6729A5A567786 
        foreign key (type) 
        references eventtype;

    alter table event 
        add constraint FK5C6729A8D8116D 
        foreign key (experimenterGroup) 
        references experimentergroup;

    alter table event 
        add constraint FK5C6729A5A4622C9 
        foreign key (containingEvent) 
        references event;

    alter table event 
        add constraint FK5C6729ABB420E11 
        foreign key (experimenter) 
        references experimenter;

    alter table event 
        add constraint FK5C6729A54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table eventlog 
        add constraint FK1093F26A5A3B0C97 
        foreign key (event) 
        references event;

    alter table eventlog 
        add constraint FK1093F26A54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table eventtype 
        add constraint FK1EE24D43680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table eventtype 
        add constraint FK1EE24D43EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table eventtype 
        add constraint FK1EE24D454212865 
        foreign key (external_id) 
        references externalinfo;

    alter table eventtype 
        add constraint FK1EE24D497E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table excitationfilter 
        add constraint FK2C012492E046324E 
        foreign key (update_id) 
        references event;

    alter table excitationfilter 
        add constraint FK2C01249240AEB2C1 
        foreign key (transmittanceRange) 
        references transmittancerange;

    alter table excitationfilter 
        add constraint FK2C0124923680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table excitationfilter 
        add constraint FK2C012492536E7DB4 
        foreign key (type) 
        references filtertype;

    alter table excitationfilter 
        add constraint FK2C0124923EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table excitationfilter 
        add constraint FK2C01249254212865 
        foreign key (external_id) 
        references externalinfo;

    alter table excitationfilter 
        add constraint FK2C01249297E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table experiment 
        add constraint FKFAE9DBFDE046324E 
        foreign key (update_id) 
        references event;

    alter table experiment 
        add constraint FKFAE9DBFD3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table experiment 
        add constraint FKFAE9DBFDAF93AFB9 
        foreign key (type) 
        references experimenttype;

    alter table experiment 
        add constraint FKFAE9DBFD3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table experiment 
        add constraint FKFAE9DBFD54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table experiment 
        add constraint FKFAE9DBFD97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table experimenter 
        add constraint FKE7E2DD6A54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table experimentergroup 
        add constraint FK8F21EB55E046324E 
        foreign key (update_id) 
        references event;

    alter table experimentergroup 
        add constraint FK8F21EB553680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table experimentergroup 
        add constraint FK8F21EB553EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table experimentergroup 
        add constraint FK8F21EB5554212865 
        foreign key (external_id) 
        references externalinfo;

    alter table experimentergroup 
        add constraint FK8F21EB5597E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table experimenttype 
        add constraint FK7AA824B73680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table experimenttype 
        add constraint FK7AA824B73EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table experimenttype 
        add constraint FK7AA824B754212865 
        foreign key (external_id) 
        references externalinfo;

    alter table experimenttype 
        add constraint FK7AA824B797E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table externalinfo 
        add constraint FKAD6DEAF93680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table externalinfo 
        add constraint FKAD6DEAF93EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table externalinfo 
        add constraint FKAD6DEAF954212865 
        foreign key (external_id) 
        references externalinfo;

    alter table externalinfo 
        add constraint FKAD6DEAF997E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table family 
        add constraint FKB3985B643680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table family 
        add constraint FKB3985B643EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table family 
        add constraint FKB3985B6454212865 
        foreign key (external_id) 
        references externalinfo;

    alter table family 
        add constraint FKB3985B6497E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table filament 
        add constraint FKD3FB8ED61F7C1812 
        foreign key (type) 
        references filamenttype;

    alter table filament 
        add constraint FKD3FB8ED61044E20D 
        foreign key (lightsource_id) 
        references lightsource;

    alter table filamenttype 
        add constraint FK3B82C3103680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table filamenttype 
        add constraint FK3B82C3103EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table filamenttype 
        add constraint FK3B82C31054212865 
        foreign key (external_id) 
        references externalinfo;

    alter table filamenttype 
        add constraint FK3B82C31097E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table filter 
        add constraint FKB408CB78F615B39B 
        foreign key (instrument) 
        references instrument;

    alter table filter 
        add constraint FKB408CB785770AA47 
        foreign key (filterSet) 
        references filterset;

    alter table filter 
        add constraint FKB408CB78E046324E 
        foreign key (update_id) 
        references event;

    alter table filter 
        add constraint FKB408CB781342F7A5 
        foreign key (customizedFilterSet) 
        references customizedfilterset;

    alter table filter 
        add constraint FKB408CB783680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table filter 
        add constraint FKB408CB783EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table filter 
        add constraint FKB408CB7854212865 
        foreign key (external_id) 
        references externalinfo;

    alter table filter 
        add constraint FKB408CB7897E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table filterset 
        add constraint FKCB779DEAE046324E 
        foreign key (update_id) 
        references event;

    alter table filterset 
        add constraint FKCB779DEA40AEB2C1 
        foreign key (transmittanceRange) 
        references transmittancerange;

    alter table filterset 
        add constraint FKCB779DEA3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table filterset 
        add constraint FKCB779DEA3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table filterset 
        add constraint FKCB779DEA54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table filterset 
        add constraint FKCB779DEA97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table filtertype 
        add constraint FKA37CDEB23680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table filtertype 
        add constraint FKA37CDEB23EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table filtertype 
        add constraint FKA37CDEB254212865 
        foreign key (external_id) 
        references externalinfo;

    alter table filtertype 
        add constraint FKA37CDEB297E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table format 
        add constraint FKB45FF7F73680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table format 
        add constraint FKB45FF7F73EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table format 
        add constraint FKB45FF7F754212865 
        foreign key (external_id) 
        references externalinfo;

    alter table format 
        add constraint FKB45FF7F797E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table frequencymultiplication 
        add constraint FKBF7EBBE23680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table frequencymultiplication 
        add constraint FKBF7EBBE23EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table frequencymultiplication 
        add constraint FKBF7EBBE254212865 
        foreign key (external_id) 
        references externalinfo;

    alter table frequencymultiplication 
        add constraint FKBF7EBBE297E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table groupexperimentermap 
        add constraint FK6A373353D90325C3 
        foreign key (child) 
        references experimenter;

    alter table groupexperimentermap 
        add constraint FK6A373353E046324E 
        foreign key (update_id) 
        references event;

    alter table groupexperimentermap 
        add constraint FK6A3733533680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table groupexperimentermap 
        add constraint FK6A3733533EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table groupexperimentermap 
        add constraint FK6A3733533E612EC2 
        foreign key (parent) 
        references experimentergroup;

    alter table groupexperimentermap 
        add constraint FK6A37335354212865 
        foreign key (external_id) 
        references externalinfo;

    alter table groupexperimentermap 
        add constraint FK6A37335397E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table illumination 
        add constraint FK792B69CF3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table illumination 
        add constraint FK792B69CF3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table illumination 
        add constraint FK792B69CF54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table illumination 
        add constraint FK792B69CF97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table image 
        add constraint FK5FAA95BF1C1F031 
        foreign key (context) 
        references experiment;

    alter table image 
        add constraint FK5FAA95BE046324E 
        foreign key (update_id) 
        references event;

    alter table image 
        add constraint FK5FAA95BBDF77F8C 
        foreign key (position) 
        references stagelabel;

    alter table image 
        add constraint FK5FAA95B3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table image 
        add constraint FK5FAA95B3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table image 
        add constraint FK5FAA95B7DD53383 
        foreign key (condition) 
        references imagingenvironment;

    alter table image 
        add constraint FK5FAA95B54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table image 
        add constraint FK5FAA95B97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table image 
        add constraint FK5FAA95BA96C8A31 
        foreign key (setup) 
        references instrument;

    alter table image 
        add constraint FK5FAA95BEBC606B 
        foreign key (objectiveSettings) 
        references objectivesettings;

    alter table imageannotation 
        add constraint FKE016DECAE046324E 
        foreign key (update_id) 
        references event;

    alter table imageannotation 
        add constraint FKE016DECA3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table imageannotation 
        add constraint FKE016DECA3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table imageannotation 
        add constraint FKE016DECA54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table imageannotation 
        add constraint FKE016DECA97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table imageannotation 
        add constraint FKE016DECA9E47F833 
        foreign key (image) 
        references image;

    alter table imagecellcount 
        add constraint FKC67001F2E046324E 
        foreign key (update_id) 
        references event;

    alter table imagecellcount 
        add constraint FKC67001F23680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table imagecellcount 
        add constraint FKC67001F23EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table imagecellcount 
        add constraint FKC67001F254212865 
        foreign key (external_id) 
        references externalinfo;

    alter table imagecellcount 
        add constraint FKC67001F297E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table imagenucleascount 
        add constraint FK57F09563E046324E 
        foreign key (update_id) 
        references event;

    alter table imagenucleascount 
        add constraint FK57F095633680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table imagenucleascount 
        add constraint FK57F095633EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table imagenucleascount 
        add constraint FK57F0956354212865 
        foreign key (external_id) 
        references externalinfo;

    alter table imagenucleascount 
        add constraint FK57F0956397E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table imagingenvironment 
        add constraint FKCB554FBBE046324E 
        foreign key (update_id) 
        references event;

    alter table imagingenvironment 
        add constraint FKCB554FBB3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table imagingenvironment 
        add constraint FKCB554FBB3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table imagingenvironment 
        add constraint FKCB554FBB54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table imagingenvironment 
        add constraint FKCB554FBB97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table immersion 
        add constraint FK43CEA9EB3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table immersion 
        add constraint FK43CEA9EB3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table immersion 
        add constraint FK43CEA9EB54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table immersion 
        add constraint FK43CEA9EB97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table importjob 
        add constraint FKF39249F81C95568E 
        foreign key (job_id) 
        references job;

    alter table instrument 
        add constraint FK532D63E7F3781FED 
        foreign key (microscope) 
        references microscope;

    alter table instrument 
        add constraint FK532D63E7E046324E 
        foreign key (update_id) 
        references event;

    alter table instrument 
        add constraint FK532D63E73680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table instrument 
        add constraint FK532D63E73EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table instrument 
        add constraint FK532D63E754212865 
        foreign key (external_id) 
        references externalinfo;

    alter table instrument 
        add constraint FK532D63E797E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table irisdiaphragm 
        add constraint FK660546163680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table irisdiaphragm 
        add constraint FK660546163EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table irisdiaphragm 
        add constraint FK6605461654212865 
        foreign key (external_id) 
        references externalinfo;

    alter table irisdiaphragm 
        add constraint FK6605461697E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table job 
        add constraint FK19BBDE046324E 
        foreign key (update_id) 
        references event;

    alter table job 
        add constraint FK19BBD3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table job 
        add constraint FK19BBD3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table job 
        add constraint FK19BBD19ED38F5 
        foreign key (status) 
        references jobstatus;

    alter table job 
        add constraint FK19BBD54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table job 
        add constraint FK19BBD97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table joboriginalfilelink 
        add constraint FKC4803204ED593BEC 
        foreign key (child) 
        references originalfile;

    alter table joboriginalfilelink 
        add constraint FKC4803204E046324E 
        foreign key (update_id) 
        references event;

    alter table joboriginalfilelink 
        add constraint FKC48032043680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table joboriginalfilelink 
        add constraint FKC48032043EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table joboriginalfilelink 
        add constraint FKC4803204C40E6D76 
        foreign key (parent) 
        references importjob;

    alter table joboriginalfilelink 
        add constraint FKC4803204DF623690 
        foreign key (parent) 
        references scriptjob;

    alter table joboriginalfilelink 
        add constraint FKC480320426147E5B 
        foreign key (parent) 
        references job;

    alter table joboriginalfilelink 
        add constraint FKC480320454212865 
        foreign key (external_id) 
        references externalinfo;

    alter table joboriginalfilelink 
        add constraint FKC480320497E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table jobstatus 
        add constraint FK79E8E9EF3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table jobstatus 
        add constraint FK79E8E9EF3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table jobstatus 
        add constraint FK79E8E9EF54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table jobstatus 
        add constraint FK79E8E9EF97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table laser 
        add constraint FK61FBECB9D205978 
        foreign key (laserMedium) 
        references lasermedium;

    alter table laser 
        add constraint FK61FBECBBB7FF28A 
        foreign key (pulse) 
        references pulse;

    alter table laser 
        add constraint FK61FBECBC4BAD93C 
        foreign key (frequencyMultiplication) 
        references frequencymultiplication;

    alter table laser 
        add constraint FK61FBECBB7DC4337 
        foreign key (type) 
        references lasertype;

    alter table laser 
        add constraint FK61FBECB1044E20D 
        foreign key (lightsource_id) 
        references lightsource;

    alter table laser 
        add constraint FK61FBECB71E9546 
        foreign key (pump) 
        references laser;

    alter table lasermedium 
        add constraint FK4528CA003680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table lasermedium 
        add constraint FK4528CA003EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table lasermedium 
        add constraint FK4528CA0054212865 
        foreign key (external_id) 
        references externalinfo;

    alter table lasermedium 
        add constraint FK4528CA0097E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table lasertype 
        add constraint FK5F73F0853680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table lasertype 
        add constraint FK5F73F0853EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table lasertype 
        add constraint FK5F73F08554212865 
        foreign key (external_id) 
        references externalinfo;

    alter table lasertype 
        add constraint FK5F73F08597E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table lightsettings 
        add constraint FK71827B39E046324E 
        foreign key (update_id) 
        references event;

    alter table lightsettings 
        add constraint FK71827B393680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table lightsettings 
        add constraint FK71827B393EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table lightsettings 
        add constraint FK71827B39B2096355 
        foreign key (lightSource) 
        references lightsource;

    alter table lightsettings 
        add constraint FK71827B3954212865 
        foreign key (external_id) 
        references externalinfo;

    alter table lightsettings 
        add constraint FK71827B3997E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table lightsource 
        add constraint FKA080F4B1F615B39B 
        foreign key (instrument) 
        references instrument;

    alter table lightsource 
        add constraint FKA080F4B1E046324E 
        foreign key (update_id) 
        references event;

    alter table lightsource 
        add constraint FKA080F4B13680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table lightsource 
        add constraint FKA080F4B13EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table lightsource 
        add constraint FKA080F4B154212865 
        foreign key (external_id) 
        references externalinfo;

    alter table lightsource 
        add constraint FKA080F4B197E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table link 
        add constraint FK32AFFAE046324E 
        foreign key (update_id) 
        references event;

    alter table link 
        add constraint FK32AFFA3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table link 
        add constraint FK32AFFA3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table link 
        add constraint FK32AFFA54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table link 
        add constraint FK32AFFA97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table logicalchannel 
        add constraint FK8406F4DAC302A88E 
        foreign key (contrastMethod) 
        references contrastmethod;

    alter table logicalchannel 
        add constraint FK8406F4DAD6282546 
        foreign key (illumination) 
        references illumination;

    alter table logicalchannel 
        add constraint FK8406F4DAA74E6B15 
        foreign key (otf) 
        references otf;

    alter table logicalchannel 
        add constraint FK8406F4DABBBF55CB 
        foreign key (mode) 
        references acquisitionmode;

    alter table logicalchannel 
        add constraint FK8406F4DA54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table logicalchannel 
        add constraint FK8406F4DA5F14391D 
        foreign key (lightSource) 
        references lightsettings;

    alter table logicalchannel 
        add constraint FK8406F4DAE046324E 
        foreign key (update_id) 
        references event;

    alter table logicalchannel 
        add constraint FK8406F4DA3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table logicalchannel 
        add constraint FK8406F4DA3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table logicalchannel 
        add constraint FK8406F4DA4C9FDDFF 
        foreign key (detectorSettings) 
        references detectorsettings;

    alter table logicalchannel 
        add constraint FK8406F4DA97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table logicalchannel 
        add constraint FK8406F4DA91451BD9 
        foreign key (auxLightSource) 
        references lightsettings;

    alter table logicalchannel 
        add constraint FK8406F4DA4465954C 
        foreign key (photometricInterpretation) 
        references photometricinterpretation;

    alter table medium 
        add constraint FKBFBE8F753680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table medium 
        add constraint FKBFBE8F753EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table medium 
        add constraint FKBFBE8F7554212865 
        foreign key (external_id) 
        references externalinfo;

    alter table medium 
        add constraint FKBFBE8F7597E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table metadata 
        add constraint FKE52D7B2FE046324E 
        foreign key (update_id) 
        references event;

    alter table metadata 
        add constraint FKE52D7B2F3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table metadata 
        add constraint FKE52D7B2F803AA373 
        foreign key (region) 
        references region;

    alter table metadata 
        add constraint FKE52D7B2F3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table metadata 
        add constraint FKE52D7B2F54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table metadata 
        add constraint FKE52D7B2F97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table microscope 
        add constraint FK51DE9A10E046324E 
        foreign key (update_id) 
        references event;

    alter table microscope 
        add constraint FK51DE9A103680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table microscope 
        add constraint FK51DE9A10E3052C4C 
        foreign key (type) 
        references microscopetype;

    alter table microscope 
        add constraint FK51DE9A103EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table microscope 
        add constraint FK51DE9A1054212865 
        foreign key (external_id) 
        references externalinfo;

    alter table microscope 
        add constraint FK51DE9A1097E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table microscopetype 
        add constraint FKAE19A14A3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table microscopetype 
        add constraint FKAE19A14A3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table microscopetype 
        add constraint FKAE19A14A54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table microscopetype 
        add constraint FKAE19A14A97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table nucleusarea 
        add constraint FK764A1E60E046324E 
        foreign key (update_id) 
        references event;

    alter table nucleusarea 
        add constraint FK764A1E603680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table nucleusarea 
        add constraint FK764A1E603EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table nucleusarea 
        add constraint FK764A1E6054212865 
        foreign key (external_id) 
        references externalinfo;

    alter table nucleusarea 
        add constraint FK764A1E6097E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table nucleuseccentricity 
        add constraint FK5C6789BBE046324E 
        foreign key (update_id) 
        references event;

    alter table nucleuseccentricity 
        add constraint FK5C6789BB3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table nucleuseccentricity 
        add constraint FK5C6789BB3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table nucleuseccentricity 
        add constraint FK5C6789BB54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table nucleuseccentricity 
        add constraint FK5C6789BB97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table nucleusextent 
        add constraint FK136AE43DE046324E 
        foreign key (update_id) 
        references event;

    alter table nucleusextent 
        add constraint FK136AE43D3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table nucleusextent 
        add constraint FK136AE43D3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table nucleusextent 
        add constraint FK136AE43D54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table nucleusextent 
        add constraint FK136AE43D97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table nucleusmajoraxislength 
        add constraint FK10DFA88DE046324E 
        foreign key (update_id) 
        references event;

    alter table nucleusmajoraxislength 
        add constraint FK10DFA88D3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table nucleusmajoraxislength 
        add constraint FK10DFA88D3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table nucleusmajoraxislength 
        add constraint FK10DFA88D54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table nucleusmajoraxislength 
        add constraint FK10DFA88D97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table nucleusminoraxislength 
        add constraint FKD9DA0F89E046324E 
        foreign key (update_id) 
        references event;

    alter table nucleusminoraxislength 
        add constraint FKD9DA0F893680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table nucleusminoraxislength 
        add constraint FKD9DA0F893EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table nucleusminoraxislength 
        add constraint FKD9DA0F8954212865 
        foreign key (external_id) 
        references externalinfo;

    alter table nucleusminoraxislength 
        add constraint FKD9DA0F8997E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table nucleusperimeter 
        add constraint FKEA448A2AE046324E 
        foreign key (update_id) 
        references event;

    alter table nucleusperimeter 
        add constraint FKEA448A2A3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table nucleusperimeter 
        add constraint FKEA448A2A3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table nucleusperimeter 
        add constraint FKEA448A2A54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table nucleusperimeter 
        add constraint FKEA448A2A97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table nucleusposition 
        add constraint FK7D5DC57CE046324E 
        foreign key (update_id) 
        references event;

    alter table nucleusposition 
        add constraint FK7D5DC57C3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table nucleusposition 
        add constraint FK7D5DC57C3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table nucleusposition 
        add constraint FK7D5DC57C54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table nucleusposition 
        add constraint FK7D5DC57C97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table nucleussolidity 
        add constraint FKA9081A76E046324E 
        foreign key (update_id) 
        references event;

    alter table nucleussolidity 
        add constraint FKA9081A763680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table nucleussolidity 
        add constraint FKA9081A763EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table nucleussolidity 
        add constraint FKA9081A7654212865 
        foreign key (external_id) 
        references externalinfo;

    alter table nucleussolidity 
        add constraint FKA9081A7697E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table objective 
        add constraint FKA736B939F615B39B 
        foreign key (instrument) 
        references instrument;

    alter table objective 
        add constraint FKA736B939E046324E 
        foreign key (update_id) 
        references event;

    alter table objective 
        add constraint FKA736B939DFDDA32E 
        foreign key (immersion) 
        references immersion;

    alter table objective 
        add constraint FKA736B9393680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table objective 
        add constraint FKA736B9393EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table objective 
        add constraint FKA736B93954212865 
        foreign key (external_id) 
        references externalinfo;

    alter table objective 
        add constraint FKA736B93997E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table objective 
        add constraint FKA736B939663C691E 
        foreign key (coating) 
        references coating;

    alter table objectivesettings 
        add constraint FKDE2D2C5CE046324E 
        foreign key (update_id) 
        references event;

    alter table objectivesettings 
        add constraint FKDE2D2C5C3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table objectivesettings 
        add constraint FKDE2D2C5C3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table objectivesettings 
        add constraint FKDE2D2C5CA876CE92 
        foreign key (medium) 
        references medium;

    alter table objectivesettings 
        add constraint FKDE2D2C5CEEF5905 
        foreign key (objective) 
        references objective;

    alter table objectivesettings 
        add constraint FKDE2D2C5C54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table objectivesettings 
        add constraint FKDE2D2C5C97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table originalfile 
        add constraint FK7F772ECD91B99F96 
        foreign key (format) 
        references format;

    alter table originalfile 
        add constraint FK7F772ECDE046324E 
        foreign key (update_id) 
        references event;

    alter table originalfile 
        add constraint FK7F772ECD3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table originalfile 
        add constraint FK7F772ECD3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table originalfile 
        add constraint FK7F772ECD54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table originalfile 
        add constraint FK7F772ECD97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table otf 
        add constraint FK1AF21A6E0ADEF 
        foreign key (pixelType) 
        references pixelstype;

    alter table otf 
        add constraint FK1AF21E046324E 
        foreign key (update_id) 
        references event;

    alter table otf 
        add constraint FK1AF213680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table otf 
        add constraint FK1AF213EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table otf 
        add constraint FK1AF2154212865 
        foreign key (external_id) 
        references externalinfo;

    alter table otf 
        add constraint FK1AF2197E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table overlay 
        add constraint FKBEF444503162083C 
        foreign key (type) 
        references overlaytype;

    alter table overlay 
        add constraint FKBEF44450858E96EF 
        foreign key (specification_id) 
        references specification;

    alter table overlay 
        add constraint FKBEF44450B00DBC48 
        foreign key (plane) 
        references xy;

    alter table overlaytype 
        add constraint FKE7D02B8A3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table overlaytype 
        add constraint FKE7D02B8A3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table overlaytype 
        add constraint FKE7D02B8A54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table overlaytype 
        add constraint FKE7D02B8A97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table photometricinterpretation 
        add constraint FKC0FC14EA3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table photometricinterpretation 
        add constraint FKC0FC14EA3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table photometricinterpretation 
        add constraint FKC0FC14EA54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table photometricinterpretation 
        add constraint FKC0FC14EA97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table pixels 
        add constraint FKC51E7EADAE92EE96 
        foreign key (pixelsType) 
        references pixelstype;

    alter table pixels 
        add constraint FKC51E7EAD7CA9D4B6 
        foreign key (relatedTo) 
        references pixels;

    alter table pixels 
        add constraint FKC51E7EADE046324E 
        foreign key (update_id) 
        references event;

    alter table pixels 
        add constraint FKC51E7EAD1881AAD8 
        foreign key (dimensionOrder) 
        references dimensionorder;

    alter table pixels 
        add constraint FKC51E7EAD3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table pixels 
        add constraint FKC51E7EAD3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table pixels 
        add constraint FKC51E7EAD5C43EAF7 
        foreign key (pixelsDimensions) 
        references pixelsdimensions;

    alter table pixels 
        add constraint FKC51E7EAD54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table pixels 
        add constraint FKC51E7EAD97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table pixels 
        add constraint FKC51E7EAD9E47F833 
        foreign key (image) 
        references image;

    alter table pixelsdimensions 
        add constraint FKBED80A3AE046324E 
        foreign key (update_id) 
        references event;

    alter table pixelsdimensions 
        add constraint FKBED80A3A3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table pixelsdimensions 
        add constraint FKBED80A3A3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table pixelsdimensions 
        add constraint FKBED80A3A54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table pixelsdimensions 
        add constraint FKBED80A3A97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table pixelsoriginalfilemap 
        add constraint FK2459992282C47DEC 
        foreign key (child) 
        references pixels;

    alter table pixelsoriginalfilemap 
        add constraint FK24599922E046324E 
        foreign key (update_id) 
        references event;

    alter table pixelsoriginalfilemap 
        add constraint FK245999223680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table pixelsoriginalfilemap 
        add constraint FK245999223EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table pixelsoriginalfilemap 
        add constraint FK24599922AC604F7A 
        foreign key (parent) 
        references originalfile;

    alter table pixelsoriginalfilemap 
        add constraint FK2459992254212865 
        foreign key (external_id) 
        references externalinfo;

    alter table pixelsoriginalfilemap 
        add constraint FK2459992297E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table pixelstype 
        add constraint FKFF6BEF673680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table pixelstype 
        add constraint FKFF6BEF673EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table pixelstype 
        add constraint FKFF6BEF6754212865 
        foreign key (external_id) 
        references externalinfo;

    alter table pixelstype 
        add constraint FKFF6BEF6797E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table planeinfo 
        add constraint FK7DA1B10A423F077D 
        foreign key (pixels) 
        references pixels;

    alter table planeinfo 
        add constraint FK7DA1B10AE046324E 
        foreign key (update_id) 
        references event;

    alter table planeinfo 
        add constraint FK7DA1B10A3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table planeinfo 
        add constraint FK7DA1B10A3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table planeinfo 
        add constraint FK7DA1B10A54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table planeinfo 
        add constraint FK7DA1B10A97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table planeslicingcontext 
        add constraint FK96BF2A5C27E86052 
        foreign key (codomainmapcontext_id) 
        references codomainmapcontext;

    alter table project 
        add constraint FKED904B19E046324E 
        foreign key (update_id) 
        references event;

    alter table project 
        add constraint FKED904B193680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table project 
        add constraint FKED904B193EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table project 
        add constraint FKED904B1954212865 
        foreign key (external_id) 
        references externalinfo;

    alter table project 
        add constraint FKED904B1997E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table projectannotation 
        add constraint FK6793EB08E046324E 
        foreign key (update_id) 
        references event;

    alter table projectannotation 
        add constraint FK6793EB085AFA50E2 
        foreign key (project) 
        references project;

    alter table projectannotation 
        add constraint FK6793EB083680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table projectannotation 
        add constraint FK6793EB083EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table projectannotation 
        add constraint FK6793EB0854212865 
        foreign key (external_id) 
        references externalinfo;

    alter table projectannotation 
        add constraint FK6793EB0897E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table projectdatasetlink 
        add constraint FK7F1563B9DB836444 
        foreign key (child) 
        references dataset;

    alter table projectdatasetlink 
        add constraint FK7F1563B9E046324E 
        foreign key (update_id) 
        references event;

    alter table projectdatasetlink 
        add constraint FK7F1563B93680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table projectdatasetlink 
        add constraint FK7F1563B93EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table projectdatasetlink 
        add constraint FK7F1563B932150E73 
        foreign key (parent) 
        references project;

    alter table projectdatasetlink 
        add constraint FK7F1563B954212865 
        foreign key (external_id) 
        references externalinfo;

    alter table projectdatasetlink 
        add constraint FK7F1563B997E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table pulse 
        add constraint FK6611B993680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table pulse 
        add constraint FK6611B993EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table pulse 
        add constraint FK6611B9954212865 
        foreign key (external_id) 
        references externalinfo;

    alter table pulse 
        add constraint FK6611B9997E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table quantumdef 
        add constraint FK7B87170AE046324E 
        foreign key (update_id) 
        references event;

    alter table quantumdef 
        add constraint FK7B87170A3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table quantumdef 
        add constraint FK7B87170A3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table quantumdef 
        add constraint FK7B87170A54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table quantumdef 
        add constraint FK7B87170A97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table region 
        add constraint FKC84826F4423F077D 
        foreign key (pixels) 
        references pixels;

    alter table region 
        add constraint FKC84826F4E046324E 
        foreign key (update_id) 
        references event;

    alter table region 
        add constraint FKC84826F43680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table region 
        add constraint FKC84826F4395BBB30 
        foreign key (type) 
        references regiontype;

    alter table region 
        add constraint FKC84826F43EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table region 
        add constraint FKC84826F454212865 
        foreign key (external_id) 
        references externalinfo;

    alter table region 
        add constraint FKC84826F497E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table regiontype 
        add constraint FK896A1C2E3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table regiontype 
        add constraint FK896A1C2E3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table regiontype 
        add constraint FK896A1C2E54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table regiontype 
        add constraint FK896A1C2E97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table renderingdef 
        add constraint FK516881F9423F077D 
        foreign key (pixels) 
        references pixels;

    alter table renderingdef 
        add constraint FK516881F9CB612E23 
        foreign key (quantization) 
        references quantumdef;

    alter table renderingdef 
        add constraint FK516881F9E046324E 
        foreign key (update_id) 
        references event;

    alter table renderingdef 
        add constraint FK516881F93680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table renderingdef 
        add constraint FK516881F93EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table renderingdef 
        add constraint FK516881F9D307DECE 
        foreign key (model) 
        references renderingmodel;

    alter table renderingdef 
        add constraint FK516881F954212865 
        foreign key (external_id) 
        references externalinfo;

    alter table renderingdef 
        add constraint FK516881F997E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table renderingmodel 
        add constraint FK99D34C1D3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table renderingmodel 
        add constraint FK99D34C1D3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table renderingmodel 
        add constraint FK99D34C1D54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table renderingmodel 
        add constraint FK99D34C1D97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table reverseintensitycontext 
        add constraint FK7C455CFE27E86052 
        foreign key (codomainmapcontext_id) 
        references codomainmapcontext;

    alter table roi5d 
        add constraint FK67A7D9B423F077D 
        foreign key (pixels) 
        references pixels;

    alter table roi5d 
        add constraint FK67A7D9BE046324E 
        foreign key (update_id) 
        references event;

    alter table roi5d 
        add constraint FK67A7D9B3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table roi5d 
        add constraint FK67A7D9B3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table roi5d 
        add constraint FK67A7D9B54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table roi5d 
        add constraint FK67A7D9B97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table roiextent 
        add constraint FK76B4EFF672FC7628 
        foreign key (roi5d) 
        references roi5d;

    alter table roiextent 
        add constraint FK76B4EFF6E046324E 
        foreign key (update_id) 
        references event;

    alter table roiextent 
        add constraint FK76B4EFF63680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table roiextent 
        add constraint FK76B4EFF63EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table roiextent 
        add constraint FK76B4EFF654212865 
        foreign key (external_id) 
        references externalinfo;

    alter table roiextent 
        add constraint FK76B4EFF697E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table roimap 
        add constraint FKC8D608107225EDA9 
        foreign key (child) 
        references roi5d;

    alter table roimap 
        add constraint FKC8D60810E046324E 
        foreign key (update_id) 
        references event;

    alter table roimap 
        add constraint FKC8D608103680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table roimap 
        add constraint FKC8D608103EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table roimap 
        add constraint FKC8D60810E86896CE 
        foreign key (parent) 
        references roiset;

    alter table roimap 
        add constraint FKC8D6081054212865 
        foreign key (external_id) 
        references externalinfo;

    alter table roimap 
        add constraint FKC8D6081097E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table roiset 
        add constraint FKC8D61F16E046324E 
        foreign key (update_id) 
        references event;

    alter table roiset 
        add constraint FKC8D61F163680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table roiset 
        add constraint FKC8D61F163EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table roiset 
        add constraint FKC8D61F1654212865 
        foreign key (external_id) 
        references externalinfo;

    alter table roiset 
        add constraint FKC8D61F1697E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table scriptjob 
        add constraint FKEE613121C95568E 
        foreign key (job_id) 
        references job;

    alter table shapearea 
        add constraint FK9E5067AE879E6E1E 
        foreign key (roiextent) 
        references roiextent;

    alter table shapearea 
        add constraint FK9E5067AEE046324E 
        foreign key (update_id) 
        references event;

    alter table shapearea 
        add constraint FK9E5067AE3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table shapearea 
        add constraint FK9E5067AE3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table shapearea 
        add constraint FK9E5067AEC4143D52 
        foreign key (extent) 
        references roiextent;

    alter table shapearea 
        add constraint FK9E5067AE54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table shapearea 
        add constraint FK9E5067AE97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table specification 
        add constraint FK4DEA4103E046324E 
        foreign key (update_id) 
        references event;

    alter table specification 
        add constraint FK4DEA41033680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table specification 
        add constraint FK4DEA4103803AA373 
        foreign key (region) 
        references region;

    alter table specification 
        add constraint FK4DEA41033EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table specification 
        add constraint FK4DEA410354212865 
        foreign key (external_id) 
        references externalinfo;

    alter table specification 
        add constraint FK4DEA410397E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table square 
        add constraint FKCAAC591D9DE4412C 
        foreign key (shapearea_id) 
        references shapearea;

    alter table stagelabel 
        add constraint FK436CEAB6E046324E 
        foreign key (update_id) 
        references event;

    alter table stagelabel 
        add constraint FK436CEAB63680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table stagelabel 
        add constraint FK436CEAB63EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table stagelabel 
        add constraint FK436CEAB654212865 
        foreign key (external_id) 
        references externalinfo;

    alter table stagelabel 
        add constraint FK436CEAB697E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table statsinfo 
        add constraint FK847AC1CDE046324E 
        foreign key (update_id) 
        references event;

    alter table statsinfo 
        add constraint FK847AC1CD3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table statsinfo 
        add constraint FK847AC1CD3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table statsinfo 
        add constraint FK847AC1CD54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table statsinfo 
        add constraint FK847AC1CD97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table thumbnail 
        add constraint FK4F4E50EC423F077D 
        foreign key (pixels) 
        references pixels;

    alter table thumbnail 
        add constraint FK4F4E50ECE046324E 
        foreign key (update_id) 
        references event;

    alter table thumbnail 
        add constraint FK4F4E50EC3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table thumbnail 
        add constraint FK4F4E50EC3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table thumbnail 
        add constraint FK4F4E50EC54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table thumbnail 
        add constraint FK4F4E50EC97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table transmittancerange 
        add constraint FK60026A0AE046324E 
        foreign key (update_id) 
        references event;

    alter table transmittancerange 
        add constraint FK60026A0A3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table transmittancerange 
        add constraint FK60026A0A3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table transmittancerange 
        add constraint FK60026A0A54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table transmittancerange 
        add constraint FK60026A0A97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table uroi 
        add constraint FK36E937F87C54A7 
        foreign key (box) 
        references boundingbox;

    alter table uroi 
        add constraint FK36E937858E96EF 
        foreign key (specification_id) 
        references specification;

    alter table ushape 
        add constraint FKCE2C504CE046324E 
        foreign key (update_id) 
        references event;

    alter table ushape 
        add constraint FKCE2C504C3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table ushape 
        add constraint FKCE2C504C3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table ushape 
        add constraint FKCE2C504C8A43E1E5 
        foreign key (uslice) 
        references uslice;

    alter table ushape 
        add constraint FKCE2C504C54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table ushape 
        add constraint FKCE2C504C97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table uslice 
        add constraint FKCE2E3E3DE046324E 
        foreign key (update_id) 
        references event;

    alter table uslice 
        add constraint FKCE2E3E3D3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table uslice 
        add constraint FKCE2E3E3D3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table uslice 
        add constraint FKCE2E3E3D1359699 
        foreign key (uroi) 
        references uroi;

    alter table uslice 
        add constraint FKCE2E3E3D54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table uslice 
        add constraint FKCE2E3E3D97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table usquare 
        add constraint FKF7E56DD252E963A5 
        foreign key (ushape_id) 
        references ushape;

    alter table xy 
        add constraint FKF012E5B2D8F 
        foreign key (boundingbox_id) 
        references boundingbox;

    alter table xyc 
        add constraint FK1D1822E5B2D8F 
        foreign key (boundingbox_id) 
        references boundingbox;

    alter table xyct 
        add constraint FK385F322E5B2D8F 
        foreign key (boundingbox_id) 
        references boundingbox;

    alter table xyctoxylink 
        add constraint FK6DDE8C38AF54D708 
        foreign key (child) 
        references xy;

    alter table xyctoxylink 
        add constraint FK6DDE8C38E046324E 
        foreign key (update_id) 
        references event;

    alter table xyctoxylink 
        add constraint FK6DDE8C383680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table xyctoxylink 
        add constraint FK6DDE8C383EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table xyctoxylink 
        add constraint FK6DDE8C3851166481 
        foreign key (parent) 
        references xyc;

    alter table xyctoxylink 
        add constraint FK6DDE8C3854212865 
        foreign key (external_id) 
        references externalinfo;

    alter table xyctoxylink 
        add constraint FK6DDE8C3897E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table xycttoxyclink 
        add constraint FK8FCB4AAF920F50F3 
        foreign key (child) 
        references xyc;

    alter table xycttoxyclink 
        add constraint FK8FCB4AAFE046324E 
        foreign key (update_id) 
        references event;

    alter table xycttoxyclink 
        add constraint FK8FCB4AAF3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table xycttoxyclink 
        add constraint FK8FCB4AAF3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table xycttoxyclink 
        add constraint FK8FCB4AAFC5AB2807 
        foreign key (parent) 
        references xyct;

    alter table xycttoxyclink 
        add constraint FK8FCB4AAF54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table xycttoxyclink 
        add constraint FK8FCB4AAF97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table xycttoxytlink 
        add constraint FK90BADA40920F5104 
        foreign key (child) 
        references xyt;

    alter table xycttoxytlink 
        add constraint FK90BADA40E046324E 
        foreign key (update_id) 
        references event;

    alter table xycttoxytlink 
        add constraint FK90BADA403680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table xycttoxytlink 
        add constraint FK90BADA403EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table xycttoxytlink 
        add constraint FK90BADA40C5AB2807 
        foreign key (parent) 
        references xyct;

    alter table xycttoxytlink 
        add constraint FK90BADA4054212865 
        foreign key (external_id) 
        references externalinfo;

    alter table xycttoxytlink 
        add constraint FK90BADA4097E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table xyt 
        add constraint FK1D1932E5B2D8F 
        foreign key (boundingbox_id) 
        references boundingbox;

    alter table xyttoxylink 
        add constraint FK4669EB49AF54D708 
        foreign key (child) 
        references xy;

    alter table xyttoxylink 
        add constraint FK4669EB49E046324E 
        foreign key (update_id) 
        references event;

    alter table xyttoxylink 
        add constraint FK4669EB493680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table xyttoxylink 
        add constraint FK4669EB493EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table xyttoxylink 
        add constraint FK4669EB4951166492 
        foreign key (parent) 
        references xyt;

    alter table xyttoxylink 
        add constraint FK4669EB4954212865 
        foreign key (external_id) 
        references externalinfo;

    alter table xyttoxylink 
        add constraint FK4669EB4997E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table xyz 
        add constraint FK1D1992E5B2D8F 
        foreign key (boundingbox_id) 
        references boundingbox;

    alter table xyzc 
        add constraint FK3861EA2E5B2D8F 
        foreign key (boundingbox_id) 
        references boundingbox;

    alter table xyzct 
        add constraint FK6D3DBCA2E5B2D8F 
        foreign key (boundingbox_id) 
        references boundingbox;

    alter table xyzctoxyclink 
        add constraint FKB776D6F7920F50F3 
        foreign key (child) 
        references xyc;

    alter table xyzctoxyclink 
        add constraint FKB776D6F7E046324E 
        foreign key (update_id) 
        references event;

    alter table xyzctoxyclink 
        add constraint FKB776D6F73680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table xyzctoxyclink 
        add constraint FKB776D6F73EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table xyzctoxyclink 
        add constraint FKB776D6F7C5AB2ABF 
        foreign key (parent) 
        references xyzc;

    alter table xyzctoxyclink 
        add constraint FKB776D6F754212865 
        foreign key (external_id) 
        references externalinfo;

    alter table xyzctoxyclink 
        add constraint FKB776D6F797E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table xyzctoxyzlink 
        add constraint FKB8BAF38E920F510A 
        foreign key (child) 
        references xyz;

    alter table xyzctoxyzlink 
        add constraint FKB8BAF38EE046324E 
        foreign key (update_id) 
        references event;

    alter table xyzctoxyzlink 
        add constraint FKB8BAF38E3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table xyzctoxyzlink 
        add constraint FKB8BAF38E3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table xyzctoxyzlink 
        add constraint FKB8BAF38EC5AB2ABF 
        foreign key (parent) 
        references xyzc;

    alter table xyzctoxyzlink 
        add constraint FKB8BAF38E54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table xyzctoxyzlink 
        add constraint FKB8BAF38E97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table xyzcttoxyctlink 
        add constraint FK594AB0F16A41479 
        foreign key (child) 
        references xyct;

    alter table xyzcttoxyctlink 
        add constraint FK594AB0F1E046324E 
        foreign key (update_id) 
        references event;

    alter table xyzcttoxyctlink 
        add constraint FK594AB0F13680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table xyzcttoxyctlink 
        add constraint FK594AB0F13EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table xyzcttoxyctlink 
        add constraint FK594AB0F1E3AF2989 
        foreign key (parent) 
        references xyzct;

    alter table xyzcttoxyctlink 
        add constraint FK594AB0F154212865 
        foreign key (external_id) 
        references externalinfo;

    alter table xyzcttoxyctlink 
        add constraint FK594AB0F197E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table xyzcttoxyzclink 
        add constraint FK7F9A97A96A41731 
        foreign key (child) 
        references xyzc;

    alter table xyzcttoxyzclink 
        add constraint FK7F9A97A9E046324E 
        foreign key (update_id) 
        references event;

    alter table xyzcttoxyzclink 
        add constraint FK7F9A97A93680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table xyzcttoxyzclink 
        add constraint FK7F9A97A93EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table xyzcttoxyzclink 
        add constraint FK7F9A97A9E3AF2989 
        foreign key (parent) 
        references xyzct;

    alter table xyzcttoxyzclink 
        add constraint FK7F9A97A954212865 
        foreign key (external_id) 
        references externalinfo;

    alter table xyzcttoxyzclink 
        add constraint FK7F9A97A997E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table xyzcttoxyztlink 
        add constraint FK808A273A6A41742 
        foreign key (child) 
        references xyzt;

    alter table xyzcttoxyztlink 
        add constraint FK808A273AE046324E 
        foreign key (update_id) 
        references event;

    alter table xyzcttoxyztlink 
        add constraint FK808A273A3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table xyzcttoxyztlink 
        add constraint FK808A273A3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table xyzcttoxyztlink 
        add constraint FK808A273AE3AF2989 
        foreign key (parent) 
        references xyzct;

    alter table xyzcttoxyztlink 
        add constraint FK808A273A54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table xyzcttoxyztlink 
        add constraint FK808A273A97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table xyzt 
        add constraint FK3861FB2E5B2D8F 
        foreign key (boundingbox_id) 
        references boundingbox;

    alter table xyztoxylink 
        add constraint FKC004854FAF54D708 
        foreign key (child) 
        references xy;

    alter table xyztoxylink 
        add constraint FKC004854FE046324E 
        foreign key (update_id) 
        references event;

    alter table xyztoxylink 
        add constraint FKC004854F3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table xyztoxylink 
        add constraint FKC004854F3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table xyztoxylink 
        add constraint FKC004854F51166498 
        foreign key (parent) 
        references xyz;

    alter table xyztoxylink 
        add constraint FKC004854F54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table xyztoxylink 
        add constraint FKC004854F97E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table xyzttoxytlink 
        add constraint FKF146E997920F5104 
        foreign key (child) 
        references xyt;

    alter table xyzttoxytlink 
        add constraint FKF146E997E046324E 
        foreign key (update_id) 
        references event;

    alter table xyzttoxytlink 
        add constraint FKF146E9973680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table xyzttoxytlink 
        add constraint FKF146E9973EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table xyzttoxytlink 
        add constraint FKF146E997C5AB2AD0 
        foreign key (parent) 
        references xyzt;

    alter table xyzttoxytlink 
        add constraint FKF146E99754212865 
        foreign key (external_id) 
        references externalinfo;

    alter table xyzttoxytlink 
        add constraint FKF146E99797E49CF3 
        foreign key (group_id) 
        references experimentergroup;

    alter table xyzttoxyzlink 
        add constraint FKF19B769D920F510A 
        foreign key (child) 
        references xyz;

    alter table xyzttoxyzlink 
        add constraint FKF19B769DE046324E 
        foreign key (update_id) 
        references event;

    alter table xyzttoxyzlink 
        add constraint FKF19B769D3680CECE 
        foreign key (owner_id) 
        references experimenter;

    alter table xyzttoxyzlink 
        add constraint FKF19B769D3EBB3C78 
        foreign key (creation_id) 
        references event;

    alter table xyzttoxyzlink 
        add constraint FKF19B769DC5AB2AD0 
        foreign key (parent) 
        references xyzt;

    alter table xyzttoxyzlink 
        add constraint FKF19B769D54212865 
        foreign key (external_id) 
        references externalinfo;

    alter table xyzttoxyzlink 
        add constraint FKF19B769D97E49CF3 
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
