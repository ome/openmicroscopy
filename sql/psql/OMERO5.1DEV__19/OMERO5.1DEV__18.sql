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
--- OMERO5 development release upgrade from OMERO5.1DEV__18 to OMERO5.1DEV__19.
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

SELECT omero_assert_db_version('OMERO5.1DEV', 18);
DROP FUNCTION omero_assert_db_version(varchar, int);

INSERT INTO dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
             VALUES ('OMERO5.1DEV',  19,                'OMERO5.1DEV',    18);

--
-- Actual upgrade
--

-- Replace globals' annotation count tables with views.

DROP TABLE count_experimenter_annotationlinks_by_owner;
DROP TABLE count_experimentergroup_annotationlinks_by_owner;
DROP TABLE count_node_annotationlinks_by_owner;
DROP TABLE count_session_annotationlinks_by_owner;

CREATE VIEW count_experimenter_annotationlinks_by_owner (experimenter_id, owner_id, count) AS
    SELECT parent, owner_id, count(*)
        FROM experimenterannotationlink
        GROUP BY parent, owner_id
        ORDER BY parent;

CREATE VIEW count_experimentergroup_annotationlinks_by_owner (experimentergroup_id, owner_id, count) AS
    SELECT parent, owner_id, count(*)
        FROM experimentergroupannotationlink
        GROUP BY parent, owner_id
        ORDER BY parent;

CREATE VIEW count_node_annotationlinks_by_owner (node_id, owner_id, count) AS
    SELECT parent, owner_id, count(*)
        FROM nodeannotationlink
        GROUP BY parent, owner_id
        ORDER BY parent;

CREATE VIEW count_session_annotationlinks_by_owner (session_id, owner_id, count) AS
    SELECT parent, owner_id, count(*)
        FROM sessionannotationlink
        GROUP BY parent, owner_id
        ORDER BY parent;

-- Namespace is now a global.

ALTER TABLE namespace DROP COLUMN creation_id;
ALTER TABLE namespace DROP COLUMN group_id;
ALTER TABLE namespace DROP COLUMN owner_id;
ALTER TABLE namespace DROP COLUMN update_id;

-- More objects are named.

ALTER TABLE annotation ADD COLUMN name VARCHAR(255);
ALTER TABLE namespace ADD COLUMN displayname VARCHAR(255);
ALTER TABLE roi ADD COLUMN name VARCHAR(255);

CREATE INDEX annotation_name ON annotation(name);
CREATE INDEX namespace_displayname ON namespace(displayname);
CREATE INDEX roi_name ON roi(name);

-- The namespace table now reflects all the namespaces used in the annotation.ns column.

CREATE FUNCTION add_to_namespace() RETURNS "trigger" AS $$
    BEGIN
        IF NOT (NEW.ns IS NULL OR EXISTS (SELECT 1 FROM namespace WHERE name = NEW.ns LIMIT 1)) THEN
            INSERT INTO namespace (id, name, permissions)
                SELECT ome_nextval('seq_namespace'), NEW.ns, -52;
        END IF;

        RETURN NULL;
    END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION update_namespace() RETURNS "trigger" AS $$
    BEGIN
        IF OLD.name <> NEW.name AND EXISTS (SELECT 1 FROM annotation WHERE ns = OLD.name LIMIT 1) THEN
            RAISE EXCEPTION 'cannot rename namespace that is still used by annotation';
        END IF;

        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION delete_from_namespace() RETURNS "trigger" AS $$
    BEGIN
        IF EXISTS (SELECT 1 FROM annotation WHERE ns = OLD.name LIMIT 1) THEN
            RAISE EXCEPTION 'cannot delete namespace that is still used by annotation';
        END IF;

        RETURN OLD;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER add_to_namespace
    AFTER INSERT OR UPDATE ON annotation
    FOR EACH ROW EXECUTE PROCEDURE add_to_namespace();

CREATE TRIGGER update_namespace
    BEFORE UPDATE ON namespace
    FOR EACH ROW EXECUTE PROCEDURE update_namespace();

CREATE TRIGGER delete_from_namespace
    BEFORE DELETE ON namespace
    FOR EACH ROW EXECUTE PROCEDURE delete_from_namespace();

INSERT INTO namespace (id, name, permissions)
    SELECT ome_nextval('seq_namespace'), ns, -52
        FROM annotation WHERE id IN
             (SELECT id_row.id 
                  FROM (SELECT id, row_number() OVER (PARTITION BY ns) AS row_n FROM annotation
                            WHERE ns IS NOT NULL AND ns NOT IN (SELECT name FROM namespace)) AS id_row
                  WHERE id_row.row_n = 1);

