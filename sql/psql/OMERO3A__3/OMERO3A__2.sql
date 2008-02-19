--
-- Copyright 2008 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

BEGIN;

UPDATE experimenttype SET value = 'Photoablation' WHERE value = 'Photablation';

CREATE SEQUENCE seq_plate;

CREATE SEQUENCE seq_plateannotationlink;

CREATE SEQUENCE seq_reagent;

CREATE SEQUENCE seq_reagentannotationlink;

CREATE SEQUENCE seq_screen;

CREATE SEQUENCE seq_screenacquisition;

CREATE SEQUENCE seq_screenacquisitionannotationlink;

CREATE SEQUENCE seq_screenannotationlink;

CREATE SEQUENCE seq_screenplatelink;

CREATE SEQUENCE seq_screenreagentlink;

CREATE SEQUENCE seq_well;

CREATE SEQUENCE seq_wellannotationlink;

CREATE SEQUENCE seq_wellsample;

CREATE SEQUENCE seq_wellsampleannotationlink;

CREATE SEQUENCE seq_wellsampleimagelink;

CREATE TABLE count_image_samplelinks_by_owner (
	image_id bigint NOT NULL,
	count bigint NOT NULL,
	owner_id bigint NOT NULL
);

CREATE TABLE count_plate_annotationlinks_by_owner (
	plate_id bigint NOT NULL,
	count bigint NOT NULL,
	owner_id bigint NOT NULL
);

CREATE TABLE count_plate_screenlinks_by_owner (
	plate_id bigint NOT NULL,
	count bigint NOT NULL,
	owner_id bigint NOT NULL
);

CREATE TABLE count_reagent_annotationlinks_by_owner (
	reagent_id bigint NOT NULL,
	count bigint NOT NULL,
	owner_id bigint NOT NULL
);

CREATE TABLE count_reagent_screenlinks_by_owner (
	reagent_id bigint NOT NULL,
	count bigint NOT NULL,
	owner_id bigint NOT NULL
);

CREATE TABLE count_screen_annotationlinks_by_owner (
	screen_id bigint NOT NULL,
	count bigint NOT NULL,
	owner_id bigint NOT NULL
);

CREATE TABLE count_screen_platelinks_by_owner (
	screen_id bigint NOT NULL,
	count bigint NOT NULL,
	owner_id bigint NOT NULL
);

CREATE TABLE count_screen_reagentlinks_by_owner (
	screen_id bigint NOT NULL,
	count bigint NOT NULL,
	owner_id bigint NOT NULL
);

CREATE TABLE count_screenacquisition_annotationlinks_by_owner (
	screenacquisition_id bigint NOT NULL,
	count bigint NOT NULL,
	owner_id bigint NOT NULL
);

CREATE TABLE count_well_annotationlinks_by_owner (
	well_id bigint NOT NULL,
	count bigint NOT NULL,
	owner_id bigint NOT NULL
);

CREATE TABLE count_wellsample_annotationlinks_by_owner (
	wellsample_id bigint NOT NULL,
	count bigint NOT NULL,
	owner_id bigint NOT NULL
);

CREATE TABLE count_wellsample_imagelinks_by_owner (
	wellsample_id bigint NOT NULL,
	count bigint NOT NULL,
	owner_id bigint NOT NULL
);

CREATE TABLE plate (
	id bigint NOT NULL,
	description text,
	permissions bigint NOT NULL,
	externalidentifier character varying(255),
	name character varying(255) NOT NULL,
	status character varying(255),
	version integer,
	external_id bigint,
	creation_id bigint NOT NULL,
	owner_id bigint NOT NULL,
	update_id bigint NOT NULL,
	group_id bigint NOT NULL
);

CREATE TABLE plateannotationlink (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	version integer,
	update_id bigint NOT NULL,
	child bigint NOT NULL,
	owner_id bigint NOT NULL,
	creation_id bigint NOT NULL,
	external_id bigint,
	parent bigint NOT NULL,
	group_id bigint NOT NULL
);

