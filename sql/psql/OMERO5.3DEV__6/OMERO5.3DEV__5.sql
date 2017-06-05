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
--- OMERO5 development release upgrade from OMERO5.3DEV__5 to OMERO5.3DEV__6.
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

SELECT omero_assert_db_version('OMERO5.3DEV', 5);
DROP FUNCTION omero_assert_db_version(varchar, int);


--
-- Actual upgrade
--

INSERT INTO dbpatch (currentVersion, currentPatch, previousVersion, previousPatch)
             VALUES ('OMERO5.3DEV',  6,            'OMERO5.3DEV',   5);

CREATE FUNCTION combine_ctm(new_transform FLOAT[], ctm FLOAT[]) RETURNS FLOAT[] AS $$

BEGIN
    RETURN ARRAY [ctm[1] * new_transform[1] + ctm[3] * new_transform[2],
                  ctm[2] * new_transform[1] + ctm[4] * new_transform[2],
                  ctm[1] * new_transform[3] + ctm[3] * new_transform[4],
                  ctm[2] * new_transform[3] + ctm[4] * new_transform[4],
                  ctm[1] * new_transform[5] + ctm[3] * new_transform[6] + ctm[5],
                  ctm[2] * new_transform[5] + ctm[4] * new_transform[6] + ctm[6]];

END;
$$ LANGUAGE plpgsql;

CREATE FUNCTION parse_transform(transform TEXT) RETURNS FLOAT[] AS $$

DECLARE
    number_text TEXT;
    number_texts TEXT[];
    transform_matrix FLOAT[];
    identity_matrix CONSTANT FLOAT[] := ARRAY [1, 0, 0, 1, 0, 0];
    angle FLOAT;

BEGIN
    IF transform IS NULL OR transform = '' OR transform = 'none' THEN
        RETURN NULL;
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
            transform_matrix := array_append(transform_matrix, CAST(number_text AS FLOAT));
        END LOOP;

    ELSIF left(transform, 10) = 'translate(' AND right(transform, 1) = ')' THEN

        number_texts := string_to_array(left(right(transform, -10), -1), ' ');

        IF array_length(number_texts, 1) = 1 THEN
            number_texts := array_append(number_texts, '0');
        ELSIF array_length(number_texts, 1) != 2 THEN
            RAISE EXCEPTION 'must have one or two numbers in shape.transform value: %', transform;
        END IF;

        transform_matrix := ARRAY [1, 0, 0, 1];

        FOREACH number_text IN ARRAY number_texts LOOP
            transform_matrix := array_append(transform_matrix, CAST(number_text AS FLOAT));
        END LOOP;

    ELSIF left(transform, 6) = 'scale(' AND right(transform, 1) = ')' THEN

        number_texts := string_to_array(left(right(transform, -6), -1), ' ');

        IF array_length(number_texts, 1) = 1 THEN
            number_texts[2] := number_texts[1];
        ELSIF array_length(number_texts, 1) != 2 THEN
            RAISE EXCEPTION 'must have one or two numbers in shape.transform value: %', transform;
        END IF;

        FOREACH number_text IN ARRAY number_texts LOOP
            transform_matrix := array_append(transform_matrix, CAST(number_text AS FLOAT)) || CAST(ARRAY [0, 0] AS FLOAT[]);
        END LOOP;

    ELSIF left(transform, 7) = 'rotate(' AND right(transform, 1) = ')' THEN

        number_texts := string_to_array(left(right(transform, -7), -1), ' ');

        IF array_length(number_texts, 1) = 1 THEN
            number_texts := number_texts || ARRAY ['0', '0'];
        ELSIF array_length(number_texts, 1) != 3 THEN
            RAISE EXCEPTION 'must have one or three numbers in shape.transform value: %', transform;
        END IF;

        FOREACH number_text IN ARRAY number_texts LOOP
            transform_matrix := array_append(transform_matrix, CAST(number_text AS FLOAT));
        END LOOP;

        angle := transform_matrix[1] * pi() / 180;

        IF transform_matrix[2] = 0 AND transform_matrix[3] = 0 THEN
            transform_matrix := ARRAY [cos(angle), sin(angle), -sin(angle), cos(angle), 0, 0];
        ELSE
            transform_matrix := combine_ctm(ARRAY [1, 0, 0, 1, -transform_matrix[2], -transform_matrix[3]],
                      combine_ctm(ARRAY [cos(angle), sin(angle), -sin(angle), cos(angle), 0, 0],
                      combine_ctm(ARRAY [1, 0, 0, 1, transform_matrix[2], transform_matrix[3]],
                                  identity_matrix)));
        END IF;

    ELSIF left(transform, 6) = 'skewX(' AND right(transform, 1) = ')' THEN

        number_texts := string_to_array(left(right(transform, -6), -1), ' ');

        IF array_length(number_texts, 1) != 1 THEN
            RAISE EXCEPTION 'must have one number in shape.transform value: %', transform;
        END IF;

        angle := CAST(number_texts[1] AS FLOAT) * pi() / 180;
        transform_matrix := ARRAY [1, 0, tan(angle), 1, 0, 0];

    ELSIF left(transform, 6) = 'skewY(' AND right(transform, 1) = ')' THEN

        number_texts := string_to_array(left(right(transform, -6), -1), ' ');

        IF array_length(number_texts, 1) != 1 THEN
            RAISE EXCEPTION 'must have one number in shape.transform value: %', transform;
        END IF;

        angle := CAST(number_texts[1] AS FLOAT) * pi() / 180;
        transform_matrix := ARRAY [1, tan(angle), 0, 1, 0, 0];

    ELSE
        RAISE EXCEPTION 'cannot parse shape.transform value: %', transform;

    END IF;

    IF transform_matrix = identity_matrix THEN
        RETURN NULL;
    ELSE
        RETURN transform_matrix;
    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE TEMPORARY TABLE transform_tests (
    svg_transform  TEXT NOT NULL,
    ctm            FLOAT[],
    from_x         FLOAT NOT NULL,
    from_y         FLOAT NOT NULL,
    expected_x     FLOAT NOT NULL,
    expected_y     FLOAT NOT NULL,
    actual_x       FLOAT,
    actual_y       FLOAT,
    x_diff         FLOAT,
    y_diff         FLOAT,
    passes         BOOLEAN
);

