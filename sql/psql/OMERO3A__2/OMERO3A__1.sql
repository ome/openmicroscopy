--
-- Copyright 2008 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--

BEGIN;

CREATE SEQUENCE seq_session;

CREATE TABLE session (
	id bigint NOT NULL,
	closed timestamp without time zone,
	defaulteventtype character varying(255) NOT NULL,
	defaultpermissions character varying(255) NOT NULL,
	permissions bigint NOT NULL,
	message character varying(255),
	started timestamp without time zone NOT NULL,
	timetoidle bigint NOT NULL,
	timetolive bigint NOT NULL,
	useragent character varying(255),
	uuid character varying(255) NOT NULL,
	external_id bigint
);

ALTER TABLE event
	ADD COLUMN session bigint;

UPDATE event SET session = 1;

INSERT INTO experimenter (id,permissions,version,omename,firstname,lastname)
        VALUES (nextval('seq_experimenter'),0,0,'guest','Guest','Account');

INSERT INTO session
        (id,permissions,timetoidle,timetolive,started,closed,defaultpermissions,defaulteventtype,uuid)
        SELECT 0,-35,0,0,now(),now(),'rw----','BOOTSTRAP',0000;

INSERT INTO session
        (id,permissions,timetoidle,timetolive,started,closed,defaultpermissions,defaulteventtype,uuid)
        SELECT nextval('seq_session'),-35, 0,0,now(),now(),'rw----','PREVIOUSITEMS','1111';

INSERT INTO EVENTTYPE (id,permissions,owner_id,group_id,creation_id,value)
        SELECT nextval('seq_eventtype'),-35,0,0,0,'Sessions';

INSERT INTO experimentergroup (id,permissions,version,owner_id,group_id,creation_id,update_id,name)
        VALUES (nextval('seq_experimentergroup'),-35,0,0,0,0,0,'guest');

INSERT INTO groupexperimentermap
        (id,permissions,version,owner_id,group_id,creation_id,update_id, parent, child, child_index)
        SELECT nextval('seq_groupexperimentermap'),-35,0,0,0,0,0,g.id,e.id,0 FROM
        experimenter e, experimentergroup g WHERE e.omeName = 'guest' and g.name = 'guest';

INSERT INTO password SELECT id AS experimenter_id, '' AS hash FROM experimenter WHERE omename = 'guest';

ALTER TABLE event
        ALTER COLUMN session SET NOT NULL;

ALTER TABLE session
	ADD CONSTRAINT session_pkey PRIMARY KEY (id);

ALTER TABLE event
	ADD CONSTRAINT fkevent_session_session FOREIGN KEY ("session") REFERENCES "session"(id);

ALTER TABLE session
	ADD CONSTRAINT session_external_id_key UNIQUE (external_id);

ALTER TABLE session
	ADD CONSTRAINT fksession_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

INSERT INTO dbpatch (currentVersion, currentPatch, previousVersion, previousPatch, message, finished)
        VALUES ('OMERO3A',  2, 'OMERO3A', 1, 'Database updated.', now());

COMMIT;

