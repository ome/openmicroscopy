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

-- 5.1DEV__11: time units

-- 5.1DEV__13 requires a rollback of the 5.1DEV__11 time unit

DROP SEQUENCE seq_unitstime;

ALTER TABLE unitstime RENAME TO unitstime_old;


CREATE TYPE UnitsTime AS ENUM ('Ys','Zs','Es','Ps','Ts','Gs','Ms','ks','hs','das','s','ds','cs','ms','µs','ns','ps','fs','as','zs','ys','min','h','d');


ALTER TABLE pixels
    ADD COLUMN timeincrementunit_tmp unitstime;

UPDATE pixels
    SET timeincrementunit_tmp = cast(unitstime_old.value as unitstime)
   FROM unitstime_old
  WHERE unitstime_old.id = timeincrementunit;

ALTER TABLE pixels
    DROP COLUMN timeincrementunit;

ALTER TABLE pixels
    RENAME COLUMN timeincrementunit_tmp TO timeincrementunit;

ALTER TABLE planeinfo
    ADD COLUMN deltatunit_tmp unitstime,
    ADD COLUMN exposuretimeunit_tmp unitstime;

UPDATE planeinfo
    SET deltatunit_tmp = cast(unitstime_old.value as unitstime)
   FROM unitstime_old
  WHERE unitstime_old.id = deltatunit;

ALTER TABLE planeinfo
    DROP COLUMN deltatunit;

ALTER TABLE planeinfo
    RENAME COLUMN deltatunit_tmp TO deltatunit;

UPDATE planeinfo
    SET exposuretimeunit_tmp = cast(unitstime_old.value as unitstime)
   FROM unitstime_old
  WHERE unitstime_old.id = exposuretimeunit;

ALTER TABLE planeinfo
    DROP COLUMN exposuretimeunit;

ALTER TABLE planeinfo
    RENAME COLUMN exposuretimeunit_tmp TO exposuretimeunit;

DROP TABLE unitstime_old;

DROP INDEX i_pixels_timeincrement;

DROP INDEX i_planeinfo_deltat;

DROP INDEX i_planeinfo_exposuretime;

CREATE INDEX i_pixels_timeincrement ON pixels USING btree (timeincrement);

CREATE INDEX i_planeinfo_deltat ON planeinfo USING btree (deltat);

CREATE INDEX i_planeinfo_exposuretime ON planeinfo USING btree (exposuretime);


-- 5.1DEV__13: other units

CREATE TYPE UnitsElectricPotential AS ENUM ('YV','ZV','EV','PV','TV','GV','MV','kV','hV','daV','V','dV','cV','mV','µV','nV','pV','fV','aV','zV','yV');

CREATE TYPE UnitsFrequency AS ENUM ('YHz','ZHz','EHz','PHz','THz','GHz','MHz','kHz','hHz','daHz','Hz','dHz','cHz','mHz','µHz','nHz','pHz','fHz','aHz','zHz','yHz');

CREATE TYPE UnitsLength AS ENUM ('Ym','Zm','Em','Pm','Tm','Gm','Mm','km','hm','dam','m','dm','cm','mm','µm','nm','pm','fm','am','zm','ym','Å','ua','ly','pc','thou','li','in','ft','yd','mi','pt','pixel','reference frame');

CREATE TYPE UnitsPower AS ENUM ('YW','ZW','EW','PW','TW','GW','MW','kW','hW','daW','W','dW','cW','mW','µW','nW','pW','fW','aW','zW','yW');

CREATE TYPE UnitsPressure AS ENUM ('YPa','ZPa','EPa','PPa','TPa','GPa','MPa','kPa','hPa','daPa','Pa','dPa','cPa','mPa','µPa','nPa','pPa','fPa','aPa','zPa','yPa','bar','Mbar','kBar','dbar','cbar','mbar','atm','psi','Torr','mTorr','mm Hg');

CREATE TYPE UnitsTemperature AS ENUM ('K','°C','°F','°R');

ALTER TABLE detector
	ADD COLUMN voltageunit unitselectricpotential;

ALTER TABLE detectorsettings
	ADD COLUMN readoutrateunit unitsfrequency,
	ADD COLUMN voltageunit unitselectricpotential;

ALTER TABLE imagingenvironment
	ADD COLUMN airpressureunit unitspressure,
	ADD COLUMN temperatureunit unitstemperature;

ALTER TABLE laser
	ADD COLUMN repetitionrateunit unitsfrequency,
	ADD COLUMN wavelengthunit unitslength;

ALTER TABLE lightsettings
	ADD COLUMN wavelengthunit unitslength;

ALTER TABLE lightsource
	ADD COLUMN powerunit unitspower;

ALTER TABLE logicalchannel
	ADD COLUMN emissionwaveunit unitslength,
	ADD COLUMN excitationwaveunit unitslength,
	ADD COLUMN pinholesizeunit unitslength;

ALTER TABLE objective
	ADD COLUMN workingdistanceunit unitslength;

