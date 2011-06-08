--
-- Copyright 2011 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

---
--- OMERO-Beta4.3 release upgrade from OMERO4.2__0 to OMERO4.3__0
--- If the upgrade fails to apply due to data checks, see the
--- script omero-4.2-data-fix.sql in the same directory.
---

BEGIN;

\timing

CREATE OR REPLACE FUNCTION omero_43_check_pg_advisory_lock() RETURNS void AS '
DECLARE
    txt text;
BEGIN
      BEGIN
        PERFORM pg_advisory_lock(1, 1);
        PERFORM pg_advisory_unlock(1, 1);
      EXCEPTION
        WHEN undefined_function THEN
         txt := chr(10) ||
            ''====================================================================================='' || chr(10) ||
            ''pg_advisory_lock does not exist!'' || chr(10) || chr(10) ||
            ''You must upgrade to PostgreSQL 8.2 or above'' || chr(10) ||
            ''====================================================================================='' || chr(10) || chr(10);
         -- 8.1 is unsupported starting with OMERO4.3 (See #4902)
         RAISE EXCEPTION ''%%'', txt;
      END;
END;' LANGUAGE plpgsql;
SELECT omero_43_check_pg_advisory_lock();
DROP FUNCTION omero_43_check_pg_advisory_lock();

CREATE OR REPLACE FUNCTION omero_assert_db_version(version varchar, patch int) RETURNS void AS '
DECLARE
    rec RECORD;
BEGIN

    SELECT INTO rec *
           FROM dbpatch
          WHERE id = ( SELECT id FROM dbpatch ORDER BY id DESC LIMIT 1 )
            AND currentversion = version
            AND currentpatch = patch;

    IF NOT FOUND THEN
        RAISE EXCEPTION ''ASSERTION ERROR: Wrong database version'';
    END IF;

END;' LANGUAGE plpgsql;

SELECT omero_assert_db_version('OMERO4.2',0);
DROP FUNCTION omero_assert_db_version(varchar, int);


INSERT into dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
             values ('OMERO4.3',  0,              'OMERO4.2',          0);


-- #2694 Remove pg_geom
CREATE OR REPLACE FUNCTION omero_43_drop_pg_geom() RETURNS void AS '
BEGIN

    IF EXISTS(SELECT column_name FROM information_schema.columns where table_name ilike ''shape'' and column_name ilike ''pg_geom'') THEN
        ALTER TABLE shape DROP COLUMN pg_geom;
    END IF;
END;'
LANGUAGE plpgsql;
SELECT omero_43_drop_pg_geom();
DROP FUNCTION omero_43_drop_pg_geom();

-- #2710 Roi.image optional
ALTER TABLE roi ALTER COLUMN image DROP NOT NULL;

-- #2574 some SPW optional names
ALTER TABLE plateacquisition ALTER COLUMN name DROP NOT NULL;
ALTER TABLE reagent ALTER COLUMN name DROP NOT NULL;

-- #2569 rename Plate.cols to columns
ALTER TABLE plate RENAME COLUMN cols TO columns;

-- #4841 add Well.status
ALTER TABLE well ADD COLUMN status varchar(255);

-- #4822 checks on numeric types
ALTER TABLE imagingenvironment ADD check (humidity >= 0 and humidity <= 1 and co2percent >= 0 and co2percent <= 1);
ALTER TABLE laser ADD check (frequencyMultiplication > 0 and wavelength > 0);
ALTER TABLE lightsettings ADD check (attenuation >= 0 and attenuation <= 1 and wavelength > 0);
ALTER TABLE logicalchannel ADD check (excitationWave > 0 and emissionWave > 0 and samplesPerPixel > 0);
ALTER TABLE objective ADD check (nominalMagnification > 0);
ALTER TABLE otf ADD check (sizeX > 0 and sizeY > 0);
ALTER TABLE pixels ADD check (sizeX > 0 and sizeY > 0 and sizeZ > 0 and sizeC > 0 and sizeT > 0);
ALTER TABLE planeinfo ADD check (theZ >= 0 and theC >= 0 and theT >= 0);
ALTER TABLE transmittancerange ADD check (cutIn > 0 and cutOut > 0 and cutInTolerance >= 0 and cutOutTolerance >= 0 and transmittance >= 0 and transmittance <= 1);

-- #2724 PixelsType.bitSize
ALTER TABLE pixelstype ADD COLUMN bitsize int4;
UPDATE pixelstype SET bitsize = 1 WHERE value = 'bit';
UPDATE pixelstype SET bitsize = 8 WHERE value = 'int8';
UPDATE pixelstype SET bitsize = 8 WHERE value = 'uint8';
UPDATE pixelstype SET bitsize = 16 WHERE value = 'int16';
UPDATE pixelstype SET bitsize = 16 WHERE value = 'uint16';
UPDATE pixelstype SET bitsize = 32 WHERE value = 'int32';
UPDATE pixelstype SET bitsize = 32 WHERE value = 'uint16';
UPDATE pixelstype SET bitsize = 32 WHERE value = 'uint32';
UPDATE pixelstype SET bitsize = 32 WHERE value = 'float';
UPDATE pixelstype SET bitsize = 64 WHERE value = 'double';
UPDATE pixelstype SET bitsize = 64 WHERE value = 'complex';
UPDATE pixelstype SET bitsize = 128 WHERE value = 'double-complex';
ALTER TABLE pixelstype ALTER COLUMN bitsize SET NOT NULL;

-- #4693 lotNumber and serialNumber
ALTER TABLE detector ADD COLUMN lotnumber character varying(255);
ALTER TABLE dichroic ADD COLUMN serialnumber character varying(255);
ALTER TABLE filter ADD COLUMN serialnumber character varying(255);
ALTER TABLE filterset ADD COLUMN serialnumber character varying(255);
ALTER TABLE lightsource ADD COLUMN lotnumber character varying(255);
ALTER TABLE microscope ADD COLUMN lotnumber character varying(255);
ALTER TABLE objective ADD COLUMN lotnumber character varying(255);

-- #4900 Adding almost all FK indexes
DROP INDEX planeinfo_pixels;
CREATE INDEX eventlog_action ON eventlog USING btree (action);
CREATE INDEX eventlog_entityid ON eventlog USING btree (entityid);
CREATE INDEX eventlog_entitytype ON eventlog USING btree (entitytype);

CREATE INDEX i_wellsampleannotationlink_owner ON wellsampleannotationlink(owner_id);
CREATE INDEX i_wellsampleannotationlink_group ON wellsampleannotationlink(group_id);
CREATE INDEX i_WellSampleAnnotationLink_parent ON wellsampleannotationlink(parent);
CREATE INDEX i_WellSampleAnnotationLink_child ON wellsampleannotationlink(child);
CREATE INDEX i_wellannotationlink_owner ON wellannotationlink(owner_id);
CREATE INDEX i_wellannotationlink_group ON wellannotationlink(group_id);
CREATE INDEX i_WellAnnotationLink_parent ON wellannotationlink(parent);
CREATE INDEX i_WellAnnotationLink_child ON wellannotationlink(child);
CREATE INDEX i_dataset_owner ON dataset(owner_id);
CREATE INDEX i_dataset_group ON dataset(group_id);
CREATE INDEX i_plate_owner ON plate(owner_id);
CREATE INDEX i_plate_group ON plate(group_id);
CREATE INDEX i_thumbnail_owner ON thumbnail(owner_id);
CREATE INDEX i_thumbnail_group ON thumbnail(group_id);
CREATE INDEX i_Thumbnail_pixels ON thumbnail(pixels);
CREATE INDEX i_channel_owner ON channel(owner_id);
CREATE INDEX i_channel_group ON channel(group_id);
CREATE INDEX i_Channel_statsInfo ON channel(statsInfo);
CREATE INDEX i_Channel_logicalChannel ON channel(logicalChannel);
CREATE INDEX i_Channel_pixels ON channel(pixels);
CREATE INDEX i_imageannotationlink_owner ON imageannotationlink(owner_id);
CREATE INDEX i_imageannotationlink_group ON imageannotationlink(group_id);
CREATE INDEX i_ImageAnnotationLink_parent ON imageannotationlink(parent);
CREATE INDEX i_ImageAnnotationLink_child ON imageannotationlink(child);
CREATE INDEX i_link_owner ON link(owner_id);
CREATE INDEX i_link_group ON link(group_id);
CREATE INDEX i_lightpathemissionfilterlink_owner ON lightpathemissionfilterlink(owner_id);
CREATE INDEX i_lightpathemissionfilterlink_group ON lightpathemissionfilterlink(group_id);
CREATE INDEX i_LightPathEmissionFilterLink_parent ON lightpathemissionfilterlink(parent);
CREATE INDEX i_LightPathEmissionFilterLink_child ON lightpathemissionfilterlink(child);
CREATE INDEX i_filtersetemissionfilterlink_owner ON filtersetemissionfilterlink(owner_id);
CREATE INDEX i_filtersetemissionfilterlink_group ON filtersetemissionfilterlink(group_id);
CREATE INDEX i_FilterSetEmissionFilterLink_parent ON filtersetemissionfilterlink(parent);
CREATE INDEX i_FilterSetEmissionFilterLink_child ON filtersetemissionfilterlink(child);
CREATE INDEX i_filtersetexcitationfilterlink_owner ON filtersetexcitationfilterlink(owner_id);
CREATE INDEX i_filtersetexcitationfilterlink_group ON filtersetexcitationfilterlink(group_id);
CREATE INDEX i_FilterSetExcitationFilterLink_parent ON filtersetexcitationfilterlink(parent);
CREATE INDEX i_FilterSetExcitationFilterLink_child ON filtersetexcitationfilterlink(child);
CREATE INDEX i_microscope_owner ON microscope(owner_id);
CREATE INDEX i_microscope_group ON microscope(group_id);
CREATE INDEX i_Microscope_type ON microscope(type);
CREATE INDEX i_originalfileannotationlink_owner ON originalfileannotationlink(owner_id);
CREATE INDEX i_originalfileannotationlink_group ON originalfileannotationlink(group_id);
CREATE INDEX i_OriginalFileAnnotationLink_parent ON originalfileannotationlink(parent);
CREATE INDEX i_OriginalFileAnnotationLink_child ON originalfileannotationlink(child);
CREATE INDEX i_wellsample_owner ON wellsample(owner_id);
CREATE INDEX i_wellsample_group ON wellsample(group_id);
CREATE INDEX i_WellSample_plateAcquisition ON wellsample(plateAcquisition);
CREATE INDEX i_WellSample_well ON wellsample(well);
CREATE INDEX i_WellSample_image ON wellsample(image);
CREATE INDEX i_planeinfo_owner ON planeinfo(owner_id);
CREATE INDEX i_planeinfo_group ON planeinfo(group_id);
CREATE INDEX i_PlaneInfo_pixels ON planeinfo(pixels);
CREATE INDEX i_lightpathexcitationfilterlink_owner ON lightpathexcitationfilterlink(owner_id);
CREATE INDEX i_lightpathexcitationfilterlink_group ON lightpathexcitationfilterlink(group_id);
CREATE INDEX i_LightPathExcitationFilterLink_parent ON lightpathexcitationfilterlink(parent);
CREATE INDEX i_LightPathExcitationFilterLink_child ON lightpathexcitationfilterlink(child);
CREATE INDEX i_GroupExperimenterMap_parent ON groupexperimentermap(parent);
CREATE INDEX i_GroupExperimenterMap_child ON groupexperimentermap(child);
CREATE INDEX i_planeinfoannotationlink_owner ON planeinfoannotationlink(owner_id);
CREATE INDEX i_planeinfoannotationlink_group ON planeinfoannotationlink(group_id);
CREATE INDEX i_PlaneInfoAnnotationLink_parent ON planeinfoannotationlink(parent);
CREATE INDEX i_PlaneInfoAnnotationLink_child ON planeinfoannotationlink(child);
CREATE INDEX i_transmittancerange_owner ON transmittancerange(owner_id);
CREATE INDEX i_transmittancerange_group ON transmittancerange(group_id);
CREATE INDEX i_wellreagentlink_owner ON wellreagentlink(owner_id);
CREATE INDEX i_wellreagentlink_group ON wellreagentlink(group_id);
CREATE INDEX i_WellReagentLink_parent ON wellreagentlink(parent);
CREATE INDEX i_WellReagentLink_child ON wellreagentlink(child);
CREATE INDEX i_Arc_type ON arc(type);
CREATE INDEX i_EventLog_event ON eventlog(event);
CREATE INDEX i_quantumdef_owner ON quantumdef(owner_id);
CREATE INDEX i_quantumdef_group ON quantumdef(group_id);
CREATE INDEX i_namespace_owner ON namespace(owner_id);
CREATE INDEX i_namespace_group ON namespace(group_id);
CREATE INDEX i_image_owner ON image(owner_id);
CREATE INDEX i_image_group ON image(group_id);
CREATE INDEX i_Image_format ON image(format);
CREATE INDEX i_Image_imagingEnvironment ON image(imagingEnvironment);
CREATE INDEX i_Image_objectiveSettings ON image(objectiveSettings);
CREATE INDEX i_Image_instrument ON image(instrument);
CREATE INDEX i_Image_stageLabel ON image(stageLabel);
CREATE INDEX i_Image_experiment ON image(experiment);
CREATE INDEX i_microbeammanipulation_owner ON microbeammanipulation(owner_id);
CREATE INDEX i_microbeammanipulation_group ON microbeammanipulation(group_id);
CREATE INDEX i_MicrobeamManipulation_type ON microbeammanipulation(type);
CREATE INDEX i_MicrobeamManipulation_experiment ON microbeammanipulation(experiment);
CREATE INDEX i_joboriginalfilelink_owner ON joboriginalfilelink(owner_id);
CREATE INDEX i_joboriginalfilelink_group ON joboriginalfilelink(group_id);
CREATE INDEX i_JobOriginalFileLink_parent ON joboriginalfilelink(parent);
CREATE INDEX i_JobOriginalFileLink_child ON joboriginalfilelink(child);
CREATE INDEX i_renderingdef_owner ON renderingdef(owner_id);
CREATE INDEX i_renderingdef_group ON renderingdef(group_id);
CREATE INDEX i_RenderingDef_pixels ON renderingdef(pixels);
CREATE INDEX i_RenderingDef_model ON renderingdef(model);
CREATE INDEX i_RenderingDef_quantization ON renderingdef(quantization);
CREATE INDEX i_datasetimagelink_owner ON datasetimagelink(owner_id);
CREATE INDEX i_datasetimagelink_group ON datasetimagelink(group_id);
CREATE INDEX i_DatasetImageLink_parent ON datasetimagelink(parent);
CREATE INDEX i_DatasetImageLink_child ON datasetimagelink(child);
CREATE INDEX i_codomainmapcontext_owner ON codomainmapcontext(owner_id);
CREATE INDEX i_codomainmapcontext_group ON codomainmapcontext(group_id);
CREATE INDEX i_CodomainMapContext_renderingDef ON codomainmapcontext(renderingDef);
CREATE INDEX i_project_owner ON project(owner_id);
CREATE INDEX i_project_group ON project(group_id);
CREATE INDEX i_channelannotationlink_owner ON channelannotationlink(owner_id);
CREATE INDEX i_channelannotationlink_group ON channelannotationlink(group_id);
CREATE INDEX i_ChannelAnnotationLink_parent ON channelannotationlink(parent);
CREATE INDEX i_ChannelAnnotationLink_child ON channelannotationlink(child);
CREATE INDEX i_stagelabel_owner ON stagelabel(owner_id);
CREATE INDEX i_stagelabel_group ON stagelabel(group_id);
CREATE INDEX i_experimentergroupannotationlink_owner ON experimentergroupannotationlink(owner_id);
CREATE INDEX i_experimentergroupannotationlink_group ON experimentergroupannotationlink(group_id);
CREATE INDEX i_ExperimenterGroupAnnotationLink_parent ON experimentergroupannotationlink(parent);
CREATE INDEX i_ExperimenterGroupAnnotationLink_child ON experimentergroupannotationlink(child);
CREATE INDEX i_pixels_owner ON pixels(owner_id);
CREATE INDEX i_pixels_group ON pixels(group_id);
CREATE INDEX i_Pixels_image ON pixels(image);
CREATE INDEX i_Pixels_relatedTo ON pixels(relatedTo);
CREATE INDEX i_Pixels_pixelsType ON pixels(pixelsType);
CREATE INDEX i_Pixels_dimensionOrder ON pixels(dimensionOrder);
CREATE INDEX i_lightpath_owner ON lightpath(owner_id);
CREATE INDEX i_lightpath_group ON lightpath(group_id);
CREATE INDEX i_LightPath_dichroic ON lightpath(dichroic);
CREATE INDEX i_roi_owner ON roi(owner_id);
CREATE INDEX i_roi_group ON roi(group_id);
CREATE INDEX i_Roi_image ON roi(image);
CREATE INDEX i_Roi_source ON roi(source);
CREATE INDEX i_roiannotationlink_owner ON roiannotationlink(owner_id);
CREATE INDEX i_roiannotationlink_group ON roiannotationlink(group_id);
CREATE INDEX i_RoiAnnotationLink_parent ON roiannotationlink(parent);
CREATE INDEX i_RoiAnnotationLink_child ON roiannotationlink(child);
CREATE INDEX i_externalinfo_owner ON externalinfo(owner_id);
CREATE INDEX i_externalinfo_group ON externalinfo(group_id);
CREATE INDEX i_FileAnnotation_file ON annotation("file");
CREATE INDEX i_annotationannotationlink_owner ON annotationannotationlink(owner_id);
CREATE INDEX i_annotationannotationlink_group ON annotationannotationlink(group_id);
CREATE INDEX i_AnnotationAnnotationLink_parent ON annotationannotationlink(parent);
CREATE INDEX i_AnnotationAnnotationLink_child ON annotationannotationlink(child);
CREATE INDEX i_objectivesettings_owner ON objectivesettings(owner_id);
CREATE INDEX i_objectivesettings_group ON objectivesettings(group_id);
CREATE INDEX i_ObjectiveSettings_medium ON objectivesettings(medium);
CREATE INDEX i_ObjectiveSettings_objective ON objectivesettings(objective);
CREATE INDEX i_nodeannotationlink_owner ON nodeannotationlink(owner_id);
CREATE INDEX i_nodeannotationlink_group ON nodeannotationlink(group_id);
CREATE INDEX i_NodeAnnotationLink_parent ON nodeannotationlink(parent);
CREATE INDEX i_NodeAnnotationLink_child ON nodeannotationlink(child);
CREATE INDEX i_Share_group ON share("group");
CREATE INDEX i_instrument_owner ON instrument(owner_id);
CREATE INDEX i_instrument_group ON instrument(group_id);
CREATE INDEX i_Instrument_microscope ON instrument(microscope);
CREATE INDEX i_namespaceannotationlink_owner ON namespaceannotationlink(owner_id);
CREATE INDEX i_namespaceannotationlink_group ON namespaceannotationlink(group_id);
CREATE INDEX i_NamespaceAnnotationLink_parent ON namespaceannotationlink(parent);
CREATE INDEX i_NamespaceAnnotationLink_child ON namespaceannotationlink(child);
CREATE INDEX i_well_owner ON well(owner_id);
CREATE INDEX i_well_group ON well(group_id);
CREATE INDEX i_Well_plate ON well(plate);
CREATE INDEX i_imagingenvironment_owner ON imagingenvironment(owner_id);
CREATE INDEX i_imagingenvironment_group ON imagingenvironment(group_id);
CREATE INDEX i_projectannotationlink_owner ON projectannotationlink(owner_id);
CREATE INDEX i_projectannotationlink_group ON projectannotationlink(group_id);
CREATE INDEX i_ProjectAnnotationLink_parent ON projectannotationlink(parent);
CREATE INDEX i_ProjectAnnotationLink_child ON projectannotationlink(child);
CREATE INDEX i_reagent_owner ON reagent(owner_id);
CREATE INDEX i_reagent_group ON reagent(group_id);
CREATE INDEX i_Reagent_screen ON reagent(screen);
CREATE INDEX i_detector_owner ON detector(owner_id);
CREATE INDEX i_detector_group ON detector(group_id);
CREATE INDEX i_Detector_type ON detector(type);
CREATE INDEX i_Detector_instrument ON detector(instrument);
CREATE INDEX i_otf_owner ON otf(owner_id);
CREATE INDEX i_otf_group ON otf(group_id);
CREATE INDEX i_OTF_pixelsType ON otf(pixelsType);
CREATE INDEX i_OTF_filterSet ON otf(filterSet);
CREATE INDEX i_OTF_objective ON otf(objective);
CREATE INDEX i_OTF_instrument ON otf(instrument);
CREATE INDEX i_reagentannotationlink_owner ON reagentannotationlink(owner_id);
CREATE INDEX i_reagentannotationlink_group ON reagentannotationlink(group_id);
CREATE INDEX i_ReagentAnnotationLink_parent ON reagentannotationlink(parent);
CREATE INDEX i_ReagentAnnotationLink_child ON reagentannotationlink(child);
CREATE INDEX i_lightsettings_owner ON lightsettings(owner_id);
CREATE INDEX i_lightsettings_group ON lightsettings(group_id);
CREATE INDEX i_LightSettings_lightSource ON lightsettings(lightSource);
CREATE INDEX i_LightSettings_microbeamManipulation ON lightsettings(microbeamManipulation);
CREATE INDEX i_originalfile_owner ON originalfile(owner_id);
CREATE INDEX i_originalfile_group ON originalfile(group_id);
CREATE INDEX i_lightsource_owner ON lightsource(owner_id);
CREATE INDEX i_lightsource_group ON lightsource(group_id);
CREATE INDEX i_LightSource_instrument ON lightsource(instrument);
CREATE INDEX i_annotation_owner ON annotation(owner_id);
CREATE INDEX i_annotation_group ON annotation(group_id);
CREATE INDEX i_job_owner ON job(owner_id);
CREATE INDEX i_job_group ON job(group_id);
CREATE INDEX i_Job_status ON job(status);
CREATE INDEX i_Mask_pixels ON shape(pixels);
CREATE INDEX i_ShareMember_parent ON sharemember(parent);
CREATE INDEX i_ShareMember_child ON sharemember(child);
CREATE INDEX i_filterset_owner ON filterset(owner_id);
CREATE INDEX i_filterset_group ON filterset(group_id);
CREATE INDEX i_FilterSet_instrument ON filterset(instrument);
CREATE INDEX i_FilterSet_dichroic ON filterset(dichroic);
CREATE INDEX i_projectdatasetlink_owner ON projectdatasetlink(owner_id);
CREATE INDEX i_projectdatasetlink_group ON projectdatasetlink(group_id);
CREATE INDEX i_ProjectDatasetLink_parent ON projectdatasetlink(parent);
CREATE INDEX i_ProjectDatasetLink_child ON projectdatasetlink(child);
CREATE INDEX i_experimenterannotationlink_owner ON experimenterannotationlink(owner_id);
CREATE INDEX i_experimenterannotationlink_group ON experimenterannotationlink(group_id);
CREATE INDEX i_ExperimenterAnnotationLink_parent ON experimenterannotationlink(parent);
CREATE INDEX i_ExperimenterAnnotationLink_child ON experimenterannotationlink(child);
CREATE INDEX i_plateannotationlink_owner ON plateannotationlink(owner_id);
CREATE INDEX i_plateannotationlink_group ON plateannotationlink(group_id);
CREATE INDEX i_PlateAnnotationLink_parent ON plateannotationlink(parent);
CREATE INDEX i_PlateAnnotationLink_child ON plateannotationlink(child);
CREATE INDEX i_Laser_type ON laser(type);
CREATE INDEX i_Laser_laserMedium ON laser(laserMedium);
CREATE INDEX i_Laser_pulse ON laser(pulse);
CREATE INDEX i_Laser_pump ON laser(pump);
CREATE INDEX i_channelbinding_owner ON channelbinding(owner_id);
CREATE INDEX i_channelbinding_group ON channelbinding(group_id);
CREATE INDEX i_ChannelBinding_renderingDef ON channelbinding(renderingDef);
CREATE INDEX i_ChannelBinding_family ON channelbinding(family);
CREATE INDEX i_statsinfo_owner ON statsinfo(owner_id);
CREATE INDEX i_statsinfo_group ON statsinfo(group_id);
CREATE INDEX i_screen_owner ON screen(owner_id);
CREATE INDEX i_screen_group ON screen(group_id);
CREATE INDEX i_dichroic_owner ON dichroic(owner_id);
CREATE INDEX i_dichroic_group ON dichroic(group_id);
CREATE INDEX i_Dichroic_instrument ON dichroic(instrument);
CREATE INDEX i_Session_node ON session(node);
CREATE INDEX i_Session_owner ON session(owner);
CREATE INDEX i_plateacquisition_owner ON plateacquisition(owner_id);
CREATE INDEX i_plateacquisition_group ON plateacquisition(group_id);
CREATE INDEX i_PlateAcquisition_plate ON plateacquisition(plate);
CREATE INDEX i_Filament_type ON filament(type);
CREATE INDEX i_screenannotationlink_owner ON screenannotationlink(owner_id);
CREATE INDEX i_screenannotationlink_group ON screenannotationlink(group_id);
CREATE INDEX i_ScreenAnnotationLink_parent ON screenannotationlink(parent);
CREATE INDEX i_ScreenAnnotationLink_child ON screenannotationlink(child);
CREATE INDEX i_pixelsannotationlink_owner ON pixelsannotationlink(owner_id);
CREATE INDEX i_pixelsannotationlink_group ON pixelsannotationlink(group_id);
CREATE INDEX i_PixelsAnnotationLink_parent ON pixelsannotationlink(parent);
CREATE INDEX i_PixelsAnnotationLink_child ON pixelsannotationlink(child);
CREATE INDEX i_objective_owner ON objective(owner_id);
CREATE INDEX i_objective_group ON objective(group_id);
CREATE INDEX i_Objective_immersion ON objective(immersion);
CREATE INDEX i_Objective_correction ON objective(correction);
CREATE INDEX i_Objective_instrument ON objective(instrument);
CREATE INDEX i_datasetannotationlink_owner ON datasetannotationlink(owner_id);
CREATE INDEX i_datasetannotationlink_group ON datasetannotationlink(group_id);
CREATE INDEX i_DatasetAnnotationLink_parent ON datasetannotationlink(parent);
CREATE INDEX i_DatasetAnnotationLink_child ON datasetannotationlink(child);
CREATE INDEX i_experiment_owner ON experiment(owner_id);
CREATE INDEX i_experiment_group ON experiment(group_id);
CREATE INDEX i_Experiment_type ON experiment(type);
CREATE INDEX i_detectorsettings_owner ON detectorsettings(owner_id);
CREATE INDEX i_detectorsettings_group ON detectorsettings(group_id);
CREATE INDEX i_DetectorSettings_binning ON detectorsettings(binning);
CREATE INDEX i_DetectorSettings_detector ON detectorsettings(detector);
CREATE INDEX i_filter_owner ON filter(owner_id);
CREATE INDEX i_filter_group ON filter(group_id);
CREATE INDEX i_Filter_type ON filter(type);
CREATE INDEX i_Filter_transmittanceRange ON filter(transmittanceRange);
CREATE INDEX i_Filter_instrument ON filter(instrument);
CREATE INDEX i_plateacquisitionannotationlink_owner ON plateacquisitionannotationlink(owner_id);
CREATE INDEX i_plateacquisitionannotationlink_group ON plateacquisitionannotationlink(group_id);
CREATE INDEX i_PlateAcquisitionAnnotationLink_parent ON plateacquisitionannotationlink(parent);
CREATE INDEX i_PlateAcquisitionAnnotationLink_child ON plateacquisitionannotationlink(child);
CREATE INDEX i_pixelsoriginalfilemap_owner ON pixelsoriginalfilemap(owner_id);
CREATE INDEX i_pixelsoriginalfilemap_group ON pixelsoriginalfilemap(group_id);
CREATE INDEX i_PixelsOriginalFileMap_parent ON pixelsoriginalfilemap(parent);
CREATE INDEX i_PixelsOriginalFileMap_child ON pixelsoriginalfilemap(child);
CREATE INDEX i_logicalchannel_owner ON logicalchannel(owner_id);
CREATE INDEX i_logicalchannel_group ON logicalchannel(group_id);
CREATE INDEX i_LogicalChannel_illumination ON logicalchannel(illumination);
CREATE INDEX i_LogicalChannel_contrastMethod ON logicalchannel(contrastMethod);
CREATE INDEX i_LogicalChannel_otf ON logicalchannel(otf);
CREATE INDEX i_LogicalChannel_detectorSettings ON logicalchannel(detectorSettings);
CREATE INDEX i_LogicalChannel_lightSourceSettings ON logicalchannel(lightSourceSettings);
CREATE INDEX i_LogicalChannel_filterSet ON logicalchannel(filterSet);
CREATE INDEX i_LogicalChannel_photometricInterpretation ON logicalchannel(photometricInterpretation);
CREATE INDEX i_LogicalChannel_mode ON logicalchannel("mode");
CREATE INDEX i_LogicalChannel_lightPath ON logicalchannel(lightPath);
CREATE INDEX i_sessionannotationlink_owner ON sessionannotationlink(owner_id);
CREATE INDEX i_sessionannotationlink_group ON sessionannotationlink(group_id);
CREATE INDEX i_SessionAnnotationLink_parent ON sessionannotationlink(parent);
CREATE INDEX i_SessionAnnotationLink_child ON sessionannotationlink(child);
CREATE INDEX i_screenplatelink_owner ON screenplatelink(owner_id);
CREATE INDEX i_screenplatelink_group ON screenplatelink(group_id);
CREATE INDEX i_ScreenPlateLink_parent ON screenplatelink(parent);
CREATE INDEX i_ScreenPlateLink_child ON screenplatelink(child);
CREATE INDEX i_shape_owner ON shape(owner_id);
CREATE INDEX i_shape_group ON shape(group_id);
CREATE INDEX i_Shape_roi ON shape(roi);
CREATE INDEX i_Event_experimenter ON event(experimenter);
CREATE INDEX i_Event_experimenterGroup ON event(experimenterGroup);
CREATE INDEX i_Event_type ON event(type);
CREATE INDEX i_Event_containingEvent ON event(containingEvent);
CREATE INDEX i_Event_session ON event("session");

--
-- #1390 : Triggering the addition of an "REINDEX" event on annotation events.
--

CREATE OR REPLACE FUNCTION _prepare_session(_event_id int8, _user_id int8, _group_id int8) RETURNS void
    AS '
    BEGIN

        IF NOT EXISTS(SELECT table_name FROM information_schema.tables where table_name = ''_current_session'') THEN
            CREATE TEMP TABLE _current_session ("event_id" int8, "user_id" int8, "group_id" int8) ON COMMIT DELETE ROWS;
            INSERT INTO _current_session VALUES (_event_id, _user_id, _group_id);
        ELSE
            DELETE FROM _current_session;
            INSERT INTO _current_session VALUES (_event_id, _user_id, _group_id);
        END IF;
    END;'
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION _current_event() RETURNS int8
    AS '
    DECLARE
        eid int8;
    BEGIN
        IF NOT EXISTS(SELECT table_name FROM information_schema.tables where table_name = ''_current_session'') THEN
            RETURN 0;
        END IF;
        SELECT INTO eid event_id FROM _current_session;
        RETURN eid;

    END;'
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION _current_or_new_event() RETURNS int8
    AS '
    DECLARE
        eid int8;
    BEGIN
        SELECT INTO eid _current_event();
        IF eid = 0 OR eid IS NULL THEN
            SELECT INTO eid ome_nextval(''seq_event'');
            INSERT INTO event (id, permissions, status, time, experimenter, experimentergroup, session, type)
                SELECT eid, -35, ''TRIGGERED'', now(), 0, 0, 0, 0;
        END IF;
        RETURN eid;
    END;'
LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION annotation_update_event_trigger() RETURNS "trigger"
    AS '
    DECLARE
        rec RECORD;
        eid int8;
        cnt int8;
    BEGIN

        IF NOT EXISTS(SELECT table_name FROM information_schema.tables where table_name = ''_updated_annotations'') THEN
            CREATE TEMP TABLE _updated_annotations (entitytype varchar, entityid int8) ON COMMIT DELETE ROWS;
        END IF;


        FOR rec IN SELECT id, parent FROM datasetannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.containers.Dataset'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM plateannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.screen.Plate'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM channelannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.core.Channel'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM wellsampleannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.screen.WellSample'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM planeinfoannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.core.PlaneInfo'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM namespaceannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.meta.Namespace'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM imageannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.core.Image'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM experimentergroupannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.meta.ExperimenterGroup'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM projectannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.containers.Project'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM pixelsannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.core.Pixels'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM roiannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.roi.Roi'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM wellannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.screen.Well'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM reagentannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.screen.Reagent'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM originalfileannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.core.OriginalFile'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM annotationannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.annotations.Annotation'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM screenannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.screen.Screen'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM sessionannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.meta.Session'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM plateacquisitionannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.screen.PlateAcquisition'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM nodeannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.meta.Node'');
        END LOOP;

        FOR rec IN SELECT id, parent FROM experimenterannotationlink WHERE child = new.id LOOP
            INSERT INTO _updated_annotations (entityid, entitytype) values (rec.parent, ''ome.model.meta.Experimenter'');
        END LOOP;

        SELECT INTO cnt count(*) FROM _updated_annotations;
        IF cnt <> 0 THEN
            SELECT INTO eid _current_or_new_event();
            INSERT INTO eventlog (id, action, permissions, entityid, entitytype, event)
                 SELECT ome_nextval(''seq_eventlog''), ''REINDEX'', -35, entityid, entitytype, eid
                   FROM _updated_annotations;
        END IF;

        RETURN new;

    END;'
LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS annotation_trigger ON annotation;

CREATE TRIGGER annotation_trigger
        AFTER UPDATE ON annotation
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_update_event_trigger();


CREATE OR REPLACE FUNCTION annotation_link_event_trigger() RETURNS "trigger"
    AS '
    DECLARE
        eid int8;
    BEGIN

        SELECT INTO eid _current_or_new_event();
        INSERT INTO eventlog (id, action, permissions, entityid, entitytype, event)
                SELECT ome_nextval(''seq_eventlog''), ''REINDEX'', -35, new.parent, TG_ARGV[0], eid;

        RETURN new;

    END;'
LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS dataset_annotation_link_event_trigger ON datasetannotationlink;

CREATE TRIGGER dataset_annotation_link_event_trigger
        AFTER UPDATE ON datasetannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.containers.Dataset');

DROP TRIGGER IF EXISTS plate_annotation_link_event_trigger ON plateannotationlink;

CREATE TRIGGER plate_annotation_link_event_trigger
        AFTER UPDATE ON plateannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.screen.Plate');

DROP TRIGGER IF EXISTS channel_annotation_link_event_trigger ON channelannotationlink;

CREATE TRIGGER channel_annotation_link_event_trigger
        AFTER UPDATE ON channelannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.core.Channel');

DROP TRIGGER IF EXISTS wellsample_annotation_link_event_trigger ON wellsampleannotationlink;

