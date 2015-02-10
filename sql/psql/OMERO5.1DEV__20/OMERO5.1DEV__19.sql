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
--- OMERO5 development release upgrade from OMERO5.1DEV__19 to OMERO5.1DEV__20.
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

SELECT omero_assert_db_version('OMERO5.1DEV', 19);
DROP FUNCTION omero_assert_db_version(varchar, int);

INSERT INTO dbpatch (currentVersion, currentPatch,   previousVersion,     previousPatch)
             VALUES ('OMERO5.1DEV',  20,                'OMERO5.1DEV',    19);

--
-- Actual upgrade
--

-- _fs_protected_mimetype replaces _fs_directory_mimetype

CREATE FUNCTION _fs_protected_mimetype() RETURNS "trigger" AS $$
    BEGIN
        IF OLD.mimetype IN ('Directory', 'Repository') AND (NEW.mimetype IS NULL OR NEW.mimetype != OLD.mimetype) THEN
            RAISE EXCEPTION 'cannot change media type % of file id=%', OLD.mimetype, OLD.id;
        END IF;
        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER _fs_protected_mimetype
    BEFORE UPDATE ON originalfile
    FOR EACH ROW EXECUTE PROCEDURE _fs_protected_mimetype();

DROP TRIGGER _fs_directory_mimetype ON originalfile;
DROP FUNCTION _fs_directory_mimetype();

-- _fs_log_delete() no longer copies params column

create or replace function _fs_log_delete() returns trigger AS $_fs_log_delete$
    begin
        if OLD.repo is not null then
            INSERT INTO _fs_deletelog (event_id, file_id, owner_id, group_id, "path", "name", repo)
                SELECT _current_or_new_event(), OLD.id, OLD.owner_id, OLD.group_id, OLD."path", OLD."name", OLD.repo;
        end if;
        return OLD;
    END;
$_fs_log_delete$ LANGUAGE plpgsql;

-- add new image.series column and populate from pixels.params

ALTER TABLE image ADD COLUMN series nonnegative_int NOT NULL DEFAULT 0;

CREATE FUNCTION check_params_columns() RETURNS void AS $$

DECLARE
  pixels_id BIGINT;
  image_id  BIGINT;
  path      TEXT;
  name      TEXT;
  params    TEXT[];
  image_no  INTEGER;
  target    TEXT;
  index     INTEGER;

BEGIN
  IF EXISTS (SELECT 1 FROM originalfile AS o WHERE o.params IS NOT NULL) THEN
    RAISE EXCEPTION 'data in originalfile.params which is to be dropped';
  END IF;

  FOR image_id, pixels_id, path, name, params IN
    SELECT p.image, p.id, p.path, p.name, p.params FROM pixels AS p
      WHERE p.params IS NOT NULL LOOP

    image_no := NULL;
    target   := NULL;

    FOR index IN array_lower(params, 1) .. array_upper(params, 1) LOOP
      CASE params[index][1]
        WHEN 'image_no' THEN
            SELECT CAST(params[index][2] AS INTEGER) INTO STRICT image_no;
        WHEN 'target' THEN
            SELECT params[index][2] INTO STRICT target;
        ELSE
            RAISE EXCEPTION 'for pixels id=%, unexpected params key: %', pixels_id, params[index][1];
      END CASE;
    END LOOP;

    -- The pixels.path column may have Windows-style separators.
    IF position('/' IN path) = 0 THEN
        path := translate(path, '\', '/');
    END IF;

    IF target IS NOT NULL AND target <> (path || '/' || name) THEN
      RAISE EXCEPTION 'for pixels id=%, params target does not match path and name', pixels_id;
    END IF;

    IF image_no > 0 THEN
      UPDATE image SET series = image_no WHERE id = image_id;
    END IF;
  END LOOP;

END;

$$ LANGUAGE plpgsql;

SELECT check_params_columns();
DROP FUNCTION check_params_columns();

-- drop params columns

ALTER TABLE originalfile DROP COLUMN params;
ALTER TABLE pixels DROP COLUMN params;

-- allow ORM to attempt to leave series as null

CREATE FUNCTION image_series_default_zero() RETURNS "trigger" AS $$
    BEGIN
        IF NEW.series IS NULL THEN
            NEW.series := 0;
        END IF;

        RETURN NEW;
    END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER image_series_default_zero
    BEFORE INSERT OR UPDATE ON image
    FOR EACH ROW EXECUTE PROCEDURE image_series_default_zero();

--
-- FINISHED
--

UPDATE dbpatch SET message = 'Database updated.', finished = clock_timestamp()
    WHERE currentVersion  = 'OMERO5.1DEV' AND
          currentPatch    = 20            AND
          previousVersion = 'OMERO5.1DEV' AND
          previousPatch   = 19;

SELECT CHR(10)||CHR(10)||CHR(10)||'YOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION OMERO5.1DEV__20'||CHR(10)||CHR(10)||CHR(10) AS Status;

COMMIT;
