BEGIN; 

-- Calculated by apg_diff

ALTER TABLE objective
	DROP COLUMN correctioncollar,
	DROP COLUMN irisdiaphragm,
	DROP COLUMN aberrationcorrection,
	DROP COLUMN immersionmedium,
	DROP COLUMN phasecondenserannulus,
	ALTER COLUMN serialnumber DROP NOT NULL;


DROP SEQUENCE seq_acquisitioncontext;

DROP SEQUENCE seq_correctioncollar;

DROP SEQUENCE seq_dyelasermedia;

DROP SEQUENCE seq_excimerlasermedia;

DROP SEQUENCE seq_freeelectronlasermedia;

DROP SEQUENCE seq_gaslasermedia;

DROP SEQUENCE seq_immersionmedium;

DROP SEQUENCE seq_metalvaporlasermedia;

DROP SEQUENCE seq_semiconductorlasermedia;

DROP TABLE acquisitioncontext CASCADE;

DROP TABLE correctioncollar CASCADE;

DROP TABLE dyelaser CASCADE;

DROP TABLE dyelasermedia CASCADE;

DROP TABLE excimerlaser CASCADE;

DROP TABLE excimerlasermedia CASCADE;

DROP TABLE freeelectronlaser CASCADE;

DROP TABLE freeelectronlasermedia CASCADE;

DROP TABLE gaslaser CASCADE;

DROP TABLE gaslasermedia CASCADE;

DROP TABLE immersionmedium CASCADE;

DROP TABLE metalvaporlaser CASCADE;

DROP TABLE metalvaporlasermedia CASCADE;

DROP TABLE semiconductorlaser CASCADE;

DROP TABLE semiconductorlasermedia CASCADE;

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

UPDATE event set permissions = -35;
UPDATE eventlog set permissions = -35;
UPDATE experimenter set permissions = -35;

ALTER TABLE event
	ALTER COLUMN permissions SET NOT NULL;

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

COMMIT;
