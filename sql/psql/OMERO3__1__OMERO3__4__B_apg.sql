
BEGIN;

ALTER TABLE image
	DROP CONSTRAINT fkimage_acquisition_objective;

ALTER TABLE objective
	DROP CONSTRAINT fkobjective_aberrationcorrection_aberrationcorrection;

ALTER TABLE objective
	DROP CONSTRAINT fkobjective_correctioncollar_correctioncollar;

ALTER TABLE objective
	DROP CONSTRAINT fkobjective_immersionmedium_immersionmedium;

ALTER TABLE objective
	DROP CONSTRAINT fkobjective_irisdiaphragm_irisdiaphragm;

ALTER TABLE pixels
	DROP CONSTRAINT fkpixels_acquisitioncontext_acquisitioncontext;

DROP SEQUENCE seq_acquisitioncontext;

DROP SEQUENCE seq_correctioncollar;

DROP SEQUENCE seq_dyelasermedia;

DROP SEQUENCE seq_excimerlasermedia;

DROP SEQUENCE seq_freeelectronlasermedia;

DROP SEQUENCE seq_gaslasermedia;

DROP SEQUENCE seq_immersionmedium;

DROP SEQUENCE seq_metalvaporlasermedia;

DROP SEQUENCE seq_semiconductorlasermedia;

DROP TABLE acquisitioncontext;

DROP TABLE correctioncollar;

DROP TABLE dyelaser;

DROP TABLE dyelasermedia;

DROP TABLE excimerlaser;

DROP TABLE excimerlasermedia;

DROP TABLE freeelectronlaser;

DROP TABLE freeelectronlasermedia;

DROP TABLE gaslaser;

DROP TABLE gaslasermedia;

DROP TABLE immersionmedium;

DROP TABLE metalvaporlaser;

DROP TABLE metalvaporlasermedia;

DROP TABLE semiconductorlaser;

DROP TABLE semiconductorlasermedia;

ALTER TABLE arc
	DROP COLUMN version;

ALTER TABLE contraststretchingcontext
	DROP COLUMN version;

ALTER TABLE customizedfilterset
	ALTER COLUMN serialnumber DROP NOT NULL;

ALTER TABLE dbpatch
	ALTER COLUMN id SET DEFAULT nextval('seq_dbpatch'::regclass),
	ALTER COLUMN permissions SET DEFAULT -35,
	ALTER COLUMN message SET DEFAULT 'Updating'::character varying;

ALTER TABLE detector
	ALTER COLUMN serialnumber DROP NOT NULL;

ALTER TABLE detectorsettings
	DROP COLUMN readrate,
	ALTER COLUMN voltage DROP NOT NULL,
	ALTER COLUMN gain DROP NOT NULL,
	ALTER COLUMN offsetvalue DROP NOT NULL;

ALTER TABLE dummystatistics
	DROP COLUMN version;

UPDATE event
	SET permissions = -35 WHERE permissions is null;

UPDATE eventlog
	SET permissions = -35 WHERE permissions is null;

UPDATE experimenter
	SET permissions = -35 WHERE permissions is null;

ALTER TABLE event
	ALTER COLUMN permissions SET NOT NULL,
	ALTER COLUMN experimentergroup SET NOT NULL,
	ALTER COLUMN type SET NOT NULL;

ALTER TABLE eventlog
	ALTER COLUMN permissions SET NOT NULL;

ALTER TABLE experimenter
	ALTER COLUMN permissions SET NOT NULL;

ALTER TABLE filament
	DROP COLUMN version;

ALTER TABLE filterset
	ALTER COLUMN serialnumber DROP NOT NULL;

ALTER TABLE image
	DROP COLUMN acquisition;

ALTER TABLE laser
	DROP COLUMN version,
	DROP COLUMN frequencydoubled,
	ALTER COLUMN type SET NOT NULL,
	ALTER COLUMN lasermedium SET NOT NULL,
	ALTER COLUMN frequencymultiplication SET NOT NULL,
	ALTER COLUMN pockelcell SET NOT NULL;

ALTER TABLE lightsource
	ALTER COLUMN power SET NOT NULL,
	ALTER COLUMN serialnumber DROP NOT NULL;

ALTER TABLE microscope
	ALTER COLUMN serialnumber DROP NOT NULL;

ALTER TABLE objective
	DROP COLUMN immersionmedium,
	DROP COLUMN aberrationcorrection,
	DROP COLUMN correctioncollar,
	DROP COLUMN phasecondenserannulus,
	DROP COLUMN irisdiaphragm,
	ALTER COLUMN serialnumber DROP NOT NULL;

ALTER TABLE originalfile
	ALTER COLUMN size TYPE bigint;

ALTER TABLE overlay
	DROP COLUMN version;

ALTER TABLE pixels
	DROP COLUMN acquisitioncontext;

ALTER TABLE planeslicingcontext
	DROP COLUMN version;

ALTER TABLE reverseintensitycontext
	DROP COLUMN version;

ALTER TABLE square
	DROP COLUMN version;

ALTER TABLE uroi
	DROP COLUMN version;

ALTER TABLE usquare
	DROP COLUMN version;

ALTER TABLE xy
	DROP COLUMN version;

ALTER TABLE xyc
	DROP COLUMN version;

ALTER TABLE xyct
	DROP COLUMN version;

ALTER TABLE xyt
	DROP COLUMN version;

ALTER TABLE xyz
	DROP COLUMN version;

ALTER TABLE xyzc
	DROP COLUMN version;

ALTER TABLE xyzct
	DROP COLUMN version;

ALTER TABLE xyzt
	DROP COLUMN version;

ALTER TABLE dbpatch
	ADD CONSTRAINT unique_dbpatch UNIQUE (currentversion, currentpatch, previousversion, previouspatch);

COMMIT;