CREATE TABLE reagent (
	id bigint NOT NULL,
	description text,
	permissions bigint NOT NULL,
	name character varying(255) NOT NULL,
	reagentidentifier character varying(255),
	version integer,
	owner_id bigint NOT NULL,
	creation_id bigint NOT NULL,
	update_id bigint NOT NULL,
	external_id bigint,
	group_id bigint NOT NULL
);

CREATE TABLE reagentannotationlink (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	version integer,
	update_id bigint NOT NULL,
	group_id bigint NOT NULL,
	external_id bigint,
	parent bigint NOT NULL,
	creation_id bigint NOT NULL,
	owner_id bigint NOT NULL,
	child bigint NOT NULL
);

CREATE TABLE screen (
	id bigint NOT NULL,
	description text,
	permissions bigint NOT NULL,
	name character varying(255) NOT NULL,
	protocoldescription character varying(255),
	protocolidentifier character varying(255),
	reagentsetdescription character varying(255),
	reagentsetidentifier character varying(255),
	type character varying(255),
	version integer,
	update_id bigint NOT NULL,
	external_id bigint,
	owner_id bigint NOT NULL,
	creation_id bigint NOT NULL,
	group_id bigint NOT NULL
);

CREATE TABLE screenacquisition (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	endtime timestamp without time zone,
	starttime timestamp without time zone,
	version integer,
	group_id bigint NOT NULL,
	external_id bigint,
	creation_id bigint NOT NULL,
	screen bigint NOT NULL,
	update_id bigint NOT NULL,
	owner_id bigint NOT NULL
);

CREATE TABLE screenacquisitionannotationlink (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	version integer,
	owner_id bigint NOT NULL,
	update_id bigint NOT NULL,
	child bigint NOT NULL,
	creation_id bigint NOT NULL,
	group_id bigint NOT NULL,
	external_id bigint,
	parent bigint NOT NULL
);

CREATE TABLE screenannotationlink (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	version integer,
	owner_id bigint NOT NULL,
	creation_id bigint NOT NULL,
	child bigint NOT NULL,
	update_id bigint NOT NULL,
	parent bigint NOT NULL,
	group_id bigint NOT NULL,
	external_id bigint
);

CREATE TABLE screenplatelink (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	version integer,
	external_id bigint,
	parent bigint NOT NULL,
	owner_id bigint NOT NULL,
	update_id bigint NOT NULL,
	child bigint NOT NULL,
	creation_id bigint NOT NULL,
	group_id bigint NOT NULL
);

CREATE TABLE screenreagentlink (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	version integer,
	update_id bigint NOT NULL,
	external_id bigint,
	parent bigint NOT NULL,
	owner_id bigint NOT NULL,
	group_id bigint NOT NULL,
	creation_id bigint NOT NULL,
	child bigint NOT NULL
);

CREATE TABLE well (
	id bigint NOT NULL,
	"column" integer,
	permissions bigint NOT NULL,
	externaldescription character varying(255),
	externalidentifier character varying(255),
	row integer,
	type character varying(255),
	version integer,
	reagent bigint,
	group_id bigint NOT NULL,
	plate bigint NOT NULL,
	external_id bigint,
	creation_id bigint NOT NULL,
	owner_id bigint NOT NULL,
	update_id bigint NOT NULL
);

CREATE TABLE wellannotationlink (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	version integer,
	parent bigint NOT NULL,
	child bigint NOT NULL,
	creation_id bigint NOT NULL,
	group_id bigint NOT NULL,
	external_id bigint,
	owner_id bigint NOT NULL,
	update_id bigint NOT NULL
);

CREATE TABLE wellsample (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	posx double precision,
	posy double precision,
	timepoint integer,
	version integer,
	screenacquisition bigint NOT NULL,
	creation_id bigint NOT NULL,
	external_id bigint,
	update_id bigint NOT NULL,
	well bigint NOT NULL,
	owner_id bigint NOT NULL,
	group_id bigint NOT NULL,
	well_index integer NOT NULL
);

CREATE TABLE wellsampleannotationlink (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	version integer,
	update_id bigint NOT NULL,
	external_id bigint,
	group_id bigint NOT NULL,
	creation_id bigint NOT NULL,
	child bigint NOT NULL,
	owner_id bigint NOT NULL,
	parent bigint NOT NULL
);