ALTER TABLE pixels
	ADD COLUMN physicalsizexunit unitslength,
	ADD COLUMN physicalsizeyunit unitslength,
	ADD COLUMN physicalsizezunit unitslength;

ALTER TABLE planeinfo
	ADD COLUMN positionxunit unitslength,
	ADD COLUMN positionyunit unitslength,
	ADD COLUMN positionzunit unitslength;

ALTER TABLE plate
	ADD COLUMN welloriginxunit unitslength,
	ADD COLUMN welloriginyunit unitslength;

ALTER TABLE shape
	ADD COLUMN fontsizeunit unitslength,
	ADD COLUMN strokewidthunit unitslength;

ALTER TABLE stagelabel
	ADD COLUMN positionxunit unitslength,
	ADD COLUMN positionyunit unitslength,
	ADD COLUMN positionzunit unitslength;

ALTER TABLE transmittancerange
	ADD COLUMN cutinunit unitslength,
	ADD COLUMN cutintoleranceunit unitslength,
	ADD COLUMN cutoutunit unitslength,
	ADD COLUMN cutouttoleranceunit unitslength;

ALTER TABLE wellsample
	ADD COLUMN posxunit unitslength,
	ADD COLUMN posyunit unitslength;

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

update pixels set timeincrementunit = 's'::unitstime where timeincrement is not null;

update planeinfo set deltatunit = 's'::unitstime where deltat is not null;
update planeinfo set exposuretimeunit = 's'::unitstime where exposuretime is not null;

update detector set voltageunit = 'V'::unitselectricpotential where  voltage is not null;

update detectorsettings set readoutrateunit = 'MHz'::unitsfrequency where readoutrate is not null;
update detectorsettings set voltageunit = 'V'::unitselectricpotential where voltage is not null;

update imagingenvironment set airpressureunit = 'mbar'::unitspressure where airpressure is not null;
update imagingenvironment set temperatureunit = '°C'::unitstemperature where temperature is not null;

update laser set repetitionrateunit = 'Hz'::unitsfrequency where repetitionrate is not null;
update laser set wavelengthunit = 'nm'::unitslength where wavelength is not null;

update lightsettings set wavelengthunit = 'nm'::unitslength where wavelength is not null;

update lightsource set powerunit = 'mW'::unitspower where power is not null;

update logicalchannel set emissionwaveunit = 'nm'::unitslength where emissionwave is not null;
update logicalchannel set excitationwaveunit = 'nm'::unitslength where excitationwave is not null;
update logicalchannel set pinholesizeunit = 'µm'::unitslength where pinholesize is not null;

update objective set workingdistanceunit = 'µm'::unitslength where workingdistance is not null;

update pixels set physicalsizexunit = 'µm'::unitslength where physicalsizex is not null;
update pixels set physicalsizeyunit = 'µm'::unitslength where physicalsizey is not null;
update pixels set physicalsizezunit = 'µm'::unitslength where physicalsizez is not null;

update planeinfo set positionxunit = 'reference frame'::unitslength where positionx is not null;
update planeinfo set positionyunit = 'reference frame'::unitslength where positiony is not null;
update planeinfo set positionzunit = 'reference frame'::unitslength where positionz is not null;

update plate set welloriginxunit = 'reference frame'::unitslength where welloriginx is not null;
update plate set welloriginyunit = 'reference frame'::unitslength where welloriginy is not null;

update shape set fontsizeunit = 'pt'::unitslength  where fontsize is not null;
update shape set strokewidthunit = 'pixel'::unitslength  where strokewidth is not null;

update stagelabel set positionxunit = 'reference frame'::unitslength where positionx is not null;
update stagelabel set positionyunit = 'reference frame'::unitslength where positiony is not null;
update stagelabel set positionzunit = 'reference frame'::unitslength where positionz is not null;

update transmittancerange set cutinunit = 'nm'::unitslength where cutin is not null;
update transmittancerange set cutintoleranceunit = 'nm'::unitslength where cutintolerance is not null;
update transmittancerange set cutoutunit = 'nm'::unitslength where cutout is not null;
update transmittancerange set cutouttoleranceunit = 'nm'::unitslength where cutouttolerance is not null;

update wellsample set posxunit = 'reference frame'::unitslength where posx is not null;
update wellsample set posyunit = 'reference frame'::unitslength where posy is not null;

-- reactivate not null constraints
alter table pixelstype alter column bitsize set not null;

-- fix column types that aren't enums
ALTER TABLE shape
        ALTER COLUMN fontsize TYPE double precision /* TYPE change - table: shape original: integer new: double precision */,
        ALTER COLUMN strokewidth TYPE double precision /* TYPE change - table: shape original: integer new: double precision */;

ALTER TABLE transmittancerange
        ALTER COLUMN cutin TYPE positive_float /* TYPE change - table: transmittancerange original: positive_int new: positive_float */,
        ALTER COLUMN cutout TYPE positive_float /* TYPE change - table: transmittancerange original: positive_int new: positive_float */;

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
