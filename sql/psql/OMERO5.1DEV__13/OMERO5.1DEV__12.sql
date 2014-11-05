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
--- OMERO5 development release upgrade from OMERO5.1DEV__12 to OMERO5.1DEV__13.
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

SELECT omero_assert_db_version('OMERO5.1DEV', 12);
DROP FUNCTION omero_assert_db_version(varchar, int);


INSERT INTO dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
             VALUES ('OMERO5.1DEV',  13,                'OMERO5.1DEV',    12);

--
-- Actual upgrade
--

-- 5.1DEV__13: other units

CREATE SEQUENCE seq_unitselectricpotential
	START WITH 1
	INCREMENT BY 1
	NO MAXVALUE
	NO MINVALUE
	CACHE 1;

CREATE SEQUENCE seq_unitsfrequency
	START WITH 1
	INCREMENT BY 1
	NO MAXVALUE
	NO MINVALUE
	CACHE 1;

CREATE SEQUENCE seq_unitslength
	START WITH 1
	INCREMENT BY 1
	NO MAXVALUE
	NO MINVALUE
	CACHE 1;

CREATE SEQUENCE seq_unitspower
	START WITH 1
	INCREMENT BY 1
	NO MAXVALUE
	NO MINVALUE
	CACHE 1;

CREATE SEQUENCE seq_unitspressure
	START WITH 1
	INCREMENT BY 1
	NO MAXVALUE
	NO MINVALUE
	CACHE 1;

CREATE SEQUENCE seq_unitstemperature
	START WITH 1
	INCREMENT BY 1
	NO MAXVALUE
	NO MINVALUE
	CACHE 1;

CREATE TABLE unitselectricpotential (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	measurementsystem character varying(255) NOT NULL,
	"value" character varying(255) NOT NULL,
	external_id bigint
);

CREATE TABLE unitsfrequency (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	measurementsystem character varying(255) NOT NULL,
	"value" character varying(255) NOT NULL,
	external_id bigint
);

CREATE TABLE unitslength (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	measurementsystem character varying(255) NOT NULL,
	"value" character varying(255) NOT NULL,
	external_id bigint
);

CREATE TABLE unitspower (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	measurementsystem character varying(255),
	"value" character varying(255) NOT NULL,
	external_id bigint
);

CREATE TABLE unitspressure (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	measurementsystem character varying(255) NOT NULL,
	"value" character varying(255) NOT NULL,
	external_id bigint
);

CREATE TABLE unitstemperature (
	id bigint NOT NULL,
	permissions bigint NOT NULL,
	measurementsystem character varying(255) NOT NULL,
	"value" character varying(255) NOT NULL,
	external_id bigint
);

ALTER TABLE detector
	ADD COLUMN voltageunit bigint;

ALTER TABLE detectorsettings
	ADD COLUMN readoutrateunit bigint,
	ADD COLUMN voltageunit bigint;

ALTER TABLE imagingenvironment
	ADD COLUMN airpressureunit bigint,
	ADD COLUMN temperatureunit bigint;

ALTER TABLE laser
	ADD COLUMN repetitionrateunit bigint,
	ADD COLUMN wavelengthunit bigint,
	ALTER COLUMN wavelength TYPE double precision /* TYPE change - table: laser original: positive_float new: double precision */;

ALTER TABLE lightsettings
	ADD COLUMN wavelengthunit bigint,
	ALTER COLUMN wavelength TYPE double precision /* TYPE change - table: lightsettings original: positive_float new: double precision */;

ALTER TABLE lightsource
	ADD COLUMN powerunit bigint;

ALTER TABLE logicalchannel
	ADD COLUMN emissionwaveunit bigint,
	ADD COLUMN excitationwaveunit bigint,
	ADD COLUMN pinholesizeunit bigint,
	ALTER COLUMN emissionwave TYPE double precision /* TYPE change - table: logicalchannel original: positive_float new: double precision */,
	ALTER COLUMN excitationwave TYPE double precision /* TYPE change - table: logicalchannel original: positive_float new: double precision */;

ALTER TABLE objective
	ADD COLUMN workingdistanceunit bigint;

