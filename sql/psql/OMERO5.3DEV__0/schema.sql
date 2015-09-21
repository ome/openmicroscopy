
    create table acquisitionmode (
        id int8 not null,
        permissions int8 not null,
        value varchar(255) not null unique,
        external_id int8 unique,
        primary key (id)
    );;

    create table annotation (
        discriminator varchar(31) not null,
        id int8 not null,
        description text,
        permissions int8 not null,
        name varchar(255),
        ns varchar(255),
        version int4,
        boolValue bool,
        textValue text,
        doubleValue float8,
        longValue int8,
        termValue text,
        timeValue timestamp,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        "file" int8,
        primary key (id)
    );;

    create table annotation_mapValue (
        annotation_id int8 not null,
        name varchar(255) not null,
        value varchar(255) not null,
        index int4 not null,
        primary key (annotation_id, index)
    );;

    create table annotationannotationlink (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        primary key (id),
        unique (parent, child, owner_id)
    );;

    create table arc (
        lightsource_id int8 not null,
        type int8 not null,
        primary key (lightsource_id)
    );;

    create table arctype (
        id int8 not null,
        permissions int8 not null,
        value varchar(255) not null unique,
        external_id int8 unique,
        primary key (id)
    );;

    create table binning (
        id int8 not null,
        permissions int8 not null,
        value varchar(255) not null unique,
        external_id int8 unique,
        primary key (id)
    );;

    create table channel (
        id int8 not null,
        alpha int4,
        blue int4,
        permissions int8 not null,
        green int4,
        lookupTable varchar(255),
        red int4,
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        logicalChannel int8 not null,
        pixels int8 not null,
        statsInfo int8,
        pixels_index int4 not null,
        primary key (id),
        unique (pixels, pixels_index)
    );;

    create table channelannotationlink (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        primary key (id),
        unique (parent, child, owner_id)
    );;

    create table channelbinding (
        id int8 not null,
        active bool not null,
        alpha int4 not null,
        blue int4 not null,
        coefficient float8 not null,
        permissions int8 not null,
        green int4 not null,
        inputEnd float8 not null,
        inputStart float8 not null,
        lookupTable varchar(255),
        noiseReduction bool not null,
        red int4 not null,
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        family int8 not null,
        renderingDef int8 not null,
        renderingDef_index int4 not null,
        primary key (id),
        unique (renderingDef, renderingDef_index)
    );;

    create table checksumalgorithm (
        id int8 not null,
        permissions int8 not null,
        value varchar(255) not null unique,
        external_id int8 unique,
        primary key (id)
    );;

    create table codomainmapcontext (
        id int8 not null,
        permissions int8 not null,
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        renderingDef int8 not null,
        renderingDef_index int4 not null,
        primary key (id),
        unique (renderingDef, renderingDef_index)
    );;

    create table contrastmethod (
        id int8 not null,
        permissions int8 not null,
        value varchar(255) not null unique,
        external_id int8 unique,
        primary key (id)
    );;

    create table contraststretchingcontext (
        xend int4 not null,
        xstart int4 not null,
        yend int4 not null,
        ystart int4 not null,
        codomainmapcontext_id int8 not null,
        primary key (codomainmapcontext_id)
    );;

    create table correction (
        id int8 not null,
        permissions int8 not null,
        value varchar(255) not null unique,
        external_id int8 unique,
        primary key (id)
    );;

    create table count_Annotation_annotationLinks_by_owner (
        Annotation_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (Annotation_id, owner_id)
    );;

    create table count_Channel_annotationLinks_by_owner (
        Channel_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (Channel_id, owner_id)
    );;

    create table count_Dataset_annotationLinks_by_owner (
        Dataset_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (Dataset_id, owner_id)
    );;

    create table count_Dataset_imageLinks_by_owner (
        Dataset_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (Dataset_id, owner_id)
    );;

    create table count_Dataset_projectLinks_by_owner (
        Dataset_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (Dataset_id, owner_id)
    );;

    create table count_Detector_annotationLinks_by_owner (
        Detector_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (Detector_id, owner_id)
    );;

    create table count_Dichroic_annotationLinks_by_owner (
        Dichroic_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (Dichroic_id, owner_id)
    );;

    create table count_ExperimenterGroup_annotationLinks_by_owner (
        ExperimenterGroup_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (ExperimenterGroup_id, owner_id)
    );;

    create table count_Experimenter_annotationLinks_by_owner (
        Experimenter_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (Experimenter_id, owner_id)
    );;

    create table count_Fileset_annotationLinks_by_owner (
        Fileset_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (Fileset_id, owner_id)
    );;

    create table count_Fileset_jobLinks_by_owner (
        Fileset_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (Fileset_id, owner_id)
    );;

    create table count_FilterSet_emissionFilterLink_by_owner (
        FilterSet_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (FilterSet_id, owner_id)
    );;

    create table count_FilterSet_excitationFilterLink_by_owner (
        FilterSet_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (FilterSet_id, owner_id)
    );;

    create table count_Filter_annotationLinks_by_owner (
        Filter_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (Filter_id, owner_id)
    );;

    create table count_Filter_emissionFilterLink_by_owner (
        Filter_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (Filter_id, owner_id)
    );;

    create table count_Filter_excitationFilterLink_by_owner (
        Filter_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (Filter_id, owner_id)
    );;

    create table count_Image_annotationLinks_by_owner (
        Image_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (Image_id, owner_id)
    );;

    create table count_Image_datasetLinks_by_owner (
        Image_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (Image_id, owner_id)
    );;

    create table count_Instrument_annotationLinks_by_owner (
        Instrument_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (Instrument_id, owner_id)
    );;

    create table count_Job_originalFileLinks_by_owner (
        Job_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (Job_id, owner_id)
    );;

    create table count_LightPath_annotationLinks_by_owner (
        LightPath_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (LightPath_id, owner_id)
    );;

    create table count_LightPath_emissionFilterLink_by_owner (
        LightPath_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (LightPath_id, owner_id)
    );;

    create table count_LightPath_excitationFilterLink_by_owner (
        LightPath_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (LightPath_id, owner_id)
    );;

    create table count_LightSource_annotationLinks_by_owner (
        LightSource_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (LightSource_id, owner_id)
    );;

    create table count_Namespace_annotationLinks_by_owner (
        Namespace_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (Namespace_id, owner_id)
    );;

    create table count_Node_annotationLinks_by_owner (
        Node_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (Node_id, owner_id)
    );;

    create table count_Objective_annotationLinks_by_owner (
        Objective_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (Objective_id, owner_id)
    );;

    create table count_OriginalFile_annotationLinks_by_owner (
        OriginalFile_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (OriginalFile_id, owner_id)
    );;

    create table count_OriginalFile_pixelsFileMaps_by_owner (
        OriginalFile_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (OriginalFile_id, owner_id)
    );;

    create table count_Pixels_pixelsFileMaps_by_owner (
        Pixels_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (Pixels_id, owner_id)
    );;

    create table count_PlaneInfo_annotationLinks_by_owner (
        PlaneInfo_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (PlaneInfo_id, owner_id)
    );;

    create table count_PlateAcquisition_annotationLinks_by_owner (
        PlateAcquisition_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (PlateAcquisition_id, owner_id)
    );;

    create table count_Plate_annotationLinks_by_owner (
        Plate_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (Plate_id, owner_id)
    );;

    create table count_Plate_screenLinks_by_owner (
        Plate_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (Plate_id, owner_id)
    );;

    create table count_Project_annotationLinks_by_owner (
        Project_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (Project_id, owner_id)
    );;

    create table count_Project_datasetLinks_by_owner (
        Project_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (Project_id, owner_id)
    );;

    create table count_Reagent_annotationLinks_by_owner (
        Reagent_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (Reagent_id, owner_id)
    );;

    create table count_Reagent_wellLinks_by_owner (
        Reagent_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (Reagent_id, owner_id)
    );;

    create table count_Roi_annotationLinks_by_owner (
        Roi_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (Roi_id, owner_id)
    );;

    create table count_Screen_annotationLinks_by_owner (
        Screen_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (Screen_id, owner_id)
    );;

    create table count_Screen_plateLinks_by_owner (
        Screen_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (Screen_id, owner_id)
    );;

    create table count_Session_annotationLinks_by_owner (
        Session_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (Session_id, owner_id)
    );;

    create table count_Shape_annotationLinks_by_owner (
        Shape_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (Shape_id, owner_id)
    );;

    create table count_Well_annotationLinks_by_owner (
        Well_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (Well_id, owner_id)
    );;

    create table count_Well_reagentLinks_by_owner (
        Well_id int8 not null,
        count int8 not null,
        owner_id int8 not null,
        primary key (Well_id, owner_id)
    );;

    create table dataset (
        id int8 not null,
        description text,
        permissions int8 not null,
        name varchar(255) not null,
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        primary key (id)
    );;

    create table datasetannotationlink (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        primary key (id),
        unique (parent, child, owner_id)
    );;

    create table datasetimagelink (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        primary key (id),
        unique (parent, child, owner_id)
    );;

    create table dbpatch (
        id int8 not null,
        currentPatch int4 not null,
        currentVersion varchar(255) not null,
        permissions int8 not null,
        finished timestamp,
        message varchar(255),
        previousPatch int4 not null,
        previousVersion varchar(255) not null,
        external_id int8 unique,
        primary key (id)
    );;

    create table detector (
        id int8 not null,
        amplificationGain float8,
        permissions int8 not null,
        gain float8,
        lotNumber varchar(255),
        manufacturer varchar(255),
        model varchar(255),
        offsetValue float8,
        serialNumber varchar(255),
        version int4,
        voltageUnit UnitsElectricPotential,
        voltage float8,
        zoom float8,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        instrument int8 not null,
        type int8 not null,
        primary key (id)
    );;

    create table detectorannotationlink (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        primary key (id),
        unique (parent, child, owner_id)
    );;

    create table detectorsettings (
        id int8 not null,
        permissions int8 not null,
        gain float8,
        integration positive_int,
        offsetValue float8,
        readOutRateUnit UnitsFrequency,
        readOutRate float8,
        version int4,
        voltageUnit UnitsElectricPotential,
        voltage float8,
        zoom float8,
        binning int8,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        detector int8 not null,
        primary key (id)
    );;

    create table detectortype (
        id int8 not null,
        permissions int8 not null,
        value varchar(255) not null unique,
        external_id int8 unique,
        primary key (id)
    );;

    create table dichroic (
        id int8 not null,
        permissions int8 not null,
        lotNumber varchar(255),
        manufacturer varchar(255),
        model varchar(255),
        serialNumber varchar(255),
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        instrument int8 not null,
        primary key (id)
    );;

    create table dichroicannotationlink (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        primary key (id),
        unique (parent, child, owner_id)
    );;

    create table dimensionorder (
        id int8 not null,
        permissions int8 not null,
        value varchar(255) not null unique,
        external_id int8 unique,
        primary key (id)
    );;

    create table event (
        id int8 not null,
        permissions int8 not null,
        status varchar(255),
        time timestamp not null,
        containingEvent int8,
        external_id int8 unique,
        experimenter int8 not null,
        experimenterGroup int8 not null,
        "session" int8 not null,
        type int8 not null,
        primary key (id)
    );;

    create table eventlog (
        id int8 not null,
        action varchar(255) not null,
        permissions int8 not null,
        entityId int8 not null,
        entityType varchar(255) not null,
        external_id int8 unique,
        event int8 not null,
        primary key (id)
    );;

    create table eventtype (
        id int8 not null,
        permissions int8 not null,
        value varchar(255) not null unique,
        external_id int8 unique,
        primary key (id)
    );;

    create table experiment (
        id int8 not null,
        description text,
        permissions int8 not null,
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        type int8 not null,
        primary key (id)
    );;

    create table experimenter (
        id int8 not null,
        permissions int8 not null,
        email varchar(255),
        firstName varchar(255) not null,
        institution varchar(255),
        lastName varchar(255) not null,
        ldap bool not null,
        middleName varchar(255),
        omeName varchar(255) not null unique,
        version int4,
        external_id int8 unique,
        primary key (id)
    );;

    create table experimenterannotationlink (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        primary key (id),
        unique (parent, child, owner_id)
    );;

    create table experimentergroup (
        id int8 not null,
        description text,
        permissions int8 not null,
        ldap bool not null,
        name varchar(255) not null unique,
        version int4,
        external_id int8 unique,
        primary key (id)
    );;

    create table experimentergroup_config (
        experimentergroup_id int8 not null,
        name varchar(255) not null,
        value varchar(255) not null,
        index int4 not null,
        primary key (experimentergroup_id, index)
    );;

    create table experimentergroupannotationlink (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        primary key (id),
        unique (parent, child, owner_id)
    );;

    create table experimenttype (
        id int8 not null,
        permissions int8 not null,
        value varchar(255) not null unique,
        external_id int8 unique,
        primary key (id)
    );;

    create table externalinfo (
        id int8 not null,
        permissions int8 not null,
        entityId int8 not null,
        entityType varchar(255) not null,
        lsid varchar(255),
        uuid varchar(255),
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        primary key (id)
    );;

    create table family (
        id int8 not null,
        permissions int8 not null,
        value varchar(255) not null unique,
        external_id int8 unique,
        primary key (id)
    );;

    create table filament (
        lightsource_id int8 not null,
        type int8 not null,
        primary key (lightsource_id)
    );;

    create table filamenttype (
        id int8 not null,
        permissions int8 not null,
        value varchar(255) not null unique,
        external_id int8 unique,
        primary key (id)
    );;

    create table fileset (
        id int8 not null,
        permissions int8 not null,
        templatePrefix text not null,
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        primary key (id)
    );;

    create table filesetannotationlink (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        primary key (id),
        unique (parent, child, owner_id)
    );;

    create table filesetentry (
        id int8 not null,
        clientPath text not null,
        permissions int8 not null,
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        fileset int8 not null,
        originalFile int8 not null,
        fileset_index int4 not null,
        primary key (id),
        unique (fileset, fileset_index)
    );;

    create table filesetjoblink (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        parent_index int4 not null,
        primary key (id),
        unique (parent, parent_index),
        unique (parent, child, owner_id)
    );;

    create table filter (
        id int8 not null,
        permissions int8 not null,
        filterWheel varchar(255),
        lotNumber varchar(255),
        manufacturer varchar(255),
        model varchar(255),
        serialNumber varchar(255),
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        instrument int8 not null,
        transmittanceRange int8,
        type int8,
        primary key (id)
    );;

    create table filterannotationlink (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        primary key (id),
        unique (parent, child, owner_id)
    );;

    create table filterset (
        id int8 not null,
        permissions int8 not null,
        lotNumber varchar(255),
        manufacturer varchar(255),
        model varchar(255),
        serialNumber varchar(255),
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        dichroic int8,
        instrument int8 not null,
        primary key (id)
    );;

    create table filtersetemissionfilterlink (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        primary key (id),
        unique (parent, child, owner_id)
    );;

    create table filtersetexcitationfilterlink (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        primary key (id),
        unique (parent, child, owner_id)
    );;

    create table filtertype (
        id int8 not null,
        permissions int8 not null,
        value varchar(255) not null unique,
        external_id int8 unique,
        primary key (id)
    );;

    create table format (
        id int8 not null,
        permissions int8 not null,
        value varchar(255) not null unique,
        external_id int8 unique,
        primary key (id)
    );;

    create table genericexcitationsource (
        lightsource_id int8 not null,
        primary key (lightsource_id)
    );;

    create table genericexcitationsource_map (
        genericexcitationsource_id int8 not null,
        name varchar(255) not null,
        value varchar(255) not null,
        index int4 not null,
        primary key (genericexcitationsource_id, index)
    );;

    create table groupexperimentermap (
        id int8 not null,
        permissions int8 not null,
        owner bool not null,
        version int4,
        child int8 not null,
        external_id int8 unique,
        parent int8 not null,
        child_index int4 not null,
        primary key (id),
        unique (parent, child),
        unique (child, child_index)
    );;

    create table illumination (
        id int8 not null,
        permissions int8 not null,
        value varchar(255) not null unique,
        external_id int8 unique,
        primary key (id)
    );;

    create table image (
        id int8 not null,
        acquisitionDate timestamp,
        archived bool,
        description text,
        permissions int8 not null,
        name varchar(255) not null,
        partial bool,
        series nonnegative_int,
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        experiment int8,
        fileset int8,
        format int8,
        imagingEnvironment int8,
        instrument int8,
        objectiveSettings int8,
        stageLabel int8,
        primary key (id)
    );;

    create table imageannotationlink (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        primary key (id),
        unique (parent, child, owner_id)
    );;

    create table imagingenvironment (
        id int8 not null,
        airPressureUnit UnitsPressure,
        airPressure float8,
        co2percent percent_fraction,
        permissions int8 not null,
        humidity percent_fraction,
        temperatureUnit UnitsTemperature,
        temperature float8,
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        primary key (id)
    );;

    create table imagingenvironment_map (
        imagingenvironment_id int8 not null,
        name varchar(255) not null,
        value varchar(255) not null,
        index int4 not null,
        primary key (imagingenvironment_id, index)
    );;

    create table immersion (
        id int8 not null,
        permissions int8 not null,
        value varchar(255) not null unique,
        external_id int8 unique,
        primary key (id)
    );;

    create table importjob (
        imageDescription varchar(255) not null,
        imageName varchar(255) not null,
        job_id int8 not null,
        primary key (job_id)
    );;

    create table indexingjob (
        job_id int8 not null,
        primary key (job_id)
    );;

    create table instrument (
        id int8 not null,
        permissions int8 not null,
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        microscope int8,
        primary key (id)
    );;

    create table instrumentannotationlink (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        primary key (id),
        unique (parent, child, owner_id)
    );;

    create table integritycheckjob (
        job_id int8 not null,
        primary key (job_id)
    );;

    create table job (
        id int8 not null,
        permissions int8 not null,
        finished timestamp,
        groupname varchar(255) not null,
        message varchar(255) not null,
        scheduledFor timestamp not null,
        started timestamp,
        submitted timestamp not null,
        type varchar(255) not null,
        username varchar(255) not null,
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        status int8 not null,
        primary key (id)
    );;

    create table joboriginalfilelink (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        primary key (id),
        unique (parent, child, owner_id)
    );;

    create table jobstatus (
        id int8 not null,
        permissions int8 not null,
        value varchar(255) not null unique,
        external_id int8 unique,
        primary key (id)
    );;

    create table laser (
        frequencyMultiplication positive_int,
        pockelCell bool,
        repetitionRateUnit UnitsFrequency,
        repetitionRate float8,
        tuneable bool,
        wavelengthUnit UnitsLength,
        wavelength float8,
        lightsource_id int8 not null,
        laserMedium int8 not null,
        pulse int8,
        pump int8,
        type int8 not null,
        primary key (lightsource_id)
    );;

    create table lasermedium (
        id int8 not null,
        permissions int8 not null,
        value varchar(255) not null unique,
        external_id int8 unique,
        primary key (id)
    );;

    create table lasertype (
        id int8 not null,
        permissions int8 not null,
        value varchar(255) not null unique,
        external_id int8 unique,
        primary key (id)
    );;

    create table lightemittingdiode (
        lightsource_id int8 not null,
        primary key (lightsource_id)
    );;

    create table lightpath (
        id int8 not null,
        permissions int8 not null,
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        dichroic int8,
        primary key (id)
    );;

    create table lightpathannotationlink (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        primary key (id),
        unique (parent, child, owner_id)
    );;

    create table lightpathemissionfilterlink (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        primary key (id),
        unique (parent, child, owner_id)
    );;

    create table lightpathexcitationfilterlink (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        parent_index int4 not null,
        primary key (id),
        unique (parent, parent_index),
        unique (parent, child, owner_id)
    );;

    create table lightsettings (
        id int8 not null,
        attenuation percent_fraction,
        permissions int8 not null,
        version int4,
        wavelengthUnit UnitsLength,
        wavelength float8,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        lightSource int8 not null,
        microbeamManipulation int8,
        primary key (id)
    );;

    create table lightsource (
        id int8 not null,
        permissions int8 not null,
        lotNumber varchar(255),
        manufacturer varchar(255),
        model varchar(255),
        powerUnit UnitsPower,
        "power" float8,
        serialNumber varchar(255),
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        instrument int8 not null,
        primary key (id)
    );;

    create table lightsourceannotationlink (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        primary key (id),
        unique (parent, child, owner_id)
    );;

    create table link (
        id int8 not null,
        permissions int8 not null,
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        primary key (id)
    );;

    create table logicalchannel (
        id int8 not null,
        permissions int8 not null,
        emissionWaveUnit UnitsLength,
        emissionWave float8,
        excitationWaveUnit UnitsLength,
        excitationWave float8,
        fluor varchar(255),
        name varchar(255),
        ndFilter float8,
        pinHoleSizeUnit UnitsLength,
        pinHoleSize float8,
        pockelCellSetting int4,
        samplesPerPixel positive_int,
        version int4,
        contrastMethod int8,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        detectorSettings int8,
        filterSet int8,
        illumination int8,
        lightPath int8,
        lightSourceSettings int8,
        "mode" int8,
        otf int8,
        photometricInterpretation int8,
        primary key (id)
    );;

    create table medium (
        id int8 not null,
        permissions int8 not null,
        value varchar(255) not null unique,
        external_id int8 unique,
        primary key (id)
    );;

    create table metadataimportjob (
        job_id int8 not null,
        primary key (job_id)
    );;

    create table metadataimportjob_versionInfo (
        metadataimportjob_id int8 not null,
        name varchar(255) not null,
        value varchar(255) not null,
        index int4 not null,
        primary key (metadataimportjob_id, index)
    );;

    create table microbeammanipulation (
        id int8 not null,
        description text,
        permissions int8 not null,
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        experiment int8 not null,
        type int8 not null,
        primary key (id)
    );;

    create table microbeammanipulationtype (
        id int8 not null,
        permissions int8 not null,
        value varchar(255) not null unique,
        external_id int8 unique,
        primary key (id)
    );;

    create table microscope (
        id int8 not null,
        permissions int8 not null,
        lotNumber varchar(255),
        manufacturer varchar(255),
        model varchar(255),
        serialNumber varchar(255),
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        type int8 not null,
        primary key (id)
    );;

    create table microscopetype (
        id int8 not null,
        permissions int8 not null,
        value varchar(255) not null unique,
        external_id int8 unique,
        primary key (id)
    );;

    create table namespace (
        id int8 not null,
        description text,
        permissions int8 not null,
        display bool,
        displayName varchar(255),
        keywords text[],
        multivalued bool,
        name varchar(255) not null,
        version int4,
        external_id int8 unique,
        primary key (id)
    );;

    create table namespaceannotationlink (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        primary key (id),
        unique (parent, child, owner_id)
    );;

    create table node (
        id int8 not null,
        conn text not null,
        permissions int8 not null,
        down timestamp,
        scale int4,
        up timestamp not null,
        uuid varchar(255) not null unique,
        version int4,
        external_id int8 unique,
        primary key (id)
    );;

    create table nodeannotationlink (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        primary key (id),
        unique (parent, child, owner_id)
    );;

    create table objective (
        id int8 not null,
        calibratedMagnification float8,
        permissions int8 not null,
        iris bool,
        lensNA float8,
        lotNumber varchar(255),
        manufacturer varchar(255),
        model varchar(255),
        nominalMagnification float8,
        serialNumber varchar(255),
        version int4,
        workingDistanceUnit UnitsLength,
        workingDistance float8,
        correction int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        immersion int8 not null,
        instrument int8 not null,
        primary key (id)
    );;

    create table objectiveannotationlink (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        primary key (id),
        unique (parent, child, owner_id)
    );;

    create table objectivesettings (
        id int8 not null,
        correctionCollar float8,
        permissions int8 not null,
        refractiveIndex float8,
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        medium int8,
        objective int8 not null,
        primary key (id)
    );;

    create table originalfile (
        id int8 not null,
        atime timestamp,
        ctime timestamp,
        permissions int8 not null,
        hash varchar(255),
        mimetype varchar(255),
        mtime timestamp,
        name varchar(255) not null,
        path text not null,
        "size" int8,
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        hasher int8,
        primary key (id)
    );;

    create table originalfileannotationlink (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        primary key (id),
        unique (parent, child, owner_id)
    );;

    create table otf (
        id int8 not null,
        permissions int8 not null,
        opticalAxisAveraged bool not null,
        path varchar(255) not null,
        sizeX positive_int not null,
        sizeY positive_int not null,
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        filterSet int8,
        instrument int8 not null,
        objective int8 not null,
        pixelsType int8 not null,
        primary key (id)
    );;

    create table parsejob (
        params bytea,
        job_id int8 not null,
        primary key (job_id)
    );;

    create table photometricinterpretation (
        id int8 not null,
        permissions int8 not null,
        value varchar(255) not null unique,
        external_id int8 unique,
        primary key (id)
    );;

    create table pixeldatajob (
        job_id int8 not null,
        primary key (job_id)
    );;

    create table pixels (
        id int8 not null,
        permissions int8 not null,
        methodology varchar(255),
        physicalSizeXUnit UnitsLength,
        physicalSizeX float8,
        physicalSizeYUnit UnitsLength,
        physicalSizeY float8,
        physicalSizeZUnit UnitsLength,
        physicalSizeZ float8,
        sha1 varchar(255) not null,
        significantBits positive_int,
        sizeC positive_int not null,
        sizeT positive_int not null,
        sizeX positive_int not null,
        sizeY positive_int not null,
        sizeZ positive_int not null,
        timeIncrementUnit UnitsTime,
        timeIncrement float8,
        version int4,
        waveIncrement int4,
        waveStart int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        dimensionOrder int8 not null,
        image int8 not null,
        pixelsType int8 not null,
        relatedTo int8,
        image_index int4 not null,
        primary key (id),
        unique (image, image_index)
    );;

    create table pixelsoriginalfilemap (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        primary key (id),
        unique (parent, child, owner_id)
    );;

    create table pixelstype (
        id int8 not null,
        bitSize int4,
        permissions int8 not null,
        value varchar(255) not null unique,
        external_id int8 unique,
        primary key (id)
    );;

    create table planeinfo (
        id int8 not null,
        deltaTUnit UnitsTime,
        deltaT float8,
        permissions int8 not null,
        exposureTimeUnit UnitsTime,
        exposureTime float8,
        positionXUnit UnitsLength,
        positionX float8,
        positionYUnit UnitsLength,
        positionY float8,
        positionZUnit UnitsLength,
        positionZ float8,
        theC nonnegative_int not null,
        theT nonnegative_int not null,
        theZ nonnegative_int not null,
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        pixels int8 not null,
        primary key (id)
    );;

    create table planeinfoannotationlink (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        primary key (id),
        unique (parent, child, owner_id)
    );;

    create table planeslicingcontext (
        "constant" bool not null,
        lowerLimit int4 not null,
        planePrevious int4 not null,
        planeSelected int4 not null,
        upperLimit int4 not null,
        codomainmapcontext_id int8 not null,
        primary key (codomainmapcontext_id)
    );;

    create table plate (
        id int8 not null,
        columnNamingConvention varchar(255),
        columns int4,
        defaultSample int4,
        description text,
        permissions int8 not null,
        externalIdentifier varchar(255),
        name varchar(255) not null,
        rowNamingConvention varchar(255),
        "rows" int4,
        status varchar(255),
        version int4,
        wellOriginXUnit UnitsLength,
        wellOriginX float8,
        wellOriginYUnit UnitsLength,
        wellOriginY float8,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        primary key (id)
    );;

    create table plateacquisition (
        id int8 not null,
        description text,
        permissions int8 not null,
        endTime timestamp,
        maximumFieldCount int4,
        name varchar(255),
        startTime timestamp,
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        plate int8 not null,
        primary key (id)
    );;

    create table plateacquisitionannotationlink (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        primary key (id),
        unique (parent, child, owner_id)
    );;

    create table plateannotationlink (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        primary key (id),
        unique (parent, child, owner_id)
    );;

    create table project (
        id int8 not null,
        description text,
        permissions int8 not null,
        name varchar(255) not null,
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        primary key (id)
    );;

    create table projectannotationlink (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        primary key (id),
        unique (parent, child, owner_id)
    );;

    create table projectdatasetlink (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        primary key (id),
        unique (parent, child, owner_id)
    );;

    create table pulse (
        id int8 not null,
        permissions int8 not null,
        value varchar(255) not null unique,
        external_id int8 unique,
        primary key (id)
    );;

    create table quantumdef (
        id int8 not null,
        bitResolution int4 not null,
        cdEnd int4 not null,
        cdStart int4 not null,
        permissions int8 not null,
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        primary key (id)
    );;

    create table reagent (
        id int8 not null,
        description text,
        permissions int8 not null,
        name varchar(255),
        reagentIdentifier varchar(255),
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        screen int8 not null,
        primary key (id)
    );;

    create table reagentannotationlink (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        primary key (id),
        unique (parent, child, owner_id)
    );;

    create table renderingdef (
        id int8 not null,
        compression float8,
        defaultT int4 not null,
        defaultZ int4 not null,
        permissions int8 not null,
        name varchar(255),
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        model int8 not null,
        pixels int8 not null,
        quantization int8 not null,
        primary key (id)
    );;

    create table renderingmodel (
        id int8 not null,
        permissions int8 not null,
        value varchar(255) not null unique,
        external_id int8 unique,
        primary key (id)
    );;

    create table reverseintensitycontext (
        "reverse" bool not null,
        codomainmapcontext_id int8 not null,
        primary key (codomainmapcontext_id)
    );;

    create table roi (
        id int8 not null,
        description text,
        permissions int8 not null,
        keywords text[][],
        name varchar(255),
        namespaces text[],
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        image int8,
        source int8,
        primary key (id)
    );;

    create table roiannotationlink (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        primary key (id),
        unique (parent, child, owner_id)
    );;

    create table screen (
        id int8 not null,
        description text,
        permissions int8 not null,
        name varchar(255) not null,
        protocolDescription varchar(255),
        protocolIdentifier varchar(255),
        reagentSetDescription varchar(255),
        reagentSetIdentifier varchar(255),
        type varchar(255),
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        primary key (id)
    );;

    create table screenannotationlink (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        primary key (id),
        unique (parent, child, owner_id)
    );;

    create table screenplatelink (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        primary key (id),
        unique (parent, child, owner_id)
    );;

    create table scriptjob (
        description varchar(255),
        job_id int8 not null,
        primary key (job_id)
    );;

    create table session (
        id int8 not null,
        closed timestamp,
        defaultEventType varchar(255) not null,
        permissions int8 not null,
        message text,
        started timestamp not null,
        timeToIdle int8 not null,
        timeToLive int8 not null,
        userAgent varchar(255),
        userIP varchar(255),
        uuid varchar(255) not null unique,
        version int4,
        external_id int8 unique,
        node int8 not null,
        owner int8 not null,
        primary key (id)
    );;

    create table sessionannotationlink (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        primary key (id),
        unique (parent, child, owner_id)
    );;

    create table shape (
        discriminator varchar(31) not null,
        id int8 not null,
        permissions int8 not null,
        fillColor int4,
        fillRule varchar(255),
        fontFamily varchar(255),
        fontSizeUnit UnitsLength,
        fontSize float8,
        fontStretch varchar(255),
        fontStyle varchar(255),
        fontVariant varchar(255),
        fontWeight varchar(255),
        locked bool,
        strokeColor int4,
        strokeDashArray varchar(255),
        strokeLineCap varchar(255),
        strokeWidthUnit UnitsLength,
        strokeWidth float8,
        theC int4,
        theT int4,
        theZ int4,
        transform varchar(255),
        vectorEffect varchar(255),
        version int4,
        cx float8,
        cy float8,
        rx float8,
        ry float8,
        textValue text,
        anchor varchar(255),
        baselineShift varchar(255),
        decoration varchar(255),
        direction varchar(255),
        glyphOrientationVertical int4,
        writingMode varchar(255),
        x float8,
        y float8,
        x1 float8,
        x2 float8,
        y1 float8,
        y2 float8,
        bytes bytea,
        height float8,
        width float8,
        d text,
        points text,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        roi int8 not null,
        pixels int8,
        roi_index int4 not null,
        primary key (id),
        unique (roi, roi_index)
    );;

    create table shapeannotationlink (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        primary key (id),
        unique (parent, child, owner_id)
    );;

    create table share (
        active bool not null,
        data bytea not null,
        itemCount int8 not null,
        session_id int8 not null,
        "group" int8 not null,
        primary key (session_id)
    );;

    create table sharemember (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        external_id int8 unique,
        parent int8 not null,
        primary key (id),
        unique (parent, child)
    );;

    create table stagelabel (
        id int8 not null,
        permissions int8 not null,
        name varchar(255) not null,
        positionXUnit UnitsLength,
        positionX float8,
        positionYUnit UnitsLength,
        positionY float8,
        positionZUnit UnitsLength,
        positionZ float8,
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        primary key (id)
    );;

    create table statsinfo (
        id int8 not null,
        permissions int8 not null,
        globalMax float8 not null,
        globalMin float8 not null,
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        primary key (id)
    );;

    create table thumbnail (
        id int8 not null,
        permissions int8 not null,
        mimeType varchar(255) not null,
        "ref" varchar(255),
        sizeX int4 not null,
        sizeY int4 not null,
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        pixels int8 not null,
        primary key (id)
    );;

    create table thumbnailgenerationjob (
        job_id int8 not null,
        primary key (job_id)
    );;

    create table transmittancerange (
        id int8 not null,
        cutInUnit UnitsLength,
        cutIn float8,
        cutInToleranceUnit UnitsLength,
        cutInTolerance float8,
        cutOutUnit UnitsLength,
        cutOut float8,
        cutOutToleranceUnit UnitsLength,
        cutOutTolerance float8,
        permissions int8 not null,
        transmittance percent_fraction,
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        primary key (id)
    );;

    create table uploadjob (
        job_id int8 not null,
        primary key (job_id)
    );;

    create table uploadjob_versionInfo (
        uploadjob_id int8 not null,
        name varchar(255) not null,
        value varchar(255) not null,
        index int4 not null,
        primary key (uploadjob_id, index)
    );;

    create table well (
        id int8 not null,
        alpha int4,
        blue int4,
        "column" int4,
        permissions int8 not null,
        externalDescription varchar(255),
        externalIdentifier varchar(255),
        green int4,
        red int4,
        "row" int4,
        status varchar(255),
        type varchar(255),
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        plate int8 not null,
        primary key (id)
    );;

    create table wellannotationlink (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        primary key (id),
        unique (parent, child, owner_id)
    );;

    create table wellreagentlink (
        id int8 not null,
        permissions int8 not null,
        version int4,
        child int8 not null,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        parent int8 not null,
        primary key (id),
        unique (parent, child, owner_id)
    );;

    create table wellsample (
        id int8 not null,
        permissions int8 not null,
        posXUnit UnitsLength,
        posX float8,
        posYUnit UnitsLength,
        posY float8,
        timepoint timestamp,
        version int4,
        creation_id int8 not null,
        external_id int8 unique,
        group_id int8 not null,
        owner_id int8 not null,
        update_id int8 not null,
        image int8 not null,
        plateAcquisition int8,
        well int8 not null,
        well_index int4 not null,
        primary key (id),
        unique (well, well_index)
    );;

    alter table acquisitionmode 
        add constraint FKacquisitionmode_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table annotation 
        add constraint FKannotation_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table annotation 
        add constraint FKannotation_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table annotation 
        add constraint FKannotation_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table annotation 
        add constraint FKfileannotation_file_originalfile 
        foreign key ("file") 
        references originalfile  ;;

    alter table annotation 
        add constraint FKannotation_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table annotation 
        add constraint FKannotation_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table annotation_mapValue 
        add constraint FKannotation_mapValue_map 
        foreign key (annotation_id) 
        references annotation  ;;

    alter table annotationannotationlink 
        add constraint FKannotationannotationlink_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table annotationannotationlink 
        add constraint FKannotationannotationlink_child_annotation 
        foreign key (child) 
        references annotation  ;;

    alter table annotationannotationlink 
        add constraint FKannotationannotationlink_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table annotationannotationlink 
        add constraint FKannotationannotationlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table annotationannotationlink 
        add constraint FKannotationannotationlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table annotationannotationlink 
        add constraint FKannotationannotationlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table annotationannotationlink 
        add constraint FKannotationannotationlink_parent_annotation 
        foreign key (parent) 
        references annotation  ;;

    alter table arc 
        add constraint FKarc_lightsource_id_lightsource 
        foreign key (lightsource_id) 
        references lightsource  ;;

    alter table arc 
        add constraint FKarc_type_arctype 
        foreign key (type) 
        references arctype  ;;

    alter table arctype 
        add constraint FKarctype_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table binning 
        add constraint FKbinning_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table channel 
        add constraint FKchannel_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table channel 
        add constraint FKchannel_logicalChannel_logicalchannel 
        foreign key (logicalChannel) 
        references logicalchannel  ;;

    alter table channel 
        add constraint FKchannel_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table channel 
        add constraint FKchannel_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table channel 
        add constraint FKchannel_statsInfo_statsinfo 
        foreign key (statsInfo) 
        references statsinfo  ;;

    alter table channel 
        add constraint FKchannel_pixels_pixels 
        foreign key (pixels) 
        references pixels  ;;

    alter table channel 
        add constraint FKchannel_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table channel 
        add constraint FKchannel_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table channelannotationlink 
        add constraint FKchannelannotationlink_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table channelannotationlink 
        add constraint FKchannelannotationlink_child_annotation 
        foreign key (child) 
        references annotation  ;;

    alter table channelannotationlink 
        add constraint FKchannelannotationlink_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table channelannotationlink 
        add constraint FKchannelannotationlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table channelannotationlink 
        add constraint FKchannelannotationlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table channelannotationlink 
        add constraint FKchannelannotationlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table channelannotationlink 
        add constraint FKchannelannotationlink_parent_channel 
        foreign key (parent) 
        references channel  ;;

    alter table channelbinding 
        add constraint FKchannelbinding_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table channelbinding 
        add constraint FKchannelbinding_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table channelbinding 
        add constraint FKchannelbinding_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table channelbinding 
        add constraint FKchannelbinding_family_family 
        foreign key (family) 
        references family  ;;

    alter table channelbinding 
        add constraint FKchannelbinding_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table channelbinding 
        add constraint FKchannelbinding_renderingDef_renderingdef 
        foreign key (renderingDef) 
        references renderingdef  ;;

    alter table channelbinding 
        add constraint FKchannelbinding_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table checksumalgorithm 
        add constraint FKchecksumalgorithm_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table codomainmapcontext 
        add constraint FKcodomainmapcontext_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table codomainmapcontext 
        add constraint FKcodomainmapcontext_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table codomainmapcontext 
        add constraint FKcodomainmapcontext_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table codomainmapcontext 
        add constraint FKcodomainmapcontext_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table codomainmapcontext 
        add constraint FKcodomainmapcontext_renderingDef_renderingdef 
        foreign key (renderingDef) 
        references renderingdef  ;;

    alter table codomainmapcontext 
        add constraint FKcodomainmapcontext_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table contrastmethod 
        add constraint FKcontrastmethod_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table contraststretchingcontext 
        add constraint FKcontraststretchingcontext_codomainmapcontext_id_codomainmapcontext 
        foreign key (codomainmapcontext_id) 
        references codomainmapcontext  ;;

    alter table correction 
        add constraint FKcorrection_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table count_Annotation_annotationLinks_by_owner 
        add constraint FK_count_to_Annotation_annotationLinks 
        foreign key (Annotation_id) 
        references annotation  ;;

    alter table count_Channel_annotationLinks_by_owner 
        add constraint FK_count_to_Channel_annotationLinks 
        foreign key (Channel_id) 
        references channel  ;;

    alter table count_Dataset_annotationLinks_by_owner 
        add constraint FK_count_to_Dataset_annotationLinks 
        foreign key (Dataset_id) 
        references dataset  ;;

    alter table count_Dataset_imageLinks_by_owner 
        add constraint FK_count_to_Dataset_imageLinks 
        foreign key (Dataset_id) 
        references dataset  ;;

    alter table count_Dataset_projectLinks_by_owner 
        add constraint FK_count_to_Dataset_projectLinks 
        foreign key (Dataset_id) 
        references dataset  ;;

    alter table count_Detector_annotationLinks_by_owner 
        add constraint FK_count_to_Detector_annotationLinks 
        foreign key (Detector_id) 
        references detector  ;;

    alter table count_Dichroic_annotationLinks_by_owner 
        add constraint FK_count_to_Dichroic_annotationLinks 
        foreign key (Dichroic_id) 
        references dichroic  ;;

    alter table count_ExperimenterGroup_annotationLinks_by_owner 
        add constraint FK_count_to_ExperimenterGroup_annotationLinks 
        foreign key (ExperimenterGroup_id) 
        references experimentergroup  ;;

    alter table count_Experimenter_annotationLinks_by_owner 
        add constraint FK_count_to_Experimenter_annotationLinks 
        foreign key (Experimenter_id) 
        references experimenter  ;;

    alter table count_Fileset_annotationLinks_by_owner 
        add constraint FK_count_to_Fileset_annotationLinks 
        foreign key (Fileset_id) 
        references fileset  ;;

    alter table count_Fileset_jobLinks_by_owner 
        add constraint FK_count_to_Fileset_jobLinks 
        foreign key (Fileset_id) 
        references fileset  ;;

    alter table count_FilterSet_emissionFilterLink_by_owner 
        add constraint FK_count_to_FilterSet_emissionFilterLink 
        foreign key (FilterSet_id) 
        references filterset  ;;

    alter table count_FilterSet_excitationFilterLink_by_owner 
        add constraint FK_count_to_FilterSet_excitationFilterLink 
        foreign key (FilterSet_id) 
        references filterset  ;;

    alter table count_Filter_annotationLinks_by_owner 
        add constraint FK_count_to_Filter_annotationLinks 
        foreign key (Filter_id) 
        references filter  ;;

    alter table count_Filter_emissionFilterLink_by_owner 
        add constraint FK_count_to_Filter_emissionFilterLink 
        foreign key (Filter_id) 
        references filter  ;;

    alter table count_Filter_excitationFilterLink_by_owner 
        add constraint FK_count_to_Filter_excitationFilterLink 
        foreign key (Filter_id) 
        references filter  ;;

    alter table count_Image_annotationLinks_by_owner 
        add constraint FK_count_to_Image_annotationLinks 
        foreign key (Image_id) 
        references image  ;;

    alter table count_Image_datasetLinks_by_owner 
        add constraint FK_count_to_Image_datasetLinks 
        foreign key (Image_id) 
        references image  ;;

    alter table count_Instrument_annotationLinks_by_owner 
        add constraint FK_count_to_Instrument_annotationLinks 
        foreign key (Instrument_id) 
        references instrument  ;;

    alter table count_Job_originalFileLinks_by_owner 
        add constraint FK_count_to_Job_originalFileLinks 
        foreign key (Job_id) 
        references job  ;;

    alter table count_LightPath_annotationLinks_by_owner 
        add constraint FK_count_to_LightPath_annotationLinks 
        foreign key (LightPath_id) 
        references lightpath  ;;

    alter table count_LightPath_emissionFilterLink_by_owner 
        add constraint FK_count_to_LightPath_emissionFilterLink 
        foreign key (LightPath_id) 
        references lightpath  ;;

    alter table count_LightPath_excitationFilterLink_by_owner 
        add constraint FK_count_to_LightPath_excitationFilterLink 
        foreign key (LightPath_id) 
        references lightpath  ;;

    alter table count_LightSource_annotationLinks_by_owner 
        add constraint FK_count_to_LightSource_annotationLinks 
        foreign key (LightSource_id) 
        references lightsource  ;;

    alter table count_Namespace_annotationLinks_by_owner 
        add constraint FK_count_to_Namespace_annotationLinks 
        foreign key (Namespace_id) 
        references namespace  ;;

    alter table count_Node_annotationLinks_by_owner 
        add constraint FK_count_to_Node_annotationLinks 
        foreign key (Node_id) 
        references node  ;;

    alter table count_Objective_annotationLinks_by_owner 
        add constraint FK_count_to_Objective_annotationLinks 
        foreign key (Objective_id) 
        references objective  ;;

    alter table count_OriginalFile_annotationLinks_by_owner 
        add constraint FK_count_to_OriginalFile_annotationLinks 
        foreign key (OriginalFile_id) 
        references originalfile  ;;

    alter table count_OriginalFile_pixelsFileMaps_by_owner 
        add constraint FK_count_to_OriginalFile_pixelsFileMaps 
        foreign key (OriginalFile_id) 
        references originalfile  ;;

    alter table count_Pixels_pixelsFileMaps_by_owner 
        add constraint FK_count_to_Pixels_pixelsFileMaps 
        foreign key (Pixels_id) 
        references pixels  ;;

    alter table count_PlaneInfo_annotationLinks_by_owner 
        add constraint FK_count_to_PlaneInfo_annotationLinks 
        foreign key (PlaneInfo_id) 
        references planeinfo  ;;

    alter table count_PlateAcquisition_annotationLinks_by_owner 
        add constraint FK_count_to_PlateAcquisition_annotationLinks 
        foreign key (PlateAcquisition_id) 
        references plateacquisition  ;;

    alter table count_Plate_annotationLinks_by_owner 
        add constraint FK_count_to_Plate_annotationLinks 
        foreign key (Plate_id) 
        references plate  ;;

    alter table count_Plate_screenLinks_by_owner 
        add constraint FK_count_to_Plate_screenLinks 
        foreign key (Plate_id) 
        references plate  ;;

    alter table count_Project_annotationLinks_by_owner 
        add constraint FK_count_to_Project_annotationLinks 
        foreign key (Project_id) 
        references project  ;;

    alter table count_Project_datasetLinks_by_owner 
        add constraint FK_count_to_Project_datasetLinks 
        foreign key (Project_id) 
        references project  ;;

    alter table count_Reagent_annotationLinks_by_owner 
        add constraint FK_count_to_Reagent_annotationLinks 
        foreign key (Reagent_id) 
        references reagent  ;;

    alter table count_Reagent_wellLinks_by_owner 
        add constraint FK_count_to_Reagent_wellLinks 
        foreign key (Reagent_id) 
        references reagent  ;;

    alter table count_Roi_annotationLinks_by_owner 
        add constraint FK_count_to_Roi_annotationLinks 
        foreign key (Roi_id) 
        references roi  ;;

    alter table count_Screen_annotationLinks_by_owner 
        add constraint FK_count_to_Screen_annotationLinks 
        foreign key (Screen_id) 
        references screen  ;;

    alter table count_Screen_plateLinks_by_owner 
        add constraint FK_count_to_Screen_plateLinks 
        foreign key (Screen_id) 
        references screen  ;;

    alter table count_Session_annotationLinks_by_owner 
        add constraint FK_count_to_Session_annotationLinks 
        foreign key (Session_id) 
        references session  ;;

    alter table count_Shape_annotationLinks_by_owner 
        add constraint FK_count_to_Shape_annotationLinks 
        foreign key (Shape_id) 
        references shape  ;;

    alter table count_Well_annotationLinks_by_owner 
        add constraint FK_count_to_Well_annotationLinks 
        foreign key (Well_id) 
        references well  ;;

    alter table count_Well_reagentLinks_by_owner 
        add constraint FK_count_to_Well_reagentLinks 
        foreign key (Well_id) 
        references well  ;;

    alter table dataset 
        add constraint FKdataset_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table dataset 
        add constraint FKdataset_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table dataset 
        add constraint FKdataset_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table dataset 
        add constraint FKdataset_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table dataset 
        add constraint FKdataset_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table datasetannotationlink 
        add constraint FKdatasetannotationlink_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table datasetannotationlink 
        add constraint FKdatasetannotationlink_child_annotation 
        foreign key (child) 
        references annotation  ;;

    alter table datasetannotationlink 
        add constraint FKdatasetannotationlink_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table datasetannotationlink 
        add constraint FKdatasetannotationlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table datasetannotationlink 
        add constraint FKdatasetannotationlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table datasetannotationlink 
        add constraint FKdatasetannotationlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table datasetannotationlink 
        add constraint FKdatasetannotationlink_parent_dataset 
        foreign key (parent) 
        references dataset  ;;

    alter table datasetimagelink 
        add constraint FKdatasetimagelink_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table datasetimagelink 
        add constraint FKdatasetimagelink_child_image 
        foreign key (child) 
        references image  ;;

    alter table datasetimagelink 
        add constraint FKdatasetimagelink_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table datasetimagelink 
        add constraint FKdatasetimagelink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table datasetimagelink 
        add constraint FKdatasetimagelink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table datasetimagelink 
        add constraint FKdatasetimagelink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table datasetimagelink 
        add constraint FKdatasetimagelink_parent_dataset 
        foreign key (parent) 
        references dataset  ;;

    alter table dbpatch 
        add constraint FKdbpatch_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table detector 
        add constraint FKdetector_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table detector 
        add constraint FKdetector_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table detector 
        add constraint FKdetector_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table detector 
        add constraint FKdetector_type_detectortype 
        foreign key (type) 
        references detectortype  ;;

    alter table detector 
        add constraint FKdetector_instrument_instrument 
        foreign key (instrument) 
        references instrument  ;;

    alter table detector 
        add constraint FKdetector_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table detector 
        add constraint FKdetector_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table detectorannotationlink 
        add constraint FKdetectorannotationlink_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table detectorannotationlink 
        add constraint FKdetectorannotationlink_child_annotation 
        foreign key (child) 
        references annotation  ;;

    alter table detectorannotationlink 
        add constraint FKdetectorannotationlink_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table detectorannotationlink 
        add constraint FKdetectorannotationlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table detectorannotationlink 
        add constraint FKdetectorannotationlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table detectorannotationlink 
        add constraint FKdetectorannotationlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table detectorannotationlink 
        add constraint FKdetectorannotationlink_parent_detector 
        foreign key (parent) 
        references detector  ;;

    alter table detectorsettings 
        add constraint FKdetectorsettings_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table detectorsettings 
        add constraint FKdetectorsettings_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table detectorsettings 
        add constraint FKdetectorsettings_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table detectorsettings 
        add constraint FKdetectorsettings_binning_binning 
        foreign key (binning) 
        references binning  ;;

    alter table detectorsettings 
        add constraint FKdetectorsettings_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table detectorsettings 
        add constraint FKdetectorsettings_detector_detector 
        foreign key (detector) 
        references detector  ;;

    alter table detectorsettings 
        add constraint FKdetectorsettings_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table detectortype 
        add constraint FKdetectortype_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table dichroic 
        add constraint FKdichroic_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table dichroic 
        add constraint FKdichroic_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table dichroic 
        add constraint FKdichroic_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table dichroic 
        add constraint FKdichroic_instrument_instrument 
        foreign key (instrument) 
        references instrument  ;;

    alter table dichroic 
        add constraint FKdichroic_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table dichroic 
        add constraint FKdichroic_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table dichroicannotationlink 
        add constraint FKdichroicannotationlink_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table dichroicannotationlink 
        add constraint FKdichroicannotationlink_child_annotation 
        foreign key (child) 
        references annotation  ;;

    alter table dichroicannotationlink 
        add constraint FKdichroicannotationlink_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table dichroicannotationlink 
        add constraint FKdichroicannotationlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table dichroicannotationlink 
        add constraint FKdichroicannotationlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table dichroicannotationlink 
        add constraint FKdichroicannotationlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table dichroicannotationlink 
        add constraint FKdichroicannotationlink_parent_dichroic 
        foreign key (parent) 
        references dichroic  ;;

    alter table dimensionorder 
        add constraint FKdimensionorder_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table event 
        add constraint FKevent_experimenterGroup_experimentergroup 
        foreign key (experimenterGroup) 
        references experimentergroup  ;;

    alter table event 
        add constraint FKevent_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table event 
        add constraint FKevent_session_session 
        foreign key ("session") 
        references session  ;;

    alter table event 
        add constraint FKevent_containingEvent_event 
        foreign key (containingEvent) 
        references event  ;;

    alter table event 
        add constraint FKevent_type_eventtype 
        foreign key (type) 
        references eventtype  ;;

    alter table event 
        add constraint FKevent_experimenter_experimenter 
        foreign key (experimenter) 
        references experimenter  ;;

    alter table eventlog 
        add constraint FKeventlog_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table eventlog 
        add constraint FKeventlog_event_event 
        foreign key (event) 
        references event  ;;

    alter table eventtype 
        add constraint FKeventtype_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table experiment 
        add constraint FKexperiment_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table experiment 
        add constraint FKexperiment_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table experiment 
        add constraint FKexperiment_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table experiment 
        add constraint FKexperiment_type_experimenttype 
        foreign key (type) 
        references experimenttype  ;;

    alter table experiment 
        add constraint FKexperiment_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table experiment 
        add constraint FKexperiment_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table experimenter 
        add constraint FKexperimenter_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table experimenterannotationlink 
        add constraint FKexperimenterannotationlink_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table experimenterannotationlink 
        add constraint FKexperimenterannotationlink_child_annotation 
        foreign key (child) 
        references annotation  ;;

    alter table experimenterannotationlink 
        add constraint FKexperimenterannotationlink_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table experimenterannotationlink 
        add constraint FKexperimenterannotationlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table experimenterannotationlink 
        add constraint FKexperimenterannotationlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table experimenterannotationlink 
        add constraint FKexperimenterannotationlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table experimenterannotationlink 
        add constraint FKexperimenterannotationlink_parent_experimenter 
        foreign key (parent) 
        references experimenter  ;;

    alter table experimentergroup 
        add constraint FKexperimentergroup_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table experimentergroup_config 
        add constraint FKexperimentergroup_config_map 
        foreign key (experimentergroup_id) 
        references experimentergroup  ;;

    alter table experimentergroupannotationlink 
        add constraint FKexperimentergroupannotationlink_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table experimentergroupannotationlink 
        add constraint FKexperimentergroupannotationlink_child_annotation 
        foreign key (child) 
        references annotation  ;;

    alter table experimentergroupannotationlink 
        add constraint FKexperimentergroupannotationlink_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table experimentergroupannotationlink 
        add constraint FKexperimentergroupannotationlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table experimentergroupannotationlink 
        add constraint FKexperimentergroupannotationlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table experimentergroupannotationlink 
        add constraint FKexperimentergroupannotationlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table experimentergroupannotationlink 
        add constraint FKexperimentergroupannotationlink_parent_experimentergroup 
        foreign key (parent) 
        references experimentergroup  ;;

    alter table experimenttype 
        add constraint FKexperimenttype_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table externalinfo 
        add constraint FKexternalinfo_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table externalinfo 
        add constraint FKexternalinfo_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table externalinfo 
        add constraint FKexternalinfo_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table externalinfo 
        add constraint FKexternalinfo_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table family 
        add constraint FKfamily_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table filament 
        add constraint FKfilament_lightsource_id_lightsource 
        foreign key (lightsource_id) 
        references lightsource  ;;

    alter table filament 
        add constraint FKfilament_type_filamenttype 
        foreign key (type) 
        references filamenttype  ;;

    alter table filamenttype 
        add constraint FKfilamenttype_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table fileset 
        add constraint FKfileset_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table fileset 
        add constraint FKfileset_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table fileset 
        add constraint FKfileset_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table fileset 
        add constraint FKfileset_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table fileset 
        add constraint FKfileset_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table filesetannotationlink 
        add constraint FKfilesetannotationlink_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table filesetannotationlink 
        add constraint FKfilesetannotationlink_child_annotation 
        foreign key (child) 
        references annotation  ;;

    alter table filesetannotationlink 
        add constraint FKfilesetannotationlink_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table filesetannotationlink 
        add constraint FKfilesetannotationlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table filesetannotationlink 
        add constraint FKfilesetannotationlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table filesetannotationlink 
        add constraint FKfilesetannotationlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table filesetannotationlink 
        add constraint FKfilesetannotationlink_parent_fileset 
        foreign key (parent) 
        references fileset  ;;

    alter table filesetentry 
        add constraint FKfilesetentry_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table filesetentry 
        add constraint FKfilesetentry_originalFile_originalfile 
        foreign key (originalFile) 
        references originalfile  ;;

    alter table filesetentry 
        add constraint FKfilesetentry_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table filesetentry 
        add constraint FKfilesetentry_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table filesetentry 
        add constraint FKfilesetentry_fileset_fileset 
        foreign key (fileset) 
        references fileset  ;;

    alter table filesetentry 
        add constraint FKfilesetentry_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table filesetentry 
        add constraint FKfilesetentry_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table filesetjoblink 
        add constraint FKfilesetjoblink_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table filesetjoblink 
        add constraint FKfilesetjoblink_child_job 
        foreign key (child) 
        references job  ;;

    alter table filesetjoblink 
        add constraint FKfilesetjoblink_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table filesetjoblink 
        add constraint FKfilesetjoblink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table filesetjoblink 
        add constraint FKfilesetjoblink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table filesetjoblink 
        add constraint FKfilesetjoblink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table filesetjoblink 
        add constraint FKfilesetjoblink_parent_fileset 
        foreign key (parent) 
        references fileset  ;;

    alter table filter 
        add constraint FKfilter_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table filter 
        add constraint FKfilter_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table filter 
        add constraint FKfilter_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table filter 
        add constraint FKfilter_transmittanceRange_transmittancerange 
        foreign key (transmittanceRange) 
        references transmittancerange  ;;

    alter table filter 
        add constraint FKfilter_type_filtertype 
        foreign key (type) 
        references filtertype  ;;

    alter table filter 
        add constraint FKfilter_instrument_instrument 
        foreign key (instrument) 
        references instrument  ;;

    alter table filter 
        add constraint FKfilter_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table filter 
        add constraint FKfilter_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table filterannotationlink 
        add constraint FKfilterannotationlink_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table filterannotationlink 
        add constraint FKfilterannotationlink_child_annotation 
        foreign key (child) 
        references annotation  ;;

    alter table filterannotationlink 
        add constraint FKfilterannotationlink_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table filterannotationlink 
        add constraint FKfilterannotationlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table filterannotationlink 
        add constraint FKfilterannotationlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table filterannotationlink 
        add constraint FKfilterannotationlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table filterannotationlink 
        add constraint FKfilterannotationlink_parent_filter 
        foreign key (parent) 
        references filter  ;;

    alter table filterset 
        add constraint FKfilterset_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table filterset 
        add constraint FKfilterset_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table filterset 
        add constraint FKfilterset_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table filterset 
        add constraint FKfilterset_instrument_instrument 
        foreign key (instrument) 
        references instrument  ;;

    alter table filterset 
        add constraint FKfilterset_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table filterset 
        add constraint FKfilterset_dichroic_dichroic 
        foreign key (dichroic) 
        references dichroic  ;;

    alter table filterset 
        add constraint FKfilterset_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table filtersetemissionfilterlink 
        add constraint FKfiltersetemissionfilterlink_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table filtersetemissionfilterlink 
        add constraint FKfiltersetemissionfilterlink_child_filter 
        foreign key (child) 
        references filter  ;;

    alter table filtersetemissionfilterlink 
        add constraint FKfiltersetemissionfilterlink_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table filtersetemissionfilterlink 
        add constraint FKfiltersetemissionfilterlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table filtersetemissionfilterlink 
        add constraint FKfiltersetemissionfilterlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table filtersetemissionfilterlink 
        add constraint FKfiltersetemissionfilterlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table filtersetemissionfilterlink 
        add constraint FKfiltersetemissionfilterlink_parent_filterset 
        foreign key (parent) 
        references filterset  ;;

    alter table filtersetexcitationfilterlink 
        add constraint FKfiltersetexcitationfilterlink_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table filtersetexcitationfilterlink 
        add constraint FKfiltersetexcitationfilterlink_child_filter 
        foreign key (child) 
        references filter  ;;

    alter table filtersetexcitationfilterlink 
        add constraint FKfiltersetexcitationfilterlink_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table filtersetexcitationfilterlink 
        add constraint FKfiltersetexcitationfilterlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table filtersetexcitationfilterlink 
        add constraint FKfiltersetexcitationfilterlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table filtersetexcitationfilterlink 
        add constraint FKfiltersetexcitationfilterlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table filtersetexcitationfilterlink 
        add constraint FKfiltersetexcitationfilterlink_parent_filterset 
        foreign key (parent) 
        references filterset  ;;

    alter table filtertype 
        add constraint FKfiltertype_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table format 
        add constraint FKformat_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table genericexcitationsource 
        add constraint FKgenericexcitationsource_lightsource_id_lightsource 
        foreign key (lightsource_id) 
        references lightsource  ;;

    alter table genericexcitationsource_map 
        add constraint FKgenericexcitationsource_map_map 
        foreign key (genericexcitationsource_id) 
        references genericexcitationsource  ;;

    alter table groupexperimentermap 
        add constraint FKgroupexperimentermap_child_experimenter 
        foreign key (child) 
        references experimenter  ;;

    alter table groupexperimentermap 
        add constraint FKgroupexperimentermap_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table groupexperimentermap 
        add constraint FKgroupexperimentermap_parent_experimentergroup 
        foreign key (parent) 
        references experimentergroup  ;;

    alter table illumination 
        add constraint FKillumination_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table image 
        add constraint FKimage_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table image 
        add constraint FKimage_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table image 
        add constraint FKimage_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table image 
        add constraint FKimage_imagingEnvironment_imagingenvironment 
        foreign key (imagingEnvironment) 
        references imagingenvironment  ;;

    alter table image 
        add constraint FKimage_experiment_experiment 
        foreign key (experiment) 
        references experiment  ;;

    alter table image 
        add constraint FKimage_instrument_instrument 
        foreign key (instrument) 
        references instrument  ;;

    alter table image 
        add constraint FKimage_format_format 
        foreign key (format) 
        references format  ;;

    alter table image 
        add constraint FKimage_fileset_fileset 
        foreign key (fileset) 
        references fileset  ;;

    alter table image 
        add constraint FKimage_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table image 
        add constraint FKimage_stageLabel_stagelabel 
        foreign key (stageLabel) 
        references stagelabel  ;;

    alter table image 
        add constraint FKimage_objectiveSettings_objectivesettings 
        foreign key (objectiveSettings) 
        references objectivesettings  ;;

    alter table image 
        add constraint FKimage_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table imageannotationlink 
        add constraint FKimageannotationlink_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table imageannotationlink 
        add constraint FKimageannotationlink_child_annotation 
        foreign key (child) 
        references annotation  ;;

    alter table imageannotationlink 
        add constraint FKimageannotationlink_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table imageannotationlink 
        add constraint FKimageannotationlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table imageannotationlink 
        add constraint FKimageannotationlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table imageannotationlink 
        add constraint FKimageannotationlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table imageannotationlink 
        add constraint FKimageannotationlink_parent_image 
        foreign key (parent) 
        references image  ;;

    alter table imagingenvironment 
        add constraint FKimagingenvironment_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table imagingenvironment 
        add constraint FKimagingenvironment_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table imagingenvironment 
        add constraint FKimagingenvironment_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table imagingenvironment 
        add constraint FKimagingenvironment_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table imagingenvironment 
        add constraint FKimagingenvironment_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table imagingenvironment_map 
        add constraint FKimagingenvironment_map_map 
        foreign key (imagingenvironment_id) 
        references imagingenvironment  ;;

    alter table immersion 
        add constraint FKimmersion_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table importjob 
        add constraint FKimportjob_job_id_job 
        foreign key (job_id) 
        references job  ;;

    alter table indexingjob 
        add constraint FKindexingjob_job_id_job 
        foreign key (job_id) 
        references job  ;;

    alter table instrument 
        add constraint FKinstrument_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table instrument 
        add constraint FKinstrument_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table instrument 
        add constraint FKinstrument_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table instrument 
        add constraint FKinstrument_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table instrument 
        add constraint FKinstrument_microscope_microscope 
        foreign key (microscope) 
        references microscope  ;;

    alter table instrument 
        add constraint FKinstrument_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table instrumentannotationlink 
        add constraint FKinstrumentannotationlink_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table instrumentannotationlink 
        add constraint FKinstrumentannotationlink_child_annotation 
        foreign key (child) 
        references annotation  ;;

    alter table instrumentannotationlink 
        add constraint FKinstrumentannotationlink_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table instrumentannotationlink 
        add constraint FKinstrumentannotationlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table instrumentannotationlink 
        add constraint FKinstrumentannotationlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table instrumentannotationlink 
        add constraint FKinstrumentannotationlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table instrumentannotationlink 
        add constraint FKinstrumentannotationlink_parent_instrument 
        foreign key (parent) 
        references instrument  ;;

    alter table integritycheckjob 
        add constraint FKintegritycheckjob_job_id_job 
        foreign key (job_id) 
        references job  ;;

    alter table job 
        add constraint FKjob_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table job 
        add constraint FKjob_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table job 
        add constraint FKjob_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table job 
        add constraint FKjob_status_jobstatus 
        foreign key (status) 
        references jobstatus  ;;

    alter table job 
        add constraint FKjob_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table job 
        add constraint FKjob_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table joboriginalfilelink 
        add constraint FKjoboriginalfilelink_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table joboriginalfilelink 
        add constraint FKjoboriginalfilelink_child_originalfile 
        foreign key (child) 
        references originalfile  ;;

    alter table joboriginalfilelink 
        add constraint FKjoboriginalfilelink_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table joboriginalfilelink 
        add constraint FKjoboriginalfilelink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table joboriginalfilelink 
        add constraint FKjoboriginalfilelink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table joboriginalfilelink 
        add constraint FKjoboriginalfilelink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table joboriginalfilelink 
        add constraint FKjoboriginalfilelink_parent_job 
        foreign key (parent) 
        references job  ;;

    alter table jobstatus 
        add constraint FKjobstatus_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table laser 
        add constraint FKlaser_lightsource_id_lightsource 
        foreign key (lightsource_id) 
        references lightsource  ;;

    alter table laser 
        add constraint FKlaser_pump_lightsource 
        foreign key (pump) 
        references lightsource  ;;

    alter table laser 
        add constraint FKlaser_laserMedium_lasermedium 
        foreign key (laserMedium) 
        references lasermedium  ;;

    alter table laser 
        add constraint FKlaser_pulse_pulse 
        foreign key (pulse) 
        references pulse  ;;

    alter table laser 
        add constraint FKlaser_type_lasertype 
        foreign key (type) 
        references lasertype  ;;

    alter table lasermedium 
        add constraint FKlasermedium_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table lasertype 
        add constraint FKlasertype_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table lightemittingdiode 
        add constraint FKlightemittingdiode_lightsource_id_lightsource 
        foreign key (lightsource_id) 
        references lightsource  ;;

    alter table lightpath 
        add constraint FKlightpath_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table lightpath 
        add constraint FKlightpath_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table lightpath 
        add constraint FKlightpath_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table lightpath 
        add constraint FKlightpath_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table lightpath 
        add constraint FKlightpath_dichroic_dichroic 
        foreign key (dichroic) 
        references dichroic  ;;

    alter table lightpath 
        add constraint FKlightpath_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table lightpathannotationlink 
        add constraint FKlightpathannotationlink_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table lightpathannotationlink 
        add constraint FKlightpathannotationlink_child_annotation 
        foreign key (child) 
        references annotation  ;;

    alter table lightpathannotationlink 
        add constraint FKlightpathannotationlink_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table lightpathannotationlink 
        add constraint FKlightpathannotationlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table lightpathannotationlink 
        add constraint FKlightpathannotationlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table lightpathannotationlink 
        add constraint FKlightpathannotationlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table lightpathannotationlink 
        add constraint FKlightpathannotationlink_parent_lightpath 
        foreign key (parent) 
        references lightpath  ;;

    alter table lightpathemissionfilterlink 
        add constraint FKlightpathemissionfilterlink_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table lightpathemissionfilterlink 
        add constraint FKlightpathemissionfilterlink_child_filter 
        foreign key (child) 
        references filter  ;;

    alter table lightpathemissionfilterlink 
        add constraint FKlightpathemissionfilterlink_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table lightpathemissionfilterlink 
        add constraint FKlightpathemissionfilterlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table lightpathemissionfilterlink 
        add constraint FKlightpathemissionfilterlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table lightpathemissionfilterlink 
        add constraint FKlightpathemissionfilterlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table lightpathemissionfilterlink 
        add constraint FKlightpathemissionfilterlink_parent_lightpath 
        foreign key (parent) 
        references lightpath  ;;

    alter table lightpathexcitationfilterlink 
        add constraint FKlightpathexcitationfilterlink_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table lightpathexcitationfilterlink 
        add constraint FKlightpathexcitationfilterlink_child_filter 
        foreign key (child) 
        references filter  ;;

    alter table lightpathexcitationfilterlink 
        add constraint FKlightpathexcitationfilterlink_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table lightpathexcitationfilterlink 
        add constraint FKlightpathexcitationfilterlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table lightpathexcitationfilterlink 
        add constraint FKlightpathexcitationfilterlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table lightpathexcitationfilterlink 
        add constraint FKlightpathexcitationfilterlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table lightpathexcitationfilterlink 
        add constraint FKlightpathexcitationfilterlink_parent_lightpath 
        foreign key (parent) 
        references lightpath  ;;

    alter table lightsettings 
        add constraint FKlightsettings_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table lightsettings 
        add constraint FKlightsettings_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table lightsettings 
        add constraint FKlightsettings_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table lightsettings 
        add constraint FKlightsettings_lightSource_lightsource 
        foreign key (lightSource) 
        references lightsource  ;;

    alter table lightsettings 
        add constraint FKlightsettings_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table lightsettings 
        add constraint FKlightsettings_microbeamManipulation_microbeammanipulation 
        foreign key (microbeamManipulation) 
        references microbeammanipulation  ;;

    alter table lightsettings 
        add constraint FKlightsettings_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table lightsource 
        add constraint FKlightsource_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table lightsource 
        add constraint FKlightsource_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table lightsource 
        add constraint FKlightsource_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table lightsource 
        add constraint FKlightsource_instrument_instrument 
        foreign key (instrument) 
        references instrument  ;;

    alter table lightsource 
        add constraint FKlightsource_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table lightsource 
        add constraint FKlightsource_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table lightsourceannotationlink 
        add constraint FKlightsourceannotationlink_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table lightsourceannotationlink 
        add constraint FKlightsourceannotationlink_child_annotation 
        foreign key (child) 
        references annotation  ;;

    alter table lightsourceannotationlink 
        add constraint FKlightsourceannotationlink_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table lightsourceannotationlink 
        add constraint FKlightsourceannotationlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table lightsourceannotationlink 
        add constraint FKlightsourceannotationlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table lightsourceannotationlink 
        add constraint FKlightsourceannotationlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table lightsourceannotationlink 
        add constraint FKlightsourceannotationlink_parent_lightsource 
        foreign key (parent) 
        references lightsource  ;;

    alter table link 
        add constraint FKlink_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table link 
        add constraint FKlink_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table link 
        add constraint FKlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table link 
        add constraint FKlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table link 
        add constraint FKlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table logicalchannel 
        add constraint FKlogicalchannel_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table logicalchannel 
        add constraint FKlogicalchannel_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table logicalchannel 
        add constraint FKlogicalchannel_photometricInterpretation_photometricinterpretation 
        foreign key (photometricInterpretation) 
        references photometricinterpretation  ;;

    alter table logicalchannel 
        add constraint FKlogicalchannel_lightPath_lightpath 
        foreign key (lightPath) 
        references lightpath  ;;

    alter table logicalchannel 
        add constraint FKlogicalchannel_mode_acquisitionmode 
        foreign key ("mode") 
        references acquisitionmode  ;;

    alter table logicalchannel 
        add constraint FKlogicalchannel_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table logicalchannel 
        add constraint FKlogicalchannel_otf_otf 
        foreign key (otf) 
        references otf  ;;

    alter table logicalchannel 
        add constraint FKlogicalchannel_lightSourceSettings_lightsettings 
        foreign key (lightSourceSettings) 
        references lightsettings  ;;

    alter table logicalchannel 
        add constraint FKlogicalchannel_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table logicalchannel 
        add constraint FKlogicalchannel_filterSet_filterset 
        foreign key (filterSet) 
        references filterset  ;;

    alter table logicalchannel 
        add constraint FKlogicalchannel_detectorSettings_detectorsettings 
        foreign key (detectorSettings) 
        references detectorsettings  ;;

    alter table logicalchannel 
        add constraint FKlogicalchannel_contrastMethod_contrastmethod 
        foreign key (contrastMethod) 
        references contrastmethod  ;;

    alter table logicalchannel 
        add constraint FKlogicalchannel_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table logicalchannel 
        add constraint FKlogicalchannel_illumination_illumination 
        foreign key (illumination) 
        references illumination  ;;

    alter table medium 
        add constraint FKmedium_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table metadataimportjob 
        add constraint FKmetadataimportjob_job_id_job 
        foreign key (job_id) 
        references job  ;;

    alter table metadataimportjob_versionInfo 
        add constraint FKmetadataimportjob_versionInfo_map 
        foreign key (metadataimportjob_id) 
        references metadataimportjob  ;;

    alter table microbeammanipulation 
        add constraint FKmicrobeammanipulation_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table microbeammanipulation 
        add constraint FKmicrobeammanipulation_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table microbeammanipulation 
        add constraint FKmicrobeammanipulation_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table microbeammanipulation 
        add constraint FKmicrobeammanipulation_experiment_experiment 
        foreign key (experiment) 
        references experiment  ;;

    alter table microbeammanipulation 
        add constraint FKmicrobeammanipulation_type_microbeammanipulationtype 
        foreign key (type) 
        references microbeammanipulationtype  ;;

    alter table microbeammanipulation 
        add constraint FKmicrobeammanipulation_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table microbeammanipulation 
        add constraint FKmicrobeammanipulation_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table microbeammanipulationtype 
        add constraint FKmicrobeammanipulationtype_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table microscope 
        add constraint FKmicroscope_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table microscope 
        add constraint FKmicroscope_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table microscope 
        add constraint FKmicroscope_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table microscope 
        add constraint FKmicroscope_type_microscopetype 
        foreign key (type) 
        references microscopetype  ;;

    alter table microscope 
        add constraint FKmicroscope_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table microscope 
        add constraint FKmicroscope_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table microscopetype 
        add constraint FKmicroscopetype_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table namespace 
        add constraint FKnamespace_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table namespaceannotationlink 
        add constraint FKnamespaceannotationlink_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table namespaceannotationlink 
        add constraint FKnamespaceannotationlink_child_annotation 
        foreign key (child) 
        references annotation  ;;

    alter table namespaceannotationlink 
        add constraint FKnamespaceannotationlink_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table namespaceannotationlink 
        add constraint FKnamespaceannotationlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table namespaceannotationlink 
        add constraint FKnamespaceannotationlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table namespaceannotationlink 
        add constraint FKnamespaceannotationlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table namespaceannotationlink 
        add constraint FKnamespaceannotationlink_parent_namespace 
        foreign key (parent) 
        references namespace  ;;

    alter table node 
        add constraint FKnode_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table nodeannotationlink 
        add constraint FKnodeannotationlink_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table nodeannotationlink 
        add constraint FKnodeannotationlink_child_annotation 
        foreign key (child) 
        references annotation  ;;

    alter table nodeannotationlink 
        add constraint FKnodeannotationlink_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table nodeannotationlink 
        add constraint FKnodeannotationlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table nodeannotationlink 
        add constraint FKnodeannotationlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table nodeannotationlink 
        add constraint FKnodeannotationlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table nodeannotationlink 
        add constraint FKnodeannotationlink_parent_node 
        foreign key (parent) 
        references node  ;;

    alter table objective 
        add constraint FKobjective_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table objective 
        add constraint FKobjective_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table objective 
        add constraint FKobjective_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table objective 
        add constraint FKobjective_immersion_immersion 
        foreign key (immersion) 
        references immersion  ;;

    alter table objective 
        add constraint FKobjective_instrument_instrument 
        foreign key (instrument) 
        references instrument  ;;

    alter table objective 
        add constraint FKobjective_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table objective 
        add constraint FKobjective_correction_correction 
        foreign key (correction) 
        references correction  ;;

    alter table objective 
        add constraint FKobjective_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table objectiveannotationlink 
        add constraint FKobjectiveannotationlink_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table objectiveannotationlink 
        add constraint FKobjectiveannotationlink_child_annotation 
        foreign key (child) 
        references annotation  ;;

    alter table objectiveannotationlink 
        add constraint FKobjectiveannotationlink_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table objectiveannotationlink 
        add constraint FKobjectiveannotationlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table objectiveannotationlink 
        add constraint FKobjectiveannotationlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table objectiveannotationlink 
        add constraint FKobjectiveannotationlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table objectiveannotationlink 
        add constraint FKobjectiveannotationlink_parent_objective 
        foreign key (parent) 
        references objective  ;;

    alter table objectivesettings 
        add constraint FKobjectivesettings_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table objectivesettings 
        add constraint FKobjectivesettings_medium_medium 
        foreign key (medium) 
        references medium  ;;

    alter table objectivesettings 
        add constraint FKobjectivesettings_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table objectivesettings 
        add constraint FKobjectivesettings_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table objectivesettings 
        add constraint FKobjectivesettings_objective_objective 
        foreign key (objective) 
        references objective  ;;

    alter table objectivesettings 
        add constraint FKobjectivesettings_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table objectivesettings 
        add constraint FKobjectivesettings_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table originalfile 
        add constraint FKoriginalfile_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table originalfile 
        add constraint FKoriginalfile_hasher_checksumalgorithm 
        foreign key (hasher) 
        references checksumalgorithm  ;;

    alter table originalfile 
        add constraint FKoriginalfile_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table originalfile 
        add constraint FKoriginalfile_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table originalfile 
        add constraint FKoriginalfile_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table originalfile 
        add constraint FKoriginalfile_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table originalfileannotationlink 
        add constraint FKoriginalfileannotationlink_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table originalfileannotationlink 
        add constraint FKoriginalfileannotationlink_child_annotation 
        foreign key (child) 
        references annotation  ;;

    alter table originalfileannotationlink 
        add constraint FKoriginalfileannotationlink_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table originalfileannotationlink 
        add constraint FKoriginalfileannotationlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table originalfileannotationlink 
        add constraint FKoriginalfileannotationlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table originalfileannotationlink 
        add constraint FKoriginalfileannotationlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table originalfileannotationlink 
        add constraint FKoriginalfileannotationlink_parent_originalfile 
        foreign key (parent) 
        references originalfile  ;;

    alter table otf 
        add constraint FKotf_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table otf 
        add constraint FKotf_filterSet_filterset 
        foreign key (filterSet) 
        references filterset  ;;

    alter table otf 
        add constraint FKotf_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table otf 
        add constraint FKotf_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table otf 
        add constraint FKotf_objective_objective 
        foreign key (objective) 
        references objective  ;;

    alter table otf 
        add constraint FKotf_instrument_instrument 
        foreign key (instrument) 
        references instrument  ;;

    alter table otf 
        add constraint FKotf_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table otf 
        add constraint FKotf_pixelsType_pixelstype 
        foreign key (pixelsType) 
        references pixelstype  ;;

    alter table otf 
        add constraint FKotf_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table parsejob 
        add constraint FKparsejob_job_id_job 
        foreign key (job_id) 
        references job  ;;

    alter table photometricinterpretation 
        add constraint FKphotometricinterpretation_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table pixeldatajob 
        add constraint FKpixeldatajob_job_id_job 
        foreign key (job_id) 
        references job  ;;

    alter table pixels 
        add constraint FKpixels_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table pixels 
        add constraint FKpixels_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table pixels 
        add constraint FKpixels_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table pixels 
        add constraint FKpixels_dimensionOrder_dimensionorder 
        foreign key (dimensionOrder) 
        references dimensionorder  ;;

    alter table pixels 
        add constraint FKpixels_relatedTo_pixels 
        foreign key (relatedTo) 
        references pixels  ;;

    alter table pixels 
        add constraint FKpixels_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table pixels 
        add constraint FKpixels_image_image 
        foreign key (image) 
        references image  ;;

    alter table pixels 
        add constraint FKpixels_pixelsType_pixelstype 
        foreign key (pixelsType) 
        references pixelstype  ;;

    alter table pixels 
        add constraint FKpixels_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table pixelsoriginalfilemap 
        add constraint FKpixelsoriginalfilemap_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table pixelsoriginalfilemap 
        add constraint FKpixelsoriginalfilemap_child_pixels 
        foreign key (child) 
        references pixels  ;;

    alter table pixelsoriginalfilemap 
        add constraint FKpixelsoriginalfilemap_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table pixelsoriginalfilemap 
        add constraint FKpixelsoriginalfilemap_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table pixelsoriginalfilemap 
        add constraint FKpixelsoriginalfilemap_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table pixelsoriginalfilemap 
        add constraint FKpixelsoriginalfilemap_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table pixelsoriginalfilemap 
        add constraint FKpixelsoriginalfilemap_parent_originalfile 
        foreign key (parent) 
        references originalfile  ;;

    alter table pixelstype 
        add constraint FKpixelstype_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table planeinfo 
        add constraint FKplaneinfo_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table planeinfo 
        add constraint FKplaneinfo_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table planeinfo 
        add constraint FKplaneinfo_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table planeinfo 
        add constraint FKplaneinfo_pixels_pixels 
        foreign key (pixels) 
        references pixels  ;;

    alter table planeinfo 
        add constraint FKplaneinfo_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table planeinfo 
        add constraint FKplaneinfo_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table planeinfoannotationlink 
        add constraint FKplaneinfoannotationlink_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table planeinfoannotationlink 
        add constraint FKplaneinfoannotationlink_child_annotation 
        foreign key (child) 
        references annotation  ;;

    alter table planeinfoannotationlink 
        add constraint FKplaneinfoannotationlink_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table planeinfoannotationlink 
        add constraint FKplaneinfoannotationlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table planeinfoannotationlink 
        add constraint FKplaneinfoannotationlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table planeinfoannotationlink 
        add constraint FKplaneinfoannotationlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table planeinfoannotationlink 
        add constraint FKplaneinfoannotationlink_parent_planeinfo 
        foreign key (parent) 
        references planeinfo  ;;

    alter table planeslicingcontext 
        add constraint FKplaneslicingcontext_codomainmapcontext_id_codomainmapcontext 
        foreign key (codomainmapcontext_id) 
        references codomainmapcontext  ;;

    alter table plate 
        add constraint FKplate_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table plate 
        add constraint FKplate_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table plate 
        add constraint FKplate_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table plate 
        add constraint FKplate_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table plate 
        add constraint FKplate_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table plateacquisition 
        add constraint FKplateacquisition_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table plateacquisition 
        add constraint FKplateacquisition_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table plateacquisition 
        add constraint FKplateacquisition_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table plateacquisition 
        add constraint FKplateacquisition_plate_plate 
        foreign key (plate) 
        references plate  ;;

    alter table plateacquisition 
        add constraint FKplateacquisition_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table plateacquisition 
        add constraint FKplateacquisition_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table plateacquisitionannotationlink 
        add constraint FKplateacquisitionannotationlink_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table plateacquisitionannotationlink 
        add constraint FKplateacquisitionannotationlink_child_annotation 
        foreign key (child) 
        references annotation  ;;

    alter table plateacquisitionannotationlink 
        add constraint FKplateacquisitionannotationlink_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table plateacquisitionannotationlink 
        add constraint FKplateacquisitionannotationlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table plateacquisitionannotationlink 
        add constraint FKplateacquisitionannotationlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table plateacquisitionannotationlink 
        add constraint FKplateacquisitionannotationlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table plateacquisitionannotationlink 
        add constraint FKplateacquisitionannotationlink_parent_plateacquisition 
        foreign key (parent) 
        references plateacquisition  ;;

    alter table plateannotationlink 
        add constraint FKplateannotationlink_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table plateannotationlink 
        add constraint FKplateannotationlink_child_annotation 
        foreign key (child) 
        references annotation  ;;

    alter table plateannotationlink 
        add constraint FKplateannotationlink_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table plateannotationlink 
        add constraint FKplateannotationlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table plateannotationlink 
        add constraint FKplateannotationlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table plateannotationlink 
        add constraint FKplateannotationlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table plateannotationlink 
        add constraint FKplateannotationlink_parent_plate 
        foreign key (parent) 
        references plate  ;;

    alter table project 
        add constraint FKproject_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table project 
        add constraint FKproject_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table project 
        add constraint FKproject_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table project 
        add constraint FKproject_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table project 
        add constraint FKproject_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table projectannotationlink 
        add constraint FKprojectannotationlink_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table projectannotationlink 
        add constraint FKprojectannotationlink_child_annotation 
        foreign key (child) 
        references annotation  ;;

    alter table projectannotationlink 
        add constraint FKprojectannotationlink_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table projectannotationlink 
        add constraint FKprojectannotationlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table projectannotationlink 
        add constraint FKprojectannotationlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table projectannotationlink 
        add constraint FKprojectannotationlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table projectannotationlink 
        add constraint FKprojectannotationlink_parent_project 
        foreign key (parent) 
        references project  ;;

    alter table projectdatasetlink 
        add constraint FKprojectdatasetlink_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table projectdatasetlink 
        add constraint FKprojectdatasetlink_child_dataset 
        foreign key (child) 
        references dataset  ;;

    alter table projectdatasetlink 
        add constraint FKprojectdatasetlink_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table projectdatasetlink 
        add constraint FKprojectdatasetlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table projectdatasetlink 
        add constraint FKprojectdatasetlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table projectdatasetlink 
        add constraint FKprojectdatasetlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table projectdatasetlink 
        add constraint FKprojectdatasetlink_parent_project 
        foreign key (parent) 
        references project  ;;

    alter table pulse 
        add constraint FKpulse_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table quantumdef 
        add constraint FKquantumdef_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table quantumdef 
        add constraint FKquantumdef_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table quantumdef 
        add constraint FKquantumdef_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table quantumdef 
        add constraint FKquantumdef_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table quantumdef 
        add constraint FKquantumdef_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table reagent 
        add constraint FKreagent_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table reagent 
        add constraint FKreagent_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table reagent 
        add constraint FKreagent_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table reagent 
        add constraint FKreagent_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table reagent 
        add constraint FKreagent_screen_screen 
        foreign key (screen) 
        references screen  ;;

    alter table reagent 
        add constraint FKreagent_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table reagentannotationlink 
        add constraint FKreagentannotationlink_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table reagentannotationlink 
        add constraint FKreagentannotationlink_child_annotation 
        foreign key (child) 
        references annotation  ;;

    alter table reagentannotationlink 
        add constraint FKreagentannotationlink_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table reagentannotationlink 
        add constraint FKreagentannotationlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table reagentannotationlink 
        add constraint FKreagentannotationlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table reagentannotationlink 
        add constraint FKreagentannotationlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table reagentannotationlink 
        add constraint FKreagentannotationlink_parent_reagent 
        foreign key (parent) 
        references reagent  ;;

    alter table renderingdef 
        add constraint FKrenderingdef_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table renderingdef 
        add constraint FKrenderingdef_quantization_quantumdef 
        foreign key (quantization) 
        references quantumdef  ;;

    alter table renderingdef 
        add constraint FKrenderingdef_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table renderingdef 
        add constraint FKrenderingdef_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table renderingdef 
        add constraint FKrenderingdef_model_renderingmodel 
        foreign key (model) 
        references renderingmodel  ;;

    alter table renderingdef 
        add constraint FKrenderingdef_pixels_pixels 
        foreign key (pixels) 
        references pixels  ;;

    alter table renderingdef 
        add constraint FKrenderingdef_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table renderingdef 
        add constraint FKrenderingdef_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table renderingmodel 
        add constraint FKrenderingmodel_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table reverseintensitycontext 
        add constraint FKreverseintensitycontext_codomainmapcontext_id_codomainmapcontext 
        foreign key (codomainmapcontext_id) 
        references codomainmapcontext  ;;

    alter table roi 
        add constraint FKroi_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table roi 
        add constraint FKroi_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table roi 
        add constraint FKroi_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table roi 
        add constraint FKroi_source_originalfile 
        foreign key (source) 
        references originalfile  ;;

    alter table roi 
        add constraint FKroi_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table roi 
        add constraint FKroi_image_image 
        foreign key (image) 
        references image  ;;

    alter table roi 
        add constraint FKroi_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table roiannotationlink 
        add constraint FKroiannotationlink_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table roiannotationlink 
        add constraint FKroiannotationlink_child_annotation 
        foreign key (child) 
        references annotation  ;;

    alter table roiannotationlink 
        add constraint FKroiannotationlink_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table roiannotationlink 
        add constraint FKroiannotationlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table roiannotationlink 
        add constraint FKroiannotationlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table roiannotationlink 
        add constraint FKroiannotationlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table roiannotationlink 
        add constraint FKroiannotationlink_parent_roi 
        foreign key (parent) 
        references roi  ;;

    alter table screen 
        add constraint FKscreen_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table screen 
        add constraint FKscreen_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table screen 
        add constraint FKscreen_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table screen 
        add constraint FKscreen_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table screen 
        add constraint FKscreen_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table screenannotationlink 
        add constraint FKscreenannotationlink_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table screenannotationlink 
        add constraint FKscreenannotationlink_child_annotation 
        foreign key (child) 
        references annotation  ;;

    alter table screenannotationlink 
        add constraint FKscreenannotationlink_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table screenannotationlink 
        add constraint FKscreenannotationlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table screenannotationlink 
        add constraint FKscreenannotationlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table screenannotationlink 
        add constraint FKscreenannotationlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table screenannotationlink 
        add constraint FKscreenannotationlink_parent_screen 
        foreign key (parent) 
        references screen  ;;

    alter table screenplatelink 
        add constraint FKscreenplatelink_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table screenplatelink 
        add constraint FKscreenplatelink_child_plate 
        foreign key (child) 
        references plate  ;;

    alter table screenplatelink 
        add constraint FKscreenplatelink_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table screenplatelink 
        add constraint FKscreenplatelink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table screenplatelink 
        add constraint FKscreenplatelink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table screenplatelink 
        add constraint FKscreenplatelink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table screenplatelink 
        add constraint FKscreenplatelink_parent_screen 
        foreign key (parent) 
        references screen  ;;

    alter table scriptjob 
        add constraint FKscriptjob_job_id_job 
        foreign key (job_id) 
        references job  ;;

    alter table session 
        add constraint FKsession_node_node 
        foreign key (node) 
        references node  ;;

    alter table session 
        add constraint FKsession_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table session 
        add constraint FKsession_owner_experimenter 
        foreign key (owner) 
        references experimenter  ;;

    alter table sessionannotationlink 
        add constraint FKsessionannotationlink_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table sessionannotationlink 
        add constraint FKsessionannotationlink_child_annotation 
        foreign key (child) 
        references annotation  ;;

    alter table sessionannotationlink 
        add constraint FKsessionannotationlink_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table sessionannotationlink 
        add constraint FKsessionannotationlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table sessionannotationlink 
        add constraint FKsessionannotationlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table sessionannotationlink 
        add constraint FKsessionannotationlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table sessionannotationlink 
        add constraint FKsessionannotationlink_parent_session 
        foreign key (parent) 
        references session  ;;

    alter table shape 
        add constraint FKshape_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table shape 
        add constraint FKshape_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table shape 
        add constraint FKshape_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table shape 
        add constraint FKmask_pixels_pixels 
        foreign key (pixels) 
        references pixels  ;;

    alter table shape 
        add constraint FKshape_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table shape 
        add constraint FKshape_roi_roi 
        foreign key (roi) 
        references roi  ;;

    alter table shape 
        add constraint FKshape_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table shapeannotationlink 
        add constraint FKshapeannotationlink_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table shapeannotationlink 
        add constraint FKshapeannotationlink_child_annotation 
        foreign key (child) 
        references annotation  ;;

    alter table shapeannotationlink 
        add constraint FKshapeannotationlink_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table shapeannotationlink 
        add constraint FKshapeannotationlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table shapeannotationlink 
        add constraint FKshapeannotationlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table shapeannotationlink 
        add constraint FKshapeannotationlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table shapeannotationlink 
        add constraint FKshapeannotationlink_parent_shape 
        foreign key (parent) 
        references shape  ;;

    alter table share 
        add constraint FKshare_group_experimentergroup 
        foreign key ("group") 
        references experimentergroup  ;;

    alter table share 
        add constraint FKshare_session_id_session 
        foreign key (session_id) 
        references session  ;;

    alter table sharemember 
        add constraint FKsharemember_child_experimenter 
        foreign key (child) 
        references experimenter  ;;

    alter table sharemember 
        add constraint FKsharemember_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table sharemember 
        add constraint FKsharemember_parent_share 
        foreign key (parent) 
        references share  ;;

    alter table stagelabel 
        add constraint FKstagelabel_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table stagelabel 
        add constraint FKstagelabel_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table stagelabel 
        add constraint FKstagelabel_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table stagelabel 
        add constraint FKstagelabel_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table stagelabel 
        add constraint FKstagelabel_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table statsinfo 
        add constraint FKstatsinfo_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table statsinfo 
        add constraint FKstatsinfo_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table statsinfo 
        add constraint FKstatsinfo_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table statsinfo 
        add constraint FKstatsinfo_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table statsinfo 
        add constraint FKstatsinfo_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table thumbnail 
        add constraint FKthumbnail_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table thumbnail 
        add constraint FKthumbnail_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table thumbnail 
        add constraint FKthumbnail_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table thumbnail 
        add constraint FKthumbnail_pixels_pixels 
        foreign key (pixels) 
        references pixels  ;;

    alter table thumbnail 
        add constraint FKthumbnail_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table thumbnail 
        add constraint FKthumbnail_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table thumbnailgenerationjob 
        add constraint FKthumbnailgenerationjob_job_id_job 
        foreign key (job_id) 
        references job  ;;

    alter table transmittancerange 
        add constraint FKtransmittancerange_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table transmittancerange 
        add constraint FKtransmittancerange_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table transmittancerange 
        add constraint FKtransmittancerange_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table transmittancerange 
        add constraint FKtransmittancerange_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table transmittancerange 
        add constraint FKtransmittancerange_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table uploadjob 
        add constraint FKuploadjob_job_id_job 
        foreign key (job_id) 
        references job  ;;

    alter table uploadjob_versionInfo 
        add constraint FKuploadjob_versionInfo_map 
        foreign key (uploadjob_id) 
        references uploadjob  ;;

    alter table well 
        add constraint FKwell_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table well 
        add constraint FKwell_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table well 
        add constraint FKwell_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table well 
        add constraint FKwell_plate_plate 
        foreign key (plate) 
        references plate  ;;

    alter table well 
        add constraint FKwell_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table well 
        add constraint FKwell_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table wellannotationlink 
        add constraint FKwellannotationlink_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table wellannotationlink 
        add constraint FKwellannotationlink_child_annotation 
        foreign key (child) 
        references annotation  ;;

    alter table wellannotationlink 
        add constraint FKwellannotationlink_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table wellannotationlink 
        add constraint FKwellannotationlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table wellannotationlink 
        add constraint FKwellannotationlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table wellannotationlink 
        add constraint FKwellannotationlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table wellannotationlink 
        add constraint FKwellannotationlink_parent_well 
        foreign key (parent) 
        references well  ;;

    alter table wellreagentlink 
        add constraint FKwellreagentlink_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table wellreagentlink 
        add constraint FKwellreagentlink_child_reagent 
        foreign key (child) 
        references reagent  ;;

    alter table wellreagentlink 
        add constraint FKwellreagentlink_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table wellreagentlink 
        add constraint FKwellreagentlink_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table wellreagentlink 
        add constraint FKwellreagentlink_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table wellreagentlink 
        add constraint FKwellreagentlink_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    alter table wellreagentlink 
        add constraint FKwellreagentlink_parent_well 
        foreign key (parent) 
        references well  ;;

    alter table wellsample 
        add constraint FKwellsample_creation_id_event 
        foreign key (creation_id) 
        references event  ;;

    alter table wellsample 
        add constraint FKwellsample_plateAcquisition_plateacquisition 
        foreign key (plateAcquisition) 
        references plateacquisition  ;;

    alter table wellsample 
        add constraint FKwellsample_update_id_event 
        foreign key (update_id) 
        references event  ;;

    alter table wellsample 
        add constraint FKwellsample_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo  ;;

    alter table wellsample 
        add constraint FKwellsample_well_well 
        foreign key (well) 
        references well  ;;

    alter table wellsample 
        add constraint FKwellsample_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup  ;;

    alter table wellsample 
        add constraint FKwellsample_image_image 
        foreign key (image) 
        references image  ;;

    alter table wellsample 
        add constraint FKwellsample_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter  ;;

    create table seq_table (
         sequence_name varchar(255) not null ,
         next_val int8,
        primary key ( sequence_name ) 
    ) ;;