-- A property value is null if and only if the corresponding unit is null.

ALTER TABLE detector ADD CONSTRAINT voltage_unitpair
    CHECK (voltage IS NULL AND voltageunit IS NULL
        OR voltage IS NOT NULL AND voltageunit IS NOT NULL);

ALTER TABLE detectorsettings ADD CONSTRAINT readoutrate_unitpair
    CHECK (readoutrate IS NULL AND readoutrateunit IS NULL
        OR readoutrate IS NOT NULL AND readoutrateunit IS NOT NULL);

ALTER TABLE detectorsettings ADD CONSTRAINT voltage_unitpair
    CHECK (voltage IS NULL AND voltageunit IS NULL
        OR voltage IS NOT NULL AND voltageunit IS NOT NULL);

ALTER TABLE imagingenvironment ADD CONSTRAINT airpressure_unitpair
    CHECK (airpressure IS NULL AND airpressureunit IS NULL
        OR airpressure IS NOT NULL AND airpressureunit IS NOT NULL);

ALTER TABLE imagingenvironment ADD CONSTRAINT temperature_unitpair
    CHECK (temperature IS NULL AND temperatureunit IS NULL
        OR temperature IS NOT NULL AND temperatureunit IS NOT NULL);

ALTER TABLE laser ADD CONSTRAINT repetitionrate_unitpair
    CHECK (repetitionrate IS NULL AND repetitionrateunit IS NULL
        OR repetitionrate IS NOT NULL AND repetitionrateunit IS NOT NULL);

ALTER TABLE laser ADD CONSTRAINT wavelength_unitpair
    CHECK (wavelength IS NULL AND wavelengthunit IS NULL
        OR wavelength IS NOT NULL AND wavelengthunit IS NOT NULL);

ALTER TABLE lightsettings ADD CONSTRAINT wavelength_unitpair
    CHECK (wavelength IS NULL AND wavelengthunit IS NULL
        OR wavelength IS NOT NULL AND wavelengthunit IS NOT NULL);

ALTER TABLE lightsource ADD CONSTRAINT power_unitpair
    CHECK (power IS NULL AND powerunit IS NULL
        OR power IS NOT NULL AND powerunit IS NOT NULL);

ALTER TABLE logicalchannel ADD CONSTRAINT emissionwave_unitpair
    CHECK (emissionwave IS NULL AND emissionwaveunit IS NULL
        OR emissionwave IS NOT NULL AND emissionwaveunit IS NOT NULL);

ALTER TABLE logicalchannel ADD CONSTRAINT excitationwave_unitpair
    CHECK (excitationwave IS NULL AND excitationwaveunit IS NULL
        OR excitationwave IS NOT NULL AND excitationwaveunit IS NOT NULL);

ALTER TABLE logicalchannel ADD CONSTRAINT pinholesize_unitpair
    CHECK (pinholesize IS NULL AND pinholesizeunit IS NULL
        OR pinholesize IS NOT NULL AND pinholesizeunit IS NOT NULL);

ALTER TABLE objective ADD CONSTRAINT workingdistance_unitpair
    CHECK (workingdistance IS NULL AND workingdistanceunit IS NULL
        OR workingdistance IS NOT NULL AND workingdistanceunit IS NOT NULL);

ALTER TABLE pixels ADD CONSTRAINT physicalsizex_unitpair
    CHECK (physicalsizex IS NULL AND physicalsizexunit IS NULL
        OR physicalsizex IS NOT NULL AND physicalsizexunit IS NOT NULL);

ALTER TABLE pixels ADD CONSTRAINT physicalsizey_unitpair
    CHECK (physicalsizey IS NULL AND physicalsizeyunit IS NULL
        OR physicalsizey IS NOT NULL AND physicalsizeyunit IS NOT NULL);

ALTER TABLE pixels ADD CONSTRAINT physicalsizez_unitpair
    CHECK (physicalsizez IS NULL AND physicalsizezunit IS NULL
        OR physicalsizez IS NOT NULL AND physicalsizezunit IS NOT NULL);

ALTER TABLE pixels ADD CONSTRAINT timeincrement_unitpair
    CHECK (timeincrement IS NULL AND timeincrementunit IS NULL
        OR timeincrement IS NOT NULL AND timeincrementunit IS NOT NULL);

ALTER TABLE planeinfo ADD CONSTRAINT deltat_unitpair
    CHECK (deltat IS NULL AND deltatunit IS NULL
        OR deltat IS NOT NULL AND deltatunit IS NOT NULL);