ALTER TABLE pixels
	ADD COLUMN physicalsizexunit bigint,
	ADD COLUMN physicalsizeyunit bigint,
	ADD COLUMN physicalsizezunit bigint,
	ALTER COLUMN physicalsizex TYPE double precision /* TYPE change - table: pixels original: positive_float new: double precision */,
	ALTER COLUMN physicalsizey TYPE double precision /* TYPE change - table: pixels original: positive_float new: double precision */,
	ALTER COLUMN physicalsizez TYPE double precision /* TYPE change - table: pixels original: positive_float new: double precision */;

ALTER TABLE planeinfo
	ADD COLUMN positionxunit bigint,
	ADD COLUMN positionyunit bigint,
	ADD COLUMN positionzunit bigint;

ALTER TABLE plate
	ADD COLUMN welloriginxunit bigint,
	ADD COLUMN welloriginyunit bigint;

ALTER TABLE shape
	ADD COLUMN fontsizeunit bigint,
	ADD COLUMN strokewidthunit bigint,
	ALTER COLUMN fontsize TYPE double precision /* TYPE change - table: shape original: integer new: double precision */,
	ALTER COLUMN strokewidth TYPE double precision /* TYPE change - table: shape original: integer new: double precision */;

ALTER TABLE stagelabel
	ADD COLUMN positionxunit bigint,
	ADD COLUMN positionyunit bigint,
	ADD COLUMN positionzunit bigint;

ALTER TABLE transmittancerange
	ADD COLUMN cutinunit bigint,
	ADD COLUMN cutintoleranceunit bigint,
	ADD COLUMN cutoutunit bigint,
	ADD COLUMN cutouttoleranceunit bigint,
	ALTER COLUMN cutin TYPE double precision /* TYPE change - table: transmittancerange original: positive_int new: double precision */,
	ALTER COLUMN cutintolerance TYPE double precision /* TYPE change - table: transmittancerange original: nonnegative_int new: double precision */,
	ALTER COLUMN cutout TYPE double precision /* TYPE change - table: transmittancerange original: positive_int new: double precision */,
	ALTER COLUMN cutouttolerance TYPE double precision /* TYPE change - table: transmittancerange original: nonnegative_int new: double precision */;

ALTER TABLE wellsample
	ADD COLUMN posxunit bigint,
	ADD COLUMN posyunit bigint;

ALTER TABLE unitselectricpotential
	ADD CONSTRAINT unitselectricpotential_pkey PRIMARY KEY (id);

ALTER TABLE unitsfrequency
	ADD CONSTRAINT unitsfrequency_pkey PRIMARY KEY (id);

ALTER TABLE unitslength
	ADD CONSTRAINT unitslength_pkey PRIMARY KEY (id);

ALTER TABLE unitspower
	ADD CONSTRAINT unitspower_pkey PRIMARY KEY (id);

ALTER TABLE unitspressure
	ADD CONSTRAINT unitspressure_pkey PRIMARY KEY (id);

ALTER TABLE unitstemperature
	ADD CONSTRAINT unitstemperature_pkey PRIMARY KEY (id);

ALTER TABLE detector
	ADD CONSTRAINT fkdetector_voltageunit_unitselectricpotential FOREIGN KEY (voltageunit) REFERENCES unitselectricpotential(id);

ALTER TABLE detectorsettings
	ADD CONSTRAINT fkdetectorsettings_voltageunit_unitselectricpotential FOREIGN KEY (voltageunit) REFERENCES unitselectricpotential(id);

ALTER TABLE detectorsettings
	ADD CONSTRAINT fkdetectorsettings_readoutrateunit_unitsfrequency FOREIGN KEY (readoutrateunit) REFERENCES unitsfrequency(id);

ALTER TABLE imagingenvironment
	ADD CONSTRAINT fkimagingenvironment_airpressureunit_unitspressure FOREIGN KEY (airpressureunit) REFERENCES unitspressure(id);

ALTER TABLE imagingenvironment
	ADD CONSTRAINT fkimagingenvironment_temperatureunit_unitstemperature FOREIGN KEY (temperatureunit) REFERENCES unitstemperature(id);

ALTER TABLE laser
	ADD CONSTRAINT fklaser_wavelengthunit_unitslength FOREIGN KEY (wavelengthunit) REFERENCES unitslength(id);

ALTER TABLE laser
	ADD CONSTRAINT fklaser_repetitionrateunit_unitsfrequency FOREIGN KEY (repetitionrateunit) REFERENCES unitsfrequency(id);