CREATE TABLE wellsampleimagelink (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	version integer,
	parent bigint NOT NULL,
	external_id bigint,
	owner_id bigint NOT NULL,
	creation_id bigint NOT NULL,
	group_id bigint NOT NULL,
	child bigint NOT NULL,
	update_id bigint NOT NULL
);

ALTER TABLE logicalchannel DROP COLUMN pockelcellsetting;

ALTER TABLE logicalchannel ADD COLUMN pockelcellsetting integer;

ALTER TABLE planeinfo
	ALTER COLUMN timestamp DROP NOT NULL;

ALTER TABLE count_image_samplelinks_by_owner
	ADD CONSTRAINT count_image_samplelinks_by_owner_pkey PRIMARY KEY (image_id, owner_id);

ALTER TABLE count_plate_annotationlinks_by_owner
	ADD CONSTRAINT count_plate_annotationlinks_by_owner_pkey PRIMARY KEY (plate_id, owner_id);

ALTER TABLE count_plate_screenlinks_by_owner
	ADD CONSTRAINT count_plate_screenlinks_by_owner_pkey PRIMARY KEY (plate_id, owner_id);

ALTER TABLE count_reagent_annotationlinks_by_owner
	ADD CONSTRAINT count_reagent_annotationlinks_by_owner_pkey PRIMARY KEY (reagent_id, owner_id);

ALTER TABLE count_reagent_screenlinks_by_owner
	ADD CONSTRAINT count_reagent_screenlinks_by_owner_pkey PRIMARY KEY (reagent_id, owner_id);

ALTER TABLE count_screen_annotationlinks_by_owner
	ADD CONSTRAINT count_screen_annotationlinks_by_owner_pkey PRIMARY KEY (screen_id, owner_id);

ALTER TABLE count_screen_platelinks_by_owner
	ADD CONSTRAINT count_screen_platelinks_by_owner_pkey PRIMARY KEY (screen_id, owner_id);

ALTER TABLE count_screen_reagentlinks_by_owner
	ADD CONSTRAINT count_screen_reagentlinks_by_owner_pkey PRIMARY KEY (screen_id, owner_id);

ALTER TABLE count_screenacquisition_annotationlinks_by_owner
	ADD CONSTRAINT count_screenacquisition_annotationlinks_by_owner_pkey PRIMARY KEY (screenacquisition_id, owner_id);

ALTER TABLE count_well_annotationlinks_by_owner
	ADD CONSTRAINT count_well_annotationlinks_by_owner_pkey PRIMARY KEY (well_id, owner_id);

ALTER TABLE count_wellsample_annotationlinks_by_owner
	ADD CONSTRAINT count_wellsample_annotationlinks_by_owner_pkey PRIMARY KEY (wellsample_id, owner_id);

ALTER TABLE count_wellsample_imagelinks_by_owner
	ADD CONSTRAINT count_wellsample_imagelinks_by_owner_pkey PRIMARY KEY (wellsample_id, owner_id);

ALTER TABLE plate
	ADD CONSTRAINT plate_pkey PRIMARY KEY (id);

ALTER TABLE plateannotationlink
	ADD CONSTRAINT plateannotationlink_pkey PRIMARY KEY (id);

ALTER TABLE reagent
	ADD CONSTRAINT reagent_pkey PRIMARY KEY (id);

ALTER TABLE reagentannotationlink
	ADD CONSTRAINT reagentannotationlink_pkey PRIMARY KEY (id);

ALTER TABLE screen
	ADD CONSTRAINT screen_pkey PRIMARY KEY (id);

ALTER TABLE screenacquisition
	ADD CONSTRAINT screenacquisition_pkey PRIMARY KEY (id);

ALTER TABLE screenacquisitionannotationlink
	ADD CONSTRAINT screenacquisitionannotationlink_pkey PRIMARY KEY (id);

ALTER TABLE screenannotationlink
	ADD CONSTRAINT screenannotationlink_pkey PRIMARY KEY (id);

ALTER TABLE screenplatelink
	ADD CONSTRAINT screenplatelink_pkey PRIMARY KEY (id);