CREATE TRIGGER wellsample_annotation_link_event_trigger
        AFTER UPDATE ON wellsampleannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.screen.WellSample');

DROP TRIGGER IF EXISTS planeinfo_annotation_link_event_trigger ON planeinfoannotationlink;

CREATE TRIGGER planeinfo_annotation_link_event_trigger
        AFTER UPDATE ON planeinfoannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.core.PlaneInfo');

DROP TRIGGER IF EXISTS namespace_annotation_link_event_trigger ON namespaceannotationlink;

CREATE TRIGGER namespace_annotation_link_event_trigger
        AFTER UPDATE ON namespaceannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.meta.Namespace');

DROP TRIGGER IF EXISTS image_annotation_link_event_trigger ON imageannotationlink;

CREATE TRIGGER image_annotation_link_event_trigger
        AFTER UPDATE ON imageannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.core.Image');

DROP TRIGGER IF EXISTS experimentergroup_annotation_link_event_trigger ON experimentergroupannotationlink;

CREATE TRIGGER experimentergroup_annotation_link_event_trigger
        AFTER UPDATE ON experimentergroupannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.meta.ExperimenterGroup');

DROP TRIGGER IF EXISTS project_annotation_link_event_trigger ON projectannotationlink;

CREATE TRIGGER project_annotation_link_event_trigger
        AFTER UPDATE ON projectannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.containers.Project');

