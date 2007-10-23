-- 
-- Copyright 2007 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

BEGIN;

 --
 -- Manually added items missed by <schemaupdate/>
 --
 alter table originalfile alter column size type int8;

 --
 -- Items calculated by <schemaupdate/>
 --
 create table binning (id int8 not null, owner_id int8 not null, group_id int8 not null, creation_id int8 not null, permissions int8 not null, external_id int8 unique, value varchar(255) not null unique, primary key (id));
 create table coating (id int8 not null, owner_id int8 not null, group_id int8 not null, creation_id int8 not null, permissions int8 not null, external_id int8 unique, value varchar(255) not null unique, primary key (id));
 alter table customizedfilterset add column transmittanceRange int8;
 create table dbpatch (id int8 not null, permissions int8 not null, external_id int8 unique, currentVersion varchar(255) not null, currentPatch int4 not null, previousVersion varchar(255) not null, previousPatch int4 not null, finished timestamp, message varchar(255), primary key (id));
 alter table detector add column voltage float4;
 alter table detector add column gain float4;
 alter table detector add column offsetValue float4;
 alter table detectorsettings add column readOutRate float4;
 alter table detectorsettings add column binning int8;
 alter table detectorsettings add column amplification float4;
 alter table emissionfilter add column transmittanceRange int8;
 alter table event add column permissions int8;
 alter table eventlog add column permissions int8;
 alter table excitationfilter add column transmittanceRange int8;
 alter table experimenter add column permissions int8;
 alter table filterset add column transmittanceRange int8;
 create table frequencymultiplication (id int8 not null, owner_id int8 not null, group_id int8 not null, creation_id int8 not null, permissions int8 not null, external_id int8 unique, value varchar(255) not null unique, primary key (id));
 alter table image add column objectiveSettings int8;
 create table immersion (id int8 not null, owner_id int8 not null, group_id int8 not null, creation_id int8 not null, permissions int8 not null, external_id int8 unique, value varchar(255) not null unique, primary key (id));
 create table importjob (job_id int8 not null, imageName varchar(255) not null, imageDescription varchar(255) not null, primary key (job_id));
 create table job (id int8 not null, owner_id int8 not null, group_id int8 not null, creation_id int8 not null, update_id int8 not null, permissions int8 not null, external_id int8 unique, version int4 default 0, username varchar(255) not null, groupname varchar(255) not null, type varchar(255) not null, message varchar(255) not null, status int8 not null, submitted timestamp not null, scheduledFor timestamp not null, started timestamp, finished timestamp, primary key (id));
 create table joboriginalfilelink (id int8 not null, owner_id int8 not null, group_id int8 not null, creation_id int8 not null, update_id int8 not null, permissions int8 not null, external_id int8 unique, version int4 default 0, parent int8 not null, child int8 not null, primary key (id));
 create table jobstatus (id int8 not null, owner_id int8 not null, group_id int8 not null, creation_id int8 not null, permissions int8 not null, external_id int8 unique, value varchar(255) not null unique, primary key (id));
 alter table laser add column type int8;
 alter table laser add column laserMedium int8;
 alter table laser add column frequencyMultiplication int8;
 alter table laser add column wavelength int4;
 alter table laser add column pockelCell bool;
 create table lasermedium (id int8 not null, owner_id int8 not null, group_id int8 not null, creation_id int8 not null, permissions int8 not null, external_id int8 unique, value varchar(255) not null unique, primary key (id));
 create table lasertype (id int8 not null, owner_id int8 not null, group_id int8 not null, creation_id int8 not null, permissions int8 not null, external_id int8 unique, value varchar(255) not null unique, primary key (id));
 alter table lightsource add column power float4;
 alter table logicalchannel add column photometricInterpretation int8;
 alter table logicalchannel add column mode int8;
 alter table logicalchannel add column pockelCellSetting varchar(255);
 create table medium (id int8 not null, owner_id int8 not null, group_id int8 not null, creation_id int8 not null, permissions int8 not null, external_id int8 unique, value varchar(255) not null unique, primary key (id));
 alter table objective add column immersion int8;
 alter table objective add column coating int8;
 create table objectivesettings (id int8 not null, owner_id int8 not null, group_id int8 not null, creation_id int8 not null, update_id int8 not null, permissions int8 not null, external_id int8 unique, version int4 default 0, correctionCollar float4, medium int8, refractiveIndex float4, objective int8 not null, primary key (id));
 alter table originalfile add column atime timestamp;
 alter table originalfile add column mtime timestamp;
 alter table originalfile add column ctime timestamp;
 alter table planeinfo add column positionX float4;
 alter table planeinfo add column positionY float4;
 alter table planeinfo add column positionZ float4;
 create table projectannotation (id int8 not null, owner_id int8 not null, group_id int8 not null, creation_id int8 not null, update_id int8 not null, permissions int8 not null, external_id int8 unique, version int4 default 0, project int8 not null, content text not null, primary key (id));
 create table transmittancerange (id int8 not null, owner_id int8 not null, group_id int8 not null, creation_id int8 not null, update_id int8 not null, permissions int8 not null, external_id int8 unique, version int4 default 0, cutIn int4 not null, cutOut int4 not null, cutInTolerance int4, cutOutTolerance int4, transmittance float4 not null, primary key (id));
 alter table binning add constraint FKbinning_owner_id_experimenter foreign key (owner_id) references experimenter;
 alter table binning add constraint FKbinning_creation_id_event foreign key (creation_id) references event;
 alter table binning add constraint FKbinning_external_id_externalinfo foreign key (external_id) references externalinfo;
 alter table binning add constraint FKbinning_group_id_experimentergroup foreign key (group_id) references experimentergroup;
 alter table coating add constraint FKcoating_owner_id_experimenter foreign key (owner_id) references experimenter;
 alter table coating add constraint FKcoating_creation_id_event foreign key (creation_id) references event;
 alter table coating add constraint FKcoating_external_id_externalinfo foreign key (external_id) references externalinfo;
 alter table coating add constraint FKcoating_group_id_experimentergroup foreign key (group_id) references experimentergroup;
 alter table customizedfilterset add constraint FKcustomizedfilterset_transmittanceRange_transmittancerange foreign key (transmittanceRange) references transmittancerange;
 alter table dbpatch add constraint FKdbpatch_external_id_externalinfo foreign key (external_id) references externalinfo;
 alter table detectorsettings add constraint FKdetectorsettings_binning_binning foreign key (binning) references binning;
 alter table emissionfilter add constraint FKemissionfilter_transmittanceRange_transmittancerange foreign key (transmittanceRange) references transmittancerange;
 alter table excitationfilter add constraint FKexcitationfilter_transmittanceRange_transmittancerange foreign key (transmittanceRange) references transmittancerange;
 alter table filterset add constraint FKfilterset_transmittanceRange_transmittancerange foreign key (transmittanceRange) references transmittancerange;
 alter table frequencymultiplication add constraint FKfrequencymultiplication_owner_id_experimenter foreign key (owner_id) references experimenter;
 alter table frequencymultiplication add constraint FKfrequencymultiplication_creation_id_event foreign key (creation_id) references event;
 alter table frequencymultiplication add constraint FKfrequencymultiplication_external_id_externalinfo foreign key (external_id) references externalinfo;
 alter table frequencymultiplication add constraint FKfrequencymultiplication_group_id_experimentergroup foreign key (group_id) references experimentergroup;
 alter table image add constraint FKimage_objectiveSettings_objectivesettings foreign key (objectiveSettings) references objectivesettings;
 alter table immersion add constraint FKimmersion_owner_id_experimenter foreign key (owner_id) references experimenter;
 alter table immersion add constraint FKimmersion_creation_id_event foreign key (creation_id) references event;
 alter table immersion add constraint FKimmersion_external_id_externalinfo foreign key (external_id) references externalinfo;
 alter table immersion add constraint FKimmersion_group_id_experimentergroup foreign key (group_id) references experimentergroup;
 alter table importjob add constraint FKimportjob_job_id_job foreign key (job_id) references job;
 alter table job add constraint FKjob_update_id_event foreign key (update_id) references event;
 alter table job add constraint FKjob_owner_id_experimenter foreign key (owner_id) references experimenter;
 alter table job add constraint FKjob_creation_id_event foreign key (creation_id) references event;
 alter table job add constraint FKjob_status_jobstatus foreign key (status) references jobstatus;
 alter table job add constraint FKjob_external_id_externalinfo foreign key (external_id) references externalinfo;
 alter table job add constraint FKjob_group_id_experimentergroup foreign key (group_id) references experimentergroup;
 alter table joboriginalfilelink add constraint FKjoboriginalfilelink_child_originalfile foreign key (child) references originalfile;
 alter table joboriginalfilelink add constraint FKjoboriginalfilelink_update_id_event foreign key (update_id) references event;
 alter table joboriginalfilelink add constraint FKjoboriginalfilelink_owner_id_experimenter foreign key (owner_id) references experimenter;
 alter table joboriginalfilelink add constraint FKjoboriginalfilelink_creation_id_event foreign key (creation_id) references event;
 alter table joboriginalfilelink add constraint FKjoboriginalfilelink_parent_importjob foreign key (parent) references importjob;
 alter table joboriginalfilelink add constraint FKjoboriginalfilelink_parent_job foreign key (parent) references job;
 alter table joboriginalfilelink add constraint FKjoboriginalfilelink_external_id_externalinfo foreign key (external_id) references externalinfo;
 alter table joboriginalfilelink add constraint FKjoboriginalfilelink_group_id_experimentergroup foreign key (group_id) references experimentergroup;
 alter table jobstatus add constraint FKjobstatus_owner_id_experimenter foreign key (owner_id) references experimenter;
 alter table jobstatus add constraint FKjobstatus_creation_id_event foreign key (creation_id) references event;
 alter table jobstatus add constraint FKjobstatus_external_id_externalinfo foreign key (external_id) references externalinfo;
 alter table jobstatus add constraint FKjobstatus_group_id_experimentergroup foreign key (group_id) references experimentergroup;
 alter table laser add constraint FKlaser_laserMedium_lasermedium foreign key (laserMedium) references lasermedium;
 alter table laser add constraint FKlaser_frequencyMultiplication_frequencymultiplication foreign key (frequencyMultiplication) references frequencymultiplication;
 alter table laser add constraint FKlaser_type_lasertype foreign key (type) references lasertype;
 alter table lasermedium add constraint FKlasermedium_owner_id_experimenter foreign key (owner_id) references experimenter;
 alter table lasermedium add constraint FKlasermedium_creation_id_event foreign key (creation_id) references event;
 alter table lasermedium add constraint FKlasermedium_external_id_externalinfo foreign key (external_id) references externalinfo;
 alter table lasermedium add constraint FKlasermedium_group_id_experimentergroup foreign key (group_id) references experimentergroup;
 alter table lasertype add constraint FKlasertype_owner_id_experimenter foreign key (owner_id) references experimenter;
 alter table lasertype add constraint FKlasertype_creation_id_event foreign key (creation_id) references event;
 alter table lasertype add constraint FKlasertype_external_id_externalinfo foreign key (external_id) references externalinfo;
 alter table lasertype add constraint FKlasertype_group_id_experimentergroup foreign key (group_id) references experimentergroup;
 alter table logicalchannel add constraint FKlogicalchannel_mode_acquisitionmode foreign key (mode) references acquisitionmode;
 alter table logicalchannel add constraint FKlogicalchannel_photometricInterpretation_photometricinterpretation foreign key (photometricInterpretation) references photometricinterpretation;
 alter table medium add constraint FKmedium_owner_id_experimenter foreign key (owner_id) references experimenter;
 alter table medium add constraint FKmedium_creation_id_event foreign key (creation_id) references event;
 alter table medium add constraint FKmedium_external_id_externalinfo foreign key (external_id) references externalinfo;
 alter table medium add constraint FKmedium_group_id_experimentergroup foreign key (group_id) references experimentergroup;
 alter table objective add constraint FKobjective_immersion_immersion foreign key (immersion) references immersion;
 alter table objective add constraint FKobjective_coating_coating foreign key (coating) references coating;
 alter table objectivesettings add constraint FKobjectivesettings_update_id_event foreign key (update_id) references event;
 alter table objectivesettings add constraint FKobjectivesettings_owner_id_experimenter foreign key (owner_id) references experimenter;
 alter table objectivesettings add constraint FKobjectivesettings_creation_id_event foreign key (creation_id) references event;
 alter table objectivesettings add constraint FKobjectivesettings_medium_medium foreign key (medium) references medium;
 alter table objectivesettings add constraint FKobjectivesettings_objective_objective foreign key (objective) references objective;
 alter table objectivesettings add constraint FKobjectivesettings_external_id_externalinfo foreign key (external_id) references externalinfo;
 alter table objectivesettings add constraint FKobjectivesettings_group_id_experimentergroup foreign key (group_id) references experimentergroup;
 alter table projectannotation add constraint FKprojectannotation_update_id_event foreign key (update_id) references event;
 alter table projectannotation add constraint FKprojectannotation_project_project foreign key (project) references project;
 alter table projectannotation add constraint FKprojectannotation_owner_id_experimenter foreign key (owner_id) references experimenter;
 alter table projectannotation add constraint FKprojectannotation_creation_id_event foreign key (creation_id) references event;
 alter table projectannotation add constraint FKprojectannotation_external_id_externalinfo foreign key (external_id) references externalinfo;
 alter table projectannotation add constraint FKprojectannotation_group_id_experimentergroup foreign key (group_id) references experimentergroup;
 alter table transmittancerange add constraint FKtransmittancerange_update_id_event foreign key (update_id) references event;
 alter table transmittancerange add constraint FKtransmittancerange_owner_id_experimenter foreign key (owner_id) references experimenter;
 alter table transmittancerange add constraint FKtransmittancerange_creation_id_event foreign key (creation_id) references event;
 alter table transmittancerange add constraint FKtransmittancerange_external_id_externalinfo foreign key (external_id) references externalinfo;
 alter table transmittancerange add constraint FKtransmittancerange_group_id_experimentergroup foreign key (group_id) references experimentergroup;
 create sequence seq_binning;
 create sequence seq_coating;
 create sequence seq_dbpatch;
 create sequence seq_frequencymultiplication;
 create sequence seq_immersion;
 create sequence seq_job;
 create sequence seq_joboriginalfilelink;
 create sequence seq_jobstatus;
 create sequence seq_lasermedium;
 create sequence seq_lasertype;
 create sequence seq_medium;
 create sequence seq_objectivesettings;
 create sequence seq_projectannotation;
 create sequence seq_transmittancerange;

