-- Copyright (C) 2012-4 Glencoe Software, Inc. All rights reserved.
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
--- OMERO5 readiness check for upgrade from OMERO5.2__0 to OMERO5.3DEV__13.
---

BEGIN;

--
-- check OMERO database version
--

CREATE OR REPLACE FUNCTION omero_assert_db_version(expected_version VARCHAR, expected_patch INTEGER) RETURNS void AS $$

DECLARE
    current_version VARCHAR;
    current_patch INTEGER;

BEGIN
    SELECT currentversion, currentpatch INTO STRICT current_version, current_patch
        FROM dbpatch ORDER BY id DESC LIMIT 1;

    IF current_version <> expected_version OR current_patch <> expected_patch THEN
        RAISE EXCEPTION 'wrong OMERO database version for this upgrade script';
    END IF;

END;$$ LANGUAGE plpgsql;

SELECT omero_assert_db_version('OMERO5.2', 0);
DROP FUNCTION omero_assert_db_version(varchar, int);


--
-- check PostgreSQL server version and database encoding
--

CREATE OR REPLACE FUNCTION db_pretty_version(version INTEGER) RETURNS TEXT AS $$

BEGIN
    RETURN (version/10000)::TEXT || '.' || ((version/100)%100)::TEXT || '.' || (version%100)::TEXT;

END;$$ LANGUAGE plpgsql;


CREATE FUNCTION assert_db_server_prerequisites(version_prereq INTEGER) RETURNS void AS $$

DECLARE
    version_num INTEGER;
    char_encoding TEXT;

BEGIN
    SELECT CAST(setting AS INTEGER) INTO STRICT version_num
        FROM pg_settings WHERE name = 'server_version_num';
    SELECT pg_encoding_to_char(encoding) INTO STRICT char_encoding
        FROM pg_database WHERE datname = current_database();

    IF version_num < version_prereq THEN
        RAISE EXCEPTION 'PostgreSQL database server version % is less than OMERO prerequisite %',
            db_pretty_version(version_num), db_pretty_version(version_prereq);
    END IF;

    IF char_encoding != 'UTF8' THEN
        RAISE EXCEPTION 'OMERO database character encoding must be UTF8, not %', char_encoding;
    ELSE
        SET client_encoding = 'UTF8';
    END IF;

END;$$ LANGUAGE plpgsql;

SELECT assert_db_server_prerequisites(90300);

DROP FUNCTION assert_db_server_prerequisites(INTEGER);
DROP FUNCTION db_pretty_version(INTEGER);


--
-- Actual upgrade check
--

-- ... for patch 0:

CREATE FUNCTION assert_no_roi_keywords_namespaces() RETURNS void AS $$

DECLARE
  roi_row roi%ROWTYPE;
  element TEXT;

BEGIN
    FOR roi_row IN SELECT * FROM roi LOOP
        IF roi_row.keywords IS NOT NULL THEN
            FOREACH element IN ARRAY roi_row.keywords LOOP
                IF element <> '' THEN
                    RAISE EXCEPTION 'data in roi.keywords row id=%', roi_row.id;
                END IF;
            END LOOP;
        END IF;
        IF roi_row.namespaces IS NOT NULL THEN
            FOREACH element IN ARRAY roi_row.namespaces LOOP
                IF element <> '' THEN
                    RAISE EXCEPTION 'data in roi.namespaces row id=%', roi_row.id;
                END IF;
            END LOOP;
        END IF;
    END LOOP;

END;$$ LANGUAGE plpgsql;

SELECT assert_no_roi_keywords_namespaces();
DROP FUNCTION assert_no_roi_keywords_namespaces();

-- ... for patch 6:

CREATE FUNCTION parse_transform(transform TEXT) RETURNS void AS $$

DECLARE
    number_text TEXT;
    number_texts TEXT[];

