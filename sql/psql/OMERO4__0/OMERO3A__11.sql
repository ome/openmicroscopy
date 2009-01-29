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
        RAISE EXCEPTION ''Table is not empty: % Please contact the OME developers for more information -- http://www.openmicroscopy.org/site/community'', tbl;
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
-- Sequences can be trivial dropped since we won't be creating more rows
--
DROP SEQUENCE seq_aberrationcorrection;
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
DROP SEQUENCE seq_coating;
DROP SEQUENCE seq_color;
DROP SEQUENCE seq_customizedfilterset;
DROP SEQUENCE seq_emissionfilter;
DROP SEQUENCE seq_excitationfilter;
DROP SEQUENCE seq_frequencymultiplication;
DROP SEQUENCE seq_imagecellcount;
DROP SEQUENCE seq_imagenucleascount;
DROP SEQUENCE seq_irisdiaphragm;
DROP SEQUENCE seq_nucleusarea;
DROP SEQUENCE seq_nucleuseccentricity;
DROP SEQUENCE seq_nucleusextent;
DROP SEQUENCE seq_nucleusmajoraxislength;
DROP SEQUENCE seq_nucleusminoraxislength;
DROP SEQUENCE seq_nucleusperimeter;
DROP SEQUENCE seq_nucleusposition;
DROP SEQUENCE seq_nucleussolidity;
DROP SEQUENCE seq_pixelsdimensions;
DROP SEQUENCE seq_roi;
DROP SEQUENCE seq_roilink;
DROP SEQUENCE seq_roilinkannotationlink;