--
-- Calculated by apg_diff
--

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

 --
 -- Calculated by hand
 --

 insert into binning (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_binning'),-35,0,0,0,'8x8';
 insert into binning (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_binning'),-35,0,0,0,'4x4';
 insert into binning (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_binning'),-35,0,0,0,'2x2';
 insert into binning (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_binning'),-35,0,0,0,'1x1';

 insert into coating (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_coating'),-35,0,0,0,'UV';
 insert into coating (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_coating'),-35,0,0,0,'SuperFluor';
 insert into coating (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_coating'),-35,0,0,0,'PlanFluor';
 insert into coating (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_coating'),-35,0,0,0,'PlanApo';

 insert into frequencymultiplication (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_frequencymultiplication'),-35,0,0,0,'x4';
 insert into frequencymultiplication (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_frequencymultiplication'),-35,0,0,0,'x3';
 insert into frequencymultiplication (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_frequencymultiplication'),-35,0,0,0,'x2';
 insert into frequencymultiplication (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_frequencymultiplication'),-35,0,0,0,'x1';

 insert into immersion (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_immersion'),-35,0,0,0,'Wl';
 insert into immersion (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_immersion'),-35,0,0,0,'Water';
 insert into immersion (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_immersion'),-35,0,0,0,'Wasser';
 insert into immersion (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_immersion'),-35,0,0,0,'Oil';
 insert into immersion (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_immersion'),-35,0,0,0,'Oel';
 insert into immersion (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_immersion'),-35,0,0,0,'Hl';
 insert into immersion (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_immersion'),-35,0,0,0,'Gly';

 insert into jobstatus (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_jobstatus'),-35,0,0,0,'Waiting';
 insert into jobstatus (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_jobstatus'),-35,0,0,0,'Submitted';
 insert into jobstatus (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_jobstatus'),-35,0,0,0,'Running';
 insert into jobstatus (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_jobstatus'),-35,0,0,0,'Resubmitted';
 insert into jobstatus (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_jobstatus'),-35,0,0,0,'Requeued';
 insert into jobstatus (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_jobstatus'),-35,0,0,0,'Queued';
 insert into jobstatus (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_jobstatus'),-35,0,0,0,'Finished';
 insert into jobstatus (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_jobstatus'),-35,0,0,0,'Error';

 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'XeFl';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'XeCl';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'XeBr';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'Xe';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'Rhodamine-5G';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'N';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'KrFl';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'KrCl';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'Kr';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'HFl';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'HeNe';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'HeCd';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'H2O';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'GaAs';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'GaAlAs';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'e-';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'Cu';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'Coumaring-C30';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'CO2';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'CO';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'ArFl';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'ArCl';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'Ar';
 insert into lasermedium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasermedium'),-35,0,0,0,'Ag';

 insert into lasertype (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasertype'),-35,0,0,0,'SolidState';
 insert into lasertype (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasertype'),-35,0,0,0,'Semiconductor';
 insert into lasertype (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasertype'),-35,0,0,0,'MetalVapor';
 insert into lasertype (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasertype'),-35,0,0,0,'Gas';
 insert into lasertype (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasertype'),-35,0,0,0,'FreeElectron';
 insert into lasertype (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasertype'),-35,0,0,0,'Excimer';
 insert into lasertype (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_lasertype'),-35,0,0,0,'Dye';

 insert into medium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_medium'),-35,0,0,0,'Water';
 insert into medium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_medium'),-35,0,0,0,'Oil';
 insert into medium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_medium'),-35,0,0,0,'Glycerol';
 insert into medium (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_medium'),-35,0,0,0,'Air';

 insert into photometricinterpretation (id,permissions,owner_id,group_id,creation_id,value) select nextval('seq_photometricinterpretation'),-35,0,0,0,'ColorMap';

 --
 -- Copied from OMERO3__4__data.sql
 --

 alter table dbpatch add constraint unique_dbpatch unique (currentVersion, currentPatch, previousVersion, previousPatch);
 alter table dbpatch alter id set default nextval('seq_dbpatch'),
                    alter permissions set default -35,
                    alter message set default 'Updating';
 update dbpatch set message = 'Updated.', finished = now() 
 where currentVersion   = 'OMERO3'    and
        currentPatch    = 4           and
        previousVersion = 'OMERO3'    and
        previousPatch   = 1;
 commit;