ALTER TABLE screenreagentlink
	ADD CONSTRAINT screenreagentlink_pkey PRIMARY KEY (id);

ALTER TABLE well
	ADD CONSTRAINT well_pkey PRIMARY KEY (id);

ALTER TABLE wellannotationlink
	ADD CONSTRAINT wellannotationlink_pkey PRIMARY KEY (id);

ALTER TABLE wellsample
	ADD CONSTRAINT wellsample_pkey PRIMARY KEY (id);

ALTER TABLE wellsampleannotationlink
	ADD CONSTRAINT wellsampleannotationlink_pkey PRIMARY KEY (id);

ALTER TABLE wellsampleimagelink
	ADD CONSTRAINT wellsampleimagelink_pkey PRIMARY KEY (id);

ALTER TABLE count_image_samplelinks_by_owner
	ADD CONSTRAINT fk_count_to_image_samplelinks FOREIGN KEY (image_id) REFERENCES image(id);

ALTER TABLE count_plate_annotationlinks_by_owner
	ADD CONSTRAINT fk_count_to_plate_annotationlinks FOREIGN KEY (plate_id) REFERENCES plate(id);

ALTER TABLE count_plate_screenlinks_by_owner
	ADD CONSTRAINT fk_count_to_plate_screenlinks FOREIGN KEY (plate_id) REFERENCES plate(id);

ALTER TABLE count_reagent_annotationlinks_by_owner
	ADD CONSTRAINT fk_count_to_reagent_annotationlinks FOREIGN KEY (reagent_id) REFERENCES reagent(id);

ALTER TABLE count_reagent_screenlinks_by_owner
	ADD CONSTRAINT fk_count_to_reagent_screenlinks FOREIGN KEY (reagent_id) REFERENCES reagent(id);

ALTER TABLE count_screen_annotationlinks_by_owner
	ADD CONSTRAINT fk_count_to_screen_annotationlinks FOREIGN KEY (screen_id) REFERENCES screen(id);

ALTER TABLE count_screen_platelinks_by_owner
	ADD CONSTRAINT fk_count_to_screen_platelinks FOREIGN KEY (screen_id) REFERENCES screen(id);

ALTER TABLE count_screen_reagentlinks_by_owner
	ADD CONSTRAINT fk_count_to_screen_reagentlinks FOREIGN KEY (screen_id) REFERENCES screen(id);

ALTER TABLE count_screenacquisition_annotationlinks_by_owner
	ADD CONSTRAINT fk_count_to_screenacquisition_annotationlinks FOREIGN KEY (screenacquisition_id) REFERENCES screenacquisition(id);

ALTER TABLE count_well_annotationlinks_by_owner
	ADD CONSTRAINT fk_count_to_well_annotationlinks FOREIGN KEY (well_id) REFERENCES well(id);

ALTER TABLE count_wellsample_annotationlinks_by_owner
	ADD CONSTRAINT fk_count_to_wellsample_annotationlinks FOREIGN KEY (wellsample_id) REFERENCES wellsample(id);

ALTER TABLE count_wellsample_imagelinks_by_owner
	ADD CONSTRAINT fk_count_to_wellsample_imagelinks FOREIGN KEY (wellsample_id) REFERENCES wellsample(id);

ALTER TABLE plate
	ADD CONSTRAINT plate_external_id_key UNIQUE (external_id);

ALTER TABLE plate
	ADD CONSTRAINT fkplate_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);

ALTER TABLE plate
	ADD CONSTRAINT fkplate_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE plate
	ADD CONSTRAINT fkplate_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);

ALTER TABLE plate
	ADD CONSTRAINT fkplate_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);

ALTER TABLE plate
	ADD CONSTRAINT fkplate_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE plateannotationlink
	ADD CONSTRAINT plateannotationlink_external_id_key UNIQUE (external_id);

ALTER TABLE plateannotationlink
	ADD CONSTRAINT fkplateannotationlink_child_annotation FOREIGN KEY (child) REFERENCES annotation(id);

ALTER TABLE plateannotationlink
	ADD CONSTRAINT fkplateannotationlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);