DROP TRIGGER IF EXISTS pixels_annotation_link_event_trigger ON pixelsannotationlink;

CREATE TRIGGER pixels_annotation_link_event_trigger
        AFTER UPDATE ON pixelsannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.core.Pixels');

DROP TRIGGER IF EXISTS roi_annotation_link_event_trigger ON roiannotationlink;

CREATE TRIGGER roi_annotation_link_event_trigger
        AFTER UPDATE ON roiannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.roi.Roi');

DROP TRIGGER IF EXISTS well_annotation_link_event_trigger ON wellannotationlink;

CREATE TRIGGER well_annotation_link_event_trigger
        AFTER UPDATE ON wellannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.screen.Well');

DROP TRIGGER IF EXISTS reagent_annotation_link_event_trigger ON reagentannotationlink;

CREATE TRIGGER reagent_annotation_link_event_trigger
        AFTER UPDATE ON reagentannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.screen.Reagent');

DROP TRIGGER IF EXISTS originalfile_annotation_link_event_trigger ON originalfileannotationlink;

CREATE TRIGGER originalfile_annotation_link_event_trigger
        AFTER UPDATE ON originalfileannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.core.OriginalFile');

DROP TRIGGER IF EXISTS annotation_annotation_link_event_trigger ON annotationannotationlink;