ALTER TABLE lightsettings
	ADD CONSTRAINT fklightsettings_wavelengthunit_unitslength FOREIGN KEY (wavelengthunit) REFERENCES unitslength(id);

ALTER TABLE lightsource
	ADD CONSTRAINT fklightsource_powerunit_unitspower FOREIGN KEY (powerunit) REFERENCES unitspower(id);

ALTER TABLE logicalchannel
	ADD CONSTRAINT fklogicalchannel_pinholesizeunit_unitslength FOREIGN KEY (pinholesizeunit) REFERENCES unitslength(id);

ALTER TABLE logicalchannel
	ADD CONSTRAINT fklogicalchannel_emissionwaveunit_unitslength FOREIGN KEY (emissionwaveunit) REFERENCES unitslength(id);

ALTER TABLE logicalchannel
	ADD CONSTRAINT fklogicalchannel_excitationwaveunit_unitslength FOREIGN KEY (excitationwaveunit) REFERENCES unitslength(id);

ALTER TABLE objective
	ADD CONSTRAINT fkobjective_workingdistanceunit_unitslength FOREIGN KEY (workingdistanceunit) REFERENCES unitslength(id);

ALTER TABLE pixels
	ADD CONSTRAINT fkpixels_physicalsizexunit_unitslength FOREIGN KEY (physicalsizexunit) REFERENCES unitslength(id);

ALTER TABLE pixels
	ADD CONSTRAINT fkpixels_physicalsizeyunit_unitslength FOREIGN KEY (physicalsizeyunit) REFERENCES unitslength(id);

ALTER TABLE pixels
	ADD CONSTRAINT fkpixels_physicalsizezunit_unitslength FOREIGN KEY (physicalsizezunit) REFERENCES unitslength(id);

ALTER TABLE planeinfo
	ADD CONSTRAINT fkplaneinfo_positionxunit_unitslength FOREIGN KEY (positionxunit) REFERENCES unitslength(id);

ALTER TABLE planeinfo
	ADD CONSTRAINT fkplaneinfo_positionyunit_unitslength FOREIGN KEY (positionyunit) REFERENCES unitslength(id);

ALTER TABLE planeinfo
	ADD CONSTRAINT fkplaneinfo_positionzunit_unitslength FOREIGN KEY (positionzunit) REFERENCES unitslength(id);

ALTER TABLE plate
	ADD CONSTRAINT fkplate_welloriginxunit_unitslength FOREIGN KEY (welloriginxunit) REFERENCES unitslength(id);

ALTER TABLE plate
	ADD CONSTRAINT fkplate_welloriginyunit_unitslength FOREIGN KEY (welloriginyunit) REFERENCES unitslength(id);

ALTER TABLE shape
	ADD CONSTRAINT fkshape_strokewidthunit_unitslength FOREIGN KEY (strokewidthunit) REFERENCES unitslength(id);

ALTER TABLE shape
	ADD CONSTRAINT fkshape_fontsizeunit_unitslength FOREIGN KEY (fontsizeunit) REFERENCES unitslength(id);

ALTER TABLE stagelabel
	ADD CONSTRAINT fkstagelabel_positionxunit_unitslength FOREIGN KEY (positionxunit) REFERENCES unitslength(id);

ALTER TABLE stagelabel
	ADD CONSTRAINT fkstagelabel_positionyunit_unitslength FOREIGN KEY (positionyunit) REFERENCES unitslength(id);

ALTER TABLE stagelabel
	ADD CONSTRAINT fkstagelabel_positionzunit_unitslength FOREIGN KEY (positionzunit) REFERENCES unitslength(id);

ALTER TABLE transmittancerange
	ADD CONSTRAINT fktransmittancerange_cutouttoleranceunit_unitslength FOREIGN KEY (cutouttoleranceunit) REFERENCES unitslength(id);

ALTER TABLE transmittancerange
	ADD CONSTRAINT fktransmittancerange_cutoutunit_unitslength FOREIGN KEY (cutoutunit) REFERENCES unitslength(id);

ALTER TABLE transmittancerange
	ADD CONSTRAINT fktransmittancerange_cutintoleranceunit_unitslength FOREIGN KEY (cutintoleranceunit) REFERENCES unitslength(id);