ALTER TABLE plateannotationlink
	ADD CONSTRAINT fkplateannotationlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE plateannotationlink
	ADD CONSTRAINT fkplateannotationlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);

ALTER TABLE plateannotationlink
	ADD CONSTRAINT fkplateannotationlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);

ALTER TABLE plateannotationlink
	ADD CONSTRAINT fkplateannotationlink_parent_plate FOREIGN KEY (parent) REFERENCES plate(id);

ALTER TABLE plateannotationlink
	ADD CONSTRAINT fkplateannotationlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE reagent
	ADD CONSTRAINT reagent_external_id_key UNIQUE (external_id);

ALTER TABLE reagent
	ADD CONSTRAINT fkreagent_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);

ALTER TABLE reagent
	ADD CONSTRAINT fkreagent_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE reagent
	ADD CONSTRAINT fkreagent_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);

ALTER TABLE reagent
	ADD CONSTRAINT fkreagent_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);

ALTER TABLE reagent
	ADD CONSTRAINT fkreagent_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE reagentannotationlink
	ADD CONSTRAINT reagentannotationlink_external_id_key UNIQUE (external_id);

ALTER TABLE reagentannotationlink
	ADD CONSTRAINT fkreagentannotationlink_child_annotation FOREIGN KEY (child) REFERENCES annotation(id);

ALTER TABLE reagentannotationlink
	ADD CONSTRAINT fkreagentannotationlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);

ALTER TABLE reagentannotationlink
	ADD CONSTRAINT fkreagentannotationlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE reagentannotationlink
	ADD CONSTRAINT fkreagentannotationlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);

ALTER TABLE reagentannotationlink
	ADD CONSTRAINT fkreagentannotationlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);

ALTER TABLE reagentannotationlink
	ADD CONSTRAINT fkreagentannotationlink_parent_reagent FOREIGN KEY (parent) REFERENCES reagent(id);

ALTER TABLE reagentannotationlink
	ADD CONSTRAINT fkreagentannotationlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE screen
	ADD CONSTRAINT screen_external_id_key UNIQUE (external_id);

ALTER TABLE screen
	ADD CONSTRAINT fkscreen_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);

ALTER TABLE screen
	ADD CONSTRAINT fkscreen_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE screen
	ADD CONSTRAINT fkscreen_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);

ALTER TABLE screen
	ADD CONSTRAINT fkscreen_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);

ALTER TABLE screen
	ADD CONSTRAINT fkscreen_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE screenacquisition
	ADD CONSTRAINT screenacquisition_external_id_key UNIQUE (external_id);

ALTER TABLE screenacquisition
	ADD CONSTRAINT fkscreenacquisition_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);

ALTER TABLE screenacquisition
	ADD CONSTRAINT fkscreenacquisition_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE screenacquisition
	ADD CONSTRAINT fkscreenacquisition_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);

ALTER TABLE screenacquisition
	ADD CONSTRAINT fkscreenacquisition_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);

ALTER TABLE screenacquisition
	ADD CONSTRAINT fkscreenacquisition_screen_screen FOREIGN KEY (screen) REFERENCES screen(id);

ALTER TABLE screenacquisition
	ADD CONSTRAINT fkscreenacquisition_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE screenacquisitionannotationlink
	ADD CONSTRAINT screenacquisitionannotationlink_external_id_key UNIQUE (external_id);

ALTER TABLE screenacquisitionannotationlink
	ADD CONSTRAINT fkscreenacquisitionannotationlink_child_annotation FOREIGN KEY (child) REFERENCES annotation(id);

ALTER TABLE screenacquisitionannotationlink
	ADD CONSTRAINT fkscreenacquisitionannotationlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);

ALTER TABLE screenacquisitionannotationlink
	ADD CONSTRAINT fkscreenacquisitionannotationlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE screenacquisitionannotationlink
	ADD CONSTRAINT fkscreenacquisitionannotationlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);

ALTER TABLE screenacquisitionannotationlink
	ADD CONSTRAINT fkscreenacquisitionannotationlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);