CREATE TRIGGER annotation_annotation_link_event_trigger
        AFTER UPDATE ON annotationannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.annotations.Annotation');

DROP TRIGGER IF EXISTS screen_annotation_link_event_trigger ON screenannotationlink;

CREATE TRIGGER screen_annotation_link_event_trigger
        AFTER UPDATE ON screenannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.screen.Screen');

DROP TRIGGER IF EXISTS session_annotation_link_event_trigger ON sessionannotationlink;

CREATE TRIGGER session_annotation_link_event_trigger
        AFTER UPDATE ON sessionannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.meta.Session');

DROP TRIGGER IF EXISTS plateacquisition_annotation_link_event_trigger ON plateacquisitionannotationlink;

CREATE TRIGGER plateacquisition_annotation_link_event_trigger
        AFTER UPDATE ON plateacquisitionannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.screen.PlateAcquisition');

DROP TRIGGER IF EXISTS node_annotation_link_event_trigger ON nodeannotationlink;

CREATE TRIGGER node_annotation_link_event_trigger
        AFTER UPDATE ON nodeannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.meta.Node');

DROP TRIGGER IF EXISTS experimenter_annotation_link_event_trigger ON experimenterannotationlink;

CREATE TRIGGER experimenter_annotation_link_event_trigger
        AFTER UPDATE ON experimenterannotationlink
        FOR EACH ROW
        EXECUTE PROCEDURE annotation_link_event_trigger('ome.model.meta.Experimenter');


--
-- END #1390
--
--
-- FINISHED
--

UPDATE dbpatch set message = 'Database updated.', finished = now()
 WHERE currentVersion  = 'OMERO4.3'    and
          currentPatch    = 0          and
          previousVersion = 'OMERO4.2' and
          previousPatch   = 0;

SELECT CHR(10)||CHR(10)||CHR(10)||'YOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION OMERO4.3__0'||CHR(10)||CHR(10)||CHR(10) as Status;

COMMIT;