ALTER TABLE transmittancerange
	ADD CONSTRAINT fktransmittancerange_cutinunit_unitslength FOREIGN KEY (cutinunit) REFERENCES unitslength(id);

ALTER TABLE unitselectricpotential
	ADD CONSTRAINT unitselectricpotential_external_id_key UNIQUE (external_id);

ALTER TABLE unitselectricpotential
	ADD CONSTRAINT unitselectricpotential_value_key UNIQUE (value);

ALTER TABLE unitselectricpotential
	ADD CONSTRAINT fkunitselectricpotential_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE unitsfrequency
	ADD CONSTRAINT unitsfrequency_external_id_key UNIQUE (external_id);

ALTER TABLE unitsfrequency
	ADD CONSTRAINT unitsfrequency_value_key UNIQUE (value);

ALTER TABLE unitsfrequency
	ADD CONSTRAINT fkunitsfrequency_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE unitslength
	ADD CONSTRAINT unitslength_external_id_key UNIQUE (external_id);

ALTER TABLE unitslength
	ADD CONSTRAINT unitslength_value_key UNIQUE (value);

ALTER TABLE unitslength
	ADD CONSTRAINT fkunitslength_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE unitspower
	ADD CONSTRAINT unitspower_external_id_key UNIQUE (external_id);

ALTER TABLE unitspower
	ADD CONSTRAINT unitspower_value_key UNIQUE (value);

ALTER TABLE unitspower
	ADD CONSTRAINT fkunitspower_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE unitspressure
	ADD CONSTRAINT unitspressure_external_id_key UNIQUE (external_id);

ALTER TABLE unitspressure
	ADD CONSTRAINT unitspressure_value_key UNIQUE (value);

ALTER TABLE unitspressure
	ADD CONSTRAINT fkunitspressure_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE unitstemperature
	ADD CONSTRAINT unitstemperature_external_id_key UNIQUE (external_id);

ALTER TABLE unitstemperature
	ADD CONSTRAINT unitstemperature_value_key UNIQUE (value);

ALTER TABLE unitstemperature
	ADD CONSTRAINT fkunitstemperature_external_id_externalinfo FOREIGN KEY (external_id) REFERENCES externalinfo(id);

ALTER TABLE wellsample
	ADD CONSTRAINT fkwellsample_posxunit_unitslength FOREIGN KEY (posxunit) REFERENCES unitslength(id);

ALTER TABLE wellsample
	ADD CONSTRAINT fkwellsample_posyunit_unitslength FOREIGN KEY (posyunit) REFERENCES unitslength(id);

CREATE INDEX i_detector_voltage ON detector USING btree (voltage);

CREATE INDEX i_detectorsettings_readoutrate ON detectorsettings USING btree (readoutrate);

CREATE INDEX i_detectorsettings_voltage ON detectorsettings USING btree (voltage);

CREATE INDEX i_imagingenvironment_airpressure ON imagingenvironment USING btree (airpressure);

CREATE INDEX i_imagingenvironment_temperature ON imagingenvironment USING btree (temperature);

CREATE INDEX i_laser_repetitionrate ON laser USING btree (repetitionrate);

CREATE INDEX i_laser_wavelength ON laser USING btree (wavelength);

CREATE INDEX i_lightsettings_wavelength ON lightsettings USING btree (wavelength);

CREATE INDEX i_lightsource_power ON lightsource USING btree (power);

CREATE INDEX i_logicalchannel_emissionwave ON logicalchannel USING btree (emissionwave);

CREATE INDEX i_logicalchannel_excitationwave ON logicalchannel USING btree (excitationwave);

CREATE INDEX i_logicalchannel_pinholesize ON logicalchannel USING btree (pinholesize);

CREATE INDEX i_objective_workingdistance ON objective USING btree (workingdistance);

CREATE INDEX i_pixels_physicalsizex ON pixels USING btree (physicalsizex);

CREATE INDEX i_pixels_physicalsizey ON pixels USING btree (physicalsizey);

CREATE INDEX i_pixels_physicalsizez ON pixels USING btree (physicalsizez);

CREATE INDEX i_planeinfo_positionx ON planeinfo USING btree (positionx);

CREATE INDEX i_planeinfo_positiony ON planeinfo USING btree (positiony);