ALTER TABLE screenacquisitionannotationlink
	ADD CONSTRAINT fkscreenacquisitionannotationlink_parent_screenacquisition FOREIGN KEY (parent) REFERENCES screenacquisition(id);

ALTER TABLE screenacquisitionannotationlink
	ADD CONSTRAINT fkscreenacquisitionannotationlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE screenannotationlink
	ADD CONSTRAINT screenannotationlink_external_id_key UNIQUE (external_id);

ALTER TABLE screenannotationlink
	ADD CONSTRAINT fkscreenannotationlink_child_annotation FOREIGN KEY (child) REFERENCES annotation(id);

ALTER TABLE screenannotationlink
	ADD CONSTRAINT fkscreenannotationlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);

ALTER TABLE screenannotationlink
	ADD CONSTRAINT fkscreenannotationlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE screenannotationlink
	ADD CONSTRAINT fkscreenannotationlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);

ALTER TABLE screenannotationlink
	ADD CONSTRAINT fkscreenannotationlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);

ALTER TABLE screenannotationlink
	ADD CONSTRAINT fkscreenannotationlink_parent_screen FOREIGN KEY (parent) REFERENCES screen(id);

ALTER TABLE screenannotationlink
	ADD CONSTRAINT fkscreenannotationlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE screenplatelink
	ADD CONSTRAINT screenplatelink_external_id_key UNIQUE (external_id);

ALTER TABLE screenplatelink
	ADD CONSTRAINT fkscreenplatelink_child_plate FOREIGN KEY (child) REFERENCES plate(id);

ALTER TABLE screenplatelink
	ADD CONSTRAINT fkscreenplatelink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);

ALTER TABLE screenplatelink
	ADD CONSTRAINT fkscreenplatelink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE screenplatelink
	ADD CONSTRAINT fkscreenplatelink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);

ALTER TABLE screenplatelink
	ADD CONSTRAINT fkscreenplatelink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);

ALTER TABLE screenplatelink
	ADD CONSTRAINT fkscreenplatelink_parent_screen FOREIGN KEY (parent) REFERENCES screen(id);

ALTER TABLE screenplatelink
	ADD CONSTRAINT fkscreenplatelink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE screenreagentlink
	ADD CONSTRAINT screenreagentlink_external_id_key UNIQUE (external_id);

ALTER TABLE screenreagentlink
	ADD CONSTRAINT fkscreenreagentlink_child_reagent FOREIGN KEY (child) REFERENCES reagent(id);

ALTER TABLE screenreagentlink
	ADD CONSTRAINT fkscreenreagentlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);

ALTER TABLE screenreagentlink
	ADD CONSTRAINT fkscreenreagentlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE screenreagentlink
	ADD CONSTRAINT fkscreenreagentlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);

ALTER TABLE screenreagentlink
	ADD CONSTRAINT fkscreenreagentlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);

ALTER TABLE screenreagentlink
	ADD CONSTRAINT fkscreenreagentlink_parent_screen FOREIGN KEY (parent) REFERENCES screen(id);

ALTER TABLE screenreagentlink
	ADD CONSTRAINT fkscreenreagentlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE well
	ADD CONSTRAINT well_external_id_key UNIQUE (external_id);

ALTER TABLE well
	ADD CONSTRAINT fkwell_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);

ALTER TABLE well
	ADD CONSTRAINT fkwell_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE well
	ADD CONSTRAINT fkwell_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);

ALTER TABLE well
	ADD CONSTRAINT fkwell_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);

ALTER TABLE well
	ADD CONSTRAINT fkwell_plate_plate FOREIGN KEY (plate) REFERENCES plate(id);

ALTER TABLE well
	ADD CONSTRAINT fkwell_reagent_reagent FOREIGN KEY (reagent) REFERENCES reagent(id);

ALTER TABLE well
	ADD CONSTRAINT fkwell_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE wellannotationlink
	ADD CONSTRAINT wellannotationlink_external_id_key UNIQUE (external_id);

ALTER TABLE wellannotationlink
	ADD CONSTRAINT fkwellannotationlink_child_annotation FOREIGN KEY (child) REFERENCES annotation(id);

