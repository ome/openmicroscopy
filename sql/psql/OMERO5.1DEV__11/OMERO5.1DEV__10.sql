-- Copyright (C) 2014 Glencoe Software, Inc. All rights reserved.
-- Use is subject to license terms supplied in LICENSE.txt
--
-- This program is free software; you can redistribute it and/or modify
-- it under the terms of the GNU General Public License as published by
-- the Free Software Foundation; either version 2 of the License, or
-- (at your option) any later version.
--
-- This program is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU General Public License for more details.
--
-- You should have received a copy of the GNU General Public License along
-- with this program; if not, write to the Free Software Foundation, Inc.,
-- 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
--

---
--- OMERO5 development release upgrade from OMERO5.1DEV__10 to OMERO5.1DEV__11.
---

BEGIN;

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

SELECT omero_assert_db_version('OMERO5.1DEV', 10);
DROP FUNCTION omero_assert_db_version(varchar, int);


--
-- check PostgreSQL server version and database encoding
--

CREATE FUNCTION assert_db_server_prerequisites(version_prereq INTEGER) RETURNS void AS $$

DECLARE
    version_num INTEGER;
    char_encoding TEXT;

BEGIN
    SELECT CAST(setting AS INTEGER) INTO STRICT version_num FROM pg_settings WHERE name = 'server_version_num';
    SELECT pg_encoding_to_char(encoding) INTO STRICT char_encoding FROM pg_database WHERE datname = current_database();

    IF version_num < version_prereq THEN
        RAISE EXCEPTION 'database server version % is less than OMERO prerequisite %', version_num, version_prereq;
    END IF;

    IF char_encoding != 'UTF8' THEN
       RAISE EXCEPTION 'OMERO database character encoding must be UTF8, not %', char_encoding;
    END IF;

END;$$ LANGUAGE plpgsql;

SELECT assert_db_server_prerequisites(84000);
DROP FUNCTION assert_db_server_prerequisites(INTEGER);

SET client_encoding = 'UTF8';


INSERT INTO dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
             VALUES ('OMERO5.1DEV',    11,              'OMERO5.1DEV',    10);

--
-- Actual upgrade: generated with apgdiff-2.3
--

CREATE SEQUENCE seq_unitstime
	START WITH 1
	INCREMENT BY 1
	NO MAXVALUE
	NO MINVALUE
	CACHE 1;

CREATE TABLE unitstime (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	measurementsystem character varying(255) NOT NULL,
	"value" character varying(255) NOT NULL,
	external_id bigint
);

ALTER TABLE pixels
	ADD COLUMN timeincrementunit bigint;

ALTER TABLE planeinfo
	ADD COLUMN deltatunit bigint,
	ADD COLUMN exposuretimeunit bigint;

ALTER TABLE unitstime
	ADD CONSTRAINT unitstime_pkey PRIMARY KEY (id);

ALTER TABLE pixels
	ADD CONSTRAINT fkpixels_timeincrementunit_unitstime FOREIGN KEY (timeincrementunit) REFERENCES unitstime(id);

ALTER TABLE planeinfo
	ADD CONSTRAINT fkplaneinfo_deltaunit_unitstime FOREIGN KEY (deltatunit) REFERENCES unitstime(id);

ALTER TABLE planeinfo
	ADD CONSTRAINT fkplaneinfo_exposuretimeunit_unitstime FOREIGN KEY (exposuretimeunit) REFERENCES unitstime(id);

ALTER TABLE unitstime
	ADD CONSTRAINT unitstime_external_id_key UNIQUE (external_id);

ALTER TABLE unitstime
	ADD CONSTRAINT unitstime_value_key UNIQUE (value);

ALTER TABLE unitstime
	ADD CONSTRAINT fkunitstime_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

CREATE INDEX i_pixels_timeincrement ON pixels USING btree (timeincrement);

CREATE INDEX i_planeinfo_deltat ON planeinfo USING btree (deltat);

CREATE INDEX i_planeinfo_exposuretime ON planeinfo USING btree (exposuretime);

--
-- Manual adjustments, mostly from psql-footer.sql
--

insert into unitstime (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitstime'),-52,'Ys','SI.SECOND';
insert into unitstime (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitstime'),-52,'Zs','SI.SECOND';
insert into unitstime (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitstime'),-52,'Es','SI.SECOND';
insert into unitstime (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitstime'),-52,'Ps','SI.SECOND';
insert into unitstime (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitstime'),-52,'Ts','SI.SECOND';
insert into unitstime (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitstime'),-52,'Gs','SI.SECOND';
insert into unitstime (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitstime'),-52,'Ms','SI.SECOND';
insert into unitstime (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitstime'),-52,'ks','SI.SECOND';
insert into unitstime (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitstime'),-52,'hs','SI.SECOND';
insert into unitstime (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitstime'),-52,'das','SI.SECOND';
insert into unitstime (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitstime'),-52,'s','SI.SECOND';
insert into unitstime (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitstime'),-52,'ds','SI.SECOND';
insert into unitstime (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitstime'),-52,'cs','SI.SECOND';
insert into unitstime (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitstime'),-52,'ms','SI.SECOND';
insert into unitstime (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitstime'),-52,'µs','SI.SECOND';
insert into unitstime (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitstime'),-52,'ns','SI.SECOND';
insert into unitstime (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitstime'),-52,'ps','SI.SECOND';
insert into unitstime (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitstime'),-52,'fs','SI.SECOND';
insert into unitstime (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitstime'),-52,'as','SI.SECOND';
insert into unitstime (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitstime'),-52,'zs','SI.SECOND';
insert into unitstime (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitstime'),-52,'ys','SI.SECOND';
insert into unitstime (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitstime'),-52,'min','SI.SECOND';
insert into unitstime (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitstime'),-52,'h','SI.SECOND';
insert into unitstime (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitstime'),-52,'d','SI.SECOND';

update pixels set timeincrementunit = (select id from unitstime where value = 's') where timeincrement is not null;

update planeinfo
  set deltatunit = case when deltat is null then null else (select id from unitstime where value = 's') end,
      exposuretimeunit = case when exposuretime is null then null else (select id from unitstime where value = 's') end
  where deltat is not null or exposuretime is not null;

--
-- FINISHED
--

UPDATE dbpatch SET message = 'Database updated.', finished = clock_timestamp()
    WHERE currentVersion  = 'OMERO5.1DEV' AND
          currentPatch    = 11            AND
          previousVersion = 'OMERO5.1DEV' AND
          previousPatch   = 10;

SELECT CHR(10)||CHR(10)||CHR(10)||'YOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION OMERO5.1DEV__11'||CHR(10)||CHR(10)||CHR(10) AS Status;

COMMIT;