CREATE INDEX i_planeinfo_positionz ON planeinfo USING btree (positionz);

CREATE INDEX i_plate_welloriginx ON plate USING btree (welloriginx);

CREATE INDEX i_plate_welloriginy ON plate USING btree (welloriginy);

CREATE INDEX i_shape_fontsize ON shape USING btree (fontsize);

CREATE INDEX i_shape_strokewidth ON shape USING btree (strokewidth);

CREATE INDEX i_stagelabel_positionx ON stagelabel USING btree (positionx);

CREATE INDEX i_stagelabel_positiony ON stagelabel USING btree (positiony);

CREATE INDEX i_stagelabel_positionz ON stagelabel USING btree (positionz);

CREATE INDEX i_transmittancerange_cutin ON transmittancerange USING btree (cutin);

CREATE INDEX i_transmittancerange_cutintolerance ON transmittancerange USING btree (cutintolerance);

CREATE INDEX i_transmittancerange_cutout ON transmittancerange USING btree (cutout);

CREATE INDEX i_transmittancerange_cutouttolerance ON transmittancerange USING btree (cutouttolerance);

CREATE INDEX i_wellsample_posx ON wellsample USING btree (posx);

CREATE INDEX i_wellsample_posy ON wellsample USING btree (posy);

-- 5.1DEV__13: Manual adjustments, mostly from psql-footer.sql