CREATE FUNCTION transform_tester() RETURNS TRIGGER AS $$

BEGIN
    NEW.ctm := parse_transform(NEW.svg_transform);
    IF NEW.ctm IS NULL THEN
        NEW.actual_x = NEW.from_x;
        NEW.actual_y = NEW.from_y;
    ELSE
        NEW.actual_x = NEW.ctm[1] * NEW.from_x + NEW.ctm[3] * NEW.from_y + NEW.ctm[5];
        NEW.actual_y = NEW.ctm[2] * NEW.from_x + NEW.ctm[4] * NEW.from_y + NEW.ctm[6];
    END IF;
    NEW.x_diff = abs(NEW.actual_x - NEW.expected_x);
    NEW.y_diff = abs(NEW.actual_y - NEW.expected_y);
    NEW.passes = NEW.x_diff < 1e-10 AND NEW.y_diff < 1e-10;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER transform_tester
    BEFORE INSERT OR UPDATE ON transform_tests
    FOR EACH ROW
    EXECUTE PROCEDURE transform_tester();

INSERT INTO transform_tests (svg_transform, from_x, from_y, expected_x, expected_y) VALUES
    ('none', 30, 40, 30, 40),
    ('[2 -3 -5 7 110 -130]', 170, 190, 2*170 - 5*190 + 110, 7*190 - 3*170 - 130),
    ('[ 2 -3 -5 7 110 -130 ]', 170, 190, 2*170 - 5*190 + 110, 7*190 - 3*170 - 130),
    ('matrix(2 -3 -5 7 110 -130)', 170, 190, 2*170 - 5*190 + 110, 7*190 - 3*170 - 130),
    ('translate(0)', 30, 40, 30, 40),
    ('translate(10)', 30, 40, 40, 40),
    ('translate(-10)', 30, 40, 20, 40),
    ('translate(0 0)', 30, 40, 30, 40),
    ('translate(10 0)', 30, 40, 40, 40),
    ('translate(0 20)', 30, 40, 30, 60),
    ('translate(10 20)', 30, 40, 40, 60),
    ('translate(10 -20)', 30, 40, 40, 20),
    ('translate(-10 20)', 30, 40, 20, 60),
    ('translate(-10 -20)', 30, 40, 20, 20),
    ('scale(1)', 40, 50, 40, 50),
    ('scale(3)', 40, 50, 120, 150),
    ('scale(-3)', 40, 50, -120, -150),
    ('scale(1 1)', 40, 50, 40, 50),
    ('scale(3 1)', 40, 50, 120, 50),
    ('scale(1 3)', 40, 50, 40, 150),
    ('scale(3 3)', 40, 50, 120, 150),
    ('scale(3 -3)', 40, 50, 120, -150),
    ('scale(-3 3)', 40, 50, -120, 150),
    ('scale(-3 -3)', 40, 50, -120, -150),
    ('rotate(0)', 50, 0, 50, 0),
    ('rotate(45)', 50, 0, 50/sqrt(2), 50/sqrt(2)),
    ('rotate(90)', 50, 0, 0, 50),
    ('rotate(135)', 50, 0, -50/sqrt(2), 50/sqrt(2)),
    ('rotate(180)', 50, 0, -50, 0),
    ('rotate(-0)', 50, 0, 50, 0),
    ('rotate(-45)', 50, 0, 50/sqrt(2), -50/sqrt(2)),
    ('rotate(-90)', 50, 0, 0, -50),
    ('rotate(-135)', 50, 0, -50/sqrt(2), -50/sqrt(2)),
    ('rotate(-180)', 50, 0, -50, 0),
    ('rotate(0 10 50)', 30, 50, 30, 50),
    ('rotate(45 10 50)', 30, 50, 10 + 20/sqrt(2), 50 + 20/sqrt(2)),
    ('rotate(90 10 50)', 30, 50, 10, 70),
    ('rotate(135 10 50)', 30, 50, 10 - 20/sqrt(2), 50 + 20/sqrt(2)),
    ('rotate(180 10 50)', 30, 50, -10, 50),
    ('rotate(-0 10 50)', 30, 50, 30, 50),
    ('rotate(-45 10 50)', 30, 50, 10 + 20/sqrt(2), 50 - 20/sqrt(2)),
    ('rotate(-90 10 50)', 30, 50, 10, 30),
    ('rotate(-135 10 50)', 30, 50, 10 - 20/sqrt(2), 50 - 20/sqrt(2)),
    ('rotate(-180 10 50)', 30, 50, -10, 50),
    ('skewX(0)', 40, 50, 40, 50),
    ('skewX(30)', 40, 50, 40 + 50/sqrt(3), 50),
    ('skewX(-30)', 40, 50, 40 - 50/sqrt(3), 50),
    ('skewX(30)', -40, 50, -40 + 50/sqrt(3), 50),
    ('skewX(-30)', -40, 50, -40 - 50/sqrt(3), 50),
    ('skewX(30)', 40, -50, 40 - 50/sqrt(3), -50),
    ('skewX(-30)', 40, -50, 40 + 50/sqrt(3), -50),
    ('skewY(0)', 40, 50, 40, 50),
    ('skewY(30)', 40, 50, 40, 50 + 40/sqrt(3)),
    ('skewY(-30)', 40, 50, 40, 50 - 40/sqrt(3)),
    ('skewY(30)', -40, 50, -40, 50 - 40/sqrt(3)),
    ('skewY(-30)', -40, 50, -40, 50 + 40/sqrt(3)),
    ('skewY(30)', 40, -50, 40, -50 + 40/sqrt(3)),
    ('skewY(-30)', 40, -50, 40, -50 - 40/sqrt(3)),
    ('scale(2.0)', 40, 50, 80, 100),
    ('scale(0.5)', 40, 50, 20, 25),
    ('scale(2E0)', 40, 50, 80, 100),
    ('scale(5E-1)', 40, 50, 20, 25),
    ('scale(2.0E0)', 40, 50, 80, 100),
    ('scale(5.0E-1)', 40, 50, 20, 25),
    ('scale(-2.0)', 40, 50, -80, -100),
    ('scale(-0.5)', 40, 50, -20, -25),
    ('scale(-2E0)', 40, 50, -80, -100),
    ('scale(-5E-1)', 40, 50, -20, -25),
    ('scale(-2.0E0)', 40, 50, -80, -100),
    ('scale(-5.0E-1)', 40, 50, -20, -25);