ALTER TABLE planeinfo ADD CONSTRAINT exposuretime_unitpair
    CHECK (exposuretime IS NULL AND exposuretimeunit IS NULL
        OR exposuretime IS NOT NULL AND exposuretimeunit IS NOT NULL);

ALTER TABLE planeinfo ADD CONSTRAINT positionx_unitpair
    CHECK (positionx IS NULL AND positionxunit IS NULL
        OR positionx IS NOT NULL AND positionxunit IS NOT NULL);

ALTER TABLE planeinfo ADD CONSTRAINT positiony_unitpair
    CHECK (positiony IS NULL AND positionyunit IS NULL
        OR positiony IS NOT NULL AND positionyunit IS NOT NULL);

ALTER TABLE planeinfo ADD CONSTRAINT positionz_unitpair
    CHECK (positionz IS NULL AND positionzunit IS NULL
        OR positionz IS NOT NULL AND positionzunit IS NOT NULL);

ALTER TABLE plate ADD CONSTRAINT welloriginx_unitpair
    CHECK (welloriginx IS NULL AND welloriginxunit IS NULL
        OR welloriginx IS NOT NULL AND welloriginxunit IS NOT NULL);

ALTER TABLE plate ADD CONSTRAINT welloriginy_unitpair
    CHECK (welloriginy IS NULL AND welloriginyunit IS NULL
        OR welloriginy IS NOT NULL AND welloriginyunit IS NOT NULL);

ALTER TABLE shape ADD CONSTRAINT fontsize_unitpair
    CHECK (fontsize IS NULL AND fontsizeunit IS NULL
        OR fontsize IS NOT NULL AND fontsizeunit IS NOT NULL);

ALTER TABLE shape ADD CONSTRAINT strokewidth_unitpair
    CHECK (strokewidth IS NULL AND strokewidthunit IS NULL
        OR strokewidth IS NOT NULL AND strokewidthunit IS NOT NULL);

ALTER TABLE stagelabel ADD CONSTRAINT positionx_unitpair
    CHECK (positionx IS NULL AND positionxunit IS NULL
        OR positionx IS NOT NULL AND positionxunit IS NOT NULL);

ALTER TABLE stagelabel ADD CONSTRAINT positiony_unitpair
    CHECK (positiony IS NULL AND positionyunit IS NULL
        OR positiony IS NOT NULL AND positionyunit IS NOT NULL);

ALTER TABLE stagelabel ADD CONSTRAINT positionz_unitpair
    CHECK (positionz IS NULL AND positionzunit IS NULL
        OR positionz IS NOT NULL AND positionzunit IS NOT NULL);

ALTER TABLE transmittancerange ADD CONSTRAINT cutintolerance_unitpair
    CHECK (cutintolerance IS NULL AND cutintoleranceunit IS NULL
        OR cutintolerance IS NOT NULL AND cutintoleranceunit IS NOT NULL);

ALTER TABLE transmittancerange ADD CONSTRAINT cutin_unitpair
    CHECK (cutin IS NULL AND cutinunit IS NULL
        OR cutin IS NOT NULL AND cutinunit IS NOT NULL);

ALTER TABLE transmittancerange ADD CONSTRAINT cutouttolerance_unitpair
    CHECK (cutouttolerance IS NULL AND cutouttoleranceunit IS NULL
        OR cutouttolerance IS NOT NULL AND cutouttoleranceunit IS NOT NULL);

ALTER TABLE transmittancerange ADD CONSTRAINT cutout_unitpair
    CHECK (cutout IS NULL AND cutoutunit IS NULL
        OR cutout IS NOT NULL AND cutoutunit IS NOT NULL);

ALTER TABLE wellsample ADD CONSTRAINT posx_unitpair
    CHECK (posx IS NULL AND posxunit IS NULL
        OR posx IS NOT NULL AND posxunit IS NOT NULL);

ALTER TABLE wellsample ADD CONSTRAINT posy_unitpair
    CHECK (posy IS NULL AND posyunit IS NULL
        OR posy IS NOT NULL AND posyunit IS NOT NULL);


--
-- FINISHED
--

UPDATE dbpatch SET message = 'Database updated.', finished = clock_timestamp()
    WHERE currentVersion  = 'OMERO5.1DEV' AND
          currentPatch    = 19            AND
          previousVersion = 'OMERO5.1DEV' AND
          previousPatch   = 18;

SELECT CHR(10)||CHR(10)||CHR(10)||'YOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION OMERO5.1DEV__19'||CHR(10)||CHR(10)||CHR(10) AS Status;

COMMIT;