BEGIN
    IF transform IS NULL OR transform = '' OR transform = 'none' THEN
        RETURN;
    END IF;

    IF left(transform, 2) = '[ ' AND right(transform, 2) = ' ]' THEN
        transform := 'matrix(' || left(right(transform, -2), -2) || ')';
    ELSIF left(transform, 1) = '[' AND right(transform, 1) = ']' THEN
        transform := 'matrix(' || left(right(transform, -1), -1) || ')';
    END IF;

    IF left(transform, 7) = 'matrix(' AND right(transform, 1) = ')' THEN

        number_texts := string_to_array(left(right(transform, -7), -1), ' ');

        IF array_length(number_texts, 1) != 6 THEN
            RAISE EXCEPTION 'must have six numbers in shape.transform value: %', transform;
        END IF;

        FOREACH number_text IN ARRAY number_texts LOOP
            PERFORM CAST(number_text AS FLOAT);
        END LOOP;

    ELSIF left(transform, 10) = 'translate(' AND right(transform, 1) = ')' THEN

        number_texts := string_to_array(left(right(transform, -10), -1), ' ');

        IF array_length(number_texts, 1) = 1 THEN
            number_texts := array_append(number_texts, '0');
        ELSIF array_length(number_texts, 1) != 2 THEN
            RAISE EXCEPTION 'must have one or two numbers in shape.transform value: %', transform;
        END IF;

        FOREACH number_text IN ARRAY number_texts LOOP
            PERFORM CAST(number_text AS FLOAT);
        END LOOP;

    ELSIF left(transform, 6) = 'scale(' AND right(transform, 1) = ')' THEN

        number_texts := string_to_array(left(right(transform, -6), -1), ' ');

        IF array_length(number_texts, 1) = 1 THEN
            number_texts[2] := number_texts[1];
        ELSIF array_length(number_texts, 1) != 2 THEN
            RAISE EXCEPTION 'must have one or two numbers in shape.transform value: %', transform;
        END IF;

        FOREACH number_text IN ARRAY number_texts LOOP
            PERFORM CAST(number_text AS FLOAT);
        END LOOP;

    ELSIF left(transform, 7) = 'rotate(' AND right(transform, 1) = ')' THEN

        number_texts := string_to_array(left(right(transform, -7), -1), ' ');

        IF array_length(number_texts, 1) = 1 THEN
            number_texts := number_texts || ARRAY ['0', '0'];
        ELSIF array_length(number_texts, 1) != 3 THEN
            RAISE EXCEPTION 'must have one or three numbers in shape.transform value: %', transform;
        END IF;

        FOREACH number_text IN ARRAY number_texts LOOP
            PERFORM CAST(number_text AS FLOAT);
        END LOOP;

    ELSIF left(transform, 6) = 'skewX(' AND right(transform, 1) = ')' THEN

        number_texts := string_to_array(left(right(transform, -6), -1), ' ');

        IF array_length(number_texts, 1) != 1 THEN
            RAISE EXCEPTION 'must have one number in shape.transform value: %', transform;
        END IF;

        PERFORM CAST(number_texts[1] AS FLOAT);

    ELSIF left(transform, 6) = 'skewY(' AND right(transform, 1) = ')' THEN

        number_texts := string_to_array(left(right(transform, -6), -1), ' ');

        IF array_length(number_texts, 1) != 1 THEN
            RAISE EXCEPTION 'must have one number in shape.transform value: %', transform;
        END IF;

        PERFORM CAST(number_texts[1] AS FLOAT);

    ELSE
        RAISE EXCEPTION 'cannot parse shape.transform value: %', transform;

    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION parse_transforms() RETURNS void AS $$

DECLARE
  svg_transform TEXT;

BEGIN
    FOR svg_transform IN SELECT DISTINCT transform FROM shape WHERE transform IS NOT NULL LOOP
        PERFORM parse_transform(svg_transform);
    END LOOP;

END;$$ LANGUAGE plpgsql;

SELECT parse_transforms();
DROP FUNCTION parse_transforms();
DROP FUNCTION parse_transform(TEXT);


--
-- FINISHED
--

SELECT CHR(10)||CHR(10)||CHR(10)||'YOUR DATABASE IS READY FOR UPGRADE TO VERSION OMERO5.3DEV__13'||CHR(10)||CHR(10)||CHR(10) AS Status;

ROLLBACK;
