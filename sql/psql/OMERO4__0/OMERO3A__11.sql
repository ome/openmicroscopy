--
-- Copyright 2009 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

--
-- OMERO-Beta4.0 release.
--
-- Note: not executing constraint drops since these columns/tables will be removed anyway
--
BEGIN;

-- Check that we are only applying this against OMERO3A__11

CREATE OR REPLACE FUNCTION omero_assert_omero3a_11() RETURNS void AS '
DECLARE
    rec RECORD;
BEGIN

    SELECT INTO rec *
           FROM dbpatch
          WHERE id = ( SELECT id FROM dbpatch ORDER BY id DESC LIMIT 1 )
            AND currentversion = ''OMERO3A''
            AND currentpatch = 11;

    IF NOT FOUND THEN
        RAISE EXCEPTION ''Current version is not OMERO3A__11! Aborting...'';
    END IF;

END;' LANGUAGE plpgsql;
SELECT omero_assert_omero3a_11();
DROP FUNCTION omero_assert_omero3a_11();

-- For conversion
INSERT into dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
             values ('OMERO4',       0,              'OMERO3A',           11);

--
-- In order to simplify conversion, we assert that various tables are empty.
-- If they are not, your site will need specialized scripts for upgrading.
-- Please contact the mailing list.
--
CREATE OR REPLACE FUNCTION omero_assert_empty(tbl VARCHAR) RETURNS void AS '
DECLARE
    rec RECORD;
    sql VARCHAR;
BEGIN

    sql := ''SELECT * FROM ''||tbl||'';'';
    FOR rec IN EXECUTE sql LOOP
        RAISE EXCEPTION ''Table is not empty: % Please contact the OME developers for more information -- https://www.openmicroscopy.org/support/'', tbl;
    END LOOP;

END;' LANGUAGE plpgsql;

-- For conversion
SELECT omero_assert_empty('dichroic');
SELECT omero_assert_empty('filter');
SELECT omero_assert_empty('filterset');
SELECT omero_assert_empty('lightsettings');
SELECT omero_assert_empty('objective');
SELECT omero_assert_empty('transmittancerange');
-- For delete
SELECT omero_assert_empty('category');
SELECT omero_assert_empty('categorygroup');
SELECT omero_assert_empty('categorygroupcategorylink');
SELECT omero_assert_empty('categoryimagelink');
SELECT omero_assert_empty('cellarea');
SELECT omero_assert_empty('celleccentricity');
SELECT omero_assert_empty('cellextent');
SELECT omero_assert_empty('cellmajoraxislength');
SELECT omero_assert_empty('cellminoraxislength');
SELECT omero_assert_empty('cellperimeter');
SELECT omero_assert_empty('cellposition');
SELECT omero_assert_empty('cellsolidity');
SELECT omero_assert_empty('customizedfilterset');
SELECT omero_assert_empty('emissionfilter');
SELECT omero_assert_empty('excitationfilter');
SELECT omero_assert_empty('imagecellcount');
SELECT omero_assert_empty('imagenucleascount');
SELECT omero_assert_empty('nucleusarea');
SELECT omero_assert_empty('nucleuseccentricity');
SELECT omero_assert_empty('nucleusextent');
SELECT omero_assert_empty('nucleusmajoraxislength');
SELECT omero_assert_empty('nucleusminoraxislength');
SELECT omero_assert_empty('nucleusperimeter');
SELECT omero_assert_empty('nucleusposition');
SELECT omero_assert_empty('nucleussolidity');
SELECT omero_assert_empty('roi');
DROP FUNCTION omero_assert_empty(VARCHAR);

--
-- New Tables
--

CREATE TABLE correction (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	value character varying(255) NOT NULL,
	creation_id bigint NOT NULL,
	external_id bigint,
	group_id bigint NOT NULL,
	owner_id bigint NOT NULL
);

CREATE TABLE nodeannotationlink (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	version integer,
	child bigint NOT NULL,
	creation_id bigint NOT NULL,
	external_id bigint,
	group_id bigint NOT NULL,
	owner_id bigint NOT NULL,
	update_id bigint NOT NULL,
	parent bigint NOT NULL
);

CREATE TABLE lightemittingdiode (
	lightsource_id bigint NOT NULL
);

CREATE TABLE microbeammanipulation (
	id bigint NOT NULL,
	description text,
	permissions bigint NOT NULL,
	version integer,
	creation_id bigint NOT NULL,
	external_id bigint,
	group_id bigint NOT NULL,
	owner_id bigint NOT NULL,
	update_id bigint NOT NULL,
	experiment bigint NOT NULL,
	type bigint NOT NULL
);

CREATE TABLE microbeammanipulationtype (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	value character varying(255) NOT NULL,
	creation_id bigint NOT NULL,
	external_id bigint,
	group_id bigint NOT NULL,
	owner_id bigint NOT NULL
);

CREATE TABLE node (
	id bigint NOT NULL,
	conn character varying(255) NOT NULL,
	permissions bigint NOT NULL,
	down timestamp without time zone,
	scale integer,
	up timestamp without time zone NOT NULL,
	uuid character varying(255) NOT NULL,
	version integer,
	external_id bigint
);