insert into unitselectricpotential (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitselectricpotential'),-35,'YV','SI.VOLT';

insert into unitselectricpotential (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitselectricpotential'),-35,'ZV','SI.VOLT';

insert into unitselectricpotential (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitselectricpotential'),-35,'EV','SI.VOLT';

insert into unitselectricpotential (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitselectricpotential'),-35,'PV','SI.VOLT';

insert into unitselectricpotential (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitselectricpotential'),-35,'TV','SI.VOLT';

insert into unitselectricpotential (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitselectricpotential'),-35,'GV','SI.VOLT';

insert into unitselectricpotential (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitselectricpotential'),-35,'MV','SI.VOLT';

insert into unitselectricpotential (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitselectricpotential'),-35,'kV','SI.VOLT';

insert into unitselectricpotential (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitselectricpotential'),-35,'hV','SI.VOLT';

insert into unitselectricpotential (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitselectricpotential'),-35,'daV','SI.VOLT';

insert into unitselectricpotential (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitselectricpotential'),-35,'V','SI.VOLT';

insert into unitselectricpotential (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitselectricpotential'),-35,'dV','SI.VOLT';

insert into unitselectricpotential (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitselectricpotential'),-35,'cV','SI.VOLT';

insert into unitselectricpotential (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitselectricpotential'),-35,'mV','SI.VOLT';

insert into unitselectricpotential (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitselectricpotential'),-35,'µV','SI.VOLT';

insert into unitselectricpotential (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitselectricpotential'),-35,'nV','SI.VOLT';

insert into unitselectricpotential (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitselectricpotential'),-35,'pV','SI.VOLT';

insert into unitselectricpotential (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitselectricpotential'),-35,'fV','SI.VOLT';

insert into unitselectricpotential (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitselectricpotential'),-35,'aV','SI.VOLT';

insert into unitselectricpotential (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitselectricpotential'),-35,'zV','SI.VOLT';

insert into unitselectricpotential (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitselectricpotential'),-35,'yV','SI.VOLT';

insert into unitsfrequency (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitsfrequency'),-35,'YHz','SI.HERTZ';

insert into unitsfrequency (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitsfrequency'),-35,'ZHz','SI.HERTZ';

insert into unitsfrequency (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitsfrequency'),-35,'EHz','SI.HERTZ';

insert into unitsfrequency (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitsfrequency'),-35,'PHz','SI.HERTZ';

insert into unitsfrequency (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitsfrequency'),-35,'THz','SI.HERTZ';

insert into unitsfrequency (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitsfrequency'),-35,'GHz','SI.HERTZ';

insert into unitsfrequency (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitsfrequency'),-35,'MHz','SI.HERTZ';

insert into unitsfrequency (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitsfrequency'),-35,'kHz','SI.HERTZ';

insert into unitsfrequency (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitsfrequency'),-35,'hHz','SI.HERTZ';

insert into unitsfrequency (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitsfrequency'),-35,'daHz','SI.HERTZ';

insert into unitsfrequency (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitsfrequency'),-35,'Hz','SI.HERTZ';

insert into unitsfrequency (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitsfrequency'),-35,'dHz','SI.HERTZ';

insert into unitsfrequency (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitsfrequency'),-35,'cHz','SI.HERTZ';

insert into unitsfrequency (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitsfrequency'),-35,'mHz','SI.HERTZ';

insert into unitsfrequency (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitsfrequency'),-35,'µHz','SI.HERTZ';

insert into unitsfrequency (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitsfrequency'),-35,'nHz','SI.HERTZ';

insert into unitsfrequency (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitsfrequency'),-35,'pHz','SI.HERTZ';

insert into unitsfrequency (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitsfrequency'),-35,'fHz','SI.HERTZ';

insert into unitsfrequency (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitsfrequency'),-35,'aHz','SI.HERTZ';

insert into unitsfrequency (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitsfrequency'),-35,'zHz','SI.HERTZ';

insert into unitsfrequency (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitsfrequency'),-35,'yHz','SI.HERTZ';

insert into unitslength (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitslength'),-35,'Ym','SI.METRE';

insert into unitslength (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitslength'),-35,'Zm','SI.METRE';

insert into unitslength (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitslength'),-35,'Em','SI.METRE';

insert into unitslength (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitslength'),-35,'Pm','SI.METRE';

insert into unitslength (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitslength'),-35,'Tm','SI.METRE';

insert into unitslength (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitslength'),-35,'Gm','SI.METRE';

insert into unitslength (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitslength'),-35,'Mm','SI.METRE';

insert into unitslength (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitslength'),-35,'km','SI.METRE';

insert into unitslength (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitslength'),-35,'hm','SI.METRE';

insert into unitslength (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitslength'),-35,'dam','SI.METRE';

insert into unitslength (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitslength'),-35,'m','SI.METRE';

insert into unitslength (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitslength'),-35,'dm','SI.METRE';

insert into unitslength (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitslength'),-35,'cm','SI.METRE';

insert into unitslength (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitslength'),-35,'mm','SI.METRE';

insert into unitslength (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitslength'),-35,'µm','SI.METRE';

insert into unitslength (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitslength'),-35,'nm','SI.METRE';

insert into unitslength (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitslength'),-35,'pm','SI.METRE';

insert into unitslength (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitslength'),-35,'fm','SI.METRE';

insert into unitslength (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitslength'),-35,'am','SI.METRE';

insert into unitslength (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitslength'),-35,'zm','SI.METRE';

insert into unitslength (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitslength'),-35,'ym','SI.METRE';

insert into unitslength (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitslength'),-35,'angstrom','SI.METRE';

insert into unitslength (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitslength'),-35,'thou','Imperial.INCH';

insert into unitslength (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitslength'),-35,'li','Imperial.INCH';

insert into unitslength (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitslength'),-35,'in','Imperial.INCH';

insert into unitslength (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitslength'),-35,'ft','Imperial.INCH';

insert into unitslength (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitslength'),-35,'yd','Imperial.INCH';

insert into unitslength (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitslength'),-35,'mi','Imperial.INCH';

insert into unitslength (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitslength'),-35,'ua','SI.METRE';

insert into unitslength (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitslength'),-35,'ly','SI.METRE';

insert into unitslength (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitslength'),-35,'pc','SI.METRE';

insert into unitslength (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitslength'),-35,'pt','Imperial.INCH';

insert into unitslength (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitslength'),-35,'pixel','Pixel';

insert into unitslength (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitslength'),-35,'reference frame','ReferenceFrame';

insert into unitspower (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspower'),-35,'YW','SI.WATT';

insert into unitspower (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspower'),-35,'ZW','SI.WATT';

insert into unitspower (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspower'),-35,'EW','SI.WATT';

insert into unitspower (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspower'),-35,'PW','SI.WATT';

insert into unitspower (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspower'),-35,'TW','SI.WATT';

insert into unitspower (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspower'),-35,'GW','SI.WATT';

insert into unitspower (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspower'),-35,'MW','SI.WATT';

insert into unitspower (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspower'),-35,'kW','SI.WATT';

insert into unitspower (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspower'),-35,'hW','SI.WATT';

insert into unitspower (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspower'),-35,'daW','SI.WATT';

insert into unitspower (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspower'),-35,'W','SI.WATT';

insert into unitspower (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspower'),-35,'dW','SI.WATT';

insert into unitspower (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspower'),-35,'cW','SI.WATT';

insert into unitspower (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspower'),-35,'mW','SI.WATT';

insert into unitspower (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspower'),-35,'µW','SI.WATT';

insert into unitspower (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspower'),-35,'nW','SI.WATT';

insert into unitspower (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspower'),-35,'pW','SI.WATT';

insert into unitspower (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspower'),-35,'fW','SI.WATT';

insert into unitspower (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspower'),-35,'aW','SI.WATT';

insert into unitspower (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspower'),-35,'zW','SI.WATT';

insert into unitspower (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspower'),-35,'yW','SI.WATT';

insert into unitspressure (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspressure'),-35,'YPa','SI.PASCAL';

insert into unitspressure (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspressure'),-35,'ZPa','SI.PASCAL';

insert into unitspressure (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspressure'),-35,'EPa','SI.PASCAL';

insert into unitspressure (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspressure'),-35,'PPa','SI.PASCAL';

insert into unitspressure (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspressure'),-35,'TPa','SI.PASCAL';

insert into unitspressure (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspressure'),-35,'GPa','SI.PASCAL';

insert into unitspressure (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspressure'),-35,'MPa','SI.PASCAL';

insert into unitspressure (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspressure'),-35,'kPa','SI.PASCAL';

insert into unitspressure (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspressure'),-35,'hPa','SI.PASCAL';

insert into unitspressure (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspressure'),-35,'daPa','SI.PASCAL';

insert into unitspressure (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspressure'),-35,'Pa','SI.PASCAL';

insert into unitspressure (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspressure'),-35,'dPa','SI.PASCAL';

insert into unitspressure (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspressure'),-35,'cPa','SI.PASCAL';

insert into unitspressure (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspressure'),-35,'mPa','SI.PASCAL';

insert into unitspressure (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspressure'),-35,'µPa','SI.PASCAL';

insert into unitspressure (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspressure'),-35,'nPa','SI.PASCAL';

insert into unitspressure (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspressure'),-35,'pPa','SI.PASCAL';

insert into unitspressure (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspressure'),-35,'fPa','SI.PASCAL';

insert into unitspressure (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspressure'),-35,'aPa','SI.PASCAL';

insert into unitspressure (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspressure'),-35,'zPa','SI.PASCAL';

insert into unitspressure (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspressure'),-35,'yPa','SI.PASCAL';

insert into unitspressure (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspressure'),-35,'Mbar','SI.PASCAL';

insert into unitspressure (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspressure'),-35,'kbar','SI.PASCAL';

insert into unitspressure (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspressure'),-35,'bar','SI.PASCAL';

insert into unitspressure (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspressure'),-35,'dbar','SI.PASCAL';

insert into unitspressure (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspressure'),-35,'mbar','SI.PASCAL';

insert into unitspressure (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspressure'),-35,'atm','SI.PASCAL';

insert into unitspressure (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspressure'),-35,'psi','SI.PASCAL';

insert into unitspressure (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspressure'),-35,'Torr','SI.PASCAL';

insert into unitspressure (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspressure'),-35,'mTorr','SI.PASCAL';

insert into unitspressure (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitspressure'),-35,'mm Hg','SI.PASCAL';

insert into unitstemperature (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitstemperature'),-35,'K','SI.KELVIN';

insert into unitstemperature (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitstemperature'),-35,'°C','SI.KELVIN';

insert into unitstemperature (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitstemperature'),-35,'°R','SI.KELVIN';

insert into unitstemperature (id,permissions,value,measurementsystem)
    select ome_nextval('seq_unitstemperature'),-35,'°F','SI.KELVIN';

update pixels set timeincrementunit = (select id from unitstime where value = 's') where timeincrement is not null;

update planeinfo set deltatunit = (select id from unitstime where value = 's')  where deltat is not null;
update planeinfo set exposuretimeunit = (select id from unitstime where value = 's') where exposuretime is not null;

update detector set voltageunit = (select id from unitselectricpotential where value = 'V') where  voltageunit is not null;

update detectorsettings set readoutrateunit = (select id from unitsfrequency where value = 'MHz') where readoutrateunit is not null;
update detectorsettings set voltageunit = (select id from unitselectricpotential where value = 'V') where voltageunit is not null;

update imagingenvironment set airpressureunit = (select id from unitspressure where value = 'mbar') where airpressureunit is not null;
update imagingenvironment set temperatureunit = (select id from unitstemperature where value = '°C') where temperatureunit is not null;

update laser set repetitionrateunit = (select id from unitsfrequency where value = 'Hz') where repetitionrateunit is not null;
update laser set wavelengthunit = (select id from unitslength where value = 'nm') where wavelengthunit is not null;

update lightsettings set wavelengthunit = (select id from unitslength where value = 'nm') where wavelengthunit is not null;

update lightsource set powerunit = (select id from unitspower where value = 'mW') where powerunit is not null;

update logicalchannel set emissionwaveunit = (select id from unitslength where value = 'nm') where emissionwaveunit is not null;
update logicalchannel set excitationwaveunit = (select id from unitslength where value = 'nm') where excitationwaveunit is not null;
update logicalchannel set pinholesizeunit = (select id from unitslength where value = 'µm') where pinholesizeunit is not null;

update objective set workingdistanceunit = (select id from unitslength where value = 'µm') where workingdistanceunit is not null;

update pixels set physicalsizexunit = (select id from unitslength where value = 'µm') where physicalsizexunit is not null;
update pixels set physicalsizeyunit = (select id from unitslength where value = 'µm') where physicalsizeyunit is not null;
update pixels set physicalsizezunit = (select id from unitslength where value = 'µm') where physicalsizezunit is not null;

update planeinfo set positionxunit = (select id from unitslength where value = 'reference frame') where positionxunit is not null;
update planeinfo set positionyunit = (select id from unitslength where value = 'reference frame') where positionyunit is not null;
update planeinfo set positionzunit = (select id from unitslength where value = 'reference frame') where positionzunit is not null;

update plate set welloriginxunit = (select id from unitslength where value = 'reference frame') where welloriginxunit is not null;
update plate set welloriginyunit = (select id from unitslength where value = 'reference frame') where welloriginyunit is not null;

update shape set fontsizeunit = (select id from unitslength  where value = 'pt') where fontsizeunit is not null;
update shape set strokewidthunit = (select id from unitslength  where value = 'pixel') where strokewidthunit is not null;

update stagelabel set positionxunit = (select id from unitslength where value = 'reference frame') where positionxunit is not null;
update stagelabel set positionyunit = (select id from unitslength where value = 'reference frame') where positionyunit is not null;
update stagelabel set positionzunit = (select id from unitslength where value = 'reference frame') where positionzunit is not null;

update transmittancerange set cutinunit = (select id from unitslength where value = 'nm') where cutinunit is not null;
update transmittancerange set cutintoleranceunit = (select id from unitslength where value = 'nm') where cutintoleranceunit is not null;
update transmittancerange set cutoutunit = (select id from unitslength where value = 'nm') where cutoutunit is not null;
update transmittancerange set cutouttoleranceunit = (select id from unitslength where value = 'nm') where cutouttoleranceunit is not null;

update wellsample set posxunit = (select id from unitslength where value = 'reference frame') where posxunit is not null;
update wellsample set posyunit = (select id from unitslength where value = 'reference frame') where posyunit is not null;

-- reactivate not null constraints
alter table pixelstype alter column bitsize set not null;
alter table unitselectricpotential alter column measurementsystem set not null;
alter table unitsfrequency alter column measurementsystem set not null;
alter table unitslength alter column measurementsystem set not null;
alter table unitspressure alter column measurementsystem set not null;
alter table unitstemperature alter column measurementsystem set not null;
alter table unitstime alter column measurementsystem set not null;

--
-- FINISHED
--

UPDATE dbpatch SET message = 'Database updated.', finished = clock_timestamp()
    WHERE currentVersion  = 'OMERO5.1DEV' AND
          currentPatch    = 13            AND
          previousVersion = 'OMERO5.1DEV' AND
          previousPatch   = 12;

SELECT CHR(10)||CHR(10)||CHR(10)||'YOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION OMERO5.1DEV__13'||CHR(10)||CHR(10)||CHR(10) AS Status;

COMMIT;