SELECT passes, COUNT(svg_transform) FROM transform_tests GROUP BY passes;

DROP TABLE transform_tests;
DROP FUNCTION transform_tester();

CREATE TABLE affinetransform (
    id BIGINT PRIMARY KEY,
    a00 DOUBLE PRECISION NOT NULL,
    a10 DOUBLE PRECISION NOT NULL,
    a01 DOUBLE PRECISION NOT NULL,
    a11 DOUBLE PRECISION NOT NULL,
    a02 DOUBLE PRECISION NOT NULL,
    a12 DOUBLE PRECISION NOT NULL,
    permissions BIGINT NOT NULL,
    version INTEGER,
    external_id BIGINT UNIQUE,
    group_id BIGINT NOT NULL,
    owner_id BIGINT NOT NULL,
    creation_id BIGINT NOT NULL,
    update_id BIGINT NOT NULL);

CREATE SEQUENCE seq_affinetransform; INSERT INTO _lock_ids (name, id)
    SELECT 'seq_affinetransform', nextval('_lock_seq');

ALTER TABLE affinetransform ADD CONSTRAINT FKaffinetransform_creation_id_event
    FOREIGN KEY (creation_id) REFERENCES event;
ALTER TABLE affinetransform ADD CONSTRAINT FKaffinetransform_update_id_event
    FOREIGN KEY (update_id) REFERENCES event;