--
-- New functions and table for dealing with generator switch (#1176)
--

CREATE OR REPLACE FUNCTION ome_nextval(seq VARCHAR) RETURNS INT8 AS '
BEGIN
      RETURN ome_nextval(seq, 1);
END;' LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION ome_nextval(seq VARCHAR, increment int4) RETURNS INT8 AS '
DECLARE
      nv   int8;
      sql  varchar;
BEGIN
      SELECT next_val INTO nv FROM seq_table WHERE sequence_name = seq FOR UPDATE OF seq_table;
      IF nv IS NULL THEN
          INSERT INTO seq_table (sequence_name, next_val) VALUES (seq, increment + 1);
          nv = increment;
      ELSE
          UPDATE seq_table SET next_val = (nv + increment) WHERE sequence_name = seq;
	  nv = nv + increment - 1;
      END IF;

      RETURN nv;
END;' LANGUAGE plpgsql;

CREATE TABLE seq_table (sequence_name VARCHAR(255) PRIMARY KEY, next_val int8);
INSERT INTO seq_table SELECT 'seq_acquisitionmode', id + 1 FROM acquisitionmode ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_annotation', id + 1 FROM annotation ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_annotationannotationlink', id + 1 FROM annotationannotationlink ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_arctype', id + 1 FROM arctype ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_binning', id + 1 FROM binning ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_channel', id + 1 FROM channel ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_channelannotationlink', id + 1 FROM channelannotationlink ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_channelbinding', id + 1 FROM channelbinding ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_codomainmapcontext', id + 1 FROM codomainmapcontext ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_contrastmethod', id + 1 FROM contrastmethod ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_correction', id + 1 FROM correction ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_dataset', id + 1 FROM dataset ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_datasetannotationlink', id + 1 FROM datasetannotationlink ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_datasetimagelink', id + 1 FROM datasetimagelink ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_dbpatch', id + 1 FROM dbpatch ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_detector', id + 1 FROM detector ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_detectorsettings', id + 1 FROM detectorsettings ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_detectortype', id + 1 FROM detectortype ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_dichroic', id + 1 FROM dichroic ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_dimensionorder', id + 1 FROM dimensionorder ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_event', id + 1 FROM event ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_eventlog', id + 1 FROM eventlog ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_eventtype', id + 1 FROM eventtype ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_experiment', id + 1 FROM experiment ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_experimenter', id + 1 FROM experimenter ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_experimenterannotationlink', id + 1 FROM experimenterannotationlink ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_experimentergroup', id + 1 FROM experimentergroup ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_experimentergroupannotationlink', id + 1 FROM experimentergroupannotationlink ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_experimenttype', id + 1 FROM experimenttype ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_externalinfo', id + 1 FROM externalinfo ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_family', id + 1 FROM family ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_filamenttype', id + 1 FROM filamenttype ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_filter', id + 1 FROM filter ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_filterset', id + 1 FROM filterset ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_filtertype', id + 1 FROM filtertype ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_format', id + 1 FROM format ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_groupexperimentermap', id + 1 FROM groupexperimentermap ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_illumination', id + 1 FROM illumination ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_image', id + 1 FROM image ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_imageannotationlink', id + 1 FROM imageannotationlink ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_imagingenvironment', id + 1 FROM imagingenvironment ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_immersion', id + 1 FROM immersion ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_instrument', id + 1 FROM instrument ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_job', id + 1 FROM job ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_joboriginalfilelink', id + 1 FROM joboriginalfilelink ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_jobstatus', id + 1 FROM jobstatus ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_lasermedium', id + 1 FROM lasermedium ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_lasertype', id + 1 FROM lasertype ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_lightsettings', id + 1 FROM lightsettings ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_lightsource', id + 1 FROM lightsource ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_link', id + 1 FROM link ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_logicalchannel', id + 1 FROM logicalchannel ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_medium', id + 1 FROM medium ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_microbeammanipulation', id + 1 FROM microbeammanipulation ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_microbeammanipulationtype', id + 1 FROM microbeammanipulationtype ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_microscope', id + 1 FROM microscope ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_microscopetype', id + 1 FROM microscopetype ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_node', id + 1 FROM node ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_nodeannotationlink', id + 1 FROM nodeannotationlink ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_objective', id + 1 FROM objective ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_objectivesettings', id + 1 FROM objectivesettings ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_originalfile', id + 1 FROM originalfile ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_originalfileannotationlink', id + 1 FROM originalfileannotationlink ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_otf', id + 1 FROM otf ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_photometricinterpretation', id + 1 FROM photometricinterpretation ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_pixels', id + 1 FROM pixels ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_pixelsannotationlink', id + 1 FROM pixelsannotationlink ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_pixelsoriginalfilemap', id + 1 FROM pixelsoriginalfilemap ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_pixelstype', id + 1 FROM pixelstype ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_planeinfo', id + 1 FROM planeinfo ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_planeinfoannotationlink', id + 1 FROM planeinfoannotationlink ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_plate', id + 1 FROM plate ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_plateannotationlink', id + 1 FROM plateannotationlink ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_project', id + 1 FROM project ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_projectannotationlink', id + 1 FROM projectannotationlink ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_projectdatasetlink', id + 1 FROM projectdatasetlink ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_pulse', id + 1 FROM pulse ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_quantumdef', id + 1 FROM quantumdef ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_reagent', id + 1 FROM reagent ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_reagentannotationlink', id + 1 FROM reagentannotationlink ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_renderingdef', id + 1 FROM renderingdef ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_renderingmodel', id + 1 FROM renderingmodel ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_screen', id + 1 FROM screen ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_screenacquisition', id + 1 FROM screenacquisition ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_screenacquisitionannotationlink', id + 1 FROM screenacquisitionannotationlink ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_screenacquisitionwellsamplelink', id + 1 FROM screenacquisitionwellsamplelink ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_screenannotationlink', id + 1 FROM screenannotationlink ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_screenplatelink', id + 1 FROM screenplatelink ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_session', id + 1 FROM session ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_sessionannotationlink', id + 1 FROM sessionannotationlink ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_stagelabel', id + 1 FROM stagelabel ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_statsinfo', id + 1 FROM statsinfo ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_thumbnail', id + 1 FROM thumbnail ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_transmittancerange', id + 1 FROM transmittancerange ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_well', id + 1 FROM well ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_wellannotationlink', id + 1 FROM wellannotationlink ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_wellreagentlink', id + 1 FROM wellreagentlink ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_wellsample', id + 1 FROM wellsample ORDER BY id DESC LIMIT 1;
INSERT INTO seq_table SELECT 'seq_wellsampleannotationlink', id + 1 FROM wellsampleannotationlink ORDER BY id DESC LIMIT 1;

--
-- Removing all sequences now that ome_nextval is in place
--
ALTER TABLE dbpatch ALTER COLUMN id DROP DEFAULT;
ALTER TABLE dbpatch ALTER COLUMN id SET DEFAULT ome_nextval('seq_dbpatch');
DROP SEQUENCE seq_aberrationcorrection;
DROP SEQUENCE seq_acquisitionmode;
DROP SEQUENCE seq_annotation;
DROP SEQUENCE seq_annotationannotationlink;
DROP SEQUENCE seq_arctype;
DROP SEQUENCE seq_binning;
DROP SEQUENCE seq_category;
DROP SEQUENCE seq_categorygroup;
DROP SEQUENCE seq_categorygroupcategorylink;
DROP SEQUENCE seq_categoryimagelink;
DROP SEQUENCE seq_cellarea;
DROP SEQUENCE seq_celleccentricity;
DROP SEQUENCE seq_cellextent;
DROP SEQUENCE seq_cellmajoraxislength;
DROP SEQUENCE seq_cellminoraxislength;
DROP SEQUENCE seq_cellperimeter;
DROP SEQUENCE seq_cellposition;
DROP SEQUENCE seq_cellsolidity;
DROP SEQUENCE seq_channel;
DROP SEQUENCE seq_channelannotationlink;
DROP SEQUENCE seq_channelbinding;
DROP SEQUENCE seq_coating;
DROP SEQUENCE seq_codomainmapcontext;
DROP SEQUENCE seq_color;
DROP SEQUENCE seq_contrastmethod;
DROP SEQUENCE seq_customizedfilterset;
DROP SEQUENCE seq_dataset;
DROP SEQUENCE seq_datasetannotationlink;
DROP SEQUENCE seq_datasetimagelink;
DROP SEQUENCE seq_dbpatch;
DROP SEQUENCE seq_detector;
DROP SEQUENCE seq_detectorsettings;
DROP SEQUENCE seq_detectortype;
DROP SEQUENCE seq_dichroic;
DROP SEQUENCE seq_dimensionorder;
DROP SEQUENCE seq_emissionfilter;
DROP SEQUENCE seq_event;
DROP SEQUENCE seq_eventlog;
DROP SEQUENCE seq_eventtype;
DROP SEQUENCE seq_excitationfilter;
DROP SEQUENCE seq_experiment;
DROP SEQUENCE seq_experimenter;
DROP SEQUENCE seq_experimenterannotationlink;
DROP SEQUENCE seq_experimentergroup;
DROP SEQUENCE seq_experimentergroupannotationlink;
DROP SEQUENCE seq_experimenttype;
DROP SEQUENCE seq_externalinfo;
DROP SEQUENCE seq_family;
DROP SEQUENCE seq_filamenttype;
DROP SEQUENCE seq_filter;
DROP SEQUENCE seq_filterset;
DROP SEQUENCE seq_filtertype;
-- DROP SEQUENCE seq_format; Going to let this hang around since might be missing (?)
DROP SEQUENCE seq_frequencymultiplication;
DROP SEQUENCE seq_groupexperimentermap;
DROP SEQUENCE seq_illumination;
DROP SEQUENCE seq_image;
DROP SEQUENCE seq_imageannotationlink;
DROP SEQUENCE seq_imagecellcount;
DROP SEQUENCE seq_imagenucleascount;
DROP SEQUENCE seq_imagingenvironment;
DROP SEQUENCE seq_immersion;
DROP SEQUENCE seq_instrument;
DROP SEQUENCE seq_irisdiaphragm;
DROP SEQUENCE seq_job;
DROP SEQUENCE seq_joboriginalfilelink;
DROP SEQUENCE seq_jobstatus;
DROP SEQUENCE seq_lasermedium;
DROP SEQUENCE seq_lasertype;
DROP SEQUENCE seq_lightsettings;
DROP SEQUENCE seq_lightsource;
DROP SEQUENCE seq_link;
DROP SEQUENCE seq_logicalchannel;
DROP SEQUENCE seq_medium;
DROP SEQUENCE seq_microscope;
DROP SEQUENCE seq_microscopetype;
DROP SEQUENCE seq_nucleusarea;
DROP SEQUENCE seq_nucleuseccentricity;
DROP SEQUENCE seq_nucleusextent;
DROP SEQUENCE seq_nucleusmajoraxislength;
DROP SEQUENCE seq_nucleusminoraxislength;
DROP SEQUENCE seq_nucleusperimeter;
DROP SEQUENCE seq_nucleusposition;
DROP SEQUENCE seq_nucleussolidity;
DROP SEQUENCE seq_objective;
DROP SEQUENCE seq_objectivesettings;
DROP SEQUENCE seq_originalfile;
DROP SEQUENCE seq_originalfileannotationlink;
DROP SEQUENCE seq_otf;
DROP SEQUENCE seq_photometricinterpretation;
DROP SEQUENCE seq_pixels;
DROP SEQUENCE seq_pixelsannotationlink;
DROP SEQUENCE seq_pixelsdimensions;
DROP SEQUENCE seq_pixelsoriginalfilemap;
DROP SEQUENCE seq_pixelstype;
DROP SEQUENCE seq_planeinfo;
DROP SEQUENCE seq_planeinfoannotationlink;
DROP SEQUENCE seq_plate;
DROP SEQUENCE seq_plateannotationlink;
DROP SEQUENCE seq_project;
DROP SEQUENCE seq_projectannotationlink;
DROP SEQUENCE seq_projectdatasetlink;
DROP SEQUENCE seq_pulse;
DROP SEQUENCE seq_quantumdef;
DROP SEQUENCE seq_reagent;
DROP SEQUENCE seq_reagentannotationlink;
DROP SEQUENCE seq_renderingdef;
DROP SEQUENCE seq_renderingmodel;
DROP SEQUENCE seq_roi;
DROP SEQUENCE seq_roilink;
DROP SEQUENCE seq_roilinkannotationlink;
DROP SEQUENCE seq_screen;
DROP SEQUENCE seq_screenacquisition;
DROP SEQUENCE seq_screenacquisitionannotationlink;
DROP SEQUENCE seq_screenacquisitionwellsamplelink;
DROP SEQUENCE seq_screenannotationlink;
DROP SEQUENCE seq_screenplatelink;
DROP SEQUENCE seq_session;
DROP SEQUENCE seq_sessionannotationlink;
DROP SEQUENCE seq_stagelabel;
DROP SEQUENCE seq_statsinfo;
DROP SEQUENCE seq_thumbnail;
DROP SEQUENCE seq_transmittancerange;
DROP SEQUENCE seq_well;
DROP SEQUENCE seq_wellannotationlink;
DROP SEQUENCE seq_wellreagentlink;
DROP SEQUENCE seq_wellsample;
DROP SEQUENCE seq_wellsampleannotationlink;


--
-- Fixing enumerations for later use by the table modifications
--
-- AcquisitionMode : ==================
INSERT INTO acquisitionmode (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_acquisitionmode'),-35,0,0,0,'Other';
INSERT INTO acquisitionmode (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_acquisitionmode'),-35,0,0,0,'Unknown';
UPDATE acquisitionmode SET value = 'WideField' WHERE value = 'Wide-field';

-- ArcType : ==================
INSERT INTO arctype (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_arctype'),-35,0,0,0,'Unknown';
UPDATE arctype SET value = 'HgXe' where VALUE = 'Hg-Xe';

-- ContractMethod : ==================
INSERT INTO contrastmethod (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_contrastmethod'),-35,0,0,0,'Unknown';
INSERT INTO contrastmethod (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_contrastmethod'),-35,0,0,0,'Other';

-- Correction : ================== (ADDED)
INSERT INTO correction (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_correction'),-35,0,0,0,'Achro';
INSERT INTO correction (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_correction'),-35,0,0,0,'Achromat';
INSERT INTO correction (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_correction'),-35,0,0,0,'Apo';
INSERT INTO correction (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_correction'),-35,0,0,0,'Fl';
INSERT INTO correction (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_correction'),-35,0,0,0,'Fluar';
INSERT INTO correction (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_correction'),-35,0,0,0,'Fluor';
INSERT INTO correction (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_correction'),-35,0,0,0,'Fluotar';
INSERT INTO correction (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_correction'),-35,0,0,0,'Neofluar';
INSERT INTO correction (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_correction'),-35,0,0,0,'Other';
INSERT INTO correction (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_correction'),-35,0,0,0,'PlanApo';
INSERT INTO correction (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_correction'),-35,0,0,0,'PlanFluor';
INSERT INTO correction (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_correction'),-35,0,0,0,'SuperFluor';
INSERT INTO correction (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_correction'),-35,0,0,0,'UV';
INSERT INTO correction (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_correction'),-35,0,0,0,'Unknown';
INSERT INTO correction (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_correction'),-35,0,0,0,'VioletCorrected';

-- DetectorType : ==================
UPDATE detectortype SET value = 'AnalogVideo' WHERE value = 'Analog-Video';
UPDATE detectortype SET value = 'CorrelationSpectroscopy' WHERE value = 'Correlation-Spectroscopy';
UPDATE detectortype SET value = 'IntensifiedCCD' WHERE value = 'Intensified-CCD';
UPDATE detectortype SET value = 'LifetimeImaging' WHERE value = 'Life-time-Imaging';
INSERT INTO detectortype (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_detectortype'),-35,0,0,0,'APD';
INSERT INTO detectortype (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_detectortype'),-35,0,0,0,'CMOS';
INSERT INTO detectortype (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_detectortype'),-35,0,0,0,'EM-CCD';
INSERT INTO detectortype (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_detectortype'),-35,0,0,0,'Other';
INSERT INTO detectortype (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_detectortype'),-35,0,0,0,'Unknown';

-- ExperimentType : ================== 
INSERT INTO experimenttype (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_experimenttype'),-35,0,0,0,'Photobleaching';
UPDATE experiment SET type  = (SELECT id FROM experimenttype WHERE value = 'Photobleaching')
    WHERE type in (select id FROM experimenttype WHERE value in ('Uncaging', 'FRAP', 'Optical-Trapping', 'Photoablation', 'Photoactivation'));
DELETE FROM experimenttype WHERE value in ('Uncaging', 'FRAP', 'Optical-Trapping', 'Photoablation', 'Photoactivation');

UPDATE experimenttype SET value = 'FourDPlus' WHERE value = '4-D+';
UPDATE experimenttype SET value = 'Immunocytochemistry' WHERE value = 'Immunocytopchemistry';
UPDATE experimenttype SET value = 'Immunofluorescence' WHERE value = 'Immunofluroescence';
UPDATE experimenttype SET value = 'IonImaging' WHERE value = 'Ion-Imaging';
UPDATE experimenttype SET value = 'FluorescenceLifetime' WHERE value = 'Fluorescence-Lifetime';
UPDATE experimenttype SET value = 'Electrophysiology' WHERE value = 'Electropyhsiology';
UPDATE experimenttype SET value = 'PGIDocumentation' WHERE value = 'PGI/Documentation';
UPDATE experimenttype SET value = 'SpectralImaging' WHERE value = 'Spectral-Imaging';
UPDATE experimenttype SET value = 'TimeLapse' WHERE value = 'Time-lapse';
INSERT INTO experimenttype (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_experimenttype'),-35,0,0,0,'Unknown';

-- FilamentType : ================== 
INSERT INTO filamenttype (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_filamenttype'),-35,0,0,0,'Other';
INSERT INTO filamenttype (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_filamenttype'),-35,0,0,0,'Unknown';

-- FilterType : ================== 
INSERT INTO filtertype (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_filtertype'),-35,0,0,0,'Other';
INSERT INTO filtertype (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_filtertype'),-35,0,0,0,'Unknown';

-- Format : ================== 
INSERT INTO format (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_format'),-35,0,0,0,'application/octet-stream';
INSERT INTO format (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_format'),-35,0,0,0,'text/richtext';
INSERT INTO format (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_format'),-35,0,0,0,'video/jpeg2000';
INSERT INTO format (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_format'),-35,0,0,0,'video/mpeg';
INSERT INTO format (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_format'),-35,0,0,0,'video/mp4';
INSERT INTO format (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_format'),-35,0,0,0,'video/quicktime';
INSERT INTO format (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_format'),-35,0,0,0,'image/bmp';
INSERT INTO format (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_format'),-35,0,0,0,'image/gif';
INSERT INTO format (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_format'),-35,0,0,0,'image/jpeg';
INSERT INTO format (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_format'),-35,0,0,0,'image/tiff';
INSERT INTO format (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_format'),-35,0,0,0,'image/png';
INSERT INTO format (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_format'),-35,0,0,0,'audio/basic';
INSERT INTO format (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_format'),-35,0,0,0,'audio/mpeg';
INSERT INTO format (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_format'),-35,0,0,0,'audio/wav';
UPDATE format SET value = 'application/msword' WHERE value = 'application/ms-word';
UPDATE format SET value = 'application/vnd.ms-excel' WHERE value = 'application/ms-excel';
UPDATE format SET value = 'application/vnd.ms-powerpoint' WHERE value = 'application/ms-powerpoint';

-- Illumination : ================== 
INSERT INTO illumination (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_illumination'),-35,0,0,0,'NonLinear';
INSERT INTO illumination (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_illumination'),-35,0,0,0,'Other';
INSERT INTO illumination (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_illumination'),-35,0,0,0,'Unknown';

-- Immersion : ================== 
INSERT INTO immersion (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_immersion'),-35,0,0,0,'Unknown';

-- LaserMedium : ================== 
UPDATE lasermedium SET value = 'CoumarinC30' WHERE VALUE = 'Coumaring-C30';
UPDATE lasermedium SET value = 'EMinus' WHERE VALUE = 'e-';
UPDATE lasermedium SET value = 'Rhodamine6G' WHERE VALUE = 'Rhodamine-5G';
INSERT INTO lasermedium (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_lasermedium'),-35,0,0,0,'Alexandrite';
INSERT INTO lasermedium (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_lasermedium'),-35,0,0,0,'ErGlass';
INSERT INTO lasermedium (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_lasermedium'),-35,0,0,0,'ErYAG';
INSERT INTO lasermedium (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_lasermedium'),-35,0,0,0,'HoYAG';
INSERT INTO lasermedium (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_lasermedium'),-35,0,0,0,'HoYLF';
INSERT INTO lasermedium (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_lasermedium'),-35,0,0,0,'NdGlass';
INSERT INTO lasermedium (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_lasermedium'),-35,0,0,0,'NdYAG';
INSERT INTO lasermedium (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_lasermedium'),-35,0,0,0,'Ruby';
INSERT INTO lasermedium (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_lasermedium'),-35,0,0,0,'TiSapphire';
INSERT INTO lasermedium (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_lasermedium'),-35,0,0,0,'Other';
INSERT INTO lasermedium (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_lasermedium'),-35,0,0,0,'Unknown';

-- LaserType : ================== 
INSERT INTO lasertype (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_lasertype'),-35,0,0,0,'Other';
INSERT INTO lasertype (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_lasertype'),-35,0,0,0,'Unknown';

-- Medium : ================== 
INSERT INTO medium (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_medium'),-35,0,0,0,'Other';
INSERT INTO medium (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_medium'),-35,0,0,0,'Unknown';
    
-- MicrobeamManipulationType : ================== 
INSERT INTO microbeammanipulationtype (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_microbeammanipulationtype'),-35,0,0,0,'FRAP';
INSERT INTO microbeammanipulationtype (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_microbeammanipulationtype'),-35,0,0,0,'OpticalTrapping';
INSERT INTO microbeammanipulationtype (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_microbeammanipulationtype'),-35,0,0,0,'Other';
INSERT INTO microbeammanipulationtype (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_microbeammanipulationtype'),-35,0,0,0,'Photoablation';
INSERT INTO microbeammanipulationtype (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_microbeammanipulationtype'),-35,0,0,0,'Photoactivation';
INSERT INTO microbeammanipulationtype (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_microbeammanipulationtype'),-35,0,0,0,'Uncaging';
INSERT INTO microbeammanipulationtype (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_microbeammanipulationtype'),-35,0,0,0,'Unknown';

-- MicroscopeType : ================== 
INSERT INTO microscopetype (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_microscopetype'),-35,0,0,0,'Other';
INSERT INTO microscopetype (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_microscopetype'),-35,0,0,0,'Unknown';

-- Pulse : ================== 
UPDATE pulse SET value = 'ModeLocked' WHERE VALUE = 'Mode-Locked';
UPDATE pulse SET value = 'QSwitched' WHERE VALUE = 'Q-Switched';
INSERT INTO pulse (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_pulse'),-35,0,0,0,'Other';
INSERT INTO pulse (id,permissions,owner_id,group_id,creation_id,value) SELECT ome_nextval('seq_pulse'),-35,0,0,0,'Unknown';

-- RenderingModel : ================== 
UPDATE renderingdef SET model = (SELECT id FROM renderingmodel WHERE value = 'rgb') WHERE model = (SELECT id FROM renderingmodel WHERE value = 'hsb');
DELETE FROM renderingmodel WHERE VALUE = 'hsb';

--
-- Table modifications
--

ALTER TABLE instrument
	ALTER COLUMN microscope DROP NOT NULL;

ALTER TABLE laser
	ADD COLUMN repetitionrate double precision,
	ADD COLUMN tuneable boolean,
	ADD COLUMN fm_tmp integer,
	ALTER COLUMN frequencymultiplication DROP NOT NULL,
	ALTER COLUMN pockelcell DROP NOT NULL;
UPDATE laser SET tuneable = tunable, fm_tmp = (
        SELECT CASE value WHEN 'x1' THEN 1 WHEN 'x2' THEN 2 WHEN 'x3' THEN 3 WHEN 'x4' THEN 4 ELSE -1 END
        FROM frequencymultiplication WHERE id = frequencymultiplication
    );
ALTER TABLE laser
        DROP COLUMN tunable,
	DROP COLUMN frequencymultiplication;
ALTER TABLE laser ADD COLUMN frequencymultiplication integer;
UPDATE laser SET frequencymultiplication = fm_tmp;
ALTER TABLE laser DROP COLUMN fm_tmp;

ALTER TABLE lightsettings
	ADD COLUMN attenuation double precision,
	ADD COLUMN wavelength integer,
	ADD COLUMN microbeammanipulation bigint;
ALTER TABLE lightsettings
	DROP COLUMN power,
	DROP COLUMN technique;

ALTER TABLE lightsource
	ALTER COLUMN manufacturer DROP NOT NULL,
	ALTER COLUMN model DROP NOT NULL,
	ALTER COLUMN power DROP NOT NULL;

ALTER TABLE logicalchannel
	ADD COLUMN samplesperpixel integer,
	ADD COLUMN filterset bigint,
	ADD COLUMN lightsourcesettings bigint,
	ADD COLUMN secondaryemissionfilter bigint,
	ADD COLUMN secondaryexcitationfilter bigint,
	ALTER COLUMN pinholesize TYPE double precision; -- This conversion should be safe
UPDATE logicalchannel SET lightsourcesettings = lightsource;
UPDATE lightsettings SET microbeammanipulation = (SELECT auxlightsource FROM logicalchannel WHERE id = lightsource);
ALTER TABLE logicalchannel
	DROP COLUMN auxlightsource,
	DROP COLUMN lightsource;

ALTER TABLE microscope
	ALTER COLUMN manufacturer DROP NOT NULL,
	ALTER COLUMN model DROP NOT NULL;

UPDATE objective SET immersion = (SELECT id FROM immersion WHERE value = 'Unknown') WHERE immersion IS NULL;
ALTER TABLE objective
	ADD COLUMN calibratedmagnification double precision,
	ADD COLUMN iris boolean,
	ADD COLUMN nominalmagnification integer,
	ADD COLUMN workingdistance double precision,
	ADD COLUMN correction bigint NOT NULL,
	ALTER COLUMN lensna DROP NOT NULL,
	ALTER COLUMN manufacturer DROP NOT NULL,
	ALTER COLUMN model DROP NOT NULL,
	ALTER COLUMN immersion SET NOT NULL;
-- Skipping since this table has been asserted empty. See above.
-- UPDATE objective SET nominalmagnification = magnification;
-- UPDATE objective SET correction = (SELECT c2.id FROM correction c2, coating c1 WHERE c2.value = c1.value);
ALTER TABLE objective
	DROP COLUMN magnificiation,
	DROP COLUMN coating;

ALTER TABLE otf
	ADD COLUMN opticalaxisaveraged boolean NOT NULL,
	ADD COLUMN filterset bigint,
	ADD COLUMN instrument bigint NOT NULL,
	ADD COLUMN objective bigint NOT NULL,
	ADD COLUMN pixelstype bigint NOT NULL;
UPDATE otf SET opticalaxisaveraged = opticalaxisavg;
UPDATE otf SET pixelstype = pixeltype;
ALTER TABLE otf
	DROP COLUMN opticalaxisavg,
	DROP COLUMN pixeltype;


ALTER TABLE pixels
	ADD COLUMN physicalsizex double precision,
	ADD COLUMN physicalsizey double precision,
	ADD COLUMN physicalsizez double precision,
	ADD COLUMN timeincrement double precision,
	ADD COLUMN waveincrement integer,
	ADD COLUMN wavestart integer;
UPDATE pixels SET physicalsizex = (SELECT sizex FROM pixelsdimensions WHERE id = pixels.pixelsdimensions);
UPDATE pixels SET physicalsizey = (SELECT sizey FROM pixelsdimensions WHERE id = pixels.pixelsdimensions);
UPDATE pixels SET physicalsizez = (SELECT sizez FROM pixelsdimensions WHERE id = pixels.pixelsdimensions);
ALTER TABLE pixels DROP COLUMN pixelsdimensions;


ALTER TABLE planeinfo ADD COLUMN deltat double precision;
UPDATE planeinfo SET deltat = timestamp;
ALTER TABLE planeinfo DROP COLUMN timestamp;

ALTER TABLE renderingdef ADD COLUMN compression double precision;

ALTER TABLE roi
	DROP COLUMN c,
	DROP COLUMN t,
	DROP COLUMN visible,
	DROP COLUMN xml,
	DROP COLUMN z,
	DROP COLUMN pixels,
	ADD COLUMN microbeammanipulation bigint;

-- These are out of alphabetic order because they are bigger tables and take longer to process

ALTER TABLE annotation
	ADD COLUMN description text,
	ADD COLUMN version integer,
	ADD COLUMN update_id bigint;
UPDATE annotation SET discriminator = '/basic/text/comment/' WHERE discriminator = '/basic/text/';
UPDATE annotation SET discriminator = '/basic/text/uri/' WHERE discriminator = '/basic/text/ur;/';
UPDATE annotation SET update_id = creation_id;
ALTER TABLE annotation ALTER COLUMN update_id SET NOT NULL;


ALTER TABLE channel
	ADD COLUMN alpha integer,
	ADD COLUMN blue integer,
	ADD COLUMN green integer,
	ADD COLUMN red integer,
	ALTER COLUMN logicalchannel SET NOT NULL;
UPDATE channel SET alpha = (SELECT alpha FROM color WHERE id = channel.colorcomponent);
UPDATE channel SET blue = (SELECT blue FROM color WHERE id = channel.colorcomponent);
UPDATE channel SET green = (SELECT green FROM color WHERE id = channel.colorcomponent);
UPDATE channel SET red = (SELECT red FROM color WHERE id = channel.colorcomponent);
ALTER TABLE channel DROP COLUMN colorcomponent;

ALTER TABLE channelbinding
	ADD COLUMN alpha integer,
	ADD COLUMN blue integer,
	ADD COLUMN green integer,
	ADD COLUMN red integer;
UPDATE channelbinding SET alpha = (SELECT alpha FROM color WHERE id = channelbinding.color);
UPDATE channelbinding SET blue = (SELECT blue FROM color WHERE id = channelbinding.color);
UPDATE channelbinding SET green = (SELECT green FROM color WHERE id = channelbinding.color);
UPDATE channelbinding SET red = (SELECT red FROM color WHERE id = channelbinding.color);
ALTER TABLE channelbinding DROP COLUMN color;
ALTER TABLE channelbinding ALTER alpha SET NOT NULL;
ALTER TABLE channelbinding ALTER blue SET NOT NULL;
ALTER TABLE channelbinding ALTER green SET NOT NULL;
ALTER TABLE channelbinding ALTER red SET NOT NULL;


ALTER TABLE detector
	ADD COLUMN amplificationgain double precision,
	ADD COLUMN zoom double precision,
	ALTER COLUMN manufacturer DROP NOT NULL,
	ALTER COLUMN model DROP NOT NULL;
UPDATE detector SET amplificationgain = (SELECT amplification FROM detectorsettings WHERE id = detectorsettings.detector);
ALTER TABLE detectorsettings
	DROP COLUMN amplification;

ALTER TABLE dichroic
	ADD COLUMN lotnumber character varying(255),
	ADD COLUMN manufacturer character varying(255),
	ADD COLUMN model character varying(255),
	ADD COLUMN instrument bigint NOT NULL; -- Ignoring since this table is empty

ALTER TABLE filter
	ADD COLUMN filterwheel character varying(255),
	ADD COLUMN lotnumber character varying(255),
	ADD COLUMN manufacturer character varying(255),
	ADD COLUMN model character varying(255),
	ADD COLUMN transmittancerange bigint,
	ADD COLUMN type bigint;
ALTER TABLE filter
	DROP COLUMN customized,
	DROP COLUMN customizedfilterset,
	DROP COLUMN filterset;

ALTER TABLE filterset
	ADD COLUMN lotnumber character varying(255),
	ADD COLUMN dichroic bigint,
	ADD COLUMN emfilter bigint,
	ADD COLUMN exfilter bigint,
	ADD COLUMN instrument bigint NOT NULL,
	ALTER COLUMN manufacturer DROP NOT NULL,
	ALTER COLUMN model DROP NOT NULL;
UPDATE filterset SET lotnumber = serialnumber;
ALTER TABLE filterset
	DROP COLUMN serialnumber,
	DROP COLUMN transmittancerange;


ALTER TABLE image
	ADD COLUMN acquisitiondate timestamp without time zone,
	ADD COLUMN experiment bigint,
	ADD COLUMN imagingenvironment bigint,
	ADD COLUMN instrument bigint,
	ADD COLUMN stagelabel bigint,
	ADD COLUMN archived bool;
UPDATE image SET imagingenvironment = condition,
    experiment = context,
    stageLabel = position,
    instrument = setup,
    acquisitiondate = now();
ALTER TABLE image
	DROP COLUMN condition,
	DROP COLUMN context,
	DROP COLUMN position,
	DROP COLUMN setup,
        ALTER COLUMN acquisitiondate SET NOT NULL;


-- Same insert as is done in data.sql
INSERT INTO node (id,permissions,uuid,conn,up,down) SELECT 0,-35,'000000000000000000000000000000000000','unknown',now(),now();
ALTER TABLE session ALTER COLUMN message TYPE text;
ALTER TABLE session
	ADD COLUMN node bigint,
	ADD COLUMN owner bigint;
UPDATE session SET node = 0, owner = 0;
ALTER TABLE session
	ALTER COLUMN node SET NOT NULL,
	ALTER COLUMN owner SET NOT NULL;

ALTER TABLE stagelabel
	ALTER COLUMN positionx DROP NOT NULL,
	ALTER COLUMN positiony DROP NOT NULL,
	ALTER COLUMN positionz DROP NOT NULL;

ALTER TABLE transmittancerange
	ALTER COLUMN cutin DROP NOT NULL,
	ALTER COLUMN cutout DROP NOT NULL,
	ALTER COLUMN transmittance DROP NOT NULL;

ALTER TABLE wellsample
        ADD COLUMN well_index integer NOT NULL;

ALTER TABLE seq_table
        DROP CONSTRAINT seq_table_pkey;

ALTER TABLE wellsample
        ADD CONSTRAINT wellsample_well_key UNIQUE (well, well_index);

CREATE OR REPLACE FUNCTION wellsample_well_index_move() RETURNS "trigger"
    AS '
    DECLARE
      duplicate INT8;
    BEGIN

      -- Avoids a query if the new and old values of x are the same.
      IF new.well = old.well AND new.well_index = old.well_index THEN
          RETURN new;
      END IF;

      -- At most, there should be one duplicate
      SELECT id INTO duplicate
        FROM wellsample
       WHERE well = new.well AND well_index = new.well_index
      OFFSET 0
       LIMIT 1;

      IF duplicate IS NOT NULL THEN
          RAISE NOTICE ''Remapping wellsample % via (-1 - oldvalue )'', duplicate;
          UPDATE wellsample SET well_index = -1 - well_index WHERE id = duplicate;
      END IF;

      RETURN new;
    END;'
LANGUAGE plpgsql;

CREATE TRIGGER wellsample_well_index_trigger
        BEFORE UPDATE ON wellsample
        FOR EACH ROW
        EXECUTE PROCEDURE wellsample_well_index_move();

--
-- Add constraints
--
ALTER TABLE correction ADD CONSTRAINT correction_pkey PRIMARY KEY (id);
ALTER TABLE nodeannotationlink ADD CONSTRAINT nodeannotationlink_pkey PRIMARY KEY (id);
ALTER TABLE lightemittingdiode ADD CONSTRAINT lightemittingdiode_pkey PRIMARY KEY (lightsource_id);
ALTER TABLE microbeammanipulation ADD CONSTRAINT microbeammanipulation_pkey PRIMARY KEY (id);
ALTER TABLE microbeammanipulationtype ADD CONSTRAINT microbeammanipulationtype_pkey PRIMARY KEY (id);
ALTER TABLE node ADD CONSTRAINT node_pkey PRIMARY KEY (id);
ALTER TABLE annotation ADD CONSTRAINT fkannotation_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);
ALTER TABLE correction ADD CONSTRAINT correction_external_id_key UNIQUE (external_id);
ALTER TABLE correction ADD CONSTRAINT correction_value_key UNIQUE (value);
ALTER TABLE correction ADD CONSTRAINT fkcorrection_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);
ALTER TABLE correction ADD CONSTRAINT fkcorrection_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);
ALTER TABLE correction ADD CONSTRAINT fkcorrection_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);
ALTER TABLE correction ADD CONSTRAINT fkcorrection_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);
ALTER TABLE nodeannotationlink ADD CONSTRAINT nodeannotationlink_external_id_key UNIQUE (external_id);
ALTER TABLE nodeannotationlink ADD CONSTRAINT nodeannotationlink_parent_key UNIQUE (parent, child);
ALTER TABLE nodeannotationlink ADD CONSTRAINT fknodeannotationlink_child_annotation FOREIGN KEY (child) REFERENCES annotation(id);
ALTER TABLE nodeannotationlink ADD CONSTRAINT fknodeannotationlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);
ALTER TABLE nodeannotationlink ADD CONSTRAINT fknodeannotationlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);
ALTER TABLE nodeannotationlink ADD CONSTRAINT fknodeannotationlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);
ALTER TABLE nodeannotationlink ADD CONSTRAINT fknodeannotationlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);
ALTER TABLE nodeannotationlink ADD CONSTRAINT fknodeannotationlink_parent_node FOREIGN KEY (parent) REFERENCES node(id);
ALTER TABLE nodeannotationlink ADD CONSTRAINT fknodeannotationlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);
ALTER TABLE dichroic ADD CONSTRAINT fkdichroic_instrument_instrument FOREIGN KEY (instrument) REFERENCES instrument(id);
ALTER TABLE filter ADD CONSTRAINT fkfilter_transmittancerange_transmittancerange FOREIGN KEY (transmittancerange) REFERENCES transmittancerange(id);
ALTER TABLE filter ADD CONSTRAINT fkfilter_type_filtertype FOREIGN KEY ("type") REFERENCES filtertype(id);
ALTER TABLE filterset ADD CONSTRAINT fkfilterset_dichroic_dichroic FOREIGN KEY (dichroic) REFERENCES dichroic(id);
ALTER TABLE filterset ADD CONSTRAINT fkfilterset_emfilter_filter FOREIGN KEY (emfilter) REFERENCES filter(id);
ALTER TABLE filterset ADD CONSTRAINT fkfilterset_exfilter_filter FOREIGN KEY (exfilter) REFERENCES filter(id);
ALTER TABLE filterset ADD CONSTRAINT fkfilterset_instrument_instrument FOREIGN KEY (instrument) REFERENCES instrument(id);
ALTER TABLE image ADD CONSTRAINT fkimage_experiment_experiment FOREIGN KEY (experiment) REFERENCES experiment(id);
ALTER TABLE image ADD CONSTRAINT fkimage_imagingenvironment_imagingenvironment FOREIGN KEY (imagingenvironment) REFERENCES imagingenvironment(id);
ALTER TABLE image ADD CONSTRAINT fkimage_instrument_instrument FOREIGN KEY (instrument) REFERENCES instrument(id);
ALTER TABLE image ADD CONSTRAINT fkimage_stagelabel_stagelabel FOREIGN KEY (stagelabel) REFERENCES stagelabel(id);
ALTER TABLE laser ADD CONSTRAINT fklaser_pump_lightsource FOREIGN KEY (pump) REFERENCES lightsource(id);
ALTER TABLE lightemittingdiode ADD CONSTRAINT fklightemittingdiode_lightsource_id_lightsource FOREIGN KEY (lightsource_id) REFERENCES lightsource(id);
ALTER TABLE lightsettings ADD CONSTRAINT fklightsettings_microbeammanipulation_microbeammanipulation FOREIGN KEY (microbeammanipulation) REFERENCES microbeammanipulation(id);
ALTER TABLE logicalchannel ADD CONSTRAINT fklogicalchannel_filterset_filterset FOREIGN KEY (filterset) REFERENCES filterset(id);
ALTER TABLE logicalchannel ADD CONSTRAINT fklogicalchannel_lightsourcesettings_lightsettings FOREIGN KEY (lightsourcesettings) REFERENCES lightsettings(id);
ALTER TABLE logicalchannel ADD CONSTRAINT fklogicalchannel_secondaryemissionfilter_filter FOREIGN KEY (secondaryemissionfilter) REFERENCES filter(id);
ALTER TABLE logicalchannel ADD CONSTRAINT fklogicalchannel_secondaryexcitationfilter_filter FOREIGN KEY (secondaryexcitationfilter) REFERENCES filter(id);
ALTER TABLE microbeammanipulation ADD CONSTRAINT microbeammanipulation_external_id_key UNIQUE (external_id);
ALTER TABLE microbeammanipulation ADD CONSTRAINT fkmicrobeammanipulation_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);
ALTER TABLE microbeammanipulation ADD CONSTRAINT fkmicrobeammanipulation_experiment_experiment FOREIGN KEY (experiment) REFERENCES experiment(id);
ALTER TABLE microbeammanipulation ADD CONSTRAINT fkmicrobeammanipulation_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);
ALTER TABLE microbeammanipulation ADD CONSTRAINT fkmicrobeammanipulation_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);
ALTER TABLE microbeammanipulation ADD CONSTRAINT fkmicrobeammanipulation_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);
ALTER TABLE microbeammanipulation ADD CONSTRAINT fkmicrobeammanipulation_type_microbeammanipulationtype FOREIGN KEY ("type") REFERENCES microbeammanipulationtype(id);
ALTER TABLE microbeammanipulation ADD CONSTRAINT fkmicrobeammanipulation_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);
ALTER TABLE microbeammanipulationtype ADD CONSTRAINT microbeammanipulationtype_external_id_key UNIQUE (external_id);
ALTER TABLE microbeammanipulationtype ADD CONSTRAINT microbeammanipulationtype_value_key UNIQUE (value);
ALTER TABLE microbeammanipulationtype ADD CONSTRAINT fkmicrobeammanipulationtype_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);
ALTER TABLE microbeammanipulationtype ADD CONSTRAINT fkmicrobeammanipulationtype_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);
ALTER TABLE microbeammanipulationtype ADD CONSTRAINT fkmicrobeammanipulationtype_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);
ALTER TABLE microbeammanipulationtype ADD CONSTRAINT fkmicrobeammanipulationtype_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);
ALTER TABLE node ADD CONSTRAINT node_external_id_key UNIQUE (external_id);
ALTER TABLE node ADD CONSTRAINT fknode_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);
ALTER TABLE objective ADD CONSTRAINT fkobjective_correction_correction FOREIGN KEY (correction) REFERENCES correction(id);
ALTER TABLE otf ADD CONSTRAINT fkotf_filterset_filterset FOREIGN KEY (filterset) REFERENCES filterset(id);
ALTER TABLE otf ADD CONSTRAINT fkotf_instrument_instrument FOREIGN KEY (instrument) REFERENCES instrument(id);
ALTER TABLE otf ADD CONSTRAINT fkotf_objective_objective FOREIGN KEY (objective) REFERENCES objective(id);
ALTER TABLE otf ADD CONSTRAINT fkotf_pixelstype_pixelstype FOREIGN KEY (pixelstype) REFERENCES pixelstype(id);
ALTER TABLE roi ADD CONSTRAINT fkroi_microbeammanipulation_microbeammanipulation FOREIGN KEY (microbeammanipulation) REFERENCES microbeammanipulation(id);
ALTER TABLE session ADD CONSTRAINT fksession_node_node FOREIGN KEY (node) REFERENCES node(id);
ALTER TABLE session ADD CONSTRAINT fksession_owner_experimenter FOREIGN KEY ("owner") REFERENCES experimenter(id);

--
-- Missing views
--

  CREATE OR REPLACE VIEW count_Node_annotationLinks_by_owner (Node_id, owner_id, count) AS select parent, owner_id, count(*)
      FROM NodeAnnotationLink GROUP BY parent, owner_id ORDER BY parent;

--
-- #1191 Annotation changes
--
UPDATE image
   SET archived = true
  FROM pixels p, pixelsannotationlink pal, annotation a
 WHERE image.id = p.image
   AND p.id = pal.parent
   AND pal.child = a.id
   AND a.ns = 'openmicroscopy.org/omero/importer/archived';

DELETE FROM pixelsannotationlink
      WHERE child in
	  (
      SELECT id FROM annotation where ns = 'openmicroscopy.org/omero/importer/archived'
	  );

      UPDATE annotation
         SET ns = 'openmicroscopy.org/omero/insight/tagset'
        FROM annotationannotationlink aal, annotation tag
       WHERE annotation.discriminator = '/basic/text/tag/'
         AND annotation.ns IS NULL
         AND tag.discriminator = '/basic/text/tag/'
         AND annotation.id = aal.parent
         AND tag.id = aal.child;

--
-- #1197 Adding share-member-links
--

    create table sharemember (
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
        unique (parent, child)
    );;

    alter table sharemember 
        add constraint FKsharemember_child_experimenter 
        foreign key (child) 
        references experimenter;;

    alter table sharemember 
        add constraint FKsharemember_update_id_event 
        foreign key (update_id) 
        references event;;

    alter table sharemember 
        add constraint FKsharemember_owner_id_experimenter 
        foreign key (owner_id) 
        references experimenter;;

    alter table sharemember 
        add constraint FKsharemember_creation_id_event 
        foreign key (creation_id) 
        references event;;

    alter table sharemember 
        add constraint FKsharemember_parent_session 
        foreign key (parent) 
        references session;;

    alter table sharemember 
        add constraint FKsharemember_group_id_experimentergroup 
        foreign key (group_id) 
        references experimentergroup;;

    alter table sharemember 
        add constraint FKsharemember_external_id_externalinfo 
        foreign key (external_id) 
        references externalinfo;;


--
-- Finally, dropping tables
--

DROP TABLE categorygroupcategorylink CASCADE;
DROP TABLE categoryimagelink CASCADE;
DROP TABLE category CASCADE;
DROP TABLE categorygroup CASCADE;

DROP TABLE roi CASCADE;
DROP TABLE roilink CASCADE;
DROP TABLE roilinkannotationlink CASCADE;

DROP TABLE aberrationcorrection;
DROP TABLE cellarea;
DROP TABLE celleccentricity;
DROP TABLE cellextent;
DROP TABLE cellmajoraxislength;
DROP TABLE cellminoraxislength;
DROP TABLE cellperimeter;
DROP TABLE cellposition;
DROP TABLE cellsolidity;
DROP TABLE coating;
DROP TABLE color;
DROP TABLE customizedfilterset;
DROP TABLE emissionfilter;
DROP TABLE excitationfilter;
DROP TABLE frequencymultiplication;
DROP TABLE imagecellcount;
DROP TABLE imagenucleascount;
DROP TABLE irisdiaphragm;
DROP TABLE nucleusarea;
DROP TABLE nucleuseccentricity;
DROP TABLE nucleusextent;
DROP TABLE nucleusmajoraxislength;
DROP TABLE nucleusminoraxislength;
DROP TABLE nucleusperimeter;
DROP TABLE nucleusposition;
DROP TABLE nucleussolidity;
DROP TABLE pixelsdimensions;

--
-- and lowering permissions (#1191)
--

UPDATE annotation
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE annotationannotationlink
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE channel
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE channelannotationlink
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE channelbinding
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE dataset
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE datasetannotationlink
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE datasetimagelink
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE dbpatch
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE detector
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE detectorsettings
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE dichroic
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE event
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE eventlog
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE experiment
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE experimenter
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111111011101' )) as int8);
UPDATE experimenterannotationlink
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE experimentergroup
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111111011101' )) as int8);
UPDATE experimentergroupannotationlink
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE externalinfo
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE filter
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE filterset
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE groupexperimentermap
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111111011101' )) as int8);
UPDATE image
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE imageannotationlink
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE imagingenvironment
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE instrument
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE job
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE joboriginalfilelink
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE lightsettings
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE lightsource
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE link
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE logicalchannel
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE microbeammanipulation
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE microscope
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE node
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE nodeannotationlink
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE objective
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE objectivesettings
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE originalfile
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE originalfileannotationlink
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE otf
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE pixels
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE pixelsannotationlink
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE pixelsoriginalfilemap
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE planeinfo
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE planeinfoannotationlink
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE plate
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE plateannotationlink
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE project
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE projectannotationlink
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE projectdatasetlink
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE quantumdef
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE reagent
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE reagentannotationlink
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE renderingdef
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE screen
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE screenacquisition
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE screenacquisitionannotationlink
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE screenacquisitionwellsamplelink
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE screenannotationlink
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE screenplatelink
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE session
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE sessionannotationlink
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE stagelabel
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE statsinfo
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE thumbnail
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE transmittancerange
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE well
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE wellannotationlink
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE wellreagentlink
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE wellsample
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);
UPDATE wellsampleannotationlink
   SET permissions =
       cast((
         (permissions::bit(64) << 53) |
         (b'0000000000011111111111111111111111111111111111111111111111111111' &
          b'1111111111111111111111111111111111111111111111111111111110011001' )) as int8);

CREATE OR REPLACE FUNCTION ome_perms(p INT8) RETURNS VARCHAR AS '
DECLARE
    ln CHAR DEFAULT ''-'';
    ur CHAR DEFAULT ''-'';
    uw CHAR DEFAULT ''-'';
    gr CHAR DEFAULT ''-'';
    gw CHAR DEFAULT ''-'';
    wr CHAR DEFAULT ''-'';
    ww CHAR DEFAULT ''-'';
BEGIN
    -- shift 8
    SELECT INTO ur CASE WHEN (cast(p as bit(64)) & cast(1024 as bit(64))) = cast(1024 as bit(64)) THEN ''r'' ELSE ''-'' END;
    SELECT INTO uw CASE WHEN (cast(p as bit(64)) & cast( 512 as bit(64))) = cast( 512 as bit(64)) THEN ''w'' ELSE ''-'' END;
    -- shift 4
    SELECT INTO gr CASE WHEN (cast(p as bit(64)) & cast(  64 as bit(64))) = cast(  64 as bit(64)) THEN ''r'' ELSE ''-'' END;
    SELECT INTO gw CASE WHEN (cast(p as bit(64)) & cast(  32 as bit(64))) = cast(  32 as bit(64)) THEN ''w'' ELSE ''-'' END;
    -- shift 0
    SELECT INTO wr CASE WHEN (cast(p as bit(64)) & cast(   4 as bit(64))) = cast(   4 as bit(64)) THEN ''r'' ELSE ''-'' END;
    SELECT INTO ww CASE WHEN (cast(p as bit(64)) & cast(   2 as bit(64))) = cast(   2 as bit(64)) THEN ''w'' ELSE ''-'' END;

    -- shift 18
    -- for high-order bits, logic is reversed
    SELECT INTO ln CASE WHEN (cast(p as bit(64)) & cast(262144 as bit(64))) = cast(262144 as bit(64)) THEN ''-'' ELSE ''L'' END;

    RETURN ln || ur || uw || gr || gw || wr || ww;
 END;' LANGUAGE plpgsql;

UPDATE dbpatch set message = 'Database updated.', finished = now()
 WHERE currentVersion  = 'OMERO4'  and
          currentPatch    = 0         and
          previousVersion = 'OMERO3A' and
          previousPatch   = 11;

COMMIT;