--
-- New sequences
--
CREATE SEQUENCE seq_correction INCREMENT BY 1 NO MAXVALUE NO MINVALUE CACHE 1; 
CREATE SEQUENCE seq_microbeammanipulation START WITH 1 INCREMENT BY 1 NO MAXVALUE NO MINVALUE CACHE 1;
CREATE SEQUENCE seq_microbeammanipulationtype INCREMENT BY 1 NO MAXVALUE NO MINVALUE CACHE 1; 
CREATE SEQUENCE seq_node START WITH 1 INCREMENT BY 1 NO MAXVALUE NO MINVALUE CACHE 1;
CREATE SEQUENCE seq_nodeannotationlink START WITH 1 INCREMENT BY 1 NO MAXVALUE NO MINVALUE CACHE 1;


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
-- Fixing enumerations for later use by the table modifications
--
-- AcquisitionMode : ==================
INSERT INTO acquisitionmode (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_acquisitionmode'),-35,0,0,0,'Other';
INSERT INTO acquisitionmode (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_acquisitionmode'),-35,0,0,0,'Unknown';
UPDATE acquisitionmode SET value = 'WideField' WHERE value = 'Wide-field';

-- ArcType : ==================
INSERT INTO arctype (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_arctype'),-35,0,0,0,'Unknown';
UPDATE arctype SET value = 'HgXe' where VALUE = 'Hg-Xe';

-- ContractMethod : ==================
INSERT INTO contrastmethod (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_contrastmethod'),-35,0,0,0,'Unknown';
INSERT INTO contrastmethod (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_contrastmethod'),-35,0,0,0,'Other';

-- Correction : ================== (ADDED)
INSERT INTO correction (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_correction'),-35,0,0,0,'Achro';
INSERT INTO correction (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_correction'),-35,0,0,0,'Achromat';
INSERT INTO correction (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_correction'),-35,0,0,0,'Apo';
INSERT INTO correction (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_correction'),-35,0,0,0,'Fl';
INSERT INTO correction (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_correction'),-35,0,0,0,'Fluar';
INSERT INTO correction (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_correction'),-35,0,0,0,'Fluor';
INSERT INTO correction (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_correction'),-35,0,0,0,'Fluotar';
INSERT INTO correction (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_correction'),-35,0,0,0,'Neofluar';
INSERT INTO correction (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_correction'),-35,0,0,0,'Other';
INSERT INTO correction (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_correction'),-35,0,0,0,'PlanApo';
INSERT INTO correction (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_correction'),-35,0,0,0,'PlanFluor';
INSERT INTO correction (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_correction'),-35,0,0,0,'SuperFluor';
INSERT INTO correction (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_correction'),-35,0,0,0,'UV';
INSERT INTO correction (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_correction'),-35,0,0,0,'Unknown';
INSERT INTO correction (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_correction'),-35,0,0,0,'VioletCorrected';

-- DetectorType : ==================
UPDATE detectortype SET value = 'AnalogVideo' WHERE value = 'Analog-Video';
UPDATE detectortype SET value = 'CorrelationSpectroscopy' WHERE value = 'Correlation-Spectroscopy';
UPDATE detectortype SET value = 'IntensifiedCCD' WHERE value = 'Intensified-CCD';
UPDATE detectortype SET value = 'LifetimeImaging' WHERE value = 'Life-time-Imaging';
INSERT INTO detectortype (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_detectortype'),-35,0,0,0,'APD';
INSERT INTO detectortype (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_detectortype'),-35,0,0,0,'CMOS';
INSERT INTO detectortype (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_detectortype'),-35,0,0,0,'EM-CCD';
INSERT INTO detectortype (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_detectortype'),-35,0,0,0,'Other';
INSERT INTO detectortype (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_detectortype'),-35,0,0,0,'Unknown';

-- ExperimentType : ================== 
INSERT INTO experimenttype (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_experimenttype'),-35,0,0,0,'Photobleaching';
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
INSERT INTO experimenttype (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_experimenttype'),-35,0,0,0,'Unknown';

-- FilamentType : ================== 
INSERT INTO filamenttype (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_filamenttype'),-35,0,0,0,'Other';
INSERT INTO filamenttype (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_filamenttype'),-35,0,0,0,'Unknown';

-- FilterType : ================== 
INSERT INTO filtertype (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_filtertype'),-35,0,0,0,'Other';
INSERT INTO filtertype (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_filtertype'),-35,0,0,0,'Unknown';

-- Format : ================== 
INSERT INTO format (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_format'),-35,0,0,0,'application/octet-stream';
UPDATE format SET value = 'application/msword' WHERE value = 'application/ms-word';
UPDATE format SET value = 'application/vnd.ms-excel' WHERE value = 'application/ms-excel';
UPDATE format SET value = 'application/vnd.ms-powerpoint' WHERE value = 'application/ms-powerpoint';

-- Illumination : ================== 
INSERT INTO illumination (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_illumination'),-35,0,0,0,'NonLinear';
INSERT INTO illumination (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_illumination'),-35,0,0,0,'Other';
INSERT INTO illumination (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_illumination'),-35,0,0,0,'Unknown';

-- Immersion : ================== 
INSERT INTO immersion (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_immersion'),-35,0,0,0,'Unknown';

-- LaserMedium : ================== 
UPDATE lasermedium SET value = 'CoumarinC30' WHERE VALUE = 'Coumaring-C30';
UPDATE lasermedium SET value = 'EMinus' WHERE VALUE = 'e-';
UPDATE lasermedium SET value = 'Rhodamine6G' WHERE VALUE = 'Rhodamine-5G';
INSERT INTO lasermedium (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_lasermedium'),-35,0,0,0,'Alexandrite';
INSERT INTO lasermedium (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_lasermedium'),-35,0,0,0,'ErGlass';
INSERT INTO lasermedium (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_lasermedium'),-35,0,0,0,'ErYAG';
INSERT INTO lasermedium (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_lasermedium'),-35,0,0,0,'HoYAG';
INSERT INTO lasermedium (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_lasermedium'),-35,0,0,0,'HoYLF';
INSERT INTO lasermedium (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_lasermedium'),-35,0,0,0,'NdGlass';
INSERT INTO lasermedium (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_lasermedium'),-35,0,0,0,'NdYAG';
INSERT INTO lasermedium (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_lasermedium'),-35,0,0,0,'Ruby';
INSERT INTO lasermedium (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_lasermedium'),-35,0,0,0,'TiSapphire';
INSERT INTO lasermedium (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_lasermedium'),-35,0,0,0,'Other';
INSERT INTO lasermedium (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_lasermedium'),-35,0,0,0,'Unknown';

-- LaserType : ================== 
INSERT INTO lasertype (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_lasertype'),-35,0,0,0,'Other';
INSERT INTO lasertype (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_lasertype'),-35,0,0,0,'Unknown';

-- Medium : ================== 
INSERT INTO medium (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_medium'),-35,0,0,0,'Other';
INSERT INTO medium (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_medium'),-35,0,0,0,'Unknown';
    
-- MicrobeamManipulationType : ================== 
INSERT INTO microbeammanipulationtype (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_microbeammanipulationtype'),-35,0,0,0,'FRAP';
INSERT INTO microbeammanipulationtype (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_microbeammanipulationtype'),-35,0,0,0,'OpticalTrapping';
INSERT INTO microbeammanipulationtype (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_microbeammanipulationtype'),-35,0,0,0,'Other';
INSERT INTO microbeammanipulationtype (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_microbeammanipulationtype'),-35,0,0,0,'Photoablation';
INSERT INTO microbeammanipulationtype (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_microbeammanipulationtype'),-35,0,0,0,'Photoactivation';
INSERT INTO microbeammanipulationtype (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_microbeammanipulationtype'),-35,0,0,0,'Uncaging';
INSERT INTO microbeammanipulationtype (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_microbeammanipulationtype'),-35,0,0,0,'Unknown';

-- MicroscopeType : ================== 
INSERT INTO microscopetype (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_microscopetype'),-35,0,0,0,'Other';
INSERT INTO microscopetype (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_microscopetype'),-35,0,0,0,'Unknown';

-- Pulse : ================== 
UPDATE pulse SET value = 'ModeLocked' WHERE VALUE = 'Mode-Locked';
UPDATE pulse SET value = 'QSwitched' WHERE VALUE = 'Q-Switched';
INSERT INTO pulse (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_pulse'),-35,0,0,0,'Other';
INSERT INTO pulse (id,permissions,owner_id,group_id,creation_id,value) SELECT NEXTVAL('seq_pulse'),-35,0,0,0,'Unknown';

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
UPDATE annotation SET discriminator = '/basic/text/comment' WHERE discriminator = '/basic/text/';
UPDATE annotation SET discriminator = '/basic/text/uri' WHERE discriminator = '/basic/text/ur;/';
UPDATE annotation SET update_id = creation_id;
ALTER TABLE annotation ALTER COLUMN update_id SET NOT NULL;


ALTER TABLE channel
	ADD COLUMN alpha integer,
	ADD COLUMN blue integer,
	ADD COLUMN green integer,
	ADD COLUMN red integer;
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
	ADD COLUMN stagelabel bigint;
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

UPDATE dbpatch set message = 'Database updated.', finished = now()
 WHERE currentVersion  = 'OMERO4'  and
          currentPatch    = 0         and
          previousVersion = 'OMERO3A' and
          previousPatch   = 11;

COMMIT;