ALTER TABLE wellannotationlink
	ADD CONSTRAINT fkwellannotationlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);

ALTER TABLE wellannotationlink
	ADD CONSTRAINT fkwellannotationlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE wellannotationlink
	ADD CONSTRAINT fkwellannotationlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);

ALTER TABLE wellannotationlink
	ADD CONSTRAINT fkwellannotationlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);

ALTER TABLE wellannotationlink
	ADD CONSTRAINT fkwellannotationlink_parent_well FOREIGN KEY (parent) REFERENCES well(id);

ALTER TABLE wellannotationlink
	ADD CONSTRAINT fkwellannotationlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE wellsample
	ADD CONSTRAINT wellsample_external_id_key UNIQUE (external_id);

ALTER TABLE wellsample
	ADD CONSTRAINT fkwellsample_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);

ALTER TABLE wellsample
	ADD CONSTRAINT fkwellsample_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE wellsample
	ADD CONSTRAINT fkwellsample_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);

ALTER TABLE wellsample
	ADD CONSTRAINT fkwellsample_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);

ALTER TABLE wellsample
	ADD CONSTRAINT fkwellsample_screenacquisition_screenacquisition FOREIGN KEY (screenacquisition) REFERENCES screenacquisition(id);

ALTER TABLE wellsample
	ADD CONSTRAINT fkwellsample_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE wellsample
	ADD CONSTRAINT fkwellsample_well_well FOREIGN KEY (well) REFERENCES well(id);

ALTER TABLE wellsampleannotationlink
	ADD CONSTRAINT wellsampleannotationlink_external_id_key UNIQUE (external_id);

ALTER TABLE wellsampleannotationlink
	ADD CONSTRAINT fkwellsampleannotationlink_child_annotation FOREIGN KEY (child) REFERENCES annotation(id);

ALTER TABLE wellsampleannotationlink
	ADD CONSTRAINT fkwellsampleannotationlink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);

ALTER TABLE wellsampleannotationlink
	ADD CONSTRAINT fkwellsampleannotationlink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE wellsampleannotationlink
	ADD CONSTRAINT fkwellsampleannotationlink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);

ALTER TABLE wellsampleannotationlink
	ADD CONSTRAINT fkwellsampleannotationlink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);

ALTER TABLE wellsampleannotationlink
	ADD CONSTRAINT fkwellsampleannotationlink_parent_wellsample FOREIGN KEY (parent) REFERENCES wellsample(id);

ALTER TABLE wellsampleannotationlink
	ADD CONSTRAINT fkwellsampleannotationlink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

ALTER TABLE wellsampleimagelink
	ADD CONSTRAINT wellsampleimagelink_external_id_key UNIQUE (external_id);

ALTER TABLE wellsampleimagelink
	ADD CONSTRAINT fkwellsampleimagelink_child_image FOREIGN KEY (child) REFERENCES image(id);

ALTER TABLE wellsampleimagelink
	ADD CONSTRAINT fkwellsampleimagelink_creation_id_event FOREIGN KEY (creation_id) REFERENCES event(id);

ALTER TABLE wellsampleimagelink
	ADD CONSTRAINT fkwellsampleimagelink_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE wellsampleimagelink
	ADD CONSTRAINT fkwellsampleimagelink_group_id_experimentergroup FOREIGN KEY (group_id) REFERENCES experimentergroup(id);

ALTER TABLE wellsampleimagelink
	ADD CONSTRAINT fkwellsampleimagelink_owner_id_experimenter FOREIGN KEY (owner_id) REFERENCES experimenter(id);

ALTER TABLE wellsampleimagelink
	ADD CONSTRAINT fkwellsampleimagelink_parent_wellsample FOREIGN KEY (parent) REFERENCES wellsample(id);

ALTER TABLE wellsampleimagelink
	ADD CONSTRAINT fkwellsampleimagelink_update_id_event FOREIGN KEY (update_id) REFERENCES event(id);

INSERT INTO dbpatch (currentVersion, currentPatch, previousVersion, previousPatch, message, finished)
        VALUES ('OMERO3A',  3, 'OMERO3A', 2, 'Database updated.', now());

COMMIT;