ALTER TABLE affinetransform ADD CONSTRAINT FKaffinetransform_external_id_externalinfo
    FOREIGN KEY (external_id) REFERENCES externalinfo;
ALTER TABLE affinetransform ADD CONSTRAINT FKaffinetransform_group_id_experimentergroup
    FOREIGN KEY (group_id) REFERENCES experimentergroup;
ALTER TABLE affinetransform ADD CONSTRAINT FKaffinetransform_owner_id_experimenter
    FOREIGN KEY (owner_id) REFERENCES experimenter;

CREATE FUNCTION upgrade_transform(
    svg_transform VARCHAR(255), permissions BIGINT, owner_id BIGINT, group_id BIGINT)
    RETURNS BIGINT AS $$

DECLARE
    matrix FLOAT[];
    transform_id BIGINT;
    event_id BIGINT;

BEGIN
    matrix := parse_transform(svg_transform);

    IF matrix IS NULL THEN
        RETURN NULL;
    END IF;

    SELECT ome_nextval('seq_affinetransform') INTO STRICT transform_id;
    SELECT _current_or_new_event() INTO STRICT event_id;

    INSERT INTO affinetransform (id, a00, a10, a01, a11, a02, a12,
                                 permissions, owner_id, group_id, creation_id, update_id)
        VALUES (transform_id, matrix[1], matrix[2], matrix[3], matrix[4], matrix[5], matrix[6],
                permissions, owner_id, group_id,  event_id, event_id);

    RETURN transform_id;
END;
$$ LANGUAGE plpgsql;

ALTER TABLE shape RENAME COLUMN transform TO transform_old;
ALTER TABLE shape ADD COLUMN transform BIGINT;

ALTER TABLE shape ADD CONSTRAINT FKshape_transform_affinetransform
    FOREIGN KEY (transform) REFERENCES affinetransform;

UPDATE shape SET transform = upgrade_transform(transform_old, permissions, owner_id, group_id)
    WHERE transform_old IS NOT NULL;

ALTER TABLE shape DROP COLUMN transform_old;

DROP FUNCTION upgrade_transform(VARCHAR(255), BIGINT, BIGINT, BIGINT);
DROP FUNCTION parse_transform(TEXT);
DROP FUNCTION combine_ctm(FLOAT[], FLOAT[]);

CREATE INDEX i_affinetransform_owner ON affinetransform(owner_id);
CREATE INDEX i_affinetransform_group ON affinetransform(group_id);
CREATE INDEX i_shape_transform ON shape(transform);


--
-- FINISHED
--

UPDATE dbpatch SET message = 'Database updated.', finished = clock_timestamp()
    WHERE currentVersion  = 'OMERO5.3DEV' AND
          currentPatch    = 6             AND
          previousVersion = 'OMERO5.3DEV' AND
          previousPatch   = 5;

SELECT CHR(10)||CHR(10)||CHR(10)||'YOU HAVE SUCCESSFULLY UPGRADED YOUR DATABASE TO VERSION OMERO5.3DEV__6'||CHR(10)||CHR(10)||CHR(10) AS Status;

COMMIT;
